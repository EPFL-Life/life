#!/usr/bin/env python3
"""Tests for configuration modules"""
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

import pytest
from scrapers.website_config import (
    SelectorConfig, WebsiteConfig, 
    ESN_EPFL_CONFIG, AGEPOLY_CONFIG,
    DEFAULT_COORDINATES, DEFAULT_LOCATION_NAME
)

class TestSelectorConfig:
    """Test SelectorConfig class"""
    
    def test_selector_creation(self):
        """Test creating SelectorConfig with defaults"""
        config = SelectorConfig()
        
        assert isinstance(config.event_container, list)
        assert isinstance(config.title, list)
        assert config.event_container == []
        assert config.title == []
    
    def test_selector_with_values(self):
        """Test SelectorConfig with specific values"""
        config = SelectorConfig(
            event_container=[".events", ".posts"],
            title=["h2", ".title"],
            date=[".date", "time"]
        )
        
        assert config.event_container == [".events", ".posts"]
        assert config.title == ["h2", ".title"]
        assert config.date == [".date", "time"]
    
    def test_get_method_without_detailed(self):
        """Test get() method without detailed flag"""
        config = SelectorConfig(
            title=["h2", ".title"],
            detailed_title=["h1.detailed"]
        )
        
        # Should return regular title selectors
        assert config.get("title") == ["h2", ".title"]
        assert config.get("date") == []  # Default empty list
    
    def test_get_method_with_detailed(self):
        """Test get() method with detailed flag"""
        config = SelectorConfig(
            title=["h2.list"],
            detailed_title=["h1.detail"]
        )
        
        # With detailed=True, should return detailed selectors
        assert config.get("title", detailed=True) == ["h1.detail"]
        
        # If no detailed selector, fall back to regular
        config2 = SelectorConfig(title=["h2"])
        assert config2.get("title", detailed=True) == ["h2"]
    
    def test_to_dict_method(self):
        """Test conversion to dictionary"""
        config = SelectorConfig(
            event_container=[".event"],
            title=["h2"],
            date=[".date"]
        )
        
        config_dict = config.to_dict()
        
        assert isinstance(config_dict, dict)
        assert config_dict["event_container"] == [".event"]
        assert config_dict["title"] == ["h2"]
        assert config_dict["date"] == [".date"]
        assert "detailed_title" in config_dict  # Should be present even if empty

class TestWebsiteConfig:
    """Test WebsiteConfig class"""
    
    def test_website_config_creation(self):
        """Test basic WebsiteConfig creation"""
        from models.event_models import Association, EventCategory
        
        selectors = SelectorConfig(title=["h2"])
        association = Association(
            id="test",
            name="Test",
            description="Desc",
            event_category=EventCategory.SOCIAL
        )
        
        config = WebsiteConfig(
            name="Test Site",
            url="https://test.com",
            base_domain="https://test.com",
            selectors=selectors,
            association=association
        )
        
        assert config.name == "Test Site"
        assert config.url == "https://test.com"
        assert config.base_domain == "https://test.com"
        assert config.selectors == selectors
        assert config.association == association
        assert config.default_location == DEFAULT_LOCATION_NAME
        assert config.coordinates == DEFAULT_COORDINATES
    
    def test_coordinates_default_factory(self):
        """Test that coordinates uses default_factory properly"""
        from models.event_models import Association, EventCategory
        
        selectors = SelectorConfig()
        association = Association("id", "name", "desc", EventCategory.SOCIAL)
        
        config1 = WebsiteConfig(
            name="Site 1",
            url="https://site1.com",
            base_domain="https://site1.com",
            selectors=selectors,
            association=association
        )
        
        config2 = WebsiteConfig(
            name="Site 2",
            url="https://site2.com",
            base_domain="https://site2.com",
            selectors=selectors,
            association=association
        )
        
        # Both should have same coordinates but different objects
        assert config1.coordinates == config2.coordinates
        assert config1.coordinates is not config2.coordinates  # Different instances

class TestPredefinedConfigs:
    """Test predefined configurations (ESN and Agepoly)"""
    
    def test_esn_config_exists(self):
        """Test ESN configuration is properly defined"""
        assert isinstance(ESN_EPFL_CONFIG, WebsiteConfig)
        assert ESN_EPFL_CONFIG.name == "ESN EPFL Lausanne"
        assert "epfl.esn.ch" in ESN_EPFL_CONFIG.url
        
        # Check selectors are defined
        assert len(ESN_EPFL_CONFIG.selectors.event_container) > 0
        assert len(ESN_EPFL_CONFIG.selectors.title) > 0
        
        # Check association
        assert ESN_EPFL_CONFIG.association.id == "esn_epfl_lausanne"
        assert ESN_EPFL_CONFIG.association.name == "ESN EPFL Lausanne"
    
    def test_agepoly_config_exists(self):
        """Test Agepoly configuration is properly defined"""
        assert isinstance(AGEPOLY_CONFIG, WebsiteConfig)
        assert AGEPOLY_CONFIG.name == "AGEPoly EPFL"
        assert "agepoly.ch" in AGEPOLY_CONFIG.url
        
        # Check selectors
        assert len(AGEPOLY_CONFIG.selectors.event_container) > 0
        assert ".o-posts-grid-post-body" in AGEPOLY_CONFIG.selectors.event_container[0]
        
        # Check association
        assert AGEPOLY_CONFIG.association.id == "agepoly_epfl"
        assert AGEPOLY_CONFIG.association.name == "AGEPoly EPFL"
    
    def test_all_websites_list(self):
        """Test ALL_WEBSITES list"""
        from scrapers.website_config import ALL_WEBSITES
        
        assert isinstance(ALL_WEBSITES, list)
        assert len(ALL_WEBSITES) >= 2
        
        # Should contain both configs
        config_names = [config.name for config in ALL_WEBSITES]
        assert "ESN EPFL Lausanne" in config_names
        assert "AGEPoly EPFL" in config_names

class TestConstants:
    """Test configuration constants"""
    
    def test_default_coordinates(self):
        """Test DEFAULT_COORDINATES"""
        assert isinstance(DEFAULT_COORDINATES, dict)
        assert "latitude" in DEFAULT_COORDINATES
        assert "longitude" in DEFAULT_COORDINATES
        assert "name" in DEFAULT_COORDINATES
        
        assert DEFAULT_COORDINATES["latitude"] == pytest.approx(46.5191)
        assert DEFAULT_COORDINATES["longitude"] == pytest.approx(6.5668)
        assert DEFAULT_COORDINATES["name"] == DEFAULT_LOCATION_NAME
    
    def test_default_location_name(self):
        """Test DEFAULT_LOCATION_NAME"""
        assert DEFAULT_LOCATION_NAME == "EPFL Campus"
        assert isinstance(DEFAULT_LOCATION_NAME, str)

if __name__ == "__main__":
    pytest.main([__file__, "-v", "--tb=short"])