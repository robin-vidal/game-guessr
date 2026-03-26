-- ============================================================
-- Seed data for local development (profile: local)
-- Applied by Spring on startup via spring.sql.init.data-locations
-- Tables are created by Hibernate (ddl-auto: update) first.
-- ============================================================

-- Pre-seed a waiting match for room "DEMO01" so you can hit
-- POST /api/v1/games/DEMO01/start right away in Swagger.
INSERT INTO
    matches (
        id,
        room_code,
        host_id,
        status,
        current_round_index
    )
VALUES (
        'a0000000-0000-0000-0000-000000000001',
        'DEMO01',
        'host-user-1',
        'WAITING',
        0
    ) ON CONFLICT (id) DO NOTHING;

-- Second room ready to start
INSERT INTO
    matches (
        id,
        room_code,
        host_id,
        status,
        current_round_index
    )
VALUES (
        'a0000000-0000-0000-0000-000000000002',
        'DEMO02',
        'host-user-2',
        'WAITING',
        0
    ) ON CONFLICT (id) DO NOTHING;