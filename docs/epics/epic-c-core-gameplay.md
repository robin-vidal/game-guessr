# EPIC C — Core Gameplay Loop

## C1 — 3D Exploration Phase

### User Story C1.1 — Explore Spawned Environment
> **As a Player**  
> I want to fly freely in the 3D level  
> So that I can explore clues.

**Acceptance Criteria:**
- Noclip iframe loads
- Controlled spawn coordinates
- All players share same spawn
- **[TODO]:** Player movement is restricted to a specific range??

---

## C2 — Phase 1: Guess Game

### User Story C2.1 — Submit Game Guess
> **As a Player**  
> I want autocomplete game input  
> So that I can quickly submit my answer.

**Acceptance Criteria:**
- Autocomplete
- Validated against pack
- First correct guess unlocks Phase 2
- `GAME_GUESSED` event sent to Kafka

*Scoring:* Binary correct/incorrect

---

## C3 — Phase 2: Guess Level

### User Story C3.1 — Level Guess
> **As a Player**  
> I want to guess the level  
> So that I can refine my answer.

**Acceptance Criteria:**
- Locked until Phase 1 correct
- Time-based bonus
- `LEVEL_GUESSED` event sent to Kafka

---

## C4 — Phase 3: Pinpoint Location (Post-MVP)

### User Story C4.1 — Drop Pin on Map
> **As a Player**  
> I want to place a marker  
> So that I can guess the exact coordinates.

**Acceptance Criteria:**
- Top-down 2D map
- Click to drop marker
- Coordinates sent to backend
- Backend calculates Euclidean distance
- `SPOT_GUESSED` event sent to Kafka

*Scoring:* 0–5000 based on distance

---

## C5 — Round & Match Results

### User Story C5.1 — Round Summary
> **As a Player**  
> I want to see my accuracy  
> So that I understand my score.

**Displays:**
- True location
- Player guess
- Distance
- Points earned

### User Story C5.2 — Match Leaderboard
> **As a Player**  
> I want final rankings  
> So that I know who won.

**Displays:**
- Sorted ranking
- Highlight winner
