# event_scraper/inspect_event.py
import requests
from bs4 import BeautifulSoup

url = "https://epfl.esn.ch/events"
response = requests.get(url, headers={'User-Agent': 'Mozilla/5.0'})
soup = BeautifulSoup(response.content, 'html.parser')

# Find the first event
first_event = soup.select_one('.views-row')
if first_event:
    print(" FIRST EVENT STRUCTURE:")
    print("=" * 60)
    
    # 1. Show complete event HTML
    print("Complete HTML:")
    print(first_event.prettify()[:2000])  # First 2000 chars
    
    print("\n" + "=" * 60)
    
    # 2. Search for specific elements
    print("\n🔍 Looking for elements within the event:")
    
    # Search for date (we know it's in some span/div)
    date_element = first_event.find(['span', 'div'], class_=lambda x: x and 'date' in x.lower())
    if date_element:
        print(f" Date found: {date_element.text.strip()}")
        print(f"   Class: {date_element.get('class', 'No class')}")
    
    # Search for title
    title_element = first_event.find(['h3', 'h2', 'h4'])
    if title_element:
        print(f" Title found: {title_element.text.strip()}")
        print(f"   Tag: {title_element.name}, Class: {title_element.get('class', 'No class')}")
    
    # Search for description
    description_element = first_event.find(['p', 'div'], class_=lambda x: x and ('body' in str(x).lower() or 'desc' in str(x).lower()))
    if description_element:
        print(f" Description (first 100 chars): {description_element.text.strip()[:100]}...")
    
    # Search for location
    location_element = first_event.find(['span', 'div'], class_=lambda x: x and 'location' in str(x).lower())
    if location_element:
        print(f" Location: {location_element.text.strip()}")
    
    # Search for price
    price_element = first_event.find(['span', 'div'], class_=lambda x: x and 'price' in str(x).lower())
    if price_element:
        print(f" Price: {price_element.text.strip()}")
    
    # Search for image
    img_element = first_event.find('img')
    if img_element:
        print(f"  Image: {img_element.get('src', 'No src')}")