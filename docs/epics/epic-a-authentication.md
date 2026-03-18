# EPIC A — Authentication & User Management

## A1 — OAuth Login (Authentik+Epita Integration)

### User Story A1.1 — Login via OAuth
> **As a Player**  
> I want to log in using OAuth  
> So that I can securely access multiplayer features.

**Acceptance Criteria:**
- "Login" button redirects to Epita Identity provider
- OAuth Authorization Code Flow
- Backend validates JWT
- User created in database if first login
- Session stored securely
- Logout supported

---

## A2 — Persistent User Profile

### User Story A2.1 — Persist Player Identity
> **As a Player**  
> I want my identity persisted  
> So that I can see my username and results across matches.

**Acceptance Criteria:**
- Unique internal user ID
- Username from OAuth claims
- Display name visible in rooms & leaderboard
- Display picture if Epita provider lets us access it
