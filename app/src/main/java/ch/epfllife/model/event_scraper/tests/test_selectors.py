# event_scraper/test_selectors.py
#!/usr/bin/env python3
"""Test ESN EPFL website selectors"""

import sys
import os
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import requests
from bs4 import BeautifulSoup

def test_esn_selectors():
    print(" Testing ESN EPFL selectors...")
    
    url = "https://epfl.esn.ch/events"
    
    try:
        response = requests.get(url, headers={
            'User-Agent': 'Mozilla/5.0'
        })
        response.raise_for_status()
        
        soup = BeautifulSoup(response.content, 'html.parser')
        
        print("\n Testing different selectors:")
        
        # Try various selectors to find event elements
        selectors = [
            '.view-content .views-row',
            '.views-row',
            '.event-item',
            'article',
            '[class*="event"]',
            '.node-event',
            '.event-listing .item'
        ]
        
        for selector in selectors:
            elements = soup.select(selector)
            if elements:
                print(f" '{selector}': {len(elements)} elements")
                if elements:
                    # Show first element sample
                    first = elements[0]
                    text = first.text.strip()[:100]
                    print(f"   First element: {text}...")
                    print(f"   HTML snippet: {str(first)[:200]}...")
            else:
                print(f" '{selector}': 0 elements")
        
        print("\n Looking for specific elements:")
        
        # Search for titles
        titles = soup.select('h3, h2, .title, [class*="title"]')
        print(f"Found {len(titles)} potential titles")
        for i, title in enumerate(titles[:3]):
            print(f"  Title {i+1}: {title.text.strip()}")
        
        # Search for dates
        dates = soup.select('[class*="date"], time, .date')
        print(f"\nFound {len(dates)} potential dates")
        for i, date in enumerate(dates[:3]):
            print(f"  Date {i+1}: {date.text.strip()}")
            
    except Exception as e:
        print(f" Error: {e}")

if __name__ == "__main__":
    test_esn_selectors()