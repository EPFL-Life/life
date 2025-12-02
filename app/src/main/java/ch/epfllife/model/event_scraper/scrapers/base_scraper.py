from abc import ABC, abstractmethod
from typing import List
from models.event_models import Event
import logging

class BaseScraper(ABC):
    """Abstract base class for all platform scrapers"""
    
    def __init__(self, source_name: str):
        self.logger = logging.getLogger(self.__class__.__name__)
        self.source_name = source_name
    
    @abstractmethod
    def scrape(self) -> List[Event]:  
        """Main scraping method - must be implemented by subclasses"""
        pass
    
    def log_results(self, events: List[Event]):
        """Standardized logging for all scrapers"""
        self.logger.info(f"{self.source_name}: Found {len(events)} events")
    
    