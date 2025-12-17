# Update tests/test_agepoly.py to show more debugging info
#!/usr/bin/env python3
"""Test Agepoly scraper configuration"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from scrapers.web_scraper import WebScraper
from scrapers.website_config import AGEPOLY_CONFIG
import logging

# Enable detailed logging
logging.basicConfig(level=logging.DEBUG, 
                   format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')

def test_agepoly():
    print("🧪 Testing Agepoly scraper...")
    print(f"URL: {AGEPOLY_CONFIG.url}")
    print(f"Coordinates: {AGEPOLY_CONFIG.coordinates}")

    try:
        # Initialize scraper with proper dictionary format
        scraper = WebScraper({
            "name": AGEPOLY_CONFIG.name,
            "url": AGEPOLY_CONFIG.url,
            "base_domain": AGEPOLY_CONFIG.base_domain,
            "selectors": AGEPOLY_CONFIG.selectors,  # SelectorConfig object
            "association": AGEPOLY_CONFIG.association,
            "coordinates": AGEPOLY_CONFIG.coordinates,
            "default_location": AGEPOLY_CONFIG.default_location
        })

        print(f"\n📡 Scraping {AGEPOLY_CONFIG.url}...")
        events = scraper.scrape()

        print(f"\n✅ Found {len(events)} events")

        if events:
            print("\n📋 Event details:")
            for i, event in enumerate(events[:3]):  # Show first 3 events
                print(f"\n  Event {i+1}:")
                print(f"    Title: {event.title}")
                print(f"    Date: {event.time}")
                print(f"    Description: {event.description[:100]}...")
                print(f"    Location: {event.location.name} (lat: {event.location.latitude}, long: {event.location.longitude})")
                print(f"    Association: {event.association.name}")
                print(f"    Tags: {event.tags}")
                print(f"    Price: {event.price.cents} cents")
                if event.picture_url:
                    print(f"    Image: {event.picture_url[:50]}...")
        else:
            print("\n⚠️  No events found. Possible issues:")
            print("   - Selectors might not match the website structure")
            print("   - Website might have changed")
            print("   - Network issues")

        return 0

    except Exception as e:
        print(f"\n❌ Error: {e}")
        import traceback
        traceback.print_exc()

        # Show more details about the configuration
        print("\n🔧 Configuration check:")
        print(f"  Coordinates type: {type(AGEPOLY_CONFIG.coordinates)}")
        print(f"  Coordinates keys: {list(AGEPOLY_CONFIG.coordinates.keys()) if hasattr(AGEPOLY_CONFIG.coordinates, 'keys') else 'N/A'}")

        return 1

if __name__ == "__main__":
    sys.exit(test_agepoly())