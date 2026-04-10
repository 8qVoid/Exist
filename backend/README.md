# Exist Auth Backend

Supports:
- Email/password signup + verification code
- Email/password login
- Resend verification with cooldown
- Forgot password + reset code
- Google sign-in (ID token verification)
- Profile onboarding completion

## Sender account
Verification and reset emails are sent from:
- `existapp1@gmail.com`

## Setup
1. Install Node.js 20+
2. `cd backend`
3. `npm install`
4. Create `.env` in `backend/`:
   - `PORT=8080`
   - `JWT_SECRET=change-this`
   - `GMAIL_USER=existapp1@gmail.com`
   - `GMAIL_APP_PASSWORD=your_gmail_app_password`
   - `GOOGLE_WEB_CLIENT_ID=your_google_web_client_id.apps.googleusercontent.com`
5. Run: `npm start`

## Required Android config
- In `app/build.gradle.kts` set:
  - `AUTH_BASE_URL` (emulator: `http://10.0.2.2:8080/`)
  - `GOOGLE_WEB_CLIENT_ID`

## Gmail sender setup (manual)
1. Sign in to `existapp1@gmail.com`
2. Turn on 2-Step Verification in Google Account security
3. Create an App Password for "Mail"
4. Copy app password into `GMAIL_APP_PASSWORD`

## Endpoints
- `POST /auth/signup` { email, password }
- `POST /auth/resend-verification` { email }
- `POST /auth/verify-email` { email, code }
- `POST /auth/login` { email, password }
- `POST /auth/google` { idToken }
- `POST /auth/forgot-password` { email }
- `POST /auth/reset-password` { email, code, newPassword }
- `POST /auth/onboarding` { fullName, birthday, profilePhotoUri } (Bearer token)
- `POST /auth/signout`

## Notes
- This backend uses file-based user storage (`backend/data/users.json`) for simplicity.
- Verification/reset codes are in-memory (dev-friendly).
- Replace with DB + Redis and production hardening before launch.
