// ─────────────────────────────────────────────
// Domain Enums
// ─────────────────────────────────────────────

export enum GamePhase {
  WAITING = "WAITING",
  EXPLORING = "EXPLORING",
  GUESSING_GAME = "GUESSING_GAME",
  GUESSING_LEVEL = "GUESSING_LEVEL",
  GUESSING_SPOT = "GUESSING_SPOT",
  ROUND_RESULTS = "ROUND_RESULTS",
  MATCH_FINISHED = "MATCH_FINISHED",
}

export enum RoomStatus {
  OPEN = "OPEN",
  IN_PROGRESS = "IN_PROGRESS",
  FINISHED = "FINISHED",
}

export enum RoomVisibility {
  PUBLIC = "PUBLIC",
  PRIVATE = "PRIVATE",
}

// ─────────────────────────────────────────────
// User & Auth
// ─────────────────────────────────────────────

export interface User {
  id: string;
  username: string;
  displayName: string;
  avatarUrl?: string;
}

export interface AuthState {
  user: User | null;
  isLoading: boolean;
  isAuthenticated: boolean;
}

export type AuthAction =
  | { type: "AUTH_LOADING" }
  | { type: "AUTH_SUCCESS"; payload: User }
  | { type: "AUTH_FAILURE" }
  | { type: "LOGOUT" };

export interface AuthContextValue extends AuthState {
  login: () => void;
  logout: () => void;
}

// ─────────────────────────────────────────────
// Room / Lobby
// ─────────────────────────────────────────────

export interface RoomSettings {
  roundCount: number;        // Number of rounds (default: 5)
  timePerRound: number;      // Seconds per round (default: 60)
  gamePack: string;          // e.g. "classic", "nintendo", "fps"
}

export interface Room {
  id: string;
  hostId: string;
  visibility: RoomVisibility;
  status: RoomStatus;
  settings: RoomSettings;
  players: Player[];
  inviteToken?: string;      // Only on private rooms, for the host
}

export interface Player {
  userId: string;
  username: string;
  avatarUrl?: string;
  isHost: boolean;
  isReady: boolean;
  score: number;
}

// ─────────────────────────────────────────────
// Game / Round
// ─────────────────────────────────────────────

export interface MapMetadata {
  id: string;
  gameTitle: string;
  levelName: string;
  noclipScene: string;       // The scene identifier to pass to the noclip iframe
  spawnCoordinates: Vec3;
  topDownMapUrl?: string;    // URL for the 2D map overlay (Phase 3)
  correctPosition?: Vec2;    // Revealed only after round ends
}

export interface Vec3 {
  x: number;
  y: number;
  z: number;
}

export interface Vec2 {
  x: number;
  y: number;
}

export interface RoundState {
  roundNumber: number;
  totalRounds: number;
  phase: GamePhase;
  timeRemainingSeconds: number;
  map?: MapMetadata;
}

// ─────────────────────────────────────────────
// Guesses & Scoring
// ─────────────────────────────────────────────

export interface GameGuess {
  gameTitle: string;
}

export interface LevelGuess {
  levelName: string;
}

export interface SpotGuess {
  position: Vec2;
}

export interface PlayerRoundResult {
  userId: string;
  username: string;
  gameGuessCorrect: boolean;
  levelGuessCorrect: boolean;
  spotDistance?: number;     // Euclidean distance in units
  pointsEarned: number;
  totalScore: number;
}

// ─────────────────────────────────────────────
// Kafka / WebSocket Event Contracts (Epic D3)
// ─────────────────────────────────────────────

export type WsEventType =
  | "MATCH_CREATED"
  | "MATCH_STARTED"
  | "GAME_GUESSED"
  | "LEVEL_GUESSED"
  | "SPOT_GUESSED"
  | "ROUND_FINISHED"
  | "MATCH_FINISHED"
  | "PLAYER_JOINED"
  | "PLAYER_LEFT"
  | "SCORE_UPDATE"
  | "TIMER_TICK"
  | "PHASE_CHANGE";

export interface WsEvent<T = unknown> {
  type: WsEventType;
  roomId: string;
  payload: T;
  timestamp: string;
}

// Typed payload shapes
export interface PhaseChangePayload {
  phase: GamePhase;
  roundState: RoundState;
}

export interface ScoreUpdatePayload {
  results: PlayerRoundResult[];
  leaderboard: LeaderboardEntry[];
}

export interface TimerTickPayload {
  secondsRemaining: number;
}

export interface PlayerJoinedPayload {
  player: Player;
}

// ─────────────────────────────────────────────
// Leaderboard
// ─────────────────────────────────────────────

export interface LeaderboardEntry {
  rank: number;
  userId: string;
  username: string;
  avatarUrl?: string;
  totalScore: number;
}
