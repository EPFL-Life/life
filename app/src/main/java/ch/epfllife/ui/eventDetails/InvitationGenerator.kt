package ch.epfllife.ui.eventDetails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import ch.epfllife.R
import ch.epfllife.model.event.Event
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InvitationGenerator(private val context: Context) {

  suspend fun generateAndStoreInvitationAttributes(
      event: Event,
      senderName: String,
      senderProfileUrl: String?
  ): Uri {
    val bitmap = generateInvitationBitmap(event, senderName, senderProfileUrl)
    return saveBitmapAndGetUri(bitmap)
  }

  private suspend fun generateInvitationBitmap(
      event: Event,
      senderName: String,
      senderProfileUrl: String?
  ): Bitmap =
      withContext(Dispatchers.IO) {
        // Dimensions
        val width = 1200
        val height = 630 // common social card aspect ratio (approx 1.91:1)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background
        val bgPaint = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Load Event Image
        val imageUrl =
            event.pictureUrl
                ?: "https://www.epfl.ch/campus/services/events/wp-content/uploads/2024/09/WEB_Image-Home-Events_ORGANISER.png"
        val imageBitmap = loadEventImage(imageUrl)

        // Draw Image on left or top? Let's do a split layout. Left 40% image, Right 60% text.
        val imageWidth = (width * 0.4).toInt()

        // Crop/Fit image to the left side
        if (imageBitmap != null) {
          val destRect = Rect(0, 0, imageWidth, height)

          canvas.save()
          canvas.clipRect(destRect)

          // Calculate matrix for center crop
          val matrix = android.graphics.Matrix()
          val scaleX = imageWidth.toFloat() / imageBitmap.width
          val scaleY = height.toFloat() / imageBitmap.height
          val finalScale = kotlin.math.max(scaleX, scaleY)

          val scaledWidth = finalScale * imageBitmap.width
          val scaledHeight = finalScale * imageBitmap.height
          val translateX = (imageWidth - scaledWidth) / 2f
          val translateY = (height - scaledHeight) / 2f

          matrix.postScale(finalScale, finalScale)
          matrix.postTranslate(translateX, translateY)

          canvas.drawBitmap(imageBitmap, matrix, null)
          canvas.restore()
        } else {
          // Fallback color
          val p = Paint().apply { color = Color.RED }
          canvas.drawRect(0f, 0f, imageWidth.toFloat(), height.toFloat(), p)
        }

        // 2.5 Draw EPFL Logo on top of the image (or better, in the white space at top left if
        // there was space, but user said "top of the card (left white part)".
        // Assuming "left white part" meant the top area of the text section (which is on the right,
        // but effectively the 'content' area).
        // OR does "left white part" mean the top left of the image part? The image covers the left.
        // Let's interpret "left white part" as the top of the white content area (the right side in
        // our layout).
        // Wait, user said "top of the card (left white part)". If our card is split Image | Text,
        // then the white part is the RIGHT side.
        // Maybe they want the image on the right? "Event Image on Left, Text on Right" is standard.
        // "left white part" is confusing if the left part is the image.
        // Let's assume they mean top-left of the TEXT section (which is the white background part).
        // Let's draw it at the top of the text section.

        // Actually, maybe they want the EPFL logo overlaid on the image top-left?
        // "add the EPFL logo on the top of the card (left white part)"
        // If the card has a white part, it's likely the text area.

        val logoBitmap = loadBitmapFromDrawable(R.drawable.epfl_life_logo)
        if (logoBitmap != null) {
          val logoWidth = 300
          val logoHeight =
              (logoBitmap.height.toFloat() / logoBitmap.width.toFloat() * logoWidth).toInt()
          val logoX = imageWidth + 40f
          val logoY = 40f
          val logoDestRect =
              Rect(
                  logoX.toInt(),
                  logoY.toInt(),
                  (logoX + logoWidth).toInt(),
                  (logoY + logoHeight).toInt())
          canvas.drawBitmap(logoBitmap, null, logoDestRect, null)
        }

        // 3. Text Content (Right Side)
        val textStartX = imageWidth + 40f
        var currentY = 260f // Moved down to accommodate larger logo

        val textPaint =
            Paint().apply {
              color = Color.BLACK
              isAntiAlias = true
            }

        // "Invited by..." with Profile Pic
        var invitedByY = currentY

        if (senderProfileUrl != null) {
          val profileBitmap = loadEventImage(senderProfileUrl)
          if (profileBitmap != null) {
            val size = 60
            val circleBitmap = getCircleBitmap(profileBitmap)
            val destRect =
                Rect(
                    textStartX.toInt(),
                    (invitedByY - size + 10).toInt(),
                    (textStartX + size).toInt(),
                    (invitedByY + 10).toInt())
            canvas.drawBitmap(circleBitmap, null, destRect, null)

            textPaint.textSize = 30f
            textPaint.color = Color.GRAY
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Invited by $senderName", textStartX + size + 20, invitedByY, textPaint)
          } else {
            textPaint.textSize = 30f
            textPaint.color = Color.GRAY
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            canvas.drawText("Invited by $senderName", textStartX, invitedByY, textPaint)
          }
        } else {
          textPaint.textSize = 30f
          textPaint.color = Color.GRAY
          textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
          canvas.drawText("Invited by $senderName", textStartX, invitedByY, textPaint)
        }
        currentY += 80f

        // Title
        textPaint.textSize = 50f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val title = if (event.title.length > 25) event.title.take(22) + "..." else event.title
        canvas.drawText(title, textStartX, currentY, textPaint)
        currentY += 80f

        // Info with Icons
        textPaint.textSize = 35f
        textPaint.color = Color.DKGRAY
        textPaint.typeface = Typeface.DEFAULT

        // Helper to draw icon + text
        val iconSize = 40
        val iconPadding = 20

        fun drawLineWithIcon(iconRes: Int, text: String, y: Float) {
          val iconBitmap = loadBitmapFromDrawable(iconRes)
          if (iconBitmap != null) {
            val destRect =
                Rect(
                    textStartX.toInt(),
                    (y - 30).toInt(),
                    (textStartX + iconSize).toInt(),
                    (y - 30 + iconSize).toInt())
            // Tint icon? Vectors are black by default which matches nicely.
            canvas.drawBitmap(iconBitmap, null, destRect, null)
          }
          canvas.drawText(text, textStartX + iconSize + iconPadding, y, textPaint)
        }

        drawLineWithIcon(R.drawable.ic_calendar, event.time, currentY)
        currentY += 60f

        drawLineWithIcon(R.drawable.ic_location, event.location.name.substringBefore(","), currentY)
        currentY += 60f

        drawLineWithIcon(R.drawable.ic_person, event.association.name, currentY)

        return@withContext bitmap
      }

  private suspend fun loadEventImage(url: String): Bitmap? =
      withContext(Dispatchers.IO) {
        val loader = ImageLoader(context)
        val request =
            ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Important for Canvas drawing
                .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
          return@withContext result.drawable.toBitmap()
        }
        return@withContext null
      }

  private fun loadBitmapFromDrawable(resId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, resId) ?: return null
    val bitmap =
        Bitmap.createBitmap(
            drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
  }

  private fun getCircleBitmap(bitmap: Bitmap): Bitmap {
    val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, bitmap.width, bitmap.height)
    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, bitmap.width / 2f, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)
    return output
  }

  private suspend fun saveBitmapAndGetUri(bitmap: Bitmap): Uri =
      withContext(Dispatchers.IO) {
        val imagesFolder = File(context.cacheDir, "images")
        if (!imagesFolder.exists()) imagesFolder.mkdirs()

        val file = File(imagesFolder, "invitation_card.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()

        return@withContext FileProvider.getUriForFile(context, "ch.epfllife.fileprovider", file)
      }
}
