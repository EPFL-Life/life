from abc import ABC, abstractmethod
from typing import List
from models.event_model import Event
import logging

class BaseScraper(ABC):
    """Abstract base class for all platform-specific scrapers"""
    
    def __init__(self):
        self.logger = logging.getLogger(self.__class__.__name__)
    
    @abstractmethod
    def scrape_events(self) -> List[Event]:
        """Must return list of Event objects from platform"""
        pass
    
    @abstractmethod
    def get_source_name(self) -> str:
        """Must return platform name (e.g., 'Web', 'Telegram', 'Instagram')"""
        pass
    
    def log_scraping_result(self, events: List[Event]):
        """Standardized logging for all scrapers"""
        self.logger.info(f" {self.get_source_name()}: {len(events)} events found")