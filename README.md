# OnCore: Musical Calendar & Social Wallet

**OnCore** is a comprehensive Android application designed for theater enthusiasts. It solves the problem of organizing physical ticket stubs and coordinating with friends by combining a digital ticket wallet, a personalized calendar, and a real-time social community feed.

### Team Members:
| Name                    | Email                |
| ----------------------- | -------------------- |
| Yutong Qin (Yoki)       | yutongq@bu.edu       |
| Yuqian Cui (Nana)       | yqcui@bu.edu         |
| Yingtong Shen (Sophie)  | shenyt@bu.edu        |

---

## Live Demo

https://github.com/user-attachments/assets/6427b675-7e79-408f-90ae-f3da33808c63

---

## Feature List & Status

| Feature Category | Feature Name | Status | Description |
| :--- | :--- | :--- | :--- |
| **Core** | **Ticket Wallet** | ✅ Completed | Digitalize tickets using Camera/Gallery; Local storage via Room. |
| **Core** | **Personal Calendar** | ✅ Completed | Monthly/Weekly views; Offline support via Room DB. |
| **Discovery** | **Daily Pick** | ✅ Completed | Fetches musical previews via **iTunes API**. |
| **Discovery** | **Event Search** | ✅ Completed | Real-time show search via **Ticketmaster API**. |
| **Social** | **Community Feed** | ✅ Completed | Real-time feed of public tickets; "Helpful" likes & comments. |
| **Social** | **Headcounts** | ✅ Completed | "Who's Going" counter synced across users via Firestore. |
| **Analytics** | **Budget Tracker** | ✅ Completed | Visual spending charts and lifetime stats dashboard. |
| **User** | **Profile & Auth** | ✅ Completed | Full Firebase Auth (Login/Register); Profile editing. |
| **Cloud** | **Image Storage** | ✅ Completed | Cloud syncing of ticket images via **Firebase Storage**. |
| **ML** | **Recommendation** | ⏸️ Future | Planned: Scoring user's wallet to recommend shows. |

---

## Tech Stack:

| Category | Technology | Usage |
| :--- | :--- | :--- |
| **Language** | **Kotlin** | Android development. |
| **UI** | **Jetpack Compose** | Modern declarative UI toolkit (Material 3 Design). |
| **Architecture** | **MVVM** | Model, UI, ViewModel. |
| **Async** | **Coroutines & Flow** | Managing background threads and reactive state updates. |
| **Networking** | **Retrofit** | API communication (Ticketmaster & iTunes). |
| **Image Loading** | **Coil** | Efficient asynchronous image loading and caching. |
| **Local Data** | **Room Database** | Offline data persistence (SQLite object mapping). |
| **Preferences** | **DataStore** | Storing lightweight user settings (e.g., Budget). |
| **Cloud** | **Firebase** | Authentication, Firestore (NoSQL), and Cloud Storage. |
| **Hardware** | **Sensors** | Camera (Ticket Scanning) & Audio (MediaPlayer). |

---

## Architecture:

The project follows the MVVM (Model-View-ViewModel) architecture pattern, utilizing a repository layer to mediate between data sources and the UI. This ensures a clean separation of concerns and facilitates Unidirectional Data Flow (UDF).

<img width="395" height="581" alt="Weixin Image_20251216150934_1451" src="https://github.com/user-attachments/assets/93d8ba86-acad-4331-84dd-caacaafaaad1" />

<img width="637" height="483" alt="Screenshot 2025-12-16 at 15 27 00" src="https://github.com/user-attachments/assets/cb22162c-0540-4a89-af96-9a7a33f7f83a" />

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
   git clone https://github.com/yoki071002/cs501_fp
   cd cs501_fp
2. Open the project in Android Studio.  
3. Let Gradle sync automatically (or trigger manually with `Sync Project with Gradle Files`).  
4. Set the `build variant` to Debug.  
5. Run the app on an emulator or a connected Android device.  

---

## AI Reflection

We utilized AI assistants (LLMs) primarily for debugging code, resolving syntax errors, and troubleshooting integration issues throughout the development process.
