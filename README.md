<p>
  <img src="assets/EPFL-LIFE.svg" alt="EPFL LIFE Logo" width="300">
</p>

# EPFL LIFE
This README provides a high-level overview of the project, while detailed technical and design documentation can be found in the Wiki and Figma links below.


## Pitch

The app for EPFL student associations. The goal is to provide an easy way for students to find associations and stay up to date with events.
Associations can publish events and make announcements. Students have a simple overview of the events that are interesting to them. An integrated map view with the current GPS location will make it easy to find the events.

## Problem statement

The problem we are trying to solve is the mess that is event announcements. Associations like ESN, AgePoly, etc. publish messages via Outlook, Telegram, Instagram, WhatsApp, EPFL website or their individual website. This makes it very hard to stay up-to-date on what is happening on and around campus. By providing a unified way of sharing this information, we help both the associations and the students.

## Core features for associations

Associations and their administrators can:
- Create and manage association pages
- Publish events with dates, locations, prices, descriptions and images
- Edit existing events and association details
- View the list of attendees for their events

## Core features for students

Students can:
- Log in using Google-based authentication 
- Browse and subscribe to associations 
- View and enroll in events 
- Access a personalized calendar showing enrolled events 
- Switch between viewing all events or only enrolled events 
- View event locations on an interactive map 
- Follow friends and see their attendance highlighted in event participant lists 
- Manage their profile information (name and profile picture)
- Share events to their friends

## Extended features (stretch)

- Live events (user-post with photo+location)
- Association announcements
- Digital membership cards
- Europe-wide login for guest students
- Ticket and payment integration
- Event discovery based on proximity

## Project Architecture Overview

EPFL LIFE is an Android application built using a modular architecture:

- **Frontend**: Android app built with Jetpack Compose, handling UI, navigation, and user interaction.
- **Authentication**: Google-based authentication used to manage user accounts and sessions.
- **Data Layer**: Centralized repositories manage associations, events, users, and attendance data.
- **External Services**: Firestore Database to store association, event and user data, and Firebase Storage to save uploaded media such as profile pictures, banners and association logos. 
- **Maps & Location**: Integrated google maps to view event locations and the user’s current position.
- **Admin Features**: Separate flows for association administrators to manage events and profiles.

A detailed breakdown of the architecture, data flow, and implementation decisions is available in the project Wiki.


## Wiki

The project Wiki serves as the main technical documentation hub.  
It includes detailed explanations of the app architecture, data flow, feature implementation, and developer guidelines.

Please take a look at the [wiki](https://github.com/EPFL-Life/life/wiki) explaining the app features.

## Figma

https://www.figma.com/design/qNjleM72FjEUEppPUSS2Ok/EPFL-Life?node-id=0-1&t=auqmWM20TucyArQS-1

### Figma Design Documentation

All wireframes, mockups, and user flow diagrams are documented in the [**Figma Design Documentation →**](docs/figma-designs.md)

## Architecture

[Architecture Diagram](assets/ArchitecturueDiagramm_graphviz.png)

## References

This project uses the [Swent B3 Solution](https://github.com/swent-epfl/bootcamp-25-B3-Solution)
as the starting point.
It also uses the [Android Sample Project](https://github.com/GabrielFleischer/Android-Sample) as an inspiration.
It may contain AI generated code and text.

