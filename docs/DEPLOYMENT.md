# Deployment Guide

This app is deployed as three independent pieces:

| Piece | Host | Why |
|---|---|---|
| Frontend (React/Vite) | **Vercel** | Best for static SPAs — fast, free, global CDN |
| Backend (Spring Boot) | **Render** | Runs a long-lived JVM server from the Dockerfile |
| Database (PostgreSQL) | **Neon** | Free, persistent managed Postgres |

```
User → Vercel (React) ──HTTPS──► Render (Spring Boot API) ──► Neon (PostgreSQL)
```

> Tip: To avoid a chicken-and-egg between CORS and the frontend URL, set the backend's
> `CORS_ALLOWED_ORIGINS` to `https://*.vercel.app` so every Vercel deployment is allowed.

---

## 1. Database — Neon

1. Sign up at https://neon.tech (log in with GitHub).
2. Create a project; Neon provisions a PostgreSQL database.
3. Open **Connection Details** and copy the connection string. It looks like:
   ```
   postgresql://USER:PASSWORD@ep-xxxx.region.aws.neon.tech/DBNAME?sslmode=require
   ```
4. Convert it to the values the backend needs (JDBC form):
   - **SPRING_DATASOURCE_URL** = `jdbc:postgresql://ep-xxxx.region.aws.neon.tech/DBNAME?sslmode=require`
   - **SPRING_DATASOURCE_USERNAME** = `USER`
   - **SPRING_DATASOURCE_PASSWORD** = `PASSWORD`

   (Just prefix the host part with `jdbc:` and drop the `USER:PASSWORD@` — keep `?sslmode=require`.)

---

## 2. Backend — Render

1. Push this repo to GitHub (already done).
2. Render → **New** → **Web Service** → connect the repo.
3. Configure:
   - **Runtime:** Docker (Render detects the root `Dockerfile`)
   - **Root Directory:** *(leave empty — the backend Dockerfile is at the repo root)*
   - **Instance Type:** Free
4. Add **Environment Variables**:

   | Key | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | the JDBC URL from step 1 |
   | `SPRING_DATASOURCE_USERNAME` | Neon user |
   | `SPRING_DATASOURCE_PASSWORD` | Neon password |
   | `JWT_SECRET` | a long random string (≥ 32 chars) |
   | `CORS_ALLOWED_ORIGINS` | `https://*.vercel.app` (or your exact Vercel URL) |
   | `GEMINI_API_KEY` | Google Gemini API key — enables natural-language entry |

   *(`PORT` is provided by Render automatically; the app already binds to it.)*

   > **Natural-language entry (AI):** `GEMINI_API_KEY` is what keeps the "⚡ Nhập nhanh"
   > box working. Render persists env vars across deploys/restarts, so set it **once** here
   > and the feature stays on — it only turns off if the variable is missing or empty.
   > Get a free key at https://aistudio.google.com/apikey (no credit card). The default
   > model is `gemini-2.5-flash`; override with `GEMINI_MODEL` if needed (do **not** use
   > `gemini-2.0-flash` — its free tier is unavailable). If you ever rotate the key, update
   > this variable too. Leave it unset and the app still runs — the AI box just hides.
5. Create the service and wait for the build. Note the URL, e.g.
   `https://personal-finance-api-xxxx.onrender.com`.
6. Verify: open `https://<backend>/api/health` → `{"status":"UP"}`. Flyway creates the
   tables on first start.

> Free instances sleep after ~15 min idle; the first request after that takes ~30–50s.

---

## 3. Frontend — Vercel

1. Vercel → **Add New Project** → import the same GitHub repo.
2. Configure:
   - **Root Directory:** `frontend`  ← important (the React app lives here)
   - **Framework Preset:** Vite (auto-detected)
   - Build Command / Output Directory: defaults (`npm run build` / `dist`)
3. Add **Environment Variable**:

   | Key | Value |
   |---|---|
   | `VITE_API_URL` | your Render backend URL, e.g. `https://personal-finance-api-xxxx.onrender.com` (no trailing slash) |

4. Deploy. Your app is live at e.g. `https://personal-finance-app.vercel.app`.

`vercel.json` rewrites all routes to `index.html` so React Router deep links / refresh work.

---

## 4. Seed a demo account (optional)

The database starts empty. Either register a new account in the live app, or seed a demo
account by calling the deployed API (PowerShell example — replace the base URL):

```powershell
$base = "https://personal-finance-api-xxxx.onrender.com"; $ct = "application/json"
$tok = (Invoke-RestMethod "$base/api/auth/register" -Method Post -ContentType $ct -Body (@{email="demo@finance.local";password="demo1234"}|ConvertTo-Json)).token
$h = @{ Authorization = "Bearer $tok" }
$an = Invoke-RestMethod "$base/api/categories" -Method Post -Headers $h -ContentType $ct -Body (@{name="An uong";type="EXPENSE"}|ConvertTo-Json)
Invoke-RestMethod "$base/api/transactions" -Method Post -Headers $h -ContentType $ct -Body (@{amount=65000;categoryId=$an.id;note="Pho bo";occurredAt="2026-06-03"}|ConvertTo-Json)
Invoke-RestMethod "$base/api/budgets" -Method Post -Headers $h -ContentType $ct -Body (@{categoryId=$an.id;amountLimit=150000;month=6;year=2026}|ConvertTo-Json)
```

---

## 5. Verify

Open the Vercel URL, register/log in, and use the app end-to-end. If login fails with a
CORS error, make sure `CORS_ALLOWED_ORIGINS` on Render includes the Vercel domain.

Add to your CV / README: **Live demo:** `https://<your-app>.vercel.app`
