import pytest

import sys
import os

# Añade el directorio raíz al path de Python
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../..'))


from models.event_models import Event, Location, Association, Price, EventCategory

class TestEventModel:
    """Test event data models with 100% coverage goal"""
    
    def test_event_creation_basic(self):
        """Test basic event creation with all required fields"""
        location = Location(46.5191, 6.5668, "EPFL Campus")
        association = Association(
            id="esn_epfl",
            name="ESN EPFL",
            description="Test association",
            eventCategory=EventCategory.SOCIAL
        )
        price = Price(1000)
        
        event = Event(
            id="test_event_1",
            title="Test Event",
            description="Test description",
            location=location,
            time="2024-01-20 18:00",
            association=association,
            tags=["test", "social"],
            price=price
        )
        
        assert event.id == "test_event_1"
        assert event.title == "Test Event"
        assert event.description == "Test description"
        assert event.time == "2024-01-20 18:00"
        assert "test" in event.tags
        assert event.price.cents == 1000
    
    def test_event_creation_optional_fields(self):
        """Test event creation with optional fields"""
        location = Location(46.5191, 6.5668, "EPFL")
        association = Association(
            id="test_assoc",
            name="Test",
            description="Test",
            eventCategory=EventCategory.TECH
        )
        price = Price(0)
        
        event = Event(
            id="test_event_2",
            title="Test Event with Picture",
            description="Test",
            location=location,
            time="2024-01-20 18:00",
            association=association,
            tags=[],
            price=price,
            pictureUrl="https://example.com/image.jpg"
        )
        
        assert event.pictureUrl == "https://example.com/image.jpg"
    
    def test_price_creation_and_formatting(self):
        """Test Price class with various amounts"""
        # Test free event
        free_price = Price(0)
        assert free_price.cents == 0
        assert free_price.format_for_display() == "Free"
        assert str(free_price) == "Free"
        
        # Test paid event (5 CHF)
        paid_price_5 = Price(500)
        assert paid_price_5.cents == 500
        assert paid_price_5.format_for_display() == "CHF 5.00"
        
        # Test paid event (12.50 CHF)
        paid_price_1250 = Price(1250)
        assert paid_price_1250.cents == 1250
        assert paid_price_1250.format_for_display() == "CHF 12.50"
        
        # Test Firestore conversion
        assert paid_price_5.to_firestore_value() == 500
    
    def test_location_conversion(self):
        """Test Location to Firestore conversion"""
        location = Location(46.5191, 6.5668, "EPFL Rolex")
        firestore_dict = location.to_firestore_dict()
        
        expected = {
            "latitude": 46.5191,
            "longitude": 6.5668,
            "name": "EPFL Rolex"
        }
        assert firestore_dict == expected
    
    def test_association_conversion_full(self):
        """Test Association with all fields to Firestore conversion"""
        association = Association(
            id="test_assoc",
            name="Test Association",
            description="Test description",
            eventCategory=EventCategory.ACADEMIC,
            pictureUrl="https://example.com/picture.jpg",
            logoUrl="https://example.com/logo.png",
            about="This is a test association for testing purposes",
            socialLinks={
                "website": "https://test.com",
                "instagram": "https://instagram.com/test",
                "telegram": "https://t.me/test"
            }
        )
        
        firestore_dict = association.to_firestore_dict()
        
        assert firestore_dict["id"] == "test_assoc"
        assert firestore_dict["name"] == "Test Association"
        assert firestore_dict["description"] == "Test description"
        assert firestore_dict["eventCategory"] == "ACADEMIC"  # Corregido: nombre del enum, no valor
        assert firestore_dict["pictureUrl"] == "https://example.com/picture.jpg"
        assert firestore_dict["logoUrl"] == "https://example.com/logo.png"
        assert firestore_dict["about"] == "This is a test association for testing purposes"
        assert "website" in firestore_dict["socialLinks"]
        assert "instagram" in firestore_dict["socialLinks"]
        assert "telegram" in firestore_dict["socialLinks"]
    
    def test_association_conversion_minimal(self):
        """Test Association with minimal fields"""
        association = Association(
            id="minimal_assoc",
            name="Minimal Assoc",
            description="Minimal",
            eventCategory=EventCategory.OTHER
        )
        
        firestore_dict = association.to_firestore_dict()
        
        assert firestore_dict["id"] == "minimal_assoc"
        assert firestore_dict["name"] == "Minimal Assoc"
        assert firestore_dict["pictureUrl"] is None
        assert firestore_dict["logoUrl"] is None
        assert firestore_dict["about"] is None
        assert firestore_dict["socialLinks"] == {}
    
    def test_event_conversion_full(self):
        """Test complete Event to Firestore conversion"""
        location = Location(46.5191, 6.5668, "EPFL")
        association = Association(
            id="test_assoc",
            name="Test",
            description="Test",
            eventCategory=EventCategory.SPORTS
        )
        price = Price(1500)
        
        event = Event(
            id="full_event",
            title="Full Test Event",
            description="Complete test event with all fields",
            location=location,
            time="2024-02-15 20:00",
            association=association,
            tags=["sports", "outdoor", "fun"],
            price=price,
            pictureUrl="https://example.com/event.jpg"
        )
        
        firestore_dict = event.to_firestore_dict()
        
        assert firestore_dict["id"] == "full_event"
        assert firestore_dict["title"] == "Full Test Event"
        assert firestore_dict["description"] == "Complete test event with all fields"
        assert firestore_dict["time"] == "2024-02-15 20:00"
        assert firestore_dict["tags"] == ["sports", "outdoor", "fun"]
        assert firestore_dict["price"] == 1500
        assert firestore_dict["pictureUrl"] == "https://example.com/event.jpg"
        assert "location" in firestore_dict
        assert "association" in firestore_dict
    
    def test_event_id_generation(self):
        """Test unique event ID generation"""
        # Same title and source should generate same ID
        id1 = Event.generate_id("Welcome Party", "ESN")
        id2 = Event.generate_id("Welcome Party", "ESN")
        assert id1 == id2
        
        # Different title should generate different ID
        id3 = Event.generate_id("Farewell Party", "ESN")
        assert id1 != id3
        
        # Different source should generate different ID
        id4 = Event.generate_id("Welcome Party", "AeroPoly")
        assert id1 != id4
        
        # ID should be reasonable length (MD5 hash first 20 chars)
        assert len(id1) == 20
        assert isinstance(id1, str)
    
    def test_event_category_enum(self):
        """Test EventCategory enum functionality"""
        # Test all enum values
        assert EventCategory.CULTURE.value == "Culture"
        assert EventCategory.SPORTS.value == "Sports"
        assert EventCategory.TECH.value == "Tech"
        assert EventCategory.SOCIAL.value == "Social"
        assert EventCategory.ACADEMIC.value == "Academic"
        assert EventCategory.CAREER.value == "Career"  # Corregido
        assert EventCategory.OTHER.value == "Other"
        
        # Test display string conversion
        assert EventCategory.CULTURE.display_string() == "Culture"
        assert EventCategory.SPORTS.display_string() == "Sports"
        assert EventCategory.TECH.display_string() == "Tech"
        assert EventCategory.SOCIAL.display_string() == "Social"
        assert EventCategory.ACADEMIC.display_string() == "Academic"
        assert EventCategory.CAREER.display_string() == "Career"  # Corregido
        assert EventCategory.OTHER.display_string() == "Other"
        
        # Test Firestore conversion
        assert EventCategory.TECH.to_firestore_value() == "TECH"