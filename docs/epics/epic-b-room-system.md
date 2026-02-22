# EPIC B — Room System (Public & Private)

## B1 — Public Matchmaking

### User Story B1.1 — Join Public Room
> **As a Player**  
> I want to join a public match  
> So that I can play with random users.

**Acceptance Criteria:**
- Click “Play Public”
- Assigned to available room
- New room auto-created if none exists
- Real-time lobby display

---

## B2 — Private Room Creation

### User Story B2.1 — Create Private Room
> **As a Player**  
> I want to create a private room  
> So that I can play only with invited friends.

**Acceptance Criteria:**
- "Create Private Room" button
- Unique room ID generated
- Room not discoverable publicly
- Player becomes Host

---

## B3 — Invite System

### User Story B3.1 — Generate Invite Link
> **As a Host**  
> I want to share an invite link  
> So that friends can join my room.

**Acceptance Criteria:**
- Unique token-based invite URL
- Token tied to room
- Token expires when match starts
- Only authenticated users can join

---

## B4 — Host Capabilities (MVP Minimal)

### User Story B4.1 — Start Match
> **As a Host**  
> I want to start the match manually  
> So that all players are ready.

**Acceptance Criteria:**
- Start button visible only to host
- Match transitions to ROUND 1
- `MATCH_STARTED` event emitted

---

## B5 — Room Settings (Post-MVP)

### User Story B5.1 — Change Settings
> **As a Host**  
> I want to configure the game so it matches my preferences.

**Acceptance Criteria:**
- Only host can modify settings (Time per round, Number of rounds, Game pack).
- Changes visible to all players in real time.
- Settings locked once game starts.
- Backend validates all parameters.
- **Edge cases:** Host changes settings while players join, attempts to change after start, invalid values.

---

## B6 — Continuous Play

### User Story B6.1 — Play Multiple Games
> **As players**, we want to replay in the same room without recreating it.

**Acceptance Criteria:**
- After game ends, show results screen and "Play Again" button.
- Same players remain in room.
- Host can kick players between games.
- **Edge cases:** Players leave between rounds, Host leaves (reassignment logic).
