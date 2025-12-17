#!/usr/bin/env python3
"""Generic tests for event models"""
import pytest
from models.event_models import (
    EventCategory, Price, Location, Association, Event
)

class TestEventCategory:
    """Test EventCategory enum"""
    
    def test_all_categories_exist(self):
        """Verify all expected categories are defined"""
        expected = ["CULTURE", "SPORTS", "TECH", "SOCIAL", "ACADEMIC", "CAREER", "OTHER"]
        actual = [cat.name for cat in EventCategory]
        assert set(expected) == set(actual)
    
    def test_display_string(self):
        """Test display_string returns correct value"""
        assert EventCategory.SPORTS.display_string() == "Sports"
        assert EventCategory.TECH.display_string() == "Tech"
    
    def test_to_firestore_value(self):
        """Test conversion to Firestore value"""
        assert EventCategory.SOCIAL.to_firestore_value() == "SOCIAL"
        assert EventCategory.CULTURE.to_firestore_value() == "CULTURE"

class TestPrice:
    """Test Price class"""
    
    @pytest.mark.parametrize("cents,expected_firestore", [
        (0, 0),
        (500, 500),
        (1000, 1000),
        (-100, -100)  # Edge case: negative price
    ])
    def test_price_conversions(self, cents, expected_firestore):
        """Test price creation and Firestore conversion"""
        price = Price(cents=cents)
        assert price.cents == cents
        assert price.to_firestore_value() == expected_firestore

class TestLocation:
    """Test Location class"""
    
    def test_location_creation(self):
        """Test basic location creation"""
        loc = Location(latitude=10.0, longitude=20.0, name="Test Location")
        assert loc.latitude == 10.0
        assert loc.longitude == 20.0
        assert loc.name == "Test Location"
    
    def test_to_firestore_dict(self):
        """Test Firestore dictionary conversion"""
        loc = Location(latitude=46.5, longitude=6.6, name="EPFL")
        result = loc.to_firestore_dict()
        
        assert isinstance(result, dict)
        assert result["latitude"] == 46.5
        assert result["longitude"] == 6.6
        assert result["name"] == "EPFL"
        assert len(result) == 3  # Only these 3 fields

class TestAssociation:
    """Test Association class"""
    
    def test_minimal_association(self):
        """Test association with only required fields"""
        assoc = Association(
            id="test_id",
            name="Test Name",
            description="Test Description",
            event_category=EventCategory.SOCIAL
        )
        
        assert assoc.id == "test_id"
        assert assoc.name == "Test Name"
        assert assoc.event_category == EventCategory.SOCIAL
        assert assoc.picture_url is None
        assert assoc.social_links is None
    
    def test_full_association(self):
        """Test association with all optional fields"""
        social_links = {
            "website": "https://example.com",
            "facebook": "https://facebook.com/test"
        }
        
        assoc = Association(
            id="full_id",
            name="Full Association",
            description="Full Description",
            event_category=EventCategory.TECH,
            picture_url="https://example.com/pic.jpg",
            logo_url="https://example.com/logo.png",
            about="About text here",
            social_links=social_links
        )
        
        firestore_dict = assoc.to_firestore_dict()
        
        assert firestore_dict["id"] == "full_id"
        assert firestore_dict["eventCategory"] == "TECH"
        assert firestore_dict["socialLinks"] == social_links
        assert "about" in firestore_dict
    
    def test_association_with_null_social_links(self):
        """Test association with None social links converts to empty dict"""
        assoc = Association(
            id="test",
            name="Test",
            description="Test",
            event_category=EventCategory.OTHER
        )
        
        firestore_dict = assoc.to_firestore_dict()
        assert firestore_dict["socialLinks"] == {}

class TestEvent:
    """Test Event class"""
    
    @pytest.fixture
    def sample_association(self):
        """Create a sample association for tests"""
        return Association(
            id="assoc_1",
            name="Sample Association",
            description="Description",
            event_category=EventCategory.SOCIAL
        )
    
    @pytest.fixture
    def sample_location(self):
        """Create a sample location for tests"""
        return Location(
            latitude=46.5191,
            longitude=6.5668,
            name="Sample Location"
        )
    
    @pytest.fixture
    def sample_price(self):
        """Create a sample price for tests"""
        return Price(cents=0)
    
    def test_event_creation(self, sample_association, sample_location, sample_price):
        """Test basic event creation"""
        event = Event(
            id="event_1",
            title="Test Event",
            description="Event Description",
            location=sample_location,
            time="2024-01-15 18:00",
            association=sample_association,
            tags=["Social", "Food"],
            price=sample_price
        )
        
        assert event.id == "event_1"
        assert event.title == "Test Event"
        assert len(event.tags) == 2
        assert event.price.cents == 0
        assert event.picture_url is None
    
    def test_event_with_picture(self, sample_association, sample_location, sample_price):
        """Test event with picture URL"""
        event = Event(
            id="event_2",
            title="Event with Picture",
            description="Desc",
            location=sample_location,
            time="2024-01-16",
            association=sample_association,
            tags=["Tech"],
            price=sample_price,
            picture_url="https://example.com/image.jpg"
        )
        
        assert event.picture_url == "https://example.com/image.jpg"
    
    def test_event_to_firestore_dict(self, sample_association, sample_location, sample_price):
        """Test Firestore dictionary conversion"""
        event = Event(
            id="firestore_test",
            title="Firestore Test",
            description="Description",
            location=sample_location,
            time="2024-01-15",
            association=sample_association,
            tags=["Test"],
            price=sample_price
        )
        
        result = event.to_firestore_dict()
        
        assert isinstance(result, dict)
        assert result["id"] == "firestore_test"
        assert result["title"] == "Firestore Test"
        assert isinstance(result["location"], dict)
        assert result["location"]["name"] == "Sample Location"
        assert result["price"] == 0
        assert result["tags"] == ["Test"]
    
    @pytest.mark.parametrize("title,source,expected_length", [
        ("Simple Title", "source1", 20),
        ("Title with Spaces", "source_2", 20),
        ("Very Long Title That Should Be Truncated Anyway", "src", 20),
        ("", "source", 20),  # Edge case: empty title
        ("Title", "", 20),   # Edge case: empty source
    ])
    def test_event_id_generation(self, title, source, expected_length):
        """Test event ID generation with various inputs"""
        event_id = Event.generate_id(title, source)
        
        assert isinstance(event_id, str)
        assert len(event_id) == expected_length
        
        # ID should be deterministic
        event_id2 = Event.generate_id(title, source)
        assert event_id == event_id2

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])