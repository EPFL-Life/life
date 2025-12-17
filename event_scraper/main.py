#!/usr/bin/env python3
"""
Main script that runs all scrapers
"""

import sys
import os
import logging
from typing import List

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

# Import scrapers
from scrapers.web_scraper import WebScraper


# Import configurations
from scrapers.website_config import ALL_WEBSITES

from firestore.database import FirebaseDatabase
from models.event_models import Event
import config

logger = logging.getLogger(__name__)

def main():
    """Run all scrapers"""
    logger.info("Starting EPFL Life Event Scraper")
    
    try:
        # 1. Initialize Firebase
        db = FirebaseDatabase()
        existing_ids = db.get_existing_event_ids()
        
        all_events = []
        
        # 2. Run WEB scrapers 
        logger.info("Running WEB scrapers...")
        for website_config in ALL_WEBSITES:
            try:
                scraper = WebScraper(website_config)
                events = scraper.scrape()
                all_events.extend(events)
                logger.info(f"  {website_config.name}: {len(events)} events")
            except Exception as e:
                logger.error(f"  Failed to scrape {website_config.name}: {e}")
        

        
        # 4. Filter duplicates and upload
        new_events = [e for e in all_events if e.id not in existing_ids]
        
        logger.info(f"Total: {len(all_events)} events, New: {len(new_events)}")
        
        if new_events:
            results = db.upload_events_batch(new_events)
            logger.info(f"Uploaded: {results['success']}, Failed: {results['failed']}")
        
    except Exception as e:
        logger.error(f"Error: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main())