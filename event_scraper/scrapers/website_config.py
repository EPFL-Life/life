
from dataclasses import dataclass, field
from typing import List
from models.event_models import Association, EventCategory

DEFAULT_LOCATION_NAME = "EPFL Campus"
DEFAULT_COORDINATES = {"latitude": 46.5191, "longitude": 6.5668, "name": DEFAULT_LOCATION_NAME}

@dataclass
class SelectorConfig:
    """Configuration for CSS selectors"""
    event_container: List[str] = field(default_factory=list)
    title: List[str] = field(default_factory=list)
    date: List[str] = field(default_factory=list)
    location: List[str] = field(default_factory=list)
    description: List[str] = field(default_factory=list)
    price: List[str] = field(default_factory=list)
    image: List[str] = field(default_factory=list)
    event_link: List[str] = field(default_factory=list)
    detailed_title: List[str] = field(default_factory=list)
    detailed_date: List[str] = field(default_factory=list)
    detailed_description: List[str] = field(default_factory=list)
    detailed_location: List[str] = field(default_factory=list)
    detailed_price: List[str] = field(default_factory=list)
    detailed_image: List[str] = field(default_factory=list)
    
    def get(self, key: str, detailed: bool = False) -> List[str]:
        """Get selectors with fallback logic"""
        if detailed and f"detailed_{key}" in self.__dataclass_fields__:
            detailed_value = getattr(self, f"detailed_{key}")
            if detailed_value:
                return detailed_value
        
        value = getattr(self, key, [])
        return value if value else []
    
    def to_dict(self) -> dict:
        """Convert to dictionary for WebScraper compatibility"""
        return {
            "event_container": self.event_container,
            "title": self.title,
            "date": self.date,
            "location": self.location,
            "description": self.description,
            "price": self.price,
            "image": self.image,
            "event_link": self.event_link,
            "detailed_title": self.detailed_title,
            "detailed_date": self.detailed_date,
            "detailed_description": self.detailed_description,
            "detailed_location": self.detailed_location,
            "detailed_price": self.detailed_price,
            "detailed_image": self.detailed_image,
        }

@dataclass
class WebsiteConfig:
    """Complete website configuration"""
    name: str
    url: str
    base_domain: str
    selectors: SelectorConfig
    association: Association
    coordinates: dict = field(default_factory=lambda: DEFAULT_COORDINATES.copy())
    default_location: str = DEFAULT_LOCATION_NAME


ESN_EPFL_CONFIG = WebsiteConfig(
    name="ESN EPFL Lausanne",
    url="https://epfl.esn.ch/events",
    base_domain="https://epfl.esn.ch",
    default_location=DEFAULT_LOCATION_NAME,
    coordinates=DEFAULT_COORDINATES.copy(),
    
    selectors=SelectorConfig(
        event_container=[".views-row", ".node.node-event", ".view-content .views-row"],
        title=[".field-name-title h2", ".field-name-title h2 a", "h2", ".title"],
        date=[".date-display-single", ".field-name-field-date", "[class*='date-display']", ".field-name-field-date span"],
        location=[".field-name-field-location", "[class*='location']", ".location", ".venue"],
        description=[".field-name-body", ".description", "p"],
        price=[".field-name-field-price", "[class*='price']", ".price", ".cost"],
        image=[".field-name-field-image img", ".group-image img", "img[src*='events']", "img"],
        event_link=[".views-row h2 a"],
        detailed_title=["h1", ".field-name-title h2"],
        detailed_date=[".date-display-single"],
        detailed_description=[".field-name-body", ".content"],
        detailed_location=[".field-name-field-location"],
        detailed_price=[".field-name-field-price"],
        detailed_image=[".field-name-field-image img", ".main-image img"],
    ),
    
    association=Association(
        id="esn_epfl_lausanne",
        name="ESN EPFL Lausanne",
        description="Erasmus Student Network at EPFL Lausanne. We organize events for international students.",
        event_category=EventCategory.SOCIAL,
        picture_url="https://epfl.esn.ch/sites/epfl.esn.ch/files/logo_epfl.png",
        logo_url="https://epfl.esn.ch/sites/epfl.esn.ch/files/logo_epfl.png",
        about="ESN EPFL Lausanne helps international students integrate at EPFL through social and cultural events.",
        social_links={
            "website": "https://epfl.esn.ch",
            "facebook": "https://facebook.com/esn.epfl",
            "instagram": "https://instagram.com/esn_epfl"
        }
    )
)

AGEPOLY_CONFIG = WebsiteConfig(
    name="AGEPoly EPFL",
    url="https://agepoly.ch/en/evenements/",
    base_domain="https://agepoly.ch",
    default_location=DEFAULT_LOCATION_NAME,
    coordinates=DEFAULT_COORDINATES.copy(),

    selectors=SelectorConfig(
        # Event container - the main wrapper for each event
        event_container=[".o-posts-grid-post-body"],

        # Title is in h4 with a link inside
        title=["h4.o-posts-grid-post-title", "h4.o-posts-grid-post-title a"],

        # Date is in a <time> element within the meta paragraph
        date=[".o-posts-grid-post-meta time", "time"],

        # Location - not shown in list view, use default EPFL location
        location=[".field-name-field-location", "[class*='location']", ".location", ".venue"],  # Will use default location

        # Description is in this specific div
        description=[".o-posts-grid-post-description"],

        # Price - not shown in list view
        price=[".field-name-field-price", "[class*='price']", ".price", ".cost"],  # Default to free

        # Image - no images in list view
        image=[".field-name-field-image img", ".group-image img", "img[src*='events']", "img"],

        # Link to detailed page is in the title anchor tag
        event_link=["h4.o-posts-grid-post-title a"],

        # Detailed page selectors - we'll need to inspect individual event pages
        # For now, using generic WordPress selectors
        detailed_title=["h1.entry-title", "h1"],
        detailed_date=[".entry-meta time", "time.entry-date", "time"],
        detailed_description=[".entry-content", ".post-content", "article"],
        detailed_location=[".event-location", ".venue", ".location"],
        detailed_price=[".price", ".cost", ".event-price"],
        detailed_image=[".wp-post-image", "img.attachment-post-thumbnail", ".entry-content img"],
    ),

    association=Association(
        id="agepoly_epfl",
        name="AGEPoly EPFL",
        description="The student association of the School of Engineering at EPFL. Organizes academic, career, and social events.",
        event_category=EventCategory.SOCIAL,  # Most Agepoly events are social
        # Found these URLs from inspecting the Agepoly site
        picture_url="https://agepoly.ch/wp-content/uploads/2023/09/banniere-agepoly.jpg",
        logo_url="https://agepoly.ch/wp-content/themes/agepoly/assets/images/logo.svg",
        about="AGEPoly represents engineering students at EPFL and organizes a wide range of events including foodtruck challenges, ski sales, and social gatherings.",
        social_links={
            "website": "https://agepoly.ch",
            "facebook": "https://www.facebook.com/AGEPoly",
            "instagram": "https://www.instagram.com/agepoly_epfl/",
            "linkedin": "https://www.linkedin.com/company/agepoly/"
        }
    )
)

ALL_WEBSITES = [ESN_EPFL_CONFIG , AGEPOLY_CONFIG]