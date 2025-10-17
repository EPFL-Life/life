# Example data to show DB structure


## Associations
```json
{
  "associations": {
    "m1q85sb9tYefVwwdiIjD": {
      "name": "ESN BQQ",
      "description": "We do stuff like fruit juggling",
      "pictureUrl": "https://i1.sndcdn.com/artworks-x8zI2HVC2pnkK7F5-4xKLyA-t500x500.jpg"
      "categories": [
        "Vibes",
        "Food"
      ]
    }
  }
}
```

## Events
```json
{
  "events": {
    "Q0plDPEWMOXD85IJr8IF": {
      "title": "The big pineapple BBQ",
      "description": "Bring your pineapple and light it on fire",
      "location": "Your mom's house",
      "time": "2025-10-29T19:30:00Z",
      "association": "associations/m1q85sb9tYefVwwdiIjD",
      "tags": [
        "fun",
        "food",
        "fire"
      ],
      "price": 15,
      "pictureUrl": "https://i.ytimg.com/vi/X1fgQTatw4M/maxresdefault.jpg"
    }
  }
}
```

## Users
```json
{
  "users": {
    "OsVukB1YTBX6JlXYXYrWOULrWfG2": {
      "name": "Gustav Onsberg",
      "subscriptions": [
        "associations/m1q85sb9tYefVwwdiIjD"
      ],
      "settings": {
        "darkfole": false
      }
    }
  }
}
```

The documentname/ID for the user is the Firebase Auth UID.