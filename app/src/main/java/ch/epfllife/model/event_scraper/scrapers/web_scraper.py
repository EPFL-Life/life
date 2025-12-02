"""
Universal web scraper for various association websites
Uemplates HTML from multiple sources and converts to Event objects
"""
import logging
from typing import List, Optional, Dict, Any
from datetime import datetime
import re

import requests
from bs4 import BeautifulSoup

from scrapers.base_scraper import BaseScraper
from models.event_models import Event, Association, Location, Price, EventCategory
import config

logger = logging.getLogger(__name__)

class WebScraper(BaseScraper):
    """
    Universal web scraper that can handle multiple association websites
    Each website configuration defines how to parse its specific HTML structure
    """
    
    def __init__(self, website_config: Dict[str, Any]):
        """
        Initialize web scraper for a specific website
        
        Args:
            website_config: Dictionary with website configuration including:
                - name: Website name
                - url: Events page URL
                - association: Association object for this website
                - selectors: CSS selectors for parsing
        """
        super().__init__(website_config.get("name", "Unknown Website"))
        self.config = website_config
        self.base_url = website_config.get("url", "")
        
        # Setup HTTP session
        self.session = requests.Session()
        self.session.headers.update(config.REQUEST_HEADERS)
        self.session.timeout = config.REQUEST_TIMEOUT
        
        logger.info(f"Initialized WebScraper for: {self.source_name}")
    
    def scrape(self) -> List[Event]:
        """Scrape events from the configured website"""
        events = []
        
        try:
            logger.info(f"Fetching from {self.base_url}")
            response = self.session.get(self.base_url)
            response.raise_for_status()
            
            soup = BeautifulSoup(response.content, 'html.parser')
            
            # Get event elements using configured selectors
            event_elements = self._find_event_elements(soup)
            logger.info(f"Found {len(event_elements)} event elements")
            
            # Parse each element
            for element in event_elements[:10]:
                try:
                    event = self._parse_event_element(element)
                    if event and self._validate_event(event):
                        events.append(event)
                except Exception as e:
                    logger.warning(f"Failed to parse event: {e}")
                    continue
            
            self.log_results(events)
            return events
            
        except Exception as e:
            logger.error(f"Error scraping {self.source_name}: {e}")
            return []
        
        
    
    def _find_event_elements(self, soup: BeautifulSoup) -> List[Any]:
        """Find event elements using configured CSS selectors"""
        selectors = self.config.get("selectors", {}).get("event_container", [])
        
        for selector in selectors:
            elements = soup.select(selector)
            if elements:
                return elements
        
        # Default fallback selectors
        default_selectors = [
            '.event-item',
            '.event',
            '.views-row',
            'article.event',
            '[class*="event"]'
        ]
        
        for selector in default_selectors:
            elements = soup.select(selector)
            if elements:
                return elements
        
        return []
    
    def _parse_event_element(self, element) -> Optional[Event]:
        """Parse event element """
        try:
            # 1. Extract title 
            title_elem = element.select_one('.field-name-title h2, .field-name-title h2 a, h2')
            if not title_elem:
                return None
            title = title_elem.text.strip()
            
            # 2. Generate ID
            event_id = Event.generate_id(title, self.source_name)
            
            # 3. Extract date 
            date_elem = element.select_one('.date-display-single, .field-name-field-date')
            date_str = date_elem.text.strip() if date_elem else "Date TBA"
            
            # 4. Create simple description
            description = f"Event organized by {self.source_name}. {title}. Visit the event page for full details."
            
            # 5. Create location (default EPFL)
            location = Location(
                latitude=46.5191,
                longitude=6.5668,
                name="EPFL Campus (check event for exact location)"
            )
            
            # 6. Extract image URL
            img_elem = element.select_one('.field-name-field-image img, .group-image img, img')
            image_url = img_elem['src'] if img_elem and img_elem.get('src') else None
            
            # 7. Get association from config
            association = self.config.get("association")
            if not association:
                return None
            
            # 8. Generate basic tags
            tags = ["ESN", "EPFL", "Student"]
            title_lower = title.lower()
            if any(word in title_lower for word in ["ski", "sport", "hike"]):
                tags.append("Sports")
            if any(word in title_lower for word in ["party", "social", "dinner"]):
                tags.append("Social")
            if any(word in title_lower for word in ["weekend", "trip", "travel"]):
                tags.append("Trip")
            
            # 9. Create price (assume free for now)
            price = Price(cents=0)
            
            # 10. Create Event object with ALL required fields
            event = Event(
                id=event_id,
                title=title[:150],
                description=description[:500],
                location=location,
                time=date_str,
                association=association,
                tags=tags,
                price=price,
                picture_url=image_url
            )
            
            return event
            
        except Exception as e:
            self.logger.error(f"Error parsing event: {e}")
            return None
    
    def _extract_with_selectors(self, element, selectors: List[str]) -> Optional[str]:
        """Extract text using multiple selector options"""
        for selector in selectors:
            found = element.select_one(selector)
            if found and found.text.strip():
                return found.text.strip()
        
        # Try element's own text as fallback
        if element.text.strip():
            return element.text.strip()[:500]
        
        return None
    
    def _extract_image(self, element, selectors: List[str]) -> Optional[str]:
        """Extract image URL"""
        for selector in selectors:
            img_elem = element.select_one(selector)
            if img_elem and img_elem.get('src'):
                url = img_elem['src']
                if url and not url.startswith('http'):
                    base_domain = self.config.get("base_domain", "")
                    if base_domain:
                        url = f"{base_domain}{url}"
                return url
        
        return None
    
    def _create_location(self, location_name: str) -> Location:
        """Create Location object from name"""
        # Use coordinates from config or defaults
        coords = self.config.get("coordinates", config.DEFAULT_EPFL_LOCATION)
        
        return Location(
            latitude=coords["latitude"],
            longitude=coords["longitude"],
            name=location_name[:100]
        )
    
    def _parse_price(self, price_text: Optional[str]) -> Price:
        """Parse price text"""
        if not price_text:
            return Price(cents=0)
        
        price_text = price_text.lower()
        
        if any(word in price_text for word in ["free", "gratuit", "0", "gratis"]):
            return Price(cents=0)
        
        try:
            match = re.search(r'chf\s*(\d+\.?\d*)', price_text, re.IGNORECASE)
            if match:
                amount = float(match.group(1))
                return Price(cents=int(amount * 100))
            
            numbers = re.findall(r'\d+\.?\d*', price_text)
            if numbers:
                amount = float(numbers[0])
                return Price(cents=int(amount * 100))
        except:
            pass
        
        return Price(cents=0)
    
    def _generate_tags(self, title: str, description: str) -> List[str]:
        """Generate tags based on content"""
        tags = {self.source_name}
        text = (title + " " + description).lower()
        
        # Add content-based tags
        content_tags = {
            "party": "Social",
            "social": "Social", 
            "workshop": "Workshop",
            "food": "Food",
            "dinner": "Food",
            "sport": "Sports",
            "hike": "Sports",
            "culture": "Cultural",
            "visit": "Cultural",
            "trip": "Cultural"
        }
        
        for keyword, tag in content_tags.items():
            if keyword in text:
                tags.add(tag)
        
        return list(tags)
    
    def _validate_event(self, event: Event) -> bool:
        """Basic validation"""
        return bool(event.title and event.description and len(event.title) > 2)