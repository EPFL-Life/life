# scheduler.py 
import time
import logging
from datetime import datetime
from config import SCRAPING_INTERVAL_HOURS

logging.basicConfig(level=logging.INFO)

def main():
    
    print(f"⏰ Scraper programed to run every {SCRAPING_INTERVAL_HOURS} hours.")
    print("Press Ctrl + C to exit \n")
    
    while True:
        # 1. Executes scraper
        print(f"\n[{datetime.now()}] 🚀 Executing scraper...")
        
        try:
            # Imports main function
            from main import main as run_scraper
            result = run_scraper()
            
            if result == 0:
                print(f"[{datetime.now()}] ✅ Scraper completed")
            else:
                print(f"[{datetime.now()}] ❌ Scraper fails")
                
        except Exception as e:
            print(f"[{datetime.now()}] ⚠️ Error: {e}")
        
        # 2. Waits for next run
        print(f"[{datetime.now()}] 😴 Sleeping {  SCRAPING_INTERVAL_HOURS  } hours...")
        time.sleep(SCRAPING_INTERVAL_HOURS * 60 * 60)

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n🛑 Stopped by the user")