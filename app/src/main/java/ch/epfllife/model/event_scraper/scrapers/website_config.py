from models.event_models import Association, EventCategory

ESN_EPFL_CONFIG = {
    "name": "ESN EPFL Lausanne",
    "url": "https://epfl.esn.ch/events",
    "base_domain": "https://epfl.esn.ch",
    "default_location": "EPFL Campus",
    "coordinates": {"latitude": 46.5191, "longitude": 6.5668, "name": "EPFL Campus"},
    
    "selectors": {
        
        "event_container": [
            ".views-row",                   
            ".node.node-event",              
            ".view-content .views-row"       
        ],
        
       
        "title": [
            ".field-name-title h2",        
            ".field-name-title h2 a",        
            "h2",                            
            ".title"                        
        ],
        
     
        "date": [
            ".date-display-single",         
            ".field-name-field-date",        
            "[class*='date-display']",       
            ".field-name-field-date span"    
        ],
        
       
        "location": [
            ".field-name-field-location",    
            "[class*='location']",           
            ".location",                     
            ".venue"                         
        ],
        
       
        "description": [
            ".field-name-body",             
            ".description",                  
            "p",                             
        ],
        
        
        "price": [
            ".field-name-field-price",       
            "[class*='price']",              
            ".price",                        
            ".cost"                          
        ],
        
       
        "image": [
            ".field-name-field-image img",   
            ".group-image img",              
            "img[src*='events']",
            "img"                           
        ],
        "event_link": [".views-row h2 a"],

        "detailed_title": ["h1", ".field-name-title h2"],
        "detailed_date": [".date-display-single"],
        "detailed_description": [".field-name-body", ".content"],
        "detailed_location": [
            ".field-name-field-location",    
            ".field-location",               
            ".location",                     
            ".venue",
            ".field-name-field-venue",       # Posible selector alternativo
            ".field-venue",
            ".address"
        ],

        "detailed_price": [
            ".field-name-field-price",       
            ".field-price",                  
            ".price",                        
            ".cost",
            ".field-name-field-cost",        # Posible selector alternativo
            ".field-cost",
            "[class*='price']",              # Cualquier clase que contenga "price"
            "[class*='cost']"                # Cualquier clase que contenga "cost"
        ],
        "detailed_image": [".field-name-field-image img", ".main-image img"],

        

    },
    
    "association": Association(
        id="esn_epfl_lausanne",
        name="ESN EPFL Lausanne",
        description="Erasmus Student Network at EPFL Lausanne. We organize events for international students.",
        event_category=EventCategory.SOCIAL,
        picture_url="https://epfl.esn.ch/sites/epfl.esn.ch/files/logo_epfl.png",
        logo_url="https://epfl.esn.ch/sites/epfl.esn.ch/files/logo_epfl.png",
        about="ESN EPFL Lausanne helps international students integrate at EPFL through social and cultural events.",
        social_links={
            "website": "https://epfl.esn.ch",
            "facebook": "https://facebook.com/esn.epfl",
            "instagram": "https://instagram.com/esn_epfl"
        }
    )
}

ALL_WEBSITES = [ESN_EPFL_CONFIG]