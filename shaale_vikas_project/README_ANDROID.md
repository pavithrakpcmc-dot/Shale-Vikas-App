# Shaale-Vikas Android App

Kotlin Android app using Jetpack Compose and Firebase.

## Included

- Firebase Phone Authentication flow.
- Firestore collections for users, school needs, pledges and leaderboard.
- Firebase Storage path support for need before-photos.
- Firestore and Storage security rules.
- Firebase Cloud Functions for cost suggestions and pledge aggregation.
- Demo fallback data so the UI opens before Firebase is configured.

## Firebase setup

1. Create a Firebase project.
2. Add Android app package: `com.shaalevikas.app`.
3. Download `google-services.json` and place it at `app/google-services.json`.
4. Enable Firebase Authentication > Phone provider.
5. Enable Cloud Firestore and Firebase Storage.
6. Install Firebase CLI and run `firebase init` only if you want to link an existing project locally.
7. Deploy backend from this folder: `firebase deploy --only firestore:rules,storage,functions`.

## Firestore collections

- `users/{uid}`: user profile and role.
- `needs/{needId}`: school needs created by headmasters.
- `pledges/{pledgeId}`: alumni pledge records.
- `leaderboard/{uid}`: total pledged per alumni.

Open this folder in Android Studio and sync Gradle. Without `google-services.json`, the app still runs in demo mode but Firebase operations show a setup message.


