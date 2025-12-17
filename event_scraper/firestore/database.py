# event_scraper/firestore/database.py
"""
Firebase Firestore database manager for EPFL Life Event Scraper.
Handles all interactions with Firebase Firestore database.
"""

import logging
from typing import List, Dict, Any, Optional
from datetime import datetime
from pathlib import Path

import firebase_admin
from firebase_admin import credentials, firestore
from firebase_admin.exceptions import FirebaseError

from models.event_models import Event, Association
import config

logger = logging.getLogger(__name__)


class FirebaseDatabase:
    """
    Manages all Firebase Firestore operations.
    """
    
    def __init__(self, credential_path: Optional[str] = None):
        """
        Initialize Firebase connection.
        
        Args:
            credential_path: Path to Firebase service account JSON file.
        """
        credential_path = credential_path or config.FIREBASE_CREDENTIALS
        
        # Check if credentials file exists
        if not Path(credential_path).exists():
            error_msg = (
                f"Firebase credentials file not found: {credential_path}. "
                "Please download serviceAccountKey.json from Firebase Console")
            logger.error(error_msg)
            raise FileNotFoundError(error_msg)
        try:
            # Initialize Firebase app
            if not firebase_admin._apps:
                cred = credentials.Certificate(credential_path)
                firebase_admin.initialize_app(cred)
            
            # Get Firestore client
            self.db = firestore.client()
            self.events_collection = self.db.collection(config.FIRESTORE_COLLECTIONS["EVENTS"])
            self.associations_collection = self.db.collection(config.FIRESTORE_COLLECTIONS["ASSOCIATIONS"])
            
            logger.info("✅ Firebase connection established")
            
        except FirebaseError as e:
            logger.error(f"❌ Firebase error: {e}")
            raise
        except Exception as e:
            logger.error(f"❌ Error initializing Firebase: {e}")
            raise
    
    def upload_event(self, event: Event) -> bool:
        """
        Upload a single event to Firestore.
        
        Args:
            event: Event object to upload
            
        Returns:
            True if successful, False otherwise
        """
        try:
            # First, ensure association exists
            self._ensure_association_exists(event.association)
            
            # Convert event to Firestore dictionary
            event_data = self._event_to_firestore_dict(event)
            
            # Upload to Firestore
            event_ref = self.events_collection.document(event.id)
            event_ref.set(event_data)
            
            logger.debug(f"Uploaded event: {event.title}")
            return True
            
        except Exception as e:
            logger.error(f"Failed to upload event '{event.title}': {e}")
            return False
    
    def upload_events_batch(self, events: List[Event]) -> Dict[str, int]:
        """
        Upload multiple events.
        
        Args:
            events: List of Event objects
            
        Returns:
            Dictionary with success/failure counts
        """
        results = {"total": len(events), "success": 0, "failed": 0}
        
        for event in events:
            if self.upload_event(event):
                results["success"] += 1
            else:
                results["failed"] += 1
        
        logger.info(f"Batch upload: {results['success']} successful, {results['failed']} failed")
        return results
    
    def get_existing_event_ids(self) -> List[str]:
        """
        Get all existing event IDs from Firestore.
        
        Returns:
            List of event IDs
        """
        try:
            docs = self.events_collection.select(["id"]).stream()
            existing_ids = [doc.id for doc in docs]
            logger.info(f"Found {len(existing_ids)} existing events")
            return existing_ids
            
        except Exception as e:
            logger.error(f"Error getting existing events: {e}")
            return []
        

    def event_exists(self, event_id: str) -> bool:
        """
        Check if an event with given ID already exists.
        
        Args:
            event_id: Event ID to check
            
        Returns:
            True if event exists, False otherwise
        """
        try:
            doc_ref = self.events_collection.document(event_id)
            doc = doc_ref.get()
            return doc.exists
        except Exception as e:
            logger.error(f"Error checking if event exists: {e}")
            return False
    
    def _ensure_association_exists(self, association: Association) -> bool:
        """
        Ensure an association exists in Firestore.
        
        Args:
            association: Association object
            
        Returns:
            True if successful
        """
        try:
            assoc_ref = self.associations_collection.document(association.id)
            assoc_doc = assoc_ref.get()
            
            if not assoc_doc.exists:
                # Association doesn't exist, create it
                association_data = self._association_to_firestore_dict(association)
                assoc_ref.set(association_data)
                logger.debug(f"Created association: {association.name}")
            
            return True
            
        except Exception as e:
            logger.error(f"Error ensuring association exists: {e}")
            return False
    
    def _event_to_firestore_dict(self, event: Event) -> Dict[str, Any]:
        """
        Convert Event object to Firestore dictionary.
        
        Args:
            event: Event object
            
        Returns:
            Dictionary for Firestore
        """
        # Start with event's own conversion
        event_dict = event.to_firestore_dict(db=self.db)
        
        # Convert association to dictionary
        # event_dict["association"] = self._association_to_firestore_dict(event.association)
        
        # Add timestamps
        event_dict["_uploadedAt"] = firestore.SERVER_TIMESTAMP
        event_dict["_source"] = "python_scraper"
        
        return event_dict
    
    def _association_to_firestore_dict(self, association: Association) -> Dict[str, Any]:
        """
        Convert Association object to Firestore dictionary.
        
        Args:
            association: Association object
            
        Returns:
            Dictionary for Firestore
        """
        assoc_dict = association.to_firestore_dict()
        
        # Ensure socialLinks is never null
        if "socialLinks" not in assoc_dict or assoc_dict["socialLinks"] is None:
            assoc_dict["socialLinks"] = {}
        
        # Add timestamp
        assoc_dict["_source"] = "python_scraper"
        assoc_dict["_lastUpdated"] = firestore.SERVER_TIMESTAMP
        
        return assoc_dict
    
    def close(self):
        """Close Firebase connection"""
        logger.info("Closing Firebase connection")