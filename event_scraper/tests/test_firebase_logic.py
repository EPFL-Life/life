#!/usr/bin/env python3
"""Test Firebase data conversion logic WITHOUT actual Firebase connection"""
import pytest
from models.event_models import Event, Association, Location, Price, EventCategory

class TestFirebaseDataConversion:
    """Test data conversion logic for Firebase"""
    
    @pytest.fixture
    def sample_association(self):
        return Association(
            id="test_assoc",
            name="Test Association",
            description="Test Description",
            event_category=EventCategory.SOCIAL,
            social_links={"website": "https://test.com"}
        )
    
    @pytest.fixture
    def sample_event(self, sample_association):
        return Event(
            id="test_event_123",
            title="Test Event",
            description="Event Description",
            location=Location(46.5191, 6.5668, "Test Location"),
            time="2024-01-15 18:00",
            association=sample_association,
            tags=["Social", "Test"],
            price=Price(cents=1000),
            picture_url="https://test.com/image.jpg"
        )
    
    def test_association_to_firestore_dict(self, sample_association):
        """Test association conversion to Firestore dict"""
        result = sample_association.to_firestore_dict()
        
        assert result["id"] == "test_assoc"
        assert result["name"] == "Test Association"
        assert result["eventCategory"] == "SOCIAL"
        assert result["socialLinks"] == {"website": "https://test.com"}
        assert "about" in result  # Optional field should be present
    
    def test_event_to_firestore_dict_without_db(self, sample_event):
        """Test event conversion without database reference"""
        result = sample_event.to_firestore_dict()  # No db parameter
        
        assert result["id"] == "test_event_123"
        assert result["title"] == "Test Event"
        assert result["price"] == 1000
        assert isinstance(result["location"], dict)
        assert result["location"]["name"] == "Test Location"
        # Without db, association should be string ID or dict
        assert "association" in result
    
    def test_event_id_uniqueness(self):
        """Test that event IDs are generated uniquely"""
        id1 = Event.generate_id("Event Title", "source1")
        id2 = Event.generate_id("Event Title", "source2")
        id3 = Event.generate_id("Different Title", "source1")
        
        # Same input = same ID
        assert Event.generate_id("Same", "Source") == Event.generate_id("Same", "Source")
        # Different input = different ID
        assert id1 != id2  # Different source
        assert id1 != id3  # Different title
    
    def test_price_firestore_conversion(self):
        """Test price conversion for Firestore"""
        test_cases = [
            (0, 0, "Free event"),
            (500, 500, "5 CHF"),
            (1000, 1000, "10 CHF"),
            (1999, 1999, "19.99 CHF")
        ]
        
        for cents, expected, description in test_cases:
            price = Price(cents=cents)
            assert price.to_firestore_value() == expected, f"Failed: {description}"
    
    def test_location_firestore_conversion(self):
        """Test location conversion for Firestore"""
        location = Location(
            latitude=46.5191,
            longitude=6.5668,
            name="EPFL Campus"
        )
        
        result = location.to_firestore_dict()
        
        assert isinstance(result, dict)
        assert set(result.keys()) == {"latitude", "longitude", "name"}
        assert result["latitude"] == 46.5191
        assert result["longitude"] == 6.5668
        assert result["name"] == "EPFL Campus"

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])