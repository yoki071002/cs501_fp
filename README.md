# OnCore: Musical Calendar & Social Wallet

**OnCore** is a comprehensive Android application designed for theater enthusiasts. It solves the problem of organizing physical ticket stubs and coordinating with friends by combining a digital ticket wallet, a personalized calendar, and a real-time social community feed.

### Team Members:
| Name                    | Email                |
| ----------------------- | -------------------- |
| Yutong Qin (Yoki)       | yutongq@bu.edu       |
| Yuqian Cui (Nana)       | yqcui@bu.edu         |
| Yingtong Shen (Sophie)  | shenyt@bu.edu        |

---

## Feature List & Status

| Feature Category | Feature Name | Status | Description |
| :--- | :--- | :--- | :--- |
| Core | **Ticket Wallet** | ✅ Completed | Digitalize tickets using Camera/Gallery; 3D Flip animation. |
| Core | **Personal Calendar** | ✅ Completed | Monthly/Weekly views; Offline support via Room DB. |
| Discovery | **Daily Pick** | ✅ Completed | Fetches musical previews via iTunes API. |
| Discovery | **Event Search** | ✅ Completed | Real-time show search via Ticketmaster API. |
| Social | **Community Feed** | ✅ Completed | Real-time feed of public tickets; "Helpful" likes. |
| Social | **Headcounts** | ✅ Completed | "Who's Going" counter synced across users. |
| Analytics | **Budget Tracker** | ✅ Completed | Visual spending charts and lifetime stats. |
| User | **Profile & Auth** | 🚧 In Progress | Firebase Auth implemented; Linking user avatars pending. |
| Cloud | **Image Storage** | ⏸️ Descoped | Using local URI for privacy/speed; Cloud Storage planned for future. |
| ML | **Recommendation System** | ⏸️ Descoped | By scanning and scoring user's wallet to determine recommendation in community feed |


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
3. Open the project in Android Studio.  
4. Let Gradle sync automatically (or trigger manually with `Sync Project with Gradle Files`).  
5. Set the `build variant` to Debug.  
6. Run the app on an emulator or a connected Android device.  

---

## Architecture

The project follows the **MVVM (Model-View-ViewModel)** architecture pattern to ensure separation of concerns and testability.

### Directory Structure
Our repository is organized to strictly follow the MVVM separation:
com.example.cs501_fp
├── data                # Model Layer
│   ├── firebase        # Auth & Cloud Logic
│   ├── local           # Room DB (Entity, DAO)
│   ├── network         # Retrofit Services (Ticketmaster, iTunes)
│   └── repository      # Mediator with data source
├── ui                  # View Layer
│   ├── components      # Reusable Composables (BottomBar, ShowCard)
│   ├── navigation      # NavGraph & Route Definitions
│   ├── pages           # Screen-level Composables (Home, Calendar, Community)
│   └── theme           # Material 3 Theme & Color System
├── viewmodel           # ViewModel Layer
│   ├── CalendarViewModel.kt
│   ├── CommunityViewModel.kt
│   └── ...
└── util                # Constants & Helpers

### Tech Stack
*   Language: Kotlin
*   UI: Jetpack Compose (Material 3)
*   Async: Coroutines & Flow
*   Local Data: Android Room Database
*   Cloud Data: Firebase Firestore & Authentication
*   Network: Retrofit & OkHttp & Coil (Image Loading)

---

## Debugging & Testing Strategy

We adopted a **Systematic Debugging Loop** rather than relying solely on print statements.

### 1. Debugging Case Study: The Serialization Crash
*   Symptom: The Community Feed returned 0 items, even though documents existed in the Firestore console.
*   Localization: We traced the failure to `FirestoreRepository.getPublicEvents()`. Logs showed a generic success but empty lists.
*   Stack Trace Analysis: Using Logcat, we finally caught a silenced exception:
    > `java.lang.RuntimeException: Could not deserialize object. Class UserEvent does not define a no-argument constructor.`
*   Root Cause: A naming conflict. Our Kotlin Data Class used `isPublic` (boolean), but Firestore's auto-generated field was named `public`. The default serializer failed to map them.
*   Fix: We applied the `@PropertyName("public")` annotation to the field and ensured all data class fields had default values (e.g., `= ""`) to satisfy the no-arg constructor requirement.

### 2. Testing Strategy
*   Manual Testing: Verified UI flows on Android Emulators.

---

## AI Usage Statement

We utilized Generative AI tools (ChatGPT-4o) to accelerate development. Below is a breakdown of our usage, limitations encountered, and corrections made.

### 1. Tools & Usage
*   **Error Analysis:** Used to explain obscure Logcat stack traces.
*   **Brainstorming:** Used to organize ideas for the "Stretch Goals" (e.g., Recommendation Algorithms, we had similar experiences before, but need to transform from large internet company's product into our personalized app).

### 2. Specific Example
*   Scenario: Implementing the Camera feature to save ticket images.
*   Prompt: "How do I use ActivityResultContracts.TakePicturePreview in Jetpack Compose to save a bitmap to internal storage?"
*   AI Response: Provided a code snippet using `LocalContext.current` and a file output stream.
*   Correction Required: The AI's code caused a memory leak by not recycling the bitmap and used deprecated `getExternalStorageDirectory` methods. We had to manually refactor it to use `context.openFileOutput` for better security and privacy.

### 3. Limitations & Learnings
*   The AI frequently suggested Material 2 code which is incompatible with our **Material 3** project. We demonstrated understanding by manually migrating these components to the latest M3 standards.
*   The AI generated Kotlin data classes without default values. This caused a RuntimeException because Firestore requires a no-argument constructor. We identified the root cause via Logcat and fixed it by initializing all fields.
