"""
Universal web scraper for various association websites
Extracts HTML from multiple sources and converts to Event objects
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
                    event_link = self._extract_event_link(element)
                    
                    if event_link:
                        # Try detailed page first
                        detailed_event = self._scrape_detailed_page(event_link)
                        if detailed_event:
                            events.append(detailed_event)
                            continue  # Success, skip fallback
                    
                    # Fallback: parse from list view
                    event = self._parse_event_element(element, is_detailed_page=False)
                    if event and self._validate_event(event):
                        events.append(event)
                        
                except Exception as e:
                    logger.warning(f"Failed to parse event: {e}")
                    continue
            
            logger.info(f"{self.source_name}: Found {len(events)} events")
            return events  
            
        except Exception as e:
            logger.error(f"Error scraping {self.source_name}: {e}")
            return []  # Return empty list on error
        
    def _scrape_detailed_page(self, url: str) -> Optional[Event]:
        """Scrape detailed event page"""
        try:
            logger.info(f"  Visiting detailed page: {url}")
            response = self.session.get(url)
            response.raise_for_status()
            soup = BeautifulSoup(response.content, 'html.parser')
            
            
            event = self._parse_event_element(soup, is_detailed_page=True)
            
            if event:
                logger.info(f"  ✅ Successfully scraped detailed event: {event.title[:50]}...")
            else:
                logger.warning(f"  ❌ Could not parse detailed event from {url}")
                
            return event
            
        except Exception as e:
            logger.error(f"Error scraping detailed page {url}: {e}")
            return None
            

    def _extract_event_link(self, element) -> Optional[str]:
        """Extract link to detailed event page from an event element."""
        
        selectors = self.config.get("selectors", {}).get("event_link", ["a"])
        
        for selector in selectors:
            link_elem = element.select_one(selector)
            if link_elem and link_elem.get('href'):
                url = link_elem['href']
                
                if not url.startswith('http'):
                    base_domain = self.config.get("base_domain", "")
                    if base_domain:
                        # Check that there is not double //
                        base_domain = base_domain.rstrip('/')
                        url = url.lstrip('/')
                        url = f"{base_domain}/{url}"
                return url
        return None
        
    
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
    
    def _parse_event_element(self, element, is_detailed_page: bool = False) -> Optional[Event]:
        """Parse event element from list or detailed page"""
        try:
            logger.debug(f"  Parsing {'detailed' if is_detailed_page else 'list'} page element")
            
            selectors_config = self.config.get("selectors", {})
            
            # Get selectors based on page type
            if is_detailed_page:
                # Use detailed page selectors
                title_selectors = selectors_config.get("detailed_title") or selectors_config.get("title") or ["h1", "h2"]
                date_selectors = selectors_config.get("detailed_date") or selectors_config.get("date") or [".date", "time"]
                desc_selectors = selectors_config.get("detailed_description") or selectors_config.get("description") or [".content", "article"]
                loc_selectors = selectors_config.get("detailed_location") or selectors_config.get("location") or [".location", ".venue"]
                price_selectors = selectors_config.get("detailed_price") or selectors_config.get("price") or [".price", ".cost"]
                img_selectors = selectors_config.get("detailed_image") or selectors_config.get("image") or ["img"]
                
                logger.debug(f"    Detailed selectors: title={title_selectors}, loc={loc_selectors}, price={price_selectors}")
            else:
                # Use list page selectors
                title_selectors = selectors_config.get("title") or ["h2", ".title"]
                date_selectors = selectors_config.get("date") or [".date", "time"]
                desc_selectors = selectors_config.get("description") or []
                loc_selectors = selectors_config.get("location") or []
                price_selectors = selectors_config.get("price") or []
                img_selectors = selectors_config.get("image") or ["img"]
            
            # 1. Extract title
            title = None
            for selector in title_selectors:
                if not selector or selector.strip() in ["", ".", "*"]:
                    continue
                title_elem = element.select_one(selector)
                if title_elem:
                    title = title_elem.text.strip()
                    logger.debug(f"    Found title with selector '{selector}': {title[:50]}...")
                    break
            
            if not title:
                logger.warning("  No title found")
                return None
            
            # 2. Generate ID
            event_id = Event.generate_id(title, self.source_name)
            
            # 3. Extract date
            date_str = "Date TBA"
            for selector in date_selectors:
                date_elem = element.select_one(selector)
                if date_elem:
                    date_str = date_elem.text.strip()
                    break
            
            # 4. Extract description
            description = ""
            if is_detailed_page and desc_selectors:
                for selector in desc_selectors:
                    desc_elem = element.select_one(selector)
                    if desc_elem and desc_elem.text.strip():
                        description = desc_elem.text.strip()[:2000]
                        break
            
            if not description:  # Fallback for list view or no description found
                description = f"Event organized by {self.source_name}. {title}. Visit the event page for full details."
            
            # 5. Extract location
            location_name = "EPFL Campus (check event for exact location)"
            for selector in loc_selectors:
                loc_elem = element.select_one(selector)
                if loc_elem and loc_elem.text.strip():
                    location_name = loc_elem.text.strip()[:100]
                    break
            
            location = Location(
                latitude=46.5191,
                longitude=6.5668,
                name=location_name
            )
            
            # 6. Extract image URL
            image_url = None
            for selector in img_selectors:
                img_elem = element.select_one(selector)
                if img_elem and img_elem.get('src'):
                    image_url = img_elem['src']
                    # Convert to absolute URL if relative
                    if image_url and not image_url.startswith('http'):
                        base_domain = self.config.get("base_domain", "")
                        if base_domain:
                            base_domain = base_domain.rstrip('/')
                            image_url = image_url.lstrip('/')
                            image_url = f"{base_domain}/{image_url}"
                    break
            
            # 7. Get association from config
            association = self.config.get("association")
            if not association:
                return None
            
            # 8. Extract price
            price_text = None
            for selector in price_selectors:
                price_elem = element.select_one(selector)
                if price_elem:
                    price_text = price_elem.text.strip()
                    break
            
            price = self._parse_price(price_text) if price_text else Price(cents=0)
            
            # 9. Generate tags
            tags = []
            title_lower = title.lower()
            if any(word in title_lower for word in ["ski", "sport", "hike", "bouldering"]):
                tags.append("Sports")
            if any(word in title_lower for word in ["party", "social", "dinner", "apero"]):
                tags.append("Social")
            if any(word in title_lower for word in ["weekend", "trip", "travel"]):
                tags.append("Trip")
            if any(word in title_lower for word in ["workshop", "course", "tutorial"]):
                tags.append("Workshop")
            
            # 10. Create Event object
            event = Event(
                id=event_id,
                title=title[:150],
                description=description[:2000] if is_detailed_page else description[:500],
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
        
    def _validate_event(self, event: Event) -> bool:
        """Validate that an event has required fields"""
        if not event or not event.title:
            return False
        if len(event.title.strip()) < 2:
            return False
        if not event.description:
            return False
        return True


    
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
        """Parse price text robustly"""
        if not price_text:
            return Price(cents=0)
        
        price_text = price_text.lower()
        
        # 2. Look for "free" first if there are no numbers following
        if "free" in price_text and not re.search(r'\d+', price_text):
            return Price(cents=0)
        
        # 2. Looks for common
        patterns = [
            r'chf\s*(\d+\.?\d*)',          # "CHF 5"
            r'(\d+\.?\d*)\s*chf',          # "5 CHF"
            r'(\d+)\s*\.-',                # "5.-"
            r'entry\s*:\s*(\d+)',          # "Entry: 5"
            r'entrée\s*:\s*(\d+)',         # "Entrée: 5"
        ]
        
        for pattern in patterns:
            match = re.search(pattern, price_text, re.IGNORECASE)
            if match:
                try:
                    amount = float(match.group(1))
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