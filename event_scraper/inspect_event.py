# event_scraper/inspect_event.py - UPDATED VERSION
import requests
from bs4 import BeautifulSoup

# Change to Agepoly
url = "https://agepoly.ch/en/evenements/"
response = requests.get(url, headers={'User-Agent': 'Mozilla/5.0'})
soup = BeautifulSoup(response.content, 'html.parser')

print(" TARGETED AGEPOLY EVENT ANALYSIS")
print("=" * 70)

# Based on your output, look for the specific event containers
print("1. Looking for event containers with class 'o-posts-grid-post-body':")
event_containers = soup.select('.o-posts-grid-post-body')

print(f"   Found {len(event_containers)} event containers")
print(f"   First container classes: {event_containers[0].get('class') if event_containers else 'None'}")

if event_containers:
    first_event = event_containers[0]
    
    print("\n2. STRUCTURE OF FIRST EVENT:")
    print("-" * 70)
    
    # Get the parent element to see full structure
    parent = first_event.parent
    print(f"   Parent element: {parent.name} | Classes: {parent.get('class', 'None')}")
    
    # Get grandparent for more context
    grandparent = parent.parent if parent else None
    if grandparent:
        print(f"   Grandparent: {grandparent.name} | Classes: {grandparent.get('class', 'None')}")
    
    print("\n3. ELEMENTS INSIDE FIRST EVENT CONTAINER:")
    print("-" * 70)
    
    # Show all direct children of the event container
    for child in first_event.children:
        if child.name:  # Only show actual HTML elements (skip strings)
            print(f"   Child: {child.name} | Classes: {child.get('class', 'None')}")
            
            # Show text if it has any
            text = child.get_text(strip=True)[:100]
            if text:
                print(f"         Text: '{text}...'")
            
            # Special handling for links
            if child.name == 'a':
                print(f"         Href: {child.get('href', 'No href')}")
            
            # Special handling for images
            if child.name == 'img':
                print(f"         Src: {child.get('src', 'No src')}")
                print(f"         Alt: {child.get('alt', 'No alt')}")
    
    print("\n4. SEARCHING FOR SPECIFIC EVENT DATA:")
    print("-" * 70)
    
    # Look for title (we know it's h4 based on your output)
    title = first_event.find('h4')
    if title:
        print(f"   Title found: '{title.text.strip()}'")
        print(f"     Full title HTML: {str(title)[:200]}...")
    
    # Look for date elements - check inside and around the container
    print("\n   Searching for date information:")
    
    # Check the event container itself for dates
    date_elements = first_event.find_all(['time', 'span', 'div'], 
                                        string=lambda text: text and any(word in text.lower() 
                                                                       for word in ['jan', 'feb', 'mar', 'apr', 'may', 'jun', 
                                                                                    'jul', 'aug', 'sep', 'oct', 'nov', 'dec',
                                                                                    '2024', '2025', 'monday', 'tuesday']))
    for elem in date_elements[:3]:
        print(f"     Possible date: '{elem.text.strip()}' | Tag: {elem.name} | Classes: {elem.get('class', 'None')}")
    
    # Look for images
    print("\n   Images in event:")
    images = first_event.find_all('img')
    for img in images:
        src = img.get('src', 'No src')
        if not src.startswith('data:'):  # Skip data URIs
            print(f"     Image src: {src}")
            print(f"       Alt: {img.get('alt', 'No alt')}")
            print(f"       Classes: {img.get('class', 'None')}")
    
    # Look for links
    print("\n   Links in event:")
    links = first_event.find_all('a', href=True)
    for link in links:
        print(f"     Link text: '{link.text.strip()[:50]}'")
        print(f"       Href: {link['href']}")
        print(f"       Classes: {link.get('class', 'None')}")
    
    # Look for description/paragraphs
    print("\n   Description/paragraphs:")
    paragraphs = first_event.find_all('p')
    for p in paragraphs[:3]:
        text = p.text.strip()
        if text and len(text) > 20:  # Only show meaningful paragraphs
            print(f"     Paragraph: '{text[:100]}...'")
            print(f"       Classes: {p.get('class', 'None')}")

print("\n" + "=" * 70)
print("5. CHECKING PARENT STRUCTURE FOR ADDITIONAL INFO:")
print("-" * 70)

# Sometimes event data is in parent elements
if event_containers:
    # Get parent and siblings
    event_wrapper = first_event.parent
    if event_wrapper:
        print(f"   Event wrapper: {event_wrapper.name} | Classes: {event_wrapper.get('class', 'None')}")
        
        # Check siblings of the event container
        print("   Checking sibling elements for date/location info:")
        siblings = event_wrapper.find_all(['div', 'span', 'time'], class_=True)
        for sibling in siblings[:5]:
            classes = sibling.get('class', [])
            text = sibling.text.strip()
            if text and len(text) < 100:  # Skip large text blocks
                print(f"     Sibling: {sibling.name}.{classes} -> '{text}'")

print("\n" + "=" * 70)
print(" ANALYSIS COMPLETE - RUN THIS SCRIPT TO GET DETAILED STRUCTURE")
print("=" * 70)