#!/usr/bin/env python3
"""Test Firebase connection"""

import sys
import os
import logging

# Add current directory to Python path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))


# Add parent directory to path so we can import our modules
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from firestore.database import FirebaseDatabase

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

def test_firebase():
    print("🧪 Testing Firebase connection...")
    
    try:
        # Test 1: Initialize Firebase
        print("1. Initializing Firebase...")
        db = FirebaseDatabase()
        print("    Firebase connection successful!")
        
        # Test 2: Get existing events
        print("2. Getting existing events...")
        existing_ids = db.get_existing_event_ids()
        print(f"    Found {len(existing_ids)} existing events")
        
        # Test 3: Try to close connection
        print("3. Testing connection cleanup...")
        db.close()
        print("    Connection closed successfully")
        
        print("\n🎉 All Firebase tests passed!")
        return 0
        
    except FileNotFoundError as e:
        print(f"  ERROR: {e}")
        print("   ℹ  Make sure serviceAccountKey.json is in the project root")
        return 1
    except Exception as e:
        print(f"  ERROR: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(test_firebase())