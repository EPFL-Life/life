

import sys
import os

# Set up path to include project root
project_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, project_root)

# Set environment variable for Firebase credentials
os.environ['FIREBASE_CREDENTIALS_PATH'] = os.path.join(project_root, "serviceAccountKey.json")