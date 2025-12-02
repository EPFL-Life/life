# config.py
import os
from datetime import timedelta

# configuration of Firebase
FIREBASE_CREDENTIALS = "serviceAccountKey.json"

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
}



#configuration of logging
LOG_LEVEL = "INFO"
LOG_FORMAT = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"