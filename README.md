# cs501_fp: Musical Calendar App

### Team Members:
| Name                    | Email                |
| ----------------------- | -------------------- |
| Yutong Qin (Yoki)       | yutongq@bu.edu       |
| Yuqian Cui (Nana)       | yqcui@bu.edu         |
| Yingtong Shen (Sophie)  | shenyt@bu.edu        |

---

## Overview

Musical Calendar is an Android application that helps users explore, track, and record their musical theatre experiences.  
It combines real-time event discovery from external APIs with personalized calendar and note-taking features, designed especially for Broadway and musical fans.

The app offers two core experiences:
1. Event Discovery: Browse currently running Broadway or Off-Broadway musicals in New York City using APIs such as *Ticketmaster* and *iTunes*.
2. Personal Calendar & Ticket Records: Add, view, and manage your own show plans or attendance history, with support for user-generated entries, notes, and images.

---

## Build & Run Instructions  

### Requirements
- Android Studio
- Android SDK 34+  
- Gradle 8.x  
- Kotlin 1.9+  

### Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/<your-team>/cs501_fp.git
   cd cs501_fp
3. Open the project in Android Studio.  
4. Let Gradle sync automatically (or trigger manually with `Sync Project with Gradle Files`).  
5. Set the `build variant` to Debug.  
6. Run the app on an emulator or a connected Android device.  

---

## Current Features  

### Home Page
- Displays a "Daily Pick" musical song fetched from the *iTunes API* with a "Listen" button.  
- Shows upcoming Broadway events for the current week using *Ticketmaster API*.  
- Supports navigation to a detailed event page with show info (title, venue, date, and ticket link).  
- Scrollable interface and week-switching planned for next iteration.

### My Calendar
- User’s personal view of scheduled and attended shows.  
- (Planned) Add/Edit/Delete custom events with notes and photos.  

### Tickets
- Placeholder page for future ticket management and wishlist features.

### Profile
- User profile page (under development).  

---

## Architecture  

The project follows MVVM architecture with clear separation of concerns:  
- **Model:** Handles data from APIs and (future) Room database.  
- **ViewModel:** Manages app state and business logic using `StateFlow`.  
- **View (Compose UI):** Displays the data reactively using Jetpack Compose.

---

## Next Steps / Roadmap  
- Integrate *Room Database* for saving user-added events.  
- Add playback controls for musical previews (pause/resume).  
- Enable reviews and user comments for attended shows.  
- Include monetary aspects for attended and planned to attend shows.  
- Add sharing features for users to interact within community.  
