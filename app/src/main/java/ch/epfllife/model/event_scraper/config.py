# config.py
import os
from datetime import timedelta

# configuration of Firebase
FIREBASE_CREDENTIALS = "serviceAccountKey.json"

# Configuration of the scraper
SCRAPING_INTERVAL_HOURS = 6
REQUEST_TIMEOUT = 30

# URLs of data sources
ESN_EVENTS_URL = "https://esn.org/events"
# Add more URLs as needed

#configuration of logging
LOG_LEVEL = "INFO"
LOG_FORMAT = "%(asctime)s - %(name)s - %(levelname)s - %(message)s"