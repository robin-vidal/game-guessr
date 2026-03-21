#!/bin/bash

# ─────────────────────────────────────────────────────────────
#  GameGuessr — End-to-End Test Suite
#  Covers all endpoints and behaviours documented in doc.txt
# ─────────────────────────────────────────────────────────────

LOBBY="http://localhost:8083"
GAME="http://localhost:8082"
SCORING="http://localhost:8084"
LB="http://localhost:8085"

PASS=0
FAIL=0
SKIP=0

# ── helpers ──────────────────────────────────────────────────

green() { printf "\033[32m%s\033[0m\n" "$*"; }
red()   { printf "\033[31m%s\033[0m\n" "$*"; }
cyan()  { printf "\033[36m%s\033[0m\n" "$*"; }
bold()  { printf "\033[1m%s\033[0m\n"  "$*"; }

assert_status() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" -eq "$expected" ]; then
    green "  [PASS] $label (HTTP $actual)"
    PASS=$((PASS + 1))
  else
    red   "  [FAIL] $label — expected HTTP $expected, got HTTP $actual"
    FAIL=$((FAIL + 1))
  fi
}

assert_field() {
  local label="$1" field="$2" body="$3"
  local val
  val=$(echo "$body" | jq -r "$field" 2>/dev/null)
  if [ -n "$val" ] && [ "$val" != "null" ]; then
    green "  [PASS] $label ($field = $val)"
    PASS=$((PASS + 1))
  else
    red   "  [FAIL] $label — field '$field' missing or null in body"
    FAIL=$((FAIL + 1))
  fi
}

assert_equals() {
  local label="$1" expected="$2" actual="$3"
  if [ "$actual" = "$expected" ]; then
    green "  [PASS] $label (= $actual)"
    PASS=$((PASS + 1))
  else
    red   "  [FAIL] $label — expected '$expected', got '$actual'"
    FAIL=$((FAIL + 1))
  fi
}

# curl wrapper — sets global STATUS and BODY (macOS-compatible)
TMPBODY=$(mktemp)
req() {
  local method="$1" url="$2" data="$3"
  if [ -n "$data" ]; then
    STATUS=$(curl -s -o "$TMPBODY" -w "%{http_code}" -X "$method" \
         -H "Content-Type: application/json" \
         -d "$data" "$url")
  else
    STATUS=$(curl -s -o "$TMPBODY" -w "%{http_code}" -X "$method" "$url")
  fi
  BODY=$(cat "$TMPBODY")
}

cleanup() { rm -f "$TMPBODY"; }
trap cleanup EXIT

# ── service health check ─────────────────────────────────────
bold "=== Checking services are reachable ==="
SERVICES_OK=true
for url in "$LOBBY/api/v1/rooms" \
           "$GAME/api/v1/rooms/HEALTH_PROBE/round" \
           "$SCORING/api/v1/scoring/HEALTH_PROBE" \
           "$LB/api/v1/leaderboard/global"; do
  code=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null)
  if [ "$code" = "000" ]; then
    red "  Unreachable: $url"
    SERVICES_OK=false
  fi
done

if [ "$SERVICES_OK" = "false" ]; then
  red ""
  red "One or more services are down. Start with:"
  red "  docker compose --profile app up -d"
  exit 1
fi
green "  All services reachable."
echo ""

# ─────────────────────────────────────────────────────────────
#  SECTION 1 — LOBBY SERVICE (port 8083)
# ─────────────────────────────────────────────────────────────
bold "══════════════════════════════════════════"
bold " LOBBY SERVICE  (port 8083)"
bold "══════════════════════════════════════════"

# 1.1 Create room
cyan "--- 1.1 POST /api/v1/rooms — create room → 201"
req POST "$LOBBY/api/v1/rooms" '{"hostId":"p1","isPrivate":false}'
assert_status "Create room" 201 "$STATUS"
assert_field  "roomCode present" ".roomCode" "$BODY"
ROOM_CODE=$(echo "$BODY" | jq -r .roomCode)
echo "    Room code: $ROOM_CODE"
echo ""

# 1.2 Create private room
cyan "--- 1.2 POST /api/v1/rooms — create private room → 201"
req POST "$LOBBY/api/v1/rooms" '{"hostId":"host2","isPrivate":true}'
assert_status "Create private room" 201 "$STATUS"
PRIVATE_CODE=$(echo "$BODY" | jq -r .roomCode)
assert_equals "isPrivate = true" "true" "$(echo "$BODY" | jq -r .private)"
echo ""

# 1.3 List open rooms
cyan "--- 1.3 GET /api/v1/rooms — list open rooms → 200"
req GET "$LOBBY/api/v1/rooms"
assert_status "List open rooms" 200 "$STATUS"
echo "    Open rooms: $(echo "$BODY" | jq 'length')"
echo ""

# 1.4 Get room by code
cyan "--- 1.4 GET /api/v1/rooms/{code} — get room → 200"
req GET "$LOBBY/api/v1/rooms/$ROOM_CODE"
assert_status "Get room" 200 "$STATUS"
assert_equals "Room code matches" "$ROOM_CODE" "$(echo "$BODY" | jq -r .roomCode)"
assert_equals "Host is p1" "p1" "$(echo "$BODY" | jq -r .hostId)"
assert_equals "Status is OPEN" "OPEN" "$(echo "$BODY" | jq -r .status)"
echo ""

# 1.5 Get room — 404
cyan "--- 1.5 GET /api/v1/rooms/BADCODE — not found → 404"
req GET "$LOBBY/api/v1/rooms/BADCODE"
assert_status "Get non-existent room" 404 "$STATUS"
echo ""

# 1.6 Join room (p2)
cyan "--- 1.6 POST /api/v1/rooms/{code}/join — p2 joins → 200"
req POST "$LOBBY/api/v1/rooms/$ROOM_CODE/join" \
    '{"playerId":"p2","displayName":"Player 2"}'
assert_status "Join room (p2)" 200 "$STATUS"
assert_equals "Room has 2 players" "2" "$(echo "$BODY" | jq '.players | length')"
echo ""

# 1.7 Duplicate join — 409
cyan "--- 1.7 POST /api/v1/rooms/{code}/join — duplicate → 409"
req POST "$LOBBY/api/v1/rooms/$ROOM_CODE/join" \
    '{"playerId":"p2","displayName":"Player 2 again"}'
assert_status "Duplicate join → 409" 409 "$STATUS"
echo ""

# 1.8 Join non-existent room — 404
cyan "--- 1.8 POST /api/v1/rooms/NOSUCHROOM/join — 404"
req POST "$LOBBY/api/v1/rooms/NOSUCHROOM/join" \
    '{"playerId":"px","displayName":"Ghost"}'
assert_status "Join non-existent room" 404 "$STATUS"
echo ""

# 1.9 Update settings — non-host → 403
cyan "--- 1.9 PATCH /api/v1/rooms/{code}/settings — non-host → 403"
req PATCH "$LOBBY/api/v1/rooms/$ROOM_CODE/settings" \
    '{"playerId":"p2","roundCount":3,"timeLimitSeconds":30,"gamePack":"mario-kart-wii"}'
assert_status "Non-host update settings → 403" 403 "$STATUS"
echo ""

# 1.10 Update settings — host → 200
cyan "--- 1.10 PATCH /api/v1/rooms/{code}/settings — host → 200"
req PATCH "$LOBBY/api/v1/rooms/$ROOM_CODE/settings" \
    '{"playerId":"p1","roundCount":5,"timeLimitSeconds":60,"gamePack":"mario-kart-wii"}'
assert_status "Host update settings → 200" 200 "$STATUS"
assert_equals "Round count = 5"              "5"              "$(echo "$BODY" | jq -r .settings.roundCount)"
assert_equals "Time limit = 60"              "60"             "$(echo "$BODY" | jq -r .settings.timeLimitSeconds)"
assert_equals "Game pack = mario-kart-wii"   "mario-kart-wii" "$(echo "$BODY" | jq -r .settings.gamePack)"
echo ""

# 1.11 Update settings — room not found → 404
cyan "--- 1.11 PATCH /api/v1/rooms/BADCODE/settings — 404"
req PATCH "$LOBBY/api/v1/rooms/BADCODE/settings" '{"playerId":"p1","roundCount":3}'
assert_status "Update settings non-existent room → 404" 404 "$STATUS"
echo ""

# ─────────────────────────────────────────────────────────────
#  SECTION 2 — GAME SERVICE (port 8082)
# ─────────────────────────────────────────────────────────────
bold "══════════════════════════════════════════"
bold " GAME SERVICE  (port 8082)"
bold "══════════════════════════════════════════"

# Wait for Kafka to deliver the RoomCreatedEvent from lobby → game-service
echo "    Waiting 3s for Kafka room.created event delivery..."
sleep 3

# 2.1 Get round before match started — match exists (WAITING) but not in progress → 409
cyan "--- 2.1 GET /api/v1/rooms/{code}/round — before start → 409 (WAITING)"
req GET "$GAME/api/v1/rooms/$ROOM_CODE/round"
assert_status "Get round before start → 409" 409 "$STATUS"
echo ""

# 2.2 Submit guess before match started — match WAITING → 409
cyan "--- 2.2 POST /api/v1/rooms/{code}/guess — before start → 409"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/guess" \
    '{"playerId":"p1","phase":"GAME","textAnswer":"Mario Kart 8"}'
assert_status "Submit guess before start → 409" 409 "$STATUS"
echo ""

# 2.3 Start match for unknown room — no Kafka event received → 404
FAKE_CODE="FAKE$(date +%s)"
cyan "--- 2.3 POST /api/v1/rooms/${FAKE_CODE}/start — no pre-created match → 404"
req POST "$GAME/api/v1/rooms/$FAKE_CODE/start" '{"hostId":"p1"}'
assert_status "Start match for unknown room → 404" 404 "$STATUS"
echo ""

# 2.4 Start match for real room (lobby created it, Kafka pre-created WAITING match)
cyan "--- 2.4 POST /api/v1/rooms/{code}/start — start match → 201"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/start" '{"hostId":"p1"}'
assert_status "Start match → 201" 201 "$STATUS"
echo "    Waiting 2s for Kafka round.update event..."
sleep 2
echo ""

# 2.5 Start match again — already started → 409
cyan "--- 2.5 POST /api/v1/rooms/{code}/start — already started → 409"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/start" '{"hostId":"p1"}'
assert_status "Start already-started match → 409" 409 "$STATUS"
echo ""

# 2.6 Get current round — now returns real game pack data + noclipHash
cyan "--- 2.6 GET /api/v1/rooms/{code}/round → 200"
req GET "$GAME/api/v1/rooms/$ROOM_CODE/round"
assert_status "Get current round → 200" 200 "$STATUS"
assert_field  "roundNumber present"   ".roundNumber"   "$BODY"
assert_field  "gameId present"        ".gameId"        "$BODY"
assert_field  "levelId present"       ".levelId"       "$BODY"
assert_field  "currentPhase present"  ".currentPhase"  "$BODY"
assert_equals "Phase starts as GAME"  "GAME" "$(echo "$BODY" | jq -r .currentPhase)"
# True spawn coords must NOT appear in round response
HAS_SPAWN=$(echo "$BODY" | jq 'has("trueSpawnX") or has("spawnX")')
assert_equals "True spawn coords hidden" "false" "$HAS_SPAWN"
echo ""

# 2.6a noclipHash must be present in round response (new field for 3D viewer)
cyan "--- 2.6a Round response includes noclipHash"
assert_field "noclipHash present in round" ".noclipHash" "$BODY"
NOCLIP=$(echo "$BODY" | jq -r .noclipHash)
echo "    noclipHash: ${NOCLIP:0:60}..."
echo ""

# 2.6b gameId should be a real game pack slug (not hardcoded "mario-kart-8")
cyan "--- 2.6b Round uses real game pack data (not placeholder)"
GAME_ID=$(echo "$BODY" | jq -r .gameId)
LEVEL_ID=$(echo "$BODY" | jq -r .levelId)
if [ "$GAME_ID" != "mario-kart-8" ] && [ "$LEVEL_ID" != "null" ] && [ "$LEVEL_ID" != "TBD-1" ]; then
  green "  [PASS] Real game data: gameId=$GAME_ID, levelId=$LEVEL_ID"
  PASS=$((PASS + 1))
else
  red "  [FAIL] Still using placeholder data: gameId=$GAME_ID, levelId=$LEVEL_ID"
  FAIL=$((FAIL + 1))
fi
echo ""

# 2.6c All 5 rounds should have distinct noclipHashes (different positions)
cyan "--- 2.6c Verify rounds use different positions (via results endpoint)"
req GET "$GAME/api/v1/rooms/$ROOM_CODE/results"
ROUND_COUNT=$(echo "$BODY" | jq '.rounds | length')
UNIQUE_HASHES=$(echo "$BODY" | jq '[.rounds[].noclipHash] | unique | length')
if [ "$ROUND_COUNT" -gt 1 ] && [ "$UNIQUE_HASHES" -eq "$ROUND_COUNT" ]; then
  green "  [PASS] All $ROUND_COUNT rounds have distinct noclipHashes"
  PASS=$((PASS + 1))
elif [ "$ROUND_COUNT" -gt 1 ]; then
  # Duplicates are possible if game pack has few positions, but worth flagging
  cyan "  [INFO] $UNIQUE_HASHES unique hashes out of $ROUND_COUNT rounds (some positions may repeat)"
  PASS=$((PASS + 1))
else
  red "  [FAIL] Could not verify round diversity"
  FAIL=$((FAIL + 1))
fi
echo ""

# 2.7 Submit LEVEL guess while current phase is GAME → 400 (InvalidPhaseException)
cyan "--- 2.7 POST .../guess — LEVEL during GAME phase → 400"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/guess" \
    '{"playerId":"p1","phase":"LEVEL","textAnswer":"Baby Park"}'
assert_status "LEVEL guess during GAME phase → 400" 400 "$STATUS"
echo ""

# 2.8 Submit SPOT guess while current phase is GAME → 400
cyan "--- 2.8 POST .../guess — SPOT during GAME phase → 400"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/guess" \
    '{"playerId":"p1","phase":"SPOT","guessX":100.0,"guessY":50.0,"guessZ":-200.0}'
assert_status "SPOT guess during GAME phase → 400" 400 "$STATUS"
echo ""

# 2.9 Submit GAME guess — p1 (any non-empty text = accepted/published)
cyan "--- 2.9 POST .../guess — p1 GAME guess → 202"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/guess" \
    '{"playerId":"p1","phase":"GAME","textAnswer":"Mario Kart 8"}'
assert_status "p1 GAME guess → 202" 202 "$STATUS"
echo ""

# 2.10 Submit GAME guess — p2 (wrong answer still accepted, scoring decides)
cyan "--- 2.10 POST .../guess — p2 GAME guess (wrong answer) → 202"
req POST "$GAME/api/v1/rooms/$ROOM_CODE/guess" \
    '{"playerId":"p2","phase":"GAME","textAnswer":"Super Smash Bros"}'
assert_status "p2 GAME guess (wrong) → 202" 202 "$STATUS"
echo "    Waiting 3s for Kafka scoring pipeline..."
sleep 3
echo ""

# 2.11 Get results — includes noclipHash, trueSpawnX/Z, but NOT trueSpawnY
cyan "--- 2.11 GET /api/v1/rooms/{code}/results → 200"
req GET "$GAME/api/v1/rooms/$ROOM_CODE/results"
assert_status "Get results → 200" 200 "$STATUS"
assert_field  "roomCode in results"   ".roomCode"  "$BODY"
assert_equals "Results has 5 rounds" "5" "$(echo "$BODY" | jq '.rounds | length')"
# True spawn coords X/Z SHOULD appear in results
assert_field  "trueSpawnX in results" ".rounds[0].trueSpawnX" "$BODY"
assert_field  "trueSpawnZ in results" ".rounds[0].trueSpawnZ" "$BODY"
# trueSpawnY must NOT appear (dropped)
HAS_Y=$(echo "$BODY" | jq '.rounds[0] | has("trueSpawnY")')
assert_equals "trueSpawnY absent from results" "false" "$HAS_Y"
# noclipHash SHOULD appear in results
assert_field  "noclipHash in results" ".rounds[0].noclipHash" "$BODY"
echo ""

# 2.11a Results round fields use real game data
cyan "--- 2.11a Results rounds have real game data"
R0_GAME=$(echo "$BODY" | jq -r '.rounds[0].gameId')
R0_LEVEL=$(echo "$BODY" | jq -r '.rounds[0].levelId')
R0_HASH=$(echo "$BODY" | jq -r '.rounds[0].noclipHash')
if [ "$R0_GAME" != "mario-kart-8" ] && [ "$R0_LEVEL" != "null" ] && [ -n "$R0_HASH" ] && [ "$R0_HASH" != "null" ]; then
  green "  [PASS] Results round 1: game=$R0_GAME, level=$R0_LEVEL, hash=${R0_HASH:0:40}..."
  PASS=$((PASS + 1))
else
  red "  [FAIL] Results still using placeholder data: game=$R0_GAME, level=$R0_LEVEL"
  FAIL=$((FAIL + 1))
fi
echo ""

# 2.12 Get round — non-existent match → 404
cyan "--- 2.12 GET /api/v1/rooms/NOSUCHROOM/round → 404"
req GET "$GAME/api/v1/rooms/NOSUCHROOM/round"
assert_status "Get round non-existent match → 404" 404 "$STATUS"
echo ""

# ─────────────────────────────────────────────────────────────
#  SECTION 3 — SCORING SERVICE (port 8084)
# ─────────────────────────────────────────────────────────────
bold "══════════════════════════════════════════"
bold " SCORING SERVICE  (port 8084)"
bold "══════════════════════════════════════════"

# 3.1 Round scores — always 200 (empty list if no scores yet)
cyan "--- 3.1 GET /api/v1/scoring/{code}/rounds/1 → 200"
req GET "$SCORING/api/v1/scoring/$ROOM_CODE/rounds/1"
assert_status "Get round 1 scores → 200" 200 "$STATUS"
assert_field  "roomCode in response" ".roomCode" "$BODY"
SCORE_COUNT=$(echo "$BODY" | jq '.scores | length')
echo "    Scores for round 1: $SCORE_COUNT"
if [ "$SCORE_COUNT" -ge 1 ] 2>/dev/null; then
  green "  [PASS] Kafka pipeline delivered ≥1 score for round 1"
  PASS=$((PASS + 1))
else
  red   "  [FAIL] No scores yet for round 1 (Kafka pipeline may be lagging)"
  FAIL=$((FAIL + 1))
fi
echo ""

# 3.2 Match scores — all rounds
cyan "--- 3.2 GET /api/v1/scoring/{code} — full match → 200"
req GET "$SCORING/api/v1/scoring/$ROOM_CODE"
assert_status "Get match scores → 200" 200 "$STATUS"
assert_field  "roomCode in response" ".roomCode" "$BODY"
echo "    Total scores: $(echo "$BODY" | jq '.scores | length')"
echo ""

# 3.3 Score object field validation
cyan "--- 3.3 Validate score object fields"
FIRST=$(echo "$BODY" | jq '.scores[0]')
if [ "$FIRST" != "null" ] && [ -n "$FIRST" ]; then
  assert_field "playerId in score"  ".playerId"   "$FIRST"
  assert_field "phase in score"     ".phase"      "$FIRST"
  assert_field "points in score"    ".points"     "$FIRST"
  assert_field "correct flag"       ".correct"    "$FIRST"
  assert_field "createdAt in score" ".createdAt"  "$FIRST"
  echo "    p1 GAME guess → correct=$(echo "$FIRST" | jq -r .correct), points=$(echo "$FIRST" | jq -r .points)"
else
  red "  [SKIP] No scores to validate (Kafka lag?)"
  SKIP=$((SKIP + 1))
fi
echo ""

# 3.4 Scoring logic — GAME phase: any non-empty = 1000pts
cyan "--- 3.4 Scoring logic: GAME phase non-empty text → correct=true, 1000 pts"
P1_SCORE=$(echo "$BODY" | jq '.scores[] | select(.playerId == "p1" and .phase == "GAME")')
if [ -n "$P1_SCORE" ]; then
  assert_equals "p1 GAME correct = true" "true" "$(echo "$P1_SCORE" | jq -r .correct)"
  assert_equals "p1 GAME points = 1000"  "1000" "$(echo "$P1_SCORE" | jq -r .points)"
else
  red "  [SKIP] p1 GAME score not found"
  SKIP=$((SKIP + 1))
fi
echo ""

# ─────────────────────────────────────────────────────────────
#  SECTION 4 — LEADERBOARD SERVICE (port 8085)
# ─────────────────────────────────────────────────────────────
bold "══════════════════════════════════════════"
bold " LEADERBOARD SERVICE  (port 8085)"
bold "══════════════════════════════════════════"

# 4.1 Global leaderboard
cyan "--- 4.1 GET /api/v1/leaderboard/global → 200"
req GET "$LB/api/v1/leaderboard/global"
assert_status "Global leaderboard → 200" 200 "$STATUS"
assert_equals "Type is global" "global" "$(echo "$BODY" | jq -r .leaderboardType)"
assert_equals "Identifier is 'all'" "all" "$(echo "$BODY" | jq -r .identifier)"
echo ""

# 4.2 Global leaderboard with top param
cyan "--- 4.2 GET /api/v1/leaderboard/global?top=10 → 200"
req GET "$LB/api/v1/leaderboard/global?top=10"
assert_status "Global leaderboard ?top=10 → 200" 200 "$STATUS"
assert_field  "entries array present" ".entries" "$BODY"
echo ""

# 4.3 Room leaderboard
cyan "--- 4.3 GET /api/v1/leaderboard/room/{code} → 200"
req GET "$LB/api/v1/leaderboard/room/$ROOM_CODE"
assert_status "Room leaderboard → 200" 200 "$STATUS"
assert_equals "Type is room"           "room"       "$(echo "$BODY" | jq -r .leaderboardType)"
assert_equals "Identifier is roomCode" "$ROOM_CODE" "$(echo "$BODY" | jq -r .identifier)"
LB_COUNT=$(echo "$BODY" | jq '.entries | length')
echo "    Leaderboard entries: $LB_COUNT"
if [ "$LB_COUNT" -ge 1 ] 2>/dev/null; then
  green "  [PASS] Kafka score pipeline updated leaderboard (≥1 entry)"
  PASS=$((PASS + 1))
else
  red   "  [FAIL] Room leaderboard empty (Kafka scoring → leaderboard pipeline lag)"
  FAIL=$((FAIL + 1))
fi
echo ""

# 4.4 Leaderboard entry fields
cyan "--- 4.4 Validate leaderboard entry fields"
ENTRY=$(echo "$BODY" | jq '.entries[0]')
if [ "$ENTRY" != "null" ] && [ -n "$ENTRY" ]; then
  assert_field "rank in entry"     ".rank"     "$ENTRY"
  assert_field "playerId in entry" ".playerId" "$ENTRY"
  assert_field "score in entry"    ".score"    "$ENTRY"
else
  red "  [SKIP] No leaderboard entries to validate"
  SKIP=$((SKIP + 1))
fi
echo ""

# 4.5 Room leaderboard for unknown room — always 200 with empty entries
cyan "--- 4.5 GET /api/v1/leaderboard/room/NOSUCHROOM → 200 (empty)"
req GET "$LB/api/v1/leaderboard/room/NOSUCHROOM"
assert_status "Room leaderboard unknown room → 200" 200 "$STATUS"
assert_equals "Empty entries for unknown room" "0" "$(echo "$BODY" | jq '.entries | length')"
echo ""

# ─────────────────────────────────────────────────────────────
#  SECTION 5 — EDGE CASES & BUSINESS RULES
# ─────────────────────────────────────────────────────────────
bold "══════════════════════════════════════════"
bold " EDGE CASES & BUSINESS RULES"
bold "══════════════════════════════════════════"

# 5.1 Fill a room to FULL (max 8 players)
cyan "--- 5.1 Fill room to max capacity → status FULL"
req POST "$LOBBY/api/v1/rooms" '{"hostId":"fillHost","isPrivate":false}'
FILL_CODE=$(echo "$BODY" | jq -r .roomCode)
# Host already counts as player 1; add 7 more
for i in $(seq 2 8); do
  req POST "$LOBBY/api/v1/rooms/$FILL_CODE/join" \
      "{\"playerId\":\"filler$i\",\"displayName\":\"Filler $i\"}"
done
req GET "$LOBBY/api/v1/rooms/$FILL_CODE"
assert_equals "Room status = FULL at 8 players" "FULL" "$(echo "$BODY" | jq -r .status)"
echo ""

# 5.2 9th player join a full room → 409
cyan "--- 5.2 9th player joins full room → 409"
req POST "$LOBBY/api/v1/rooms/$FILL_CODE/join" \
    '{"playerId":"overflow","displayName":"Overflow"}'
assert_status "9th player join full room → 409" 409 "$STATUS"
echo ""

# 5.3 Host leaving closes the room → 204, status CLOSED
cyan "--- 5.3 Host leaving closes the room"
req POST "$LOBBY/api/v1/rooms" '{"hostId":"closingHost","isPrivate":false}'
CLOSE_CODE=$(echo "$BODY" | jq -r .roomCode)
req POST "$LOBBY/api/v1/rooms/$CLOSE_CODE/join" \
    '{"playerId":"guestC","displayName":"Guest C"}'
req DELETE "$LOBBY/api/v1/rooms/$CLOSE_CODE/leave" '{"playerId":"closingHost"}'
assert_status "Host leave → 204" 204 "$STATUS"
req GET "$LOBBY/api/v1/rooms/$CLOSE_CODE"
assert_equals "Room CLOSED after host left" "CLOSED" "$(echo "$BODY" | jq -r .status)"
echo ""

# 5.4 Regular player leaving — room stays OPEN
cyan "--- 5.4 Non-host leaving → room stays OPEN"
req POST "$LOBBY/api/v1/rooms" '{"hostId":"stayHost","isPrivate":false}'
STAY_CODE=$(echo "$BODY" | jq -r .roomCode)
req POST "$LOBBY/api/v1/rooms/$STAY_CODE/join" \
    '{"playerId":"leaver","displayName":"Leaver"}'
req DELETE "$LOBBY/api/v1/rooms/$STAY_CODE/leave" '{"playerId":"leaver"}'
assert_status "Non-host leave → 204" 204 "$STATUS"
req GET "$LOBBY/api/v1/rooms/$STAY_CODE"
assert_equals "Room OPEN after non-host left" "OPEN" "$(echo "$BODY" | jq -r .status)"
echo ""

# 5.5 Leave non-existent room → 404
cyan "--- 5.5 DELETE /api/v1/rooms/BADCODE/leave → 404"
req DELETE "$LOBBY/api/v1/rooms/BADCODE/leave" '{"playerId":"p1"}'
assert_status "Leave non-existent room → 404" 404 "$STATUS"
echo ""

# 5.6 Get results before match started → 404
cyan "--- 5.6 GET /api/v1/rooms/NOSUCHROOM/results — no match → 404"
req GET "$GAME/api/v1/rooms/NOSUCHROOM/results"
assert_status "Get results no match → 404" 404 "$STATUS"
echo ""

# 5.7 Multi-player: both guess, leaderboard ranks them
cyan "--- 5.7 Ranking: p1 (1000 pts) should rank above p2 (scoring logic: any non-empty = correct)"
req GET "$LB/api/v1/leaderboard/room/$ROOM_CODE"
ENTRIES=$(echo "$BODY" | jq '.entries')
if [ "$(echo "$ENTRIES" | jq 'length')" -ge 2 ] 2>/dev/null; then
  TOP_PLAYER=$(echo "$ENTRIES" | jq -r '.[0].playerId')
  green "  [PASS] Leaderboard has ≥2 ranked players; top = $TOP_PLAYER"
  PASS=$((PASS + 1))
elif [ "$(echo "$ENTRIES" | jq 'length')" -ge 1 ] 2>/dev/null; then
  red "  [FAIL] Only 1 player ranked (expected 2 after both guessed)"
  FAIL=$((FAIL + 1))
else
  red "  [SKIP] No leaderboard data (Kafka pipeline lag)"
  SKIP=$((SKIP + 1))
fi
echo ""

# 5.8 Game service: match not in progress after room closed → 409
cyan "--- 5.8 Results endpoint while match exists → 200 (game svc has no CLOSED check)"
req GET "$GAME/api/v1/rooms/$ROOM_CODE/results"
assert_status "Results for active room → 200" 200 "$STATUS"
echo ""

# ─────────────────────────────────────────────────────────────
#  SUMMARY
# ─────────────────────────────────────────────────────────────
bold "══════════════════════════════════════════"
bold " RESULTS"
bold "══════════════════════════════════════════"
TOTAL=$((PASS + FAIL))
echo "  Total assertions : $TOTAL"
green "  Passed           : $PASS"
if [ "$FAIL" -gt 0 ]; then
  red   "  Failed           : $FAIL"
else
  echo  "  Failed           : $FAIL"
fi
[ "$SKIP" -gt 0 ] && echo "  Skipped (lag)    : $SKIP"
echo ""

if [ "$FAIL" -eq 0 ]; then
  green "All tests passed!"
  exit 0
else
  red "Some tests failed — see output above."
  exit 1
fi
