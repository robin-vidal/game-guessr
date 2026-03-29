// ─────────────────────────────────────────────
// Game Phase Enum
// Used in WS events and round state — not generated from backend
// ─────────────────────────────────────────────

export enum GamePhase {
  WAITING = 'WAITING',
  EXPLORING = 'EXPLORING',
  GUESSING_GAME = 'GUESSING_GAME',
  GUESSING_LEVEL = 'GUESSING_LEVEL',
  GUESSING_SPOT = 'GUESSING_SPOT',
  ROUND_RESULTS = 'ROUND_RESULTS',
  MATCH_FINISHED = 'MATCH_FINISHED',
}

// ─────────────────────────────────────────────
// Round State
// Client-side state derived from polling game-service
// ─────────────────────────────────────────────

export interface RoundState {
  roundNumber: number;
  totalRounds: number;
  phase: GamePhase;
  timeRemainingSeconds: number;
  noclipHash?: string;
}

// ─────────────────────────────────────────────
// Kafka / WebSocket Event Contracts (Epic D3)
// Defined here until a WebSocket service exposes an OpenAPI spec
// ─────────────────────────────────────────────

export type WsEventType =
  | 'MATCH_CREATED'
  | 'MATCH_STARTED'
  | 'GAME_GUESSED'
  | 'LEVEL_GUESSED'
  | 'SPOT_GUESSED'
  | 'ROUND_FINISHED'
  | 'MATCH_FINISHED'
  | 'PLAYER_JOINED'
  | 'PLAYER_LEFT'
  | 'SCORE_UPDATE'
  | 'TIMER_TICK'
  | 'PHASE_CHANGE';

export interface WsEvent<T = unknown> {
  type: WsEventType;
  roomId: string;
  payload: T;
  timestamp: string;
}

export interface PhaseChangePayload {
  phase: GamePhase;
  roundState: RoundState;
}

export interface ScoreUpdatePayload {
  // References generated type from scoring-service
  roomCode: string;
  roundNumber: number;
}

export interface TimerTickPayload {
  secondsRemaining: number;
}
