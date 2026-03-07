# EPIC F — Social & Ecosystem

## F1 — Friends List & Status

### User Story F1.1 — View Friends List
> **As a user**, I want to see my friends so I can interact and invite them.

**Acceptance Criteria:**
- Display username, avatar, online/offline status, and current activity (in-game / idle).
- List updates in real-time.
- Empty state shown if no friends.
- **Edge cases:** Friend deleted account, blocked, real-time sync fails.

---

## F2 — Add / Delete Friends

### User Story F2.1 — Add Friend
> **As a user**, I want to add friends to manage my social circle.

**Acceptance Criteria:**
- Search by username.
- Send friend request (notification when received).
- Accept / Decline request.
- Cannot add self or add twice.
- **Edge cases:** Crossed requests, blocked users, already friends.

### User Story F2.2 — Delete Friend
> **As a user**, I want to remove a friend.

**Acceptance Criteria:**
- Remove friend instantly.
- Both users no longer see each other in their friend lists.

---

## F3 — Friends Leaderboard

### User Story F3.1 — View Friends Leaderboard
> **As a user**, I want to see how I rank among my friends.

**Acceptance Criteria:**
- Ranking sorted by total score, wins, or ELO (if applicable).
- Highlight current user.
- Real-time or periodic update.
- Tie-break logic defined.
- **Edge cases:** Friends with no games played, equal scores, large friend list (pagination).

---

## F4 — Solo Mode

### User Story F4.1 — Play Solo
> **As a user**, I want to play alone so I can practice.

**Acceptance Criteria:**
- No room required.
- Select difficulty level.
- AI opponent or timed solo mode.
- Score saved to profile (appears in leaderboard if applicable).
- **Edge cases:** No internet connection, abuse prevention (botting), AI difficulty scaling.
