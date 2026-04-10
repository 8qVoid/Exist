import "dotenv/config";
import fs from "fs";
import path from "path";
import express from "express";
import cors from "cors";
import jwt from "jsonwebtoken";
import bcrypt from "bcryptjs";
import nodemailer from "nodemailer";
import { OAuth2Client } from "google-auth-library";

const app = express();
app.use(cors());
app.use(express.json());

const JWT_SECRET = process.env.JWT_SECRET || "dev-secret";
const PORT = Number(process.env.PORT || 8080);
const EMAIL_VERIFICATION_TTL_MS = 10 * 60 * 1000;
const RESET_TTL_MS = 10 * 60 * 1000;
const COOLDOWN_MS = 60 * 1000;
const GOOGLE_WEB_CLIENT_ID = process.env.GOOGLE_WEB_CLIENT_ID || "";

const googleClient = new OAuth2Client();

const dataDir = path.resolve("backend/data");
const usersFile = path.join(dataDir, "users.json");

if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });
if (!fs.existsSync(usersFile)) fs.writeFileSync(usersFile, JSON.stringify({ users: [] }, null, 2));

function loadUsers() {
  return JSON.parse(fs.readFileSync(usersFile, "utf8")).users;
}

function saveUsers(users) {
  fs.writeFileSync(usersFile, JSON.stringify({ users }, null, 2));
}

const verificationStore = new Map();
const resetStore = new Map();

function normalizeEmail(email) {
  return (email || "").trim().toLowerCase();
}

function generateCode() {
  return Math.floor(100000 + Math.random() * 900000).toString();
}

const transporter = nodemailer.createTransport({
  service: "gmail",
  auth: {
    user: process.env.GMAIL_USER || "existapp1@gmail.com",
    pass: process.env.GMAIL_APP_PASSWORD || ""
  }
});

async function sendEmail(to, subject, text) {
  if (!process.env.GMAIL_APP_PASSWORD) {
    throw new Error("GMAIL_APP_PASSWORD is not set");
  }
  await transporter.sendMail({
    from: `Exist <${process.env.GMAIL_USER || "existapp1@gmail.com"}>`,
    to,
    subject,
    text
  });
}

function toAuthResponse(user) {
  const accessToken = jwt.sign({ sub: user.id, email: user.email }, JWT_SECRET, { expiresIn: "30d" });
  return {
    accessToken,
    userId: user.id,
    email: user.email,
    displayName: user.fullName || user.email.split("@")[0],
    needsOnboarding: !(user.fullName && user.birthday)
  };
}

function authMiddleware(req, res, next) {
  const auth = req.headers.authorization || "";
  const token = auth.startsWith("Bearer ") ? auth.slice(7) : "";
  if (!token) return res.status(401).json({ error: "Unauthorized" });

  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.userId = decoded.sub;
    next();
  } catch {
    return res.status(401).json({ error: "Invalid token" });
  }
}

app.post("/auth/signup", async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const password = (req.body?.password || "").toString();

  if (!email.includes("@") || password.length < 6) {
    return res.status(400).json({ error: "Invalid email or password" });
  }

  const users = loadUsers();
  if (users.find((u) => u.email === email)) {
    return res.status(409).json({ error: "Account already exists" });
  }

  const passwordHash = await bcrypt.hash(password, 10);
  const user = {
    id: `u_${Date.now()}`,
    email,
    passwordHash,
    verified: false,
    fullName: "",
    birthday: "",
    profilePhotoUri: "",
    provider: "email"
  };
  users.push(user);
  saveUsers(users);

  const code = generateCode();
  verificationStore.set(email, {
    code,
    expiresAt: Date.now() + EMAIL_VERIFICATION_TTL_MS,
    lastSentAt: Date.now()
  });

  try {
    await sendEmail(email, "Verify your Exist account", `Your verification code is ${code}. It expires in 10 minutes.`);
    return res.status(204).send();
  } catch {
    return res.status(500).json({ error: "Failed to send verification email" });
  }
});

app.post("/auth/resend-verification", async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const users = loadUsers();
  const user = users.find((u) => u.email === email);
  if (!user) return res.status(404).json({ error: "User not found" });
  if (user.verified) return res.status(400).json({ error: "Already verified" });

  const existing = verificationStore.get(email);
  if (existing && Date.now() - existing.lastSentAt < COOLDOWN_MS) {
    return res.status(429).json({ error: "Cooldown active" });
  }

  const code = generateCode();
  verificationStore.set(email, {
    code,
    expiresAt: Date.now() + EMAIL_VERIFICATION_TTL_MS,
    lastSentAt: Date.now()
  });

  try {
    await sendEmail(email, "Verify your Exist account", `Your verification code is ${code}.`);
    return res.status(204).send();
  } catch {
    return res.status(500).json({ error: "Failed to send verification email" });
  }
});

app.post("/auth/verify-email", (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const code = (req.body?.code || "").toString().trim();

  const record = verificationStore.get(email);
  if (!record) return res.status(400).json({ error: "No verification pending" });
  if (Date.now() > record.expiresAt) {
    verificationStore.delete(email);
    return res.status(400).json({ error: "Code expired" });
  }
  if (record.code !== code) return res.status(400).json({ error: "Invalid code" });

  const users = loadUsers();
  const user = users.find((u) => u.email === email);
  if (!user) return res.status(404).json({ error: "User not found" });

  user.verified = true;
  saveUsers(users);
  verificationStore.delete(email);

  return res.json(toAuthResponse(user));
});

app.post("/auth/login", async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const password = (req.body?.password || "").toString();

  const users = loadUsers();
  const user = users.find((u) => u.email === email && u.provider === "email");
  if (!user) return res.status(401).json({ error: "Invalid credentials" });
  if (!user.verified) return res.status(403).json({ error: "Email not verified" });

  const ok = await bcrypt.compare(password, user.passwordHash || "");
  if (!ok) return res.status(401).json({ error: "Invalid credentials" });

  return res.json(toAuthResponse(user));
});

app.post("/auth/google", async (req, res) => {
  const idToken = (req.body?.idToken || "").toString();
  if (!idToken || !GOOGLE_WEB_CLIENT_ID) {
    return res.status(400).json({ error: "Google sign-in not configured" });
  }

  try {
    const ticket = await googleClient.verifyIdToken({ idToken, audience: GOOGLE_WEB_CLIENT_ID });
    const payload = ticket.getPayload();
    const email = normalizeEmail(payload?.email || "");
    if (!email) return res.status(400).json({ error: "Invalid Google account" });

    const users = loadUsers();
    let user = users.find((u) => u.email === email);
    if (!user) {
      user = {
        id: `u_${Date.now()}`,
        email,
        passwordHash: "",
        verified: true,
        fullName: payload?.name || "",
        birthday: "",
        profilePhotoUri: payload?.picture || "",
        provider: "google"
      };
      users.push(user);
      saveUsers(users);
    }

    return res.json(toAuthResponse(user));
  } catch {
    return res.status(401).json({ error: "Google token verification failed" });
  }
});

app.post("/auth/forgot-password", async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const users = loadUsers();
  const user = users.find((u) => u.email === email && u.provider === "email");
  if (!user) return res.status(404).json({ error: "User not found" });

  const existing = resetStore.get(email);
  if (existing && Date.now() - existing.lastSentAt < COOLDOWN_MS) {
    return res.status(429).json({ error: "Cooldown active" });
  }

  const code = generateCode();
  resetStore.set(email, {
    code,
    expiresAt: Date.now() + RESET_TTL_MS,
    lastSentAt: Date.now()
  });

  try {
    await sendEmail(email, "Reset your Exist password", `Your reset code is ${code}.`);
    return res.status(204).send();
  } catch {
    return res.status(500).json({ error: "Failed to send reset email" });
  }
});

app.post("/auth/reset-password", async (req, res) => {
  const email = normalizeEmail(req.body?.email);
  const code = (req.body?.code || "").toString().trim();
  const newPassword = (req.body?.newPassword || "").toString();

  if (newPassword.length < 6) return res.status(400).json({ error: "Weak password" });

  const record = resetStore.get(email);
  if (!record) return res.status(400).json({ error: "No reset requested" });
  if (Date.now() > record.expiresAt) {
    resetStore.delete(email);
    return res.status(400).json({ error: "Reset code expired" });
  }
  if (record.code !== code) return res.status(400).json({ error: "Invalid reset code" });

  const users = loadUsers();
  const user = users.find((u) => u.email === email && u.provider === "email");
  if (!user) return res.status(404).json({ error: "User not found" });

  user.passwordHash = await bcrypt.hash(newPassword, 10);
  saveUsers(users);
  resetStore.delete(email);

  return res.status(204).send();
});

app.post("/auth/onboarding", authMiddleware, (req, res) => {
  const fullName = (req.body?.fullName || "").toString().trim();
  const birthday = (req.body?.birthday || "").toString().trim();
  const profilePhotoUri = (req.body?.profilePhotoUri || "").toString().trim();

  if (!fullName || !birthday) return res.status(400).json({ error: "Missing profile fields" });

  const users = loadUsers();
  const user = users.find((u) => u.id === req.userId);
  if (!user) return res.status(404).json({ error: "User not found" });

  user.fullName = fullName;
  user.birthday = birthday;
  user.profilePhotoUri = profilePhotoUri;
  saveUsers(users);

  return res.json(toAuthResponse(user));
});

app.post("/auth/signout", (_req, res) => {
  return res.status(204).send();
});

app.get("/health", (_req, res) => {
  res.status(200).json({ ok: true });
});

app.listen(PORT, "0.0.0.0", () => {
  console.log(`Exist auth backend running on 0.0.0.0:${PORT}`);
});
