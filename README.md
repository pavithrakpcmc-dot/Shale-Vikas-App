# Shaale-Vikas Android App

**Shaale-Vikas** is a School-Alumni Bridge Platform built with Kotlin, Jetpack Compose, and Firebase. The app connects headmasters and alumni to raise funds for school needs like repairs, furniture, and learning resources.

## Project Overview

- **Role-based app** for Headmaster and Alumni users
- **Firebase Phone Authentication** for secure login
- **Firestore** collections for users, needs, pledges, and leaderboard
- **Firebase Storage** support for need photos
- **Cloud Functions** for cost suggestions and pledge aggregation
- **Demo fallback mode** when Firebase is not configured

## Features

- Headmaster and alumni login via phone number
- Create and manage school needs with cost estimates
- Track funding progress for each need
- View leaderboard / Hall of Fame for top pledges
- Search and filter needs by category
- View need details, cost breakdown, and funding status

## Tech Stack

- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Firestore
- Firebase Cloud Functions
- Firebase Storage
- Gradle

## Setup and Run

1. Clone this repository.
2. Open the project in Android Studio.
3. Add `app/google-services.json` from your Firebase project.
4. Enable Firebase Authentication > Phone provider.
5. Enable Cloud Firestore and Firebase Storage.
6. Sync Gradle and build the project.

### Firebase deployment

- If you want to deploy backend rules and functions, install Firebase CLI.
- Run:

```bash
firebase deploy --only firestore:rules,storage,functions
```

## Screenshot Gallery

The current screenshot files in the `IMAGES/` folder are shown below and are embedded for GitHub preview with reduced width.

1. **Screenshot 1**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.35%20PM.jpeg" alt="Screenshot 1" width="400" />

2. **Screenshot 2**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.35%20PM%20%281%29.jpeg" alt="Screenshot 2" width="400" />

3. **Screenshot 3**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.35%20PM%20%282%29.jpeg" alt="Screenshot 3" width="400" />

4. **Screenshot 4**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.36%20PM.jpeg" alt="Screenshot 4" width="400" />

5. **Screenshot 5**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.36%20PM%20%281%29.jpeg" alt="Screenshot 5" width="400" />

6. **Screenshot 6**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.36%20PM%20%282%29.jpeg" alt="Screenshot 6" width="400" />

7. **Screenshot 7**

   <img src="IMAGES/WhatsApp%20Image%202026-05-15%20at%203.20.37%20PM.jpeg" alt="Screenshot 7" width="400" />

> Note: The previous README image references pointed to missing screenshot files such as `00_splash.png` and `01_phone_login.png`. This section now uses the actual screenshot assets currently present.

## Documentation

- See [PROJECT_EVALUATION_CRITERIA.md](PROJECT_EVALUATION_CRITERIA.md) for the automated project evaluation guidance used by Team MindMatrix.

## Notes

- Keep the repository public until evaluation is complete.
- Do not commit large generated build files or local Firebase config files.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
