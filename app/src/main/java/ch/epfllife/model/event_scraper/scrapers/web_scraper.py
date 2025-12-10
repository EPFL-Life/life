"""
Universal web scraper for various association websites
Extracts HTML from multiple sources and converts to Event objects
"""
import logging
from typing import List, Optional, Dict, Any
from datetime import datetime
import re
from scrapers.website_config import WebsiteConfig

from config import MAX_FIELD_LENGTHS

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
        self.config = WebsiteConfig(**website_config)
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
        
        selectors = self.config.selectors.event_link or ["a"]
        
        for selector in selectors:
            link_elem = element.select_one(selector)
            if link_elem and link_elem.get('href'):
                url = link_elem['href']
                
                if not url.startswith('http'):
                    base_domain = self.config.base_domain
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
            
            # Extract basic fields
            title = self._extract_title_from_element(element, is_detailed_page)
            if not title:
                return None
            
            event_id = Event.generate_id(title, self.source_name)
            
            # Extract remaining fields
            date_str = self._extract_date_from_element(element, is_detailed_page)
            description = self._extract_description_from_element(element, is_detailed_page, title)
            location = self._create_location_from_element(element, is_detailed_page)
            
            # Get association from config
            association = self.config.association
            if not association:
                self.logger.error("No association found in config")
                return None
            
            # Extract remaining fields
            price = self._extract_price_from_element(element, is_detailed_page)
            tags = self._generate_tags(title, description)
            image_url = self._extract_image_url(element, self._get_image_selectors(is_detailed_page))
            
            # Create Event object
            return self._create_event_from_fields(
                event_id, title, description, location, date_str, 
                association, tags, price, image_url
            )
                
        except Exception as e:
            self.logger.error(f"Error parsing event: {e}")
            return None
        
    def _extract_title_from_element(self, element, is_detailed_page: bool) -> Optional[str]:
        """Extract title from element"""
        return self._extract_text_with_selectors(
            element, 
            self.config.selectors.get("title", detailed=is_detailed_page),
            field_name="title",
            default_selectors=["h1", "h2"] if is_detailed_page else ["h2", ".title"]
        )
    
    def _extract_date_from_element(self, element, is_detailed_page: bool) -> str:
        """Extract date from element"""
        return self._extract_text_with_selectors(
            element,
            self.config.selectors.get("date", is_detailed_page),
            field_name="date",
            default_selectors=[".date", "time"],
            default_value="Date TBA"
        )
    
    def _extract_description_from_element(self, element, is_detailed_page: bool, title: str) -> str:
        """Extract description from element"""
        return self._extract_text_with_selectors(
            element,
            self.config.selectors.get("description", is_detailed_page),
            field_name="description",
            default_selectors=[".content", "article"] if is_detailed_page else [],
            default_value=f"Event organized by {self.source_name}. {title}. Visit event page for details."
        )
    
    def _create_location_from_element(self, element, is_detailed_page: bool) -> Location:
        """Create Location object from element"""
        location_name = self._extract_text_with_selectors(
            element,
            self.config.selectors.get("location", is_detailed_page),
            field_name="location",
            default_selectors=[".location", ".venue"],
            default_value="EPFL Campus (check event for exact location)"
        )
        
    def _create_location_from_element(self, element, is_detailed_page: bool) -> Location:
        """Create Location object from element"""
        location_name = self._extract_text_with_selectors(
            element,
            self.config.selectors.get("location", is_detailed_page),
            field_name="location",
            default_selectors=[".location", ".venue"],
            default_value="EPFL Campus (check event for exact location)"
        )
        return Location(
            latitude=self.config.coordinates["latitude"],
            longitude=self.config.coordinates["longitude"],
            name=location_name[:100]
        )
    
    def _extract_price_from_element(self, element, is_detailed_page: bool) -> Price:
        """Extract price from element"""
        price_text = self._extract_text_with_selectors(
            element,
            self.config.selectors.get("price", is_detailed_page),
            field_name="price",
            default_selectors=[".price", ".cost"],
            default_value=""
        )
        return self._parse_price(price_text)
    
    def _get_image_selectors(self, is_detailed_page: bool) -> List[str]:
        """Get image selectors based on page type"""
        return self.config.selectors.get("image", is_detailed_page)
    
    def _create_event_from_fields(self, event_id: str, title: str, description: str, 
                                location: Location, date_str: str, association: Association,
                                tags: List[str], price: Price, image_url: Optional[str]) -> Event:
        """Create Event object from all extracted fields"""
        return Event(
            id=event_id,
            title=self._safe_truncate(title, "title"),
            description=self._safe_truncate(description, "description"),
            location=location,
            time=date_str,
            association=association,
            tags=tags,
            price=price,
            picture_url=image_url
        )
    

    def _extract_text_with_selectors(self, element, selectors: List[str], 
                                field_name: str, 
                                default_selectors: List[str] = None,
                                default_value: str = "") -> str:
        """
        Extract text using selectors with proper fallback logic
        """
        if not selectors and default_selectors:
            selectors = default_selectors
        
        for selector in selectors:
            if not selector or selector.strip() in ["", ".", "*"]:
                continue
                
            elem = element.select_one(selector)
            if elem and elem.text.strip():
                
                from config import MAX_FIELD_LENGTHS
                max_len = MAX_FIELD_LENGTHS.get(field_name, 500)
                return elem.text.strip()[:max_len]
        
        return default_value
            
    def _validate_event(self, event: Event) -> bool:
        """Validate that an event has required fields"""
        if not event or not event.title:
            return False
        if len(event.title.strip()) < 2:
            return False
        if not event.description:
            return False
        return True

    def _generate_tags(self, title: str, description: str) -> List[str]:
        """Generate tags based on content"""
        tags = []
        text = (title + " " + description).lower()
        
        
        if any(word in text for word in ["ski", "snowboard", "bouldering", "sport", "hike"]):
            tags.append("Sports")
        if any(word in text for word in ["party", "social", "dinner", "apero", "drinks"]):
            tags.append("Social")
        if any(word in text for word in ["weekend", "trip", "travel", "excursion"]):
            tags.append("Trip")
        if any(word in text for word in ["workshop", "course", "tutorial", "lecture"]):
            tags.append("Workshop")
        if any(word in text for word in ["market", "christmas", "festive"]):
            tags.append("Cultural")
        if any(word in text for word in ["chocolate", "food", "dinner", "sushi"]):
            tags.append("Food")
        
        return tags


    

    
    def _safe_truncate(self, text: str, field: str) -> str:
        """Safely truncate text to max length for field"""
        
        if not text:
            return text
        
        max_len = MAX_FIELD_LENGTHS.get(field, 500)
        return text.strip()[:max_len]
    
    def _extract_image_url(self, element, selectors: List[str], 
                       default_selectors: List[str] = None) -> Optional[str]:
        """Extract image URL from selectors"""
        if not selectors and default_selectors:
            selectors = default_selectors
        
        for selector in selectors:
            if not selector or selector.strip() in ["", ".", "*"]:
                continue
                
            img_elem = element.select_one(selector)
            if img_elem and img_elem.get('src'):
                url = img_elem['src']
                
                if url and not url.startswith('http'):
                    base_domain = self.config.base_domain
                    if base_domain:
                        base_domain = base_domain.rstrip('/')
                        url = url.lstrip('/')
                        url = f"{base_domain}/{url}"
                return url
        
        return None

    
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
    
