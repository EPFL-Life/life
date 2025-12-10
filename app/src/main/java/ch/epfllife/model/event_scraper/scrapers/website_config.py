
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
    coordinates={"latitude": DEFAULT_COORDINATES.copy()},
    
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

ALL_WEBSITES = [ESN_EPFL_CONFIG]