import pytest
from unittest.mock import Mock, patch
import sys
import os
import io
import importlib.util
from bs4 import BeautifulSoup
import requests

# Add the root directory to the path to be able to import the module
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

# Mock HTML content for different scenarios
MOCK_HTML_WITH_EVENTS = """
<!DOCTYPE html>
<html>
<body>
    <div class="o-posts-grid-post-body">
        <h4>Test Event Title</h4>
        <time>January 15, 2024</time>
        <img src="test.jpg" alt="Test Image">
        <a href="/event/1">Read more</a>
        <p>This is a test event description that is longer than 20 characters.</p>
    </div>
    <div class="o-posts-grid-post-body">
        <h4>Another Event</h4>
        <span>February 20, 2024</span>
        <p>Short desc.</p>
    </div>
</body>
</html>
"""

MOCK_HTML_NO_EVENTS = """
<!DOCTYPE html>
<html>
<body>
    <div class="other-class">No events here</div>
</body>
</html>
"""

MOCK_HTML_WITH_DATA_URI = """
<!DOCTYPE html>
<html>
<body>
    <div class="o-posts-grid-post-body">
        <h4>Event with Data URI</h4>
        <img src="data:image/png,base64...">
        <img src="real-image.jpg">
    </div>
</body>
</html>
"""

MOCK_HTML_WITH_PARENT_STRUCTURE = """
<!DOCTYPE html>
<html>
<body>
    <div class="parent-class">
        <div class="event-wrapper">
            <div class="o-posts-grid-post-body">
                <h4>Nested Event</h4>
            </div>
            <div class="sibling">Sibling info</div>
            <time class="date-class">2024-01-01</time>
        </div>
    </div>
</body>
</html>
"""

def load_inspect_event_module():
    """Load the inspect_event module for testing"""
    # Get the path to inspect_event.py
    current_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(current_dir)
    module_path = os.path.join(project_root, 'inspect_event.py')
    
    # Load the module
    spec = importlib.util.spec_from_file_location("inspect_event", module_path)
    module = importlib.util.module_from_spec(spec)
    
    # This will execute the module code when loaded
    return spec, module

class TestInspectEvent:
    """Tests for inspect_event.py"""
    
    @patch('requests.get')
    def test_with_events_found(self, mock_get):
        """Test when events are found"""
        # Configure mock
        mock_response = Mock()
        mock_response.content = MOCK_HTML_WITH_EVENTS.encode('utf-8')
        mock_get.return_value = mock_response
        
        # Capture stdout
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            # Load and execute the module
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        # Verifications
        assert "Found 2 event containers" in output
        assert "Test Event Title" in output
        assert "January 15, 2024" in output or "Jan" in output
        assert "test.jpg" in output
        assert "/event/1" in output
        assert "longer than 20 characters" in output
        assert "TARGETED AGEPOLY EVENT ANALYSIS" in output
    
    @patch('requests.get')
    def test_no_events_found(self, mock_get):
        """Test when no events are found"""
        mock_response = Mock()
        mock_response.content = MOCK_HTML_NO_EVENTS.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        assert "Found 0 event containers" in output
        assert "First container classes: None" in output
    
    @patch('requests.get')
    def test_with_data_uri_images(self, mock_get):
        """Test that verifies data URIs are filtered"""
        mock_response = Mock()
        mock_response.content = MOCK_HTML_WITH_DATA_URI.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        # The real image should be mentioned
        assert "real-image.jpg" in output
        
        # Data URI should NOT appear in image src lines
        # Check all lines that mention "Image src:"
        lines = output.split('\n')
        image_src_lines = [line for line in lines if "Image src:" in line]
        
        # Verify no data URIs in image src lines
        for line in image_src_lines:
            assert "data:" not in line, f"Data URI found in image src: {line}"
        
        # Verify images section exists
        assert "Images in event:" in output
    
    @patch('requests.get')
    def test_parent_structure_analysis(self, mock_get):
        """Test that verifies parent structure analysis"""
        mock_response = Mock()
        mock_response.content = MOCK_HTML_WITH_PARENT_STRUCTURE.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        assert "Nested Event" in output
        # Verify parent structure is analyzed
        assert "Parent element" in output or "Grandparent" in output
    
    @patch('requests.get')
    def test_request_headers(self, mock_get):
        """Test that verifies correct headers are sent"""
        mock_response = Mock()
        mock_response.content = MOCK_HTML_WITH_EVENTS.encode('utf-8')
        mock_get.return_value = mock_response
        
        spec, module = load_inspect_event_module()
        try:
            spec.loader.exec_module(module)
        except SystemExit:
            pass
        
        # Verify requests.get was called with correct headers
        mock_get.assert_called_once()
        call_args = mock_get.call_args
        assert call_args[0][0] == "https://agepoly.ch/en/evenements/"
        assert 'User-Agent' in call_args[1]['headers']
        assert call_args[1]['headers']['User-Agent'] == 'Mozilla/5.0'
    
    @patch('requests.get')
    def test_empty_event_containers(self, mock_get):
        """Test when event_containers is empty"""
        mock_response = Mock()
        mock_response.content = b'<html></html>'
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        assert "Found 0 event containers" in output
    
    @patch('requests.get')
    def test_event_without_title(self, mock_get):
        """Test when an event has no title"""
        html = """
        <html>
        <body>
            <div class="o-posts-grid-post-body">
                <p>No title here</p>
            </div>
        </body>
        </html>
        """
        
        mock_response = Mock()
        mock_response.content = html.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        assert "Found 1 event containers" in output
    
    @patch('requests.get')
    def test_network_error_handling(self, mock_get):
        """Test when there's a network error"""
        # The original script doesn't handle exceptions, so this will fail
        mock_get.side_effect = requests.exceptions.ConnectionError("Network error")
        
        # This test verifies the script fails when there's a network error
        with pytest.raises(requests.exceptions.ConnectionError):
            spec, module = load_inspect_event_module()
            spec.loader.exec_module(module)
    
    @patch('requests.get')
    def test_all_sections_printed(self, mock_get):
        """Test that verifies all sections are printed"""
        mock_response = Mock()
        mock_response.content = MOCK_HTML_WITH_EVENTS.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        # Verify all main sections are present
        sections = [
            "TARGETED AGEPOLY EVENT ANALYSIS",
            "1. Looking for event containers",
            "2. STRUCTURE OF FIRST EVENT",
            "3. ELEMENTS INSIDE FIRST EVENT CONTAINER",
            "4. SEARCHING FOR SPECIFIC EVENT DATA",
            "5. CHECKING PARENT STRUCTURE",
            "ANALYSIS COMPLETE"
        ]
        
        for section in sections:
            assert section in output, f"Section '{section}' not found in output"
    
    @patch('requests.get')
    def test_event_with_multiple_children(self, mock_get):
        """Test with event that has multiple child elements"""
        html = """
        <html>
        <body>
            <div class="o-posts-grid-post-body">
                <h4 class="event-title">Event 1</h4>
                <div class="date">2024-01-01</div>
                <img src="img1.jpg" alt="Image 1">
                <img src="img2.jpg" alt="Image 2">
                <a href="/link1" class="btn">Link 1</a>
                <a href="/link2" class="btn">Link 2</a>
                <p>Description 1</p>
                <p>Description 2</p>
            </div>
        </body>
        </html>
        """
        
        mock_response = Mock()
        mock_response.content = html.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        assert "Event 1" in output
        # The images and links should be found
        assert any("img" in line.lower() for line in output.split('\n'))
    
    @patch('requests.get')
    def test_event_with_date_search(self, mock_get):
        """Test that verifies date search functionality"""
        html = """
        <html>
        <body>
            <div class="o-posts-grid-post-body">
                <h4>Event with date</h4>
                <time datetime="2024-03-15">March 15, 2024</time>
                <span>Monday, March 18, 2024</span>
                <div>Some other text</div>
            </div>
        </body>
        </html>
        """
        
        mock_response = Mock()
        mock_response.content = html.encode('utf-8')
        mock_get.return_value = mock_response
        
        with patch('sys.stdout', new_callable=io.StringIO) as mock_stdout:
            spec, module = load_inspect_event_module()
            try:
                spec.loader.exec_module(module)
            except SystemExit:
                pass
            output = mock_stdout.getvalue()
        
        # Should find dates or search for them
        assert "date" in output.lower() or "March" in output or "2024" in output
    
    def test_module_can_be_loaded(self):
        """Test that the module can be loaded"""
        spec, module = load_inspect_event_module()
        assert spec is not None
        assert module is not None