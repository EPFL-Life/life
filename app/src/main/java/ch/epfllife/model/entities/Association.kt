package ch.epfllife.model.entities

import ch.epfllife.model.enums.Category

data class Association(
    val id: String,
    var name: String,
    var description: String,
    var pictureUrl: String? = null,
    var category: Category
)

{

    // It changes the description of the association
    fun updateDescription(newDescription: String) {
        if (newDescription.isNotBlank()) {
            description = newDescription
        }
    }

    // It changes the picture URL of the association
    fun updatePicture(newPictureUrl: String) {
        pictureUrl = newPictureUrl
    }

    // It changes the category of the association
    fun changeCategory(newCategory: Category) {

        category = newCategory
    }

    // Verify if the association matches a search query
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return name.lowercase().contains(lowerQuery) || description.lowercase().contains(lowerQuery)
    }

    // show the summary of the association
    fun getSummary(): String {
        return "[$category] $name - $description"
    }
}