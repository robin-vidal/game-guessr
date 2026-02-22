# ADR 0004: Frontend Architecture & Noclip Integration

**Date:** 2026-02-22

## Context
Our game relies heavily on parsing and rendering 3D environments from classic video games in the browser. The open-source project `noclip.website` already provides a highly optimized WebGL engine capable of extracting and rendering these maps from raw game data. We need to decide whether to fork and heavily modify their engine natively within our React application or to decouple it using an Iframe architecture.

## Decision
We will use an **Iframe Bridge Architecture**. The main Frontend Service (React) will act as a wrapper that hosts the vanilla `noclip.website` within an `<iframe src="noclip.website">`. All bidirectional communication (e.g., setting the map, spawning the player, retrieving coordinates) will be handled via the standard browser `Window.postMessage` API.

## Rationale
- **Maintainability**: Forking a complex WebGL rendering engine like `noclip` would require immense specialized knowledge and make merging upstream updates difficult. By treating `noclip` as a black box inside an iframe, we guarantee we can easily upgrade if they add new features or bug fixes.
- **Separation of Concerns**: The custom React frontend will purely handle the Game UI (menus, lobbies, chat, scoreboards), while the iframe remains 100% focused on 3D WebGL rendering.
- **Security & Sandboxing**: Iframes naturally sandbox the heavy WebGL execution context from our DOM-heavy React application, potentially preventing the 3D renderer's requestAnimationFrame loops from blocking UI thread operations.

## Consequences
- **Positive:** Massive reduction in development time by leveraging an existing open-source tool. Clear boundary between the game UI and the 3D engine.
- **Negative / Risk:** We are restricted by the capabilities of the `postMessage` API. If `noclip`'s engine does not expose a specific hook we need (e.g., restricting player movement ranges), we might still have to maintain a light fork of `noclip` to inject those `postMessage` listeners.
- **Implication:** The frontend team must design a strict, standardized message payload format (Contract) between the parent window and the iframe to ensure states do not desync during gameplay.
