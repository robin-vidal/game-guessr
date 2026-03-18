#!/bin/bash
# Create the scoringdb database used by scoring-service.
# This script is automatically run by the Postgres container on init.
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE DATABASE scoringdb;
    GRANT ALL PRIVILEGES ON DATABASE scoringdb TO $POSTGRES_USER;
EOSQL
