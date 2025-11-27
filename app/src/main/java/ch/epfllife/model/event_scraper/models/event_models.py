from dataclasses import dataclass
from typing import Optional, Dict, Any, List
from enum import Enum
import hashlib

class EventCategory(Enum):
    """
    Event categories enum - MUST match Android's EventCategory enum exactly
    This ensures data consistency between Python scraper and Android app
    """
    CULTURE = "Culture"
    SPORTS = "Sports"
    TECH = "Tech"
    SOCIAL = "Social" 
    ACADEMIC = "Academic"
    CAREER = "Career"  
    OTHER = "Other"
    
    def display_string(self) -> str:
        """Return display string for the category"""
        return self.value
    
    def to_firestore_value(self) -> str:
        """Convert to Firestore-compatible value"""
        return self.name

@dataclass
class Price:
    """
    Price value class - represents price in cents
    """
    cents: int  # Price in cents (rappens)
    
    def to_firestore_value(self) -> int:
        """
        Convert to Firestore-compatible value
        Firestore stores this as a simple integer field
        """
        return self.cents
    


@dataclass
class Location:
    """
    Location data class - represents event location
    """
    latitude: float
    longitude: float
    name: str
    
    def to_firestore_dict(self) -> Dict[str, Any]:
        """
        Convert to Firestore-compatible dictionary
        """
        return {
            "latitude": self.latitude,
            "longitude": self.longitude,
            "name": self.name
        }

@dataclass
class Association:
    """
    Association data class - represents student associations
    This ensures seamless data integration with the mobile app
    """
    
    id: str
    name: str
    description: str
    event_category: EventCategory  
    
    
    picture_url: Optional[str] = None  
    logo_url: Optional[str] = None  
    about: Optional[str] = None  
    social_links: Optional[Dict[str, str]] = None 
    
    def to_firestore_dict(self) -> Dict[str, Any]:
        """
        Convert to Firestore-compatible dictionary
        """
        return {
            "id": self.id,
            "name": self.name,
            "description": self.description,
            "eventCategory": self.event_category.name,  # Store enum as string
            "pictureUrl": self.picture_url,
            "logoUrl": self.logo_url,
            "about": self.about,
            "socialLinks": self.social_links or {}  # Ensure never null
        }

@dataclass
class Event:
    """
    Event data class - represents university events
    This ensures the mobile app can parse data without issues
    """
    #Whithout default values
    id: str
    title: str
    description: str
    location: Location
    time: str
    association: Association
    tags: List[str]
    price: Price
    
    #With default values
    picture_url: Optional[str] = None  
    
    def to_firestore_dict(self) -> Dict[str, Any]:
        """
        Convert to Firestore-compatible dictionary
        This is critical for app compatibility
        """
        return {
            "id": self.id,
            "title": self.title,
            "description": self.description,
            "location": self.location.to_firestore_dict(),
            "time": self.time,
            "association": self.association.to_firestore_dict(),
            "tags": self.tags,
            "price": self.price.to_firestore_value(),
            "pictureUrl": self.picture_url
        }
    
    @classmethod
    def generate_id(cls, title: str, source: str) -> str:
        """
        Generate unique event ID based on title and source
        Uses MD5 hash to ensure uniqueness while keeping IDs readable
        """
        base_string = f"{source}_{title}".lower().replace(" ", "_")
        return hashlib.md5(base_string.encode()).hexdigest()[:20]