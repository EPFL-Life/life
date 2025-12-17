# config.py
import os
from datetime import timedelta

# configuration of Firebase
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
FIREBASE_CREDENTIALS = os.path.join(BASE_DIR, "serviceAccountKey.json")

# Firestore collection names (MUST match Android app)
FIRESTORE_COLLECTIONS = {
    "EVENTS": "events",
    "ASSOCIATIONS": "associations",
    "USERS": "users"
}


# Request configuration
REQUEST_TIMEOUT = 30
REQUEST_HEADERS = {
    'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8',
}


# Configuration of the scraper
SCRAPING_INTERVAL_HOURS = 6
REQUEST_TIMEOUT = 30


# Default EPFL location
DEFAULT_EPFL_LOCATION = {
    "latitude": 46.5191,
    "longitude": 6.5668,
    "name": "EPFL Campus"
}

# Maximum field lengths
MAX_FIELD_LENGTHS = {
    "title": 200,
    "description": 2000,
    "location": 100,
}



#configuration of logging
LOG_LEVEL = "INFO"
LOG_FORMAT = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"



"""
CSS selector constants to avoid duplication.
"""

# Date selectors
DATE_FIELD_SELECTOR = ".field-name-field-date span"
DATE_CLASS_SELECTOR = "[class*='date']"
DATE_ELEMENT_SELECTOR = ".date"
DATE_TIME_SELECTOR = "time"

# Location selectors
LOCATION_FIELD_SELECTOR = ".field-name-field-location"
LOCATION_CLASS_SELECTOR = "[class*='location']"
LOCATION_ELEMENT_SELECTOR = ".location"
VENUE_SELECTOR = ".venue"

# Price selectors
PRICE_FIELD_SELECTOR = ".field-name-field-price"
PRICE_CLASS_SELECTOR = "[class*='price']"
PRICE_ELEMENT_SELECTOR = ".price"
COST_SELECTOR = ".cost"

# Description selectors
DESCRIPTION_FIELD_SELECTOR = ".field-name-body"
DESCRIPTION_CLASS_SELECTOR = ".description"
PARAGRAPH_SELECTOR = "p"

# Image selectors
IMAGE_FIELD_SELECTOR = ".field-name-field-image img"
IMAGE_GROUP_SELECTOR = ".group-image img"
EVENTS_IMAGE_SELECTOR = "img[src*='events']"
GENERIC_IMAGE_SELECTOR = "img"

# Title selectors
TITLE_SELECTOR = "h1, h2, h3"
EVENT_TITLE_SELECTOR = ".event-title"
TITLE_FIELD_SELECTOR = ".field-name-field-title"

# Link selectors
LINK_SELECTOR = "a[href]"
READ_MORE_SELECTOR = ".read-more"
DETAILS_LINK_SELECTOR = ".details-link"