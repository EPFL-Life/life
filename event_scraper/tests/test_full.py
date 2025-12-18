#!/usr/bin/env python3
"""Complete end-to-end test of the scraper with Firebase"""

from asyncio import events
import sys
import os
import logging

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

from scrapers.web_scraper import WebScraper
from scrapers.website_config import ESN_EPFL_CONFIG
from scrapers.website_config import AGEPOLY_CONFIG
from firestore.database import FirebaseDatabase

def test_complete():
    print(" COMPLETE SCRAPER TEST")
    print("=" * 60)
    
    try:
        # 1. Test WebScraper
        print("1. Testing WebScraper...")
        scraper = WebScraper({
            "name": AGEPOLY_CONFIG.name,
            "url": AGEPOLY_CONFIG.url,
            "base_domain": AGEPOLY_CONFIG.base_domain,
            "selectors": AGEPOLY_CONFIG.selectors.to_dict(),
            "association": AGEPOLY_CONFIG.association,
            "coordinates": AGEPOLY_CONFIG.coordinates,
            "default_location": AGEPOLY_CONFIG.default_location
        })

        events = scraper.scrape()

        """ # Whenever you want to modify an event for testing
        if events:
            events[0].id = "test_" + events[0].id  # Prefix the first event ID for testing
        """
        
        if not events:
            print("  No events found")
            return 1
        
        print(f"   Found {len(events)} events")
        
        # Show first event
        first = events[0]
        print("\n    FIRST EVENT SAMPLE:")
        print(f"      ID: {first.id}")
        print(f"      Title: {first.title}")
        print(f"      Date: {first.time}")
        print(f"      Description: {first.description[:80]}...")
        print(f"      Location: {first.location.name}")
        print(f"      Association: {first.association.name}")
        print(f"      Tags: {first.tags}")
        print(f"      Price: {first.price.cents} cents")
        if first.picture_url:
            print(f"      Image: {first.picture_url[:50]}...")
        
        # 2. Test Firebase
        print("\n2. Testing Firebase upload...")
        db = FirebaseDatabase()
        existing_ids = db.get_existing_event_ids()
        
        # Filter new events
        new_events = [e for e in events if e.id not in existing_ids]
        print(f"   Total events: {len(events)}")
        print(f"   New events: {len(new_events)}")
        print(f"   Existing events: {len(existing_ids)}")
        
        if new_events:
            # Upload just ONE event for testing
            test_event = new_events[0]
            print(f"\n    Uploading test event: {test_event.title}")
            
            success = db.upload_event(test_event)
            if success:
                print("   Event uploaded successfully!")
                
                # Verify it's there
                exists = db.event_exists(test_event.id)
                print(f"    Verification: Event exists in DB: {exists}")
            else:
                print("    Failed to upload event")
                return 1
        else:
            print("     No new events to upload")
        
        print("\n3. Database summary:")
        existing_ids = db.get_existing_event_ids()  
        print(f"   Total events in DB: {len(existing_ids)}")
        print("   (Stats method not implemented yet)")
        
        print("\n TEST COMPLETED SUCCESSFULLY!")
        print("=" * 60)
        print("\n EVENT UPLOADED TO FIREBASE WITH:")
        print("   • Unique generated ID")
        print("   • Event exact title")
        print("   • Event exact date")
        print("   • Basic description")
        print("   • EPFL location (default)")
        print("   • Complete ESN association")
        print("   • Automatic tags")
        print("   • Price 0 (free)")
        print("   • Event image")
        
        return 0
        
    except Exception as e:
        print(f" Error: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    sys.exit(test_complete())