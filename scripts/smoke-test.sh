#!/usr/bin/env bash
set -euo pipefail

GATEWAY_URL="${GATEWAY_URL:-http://localhost:8092}"
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8090}"

echo "== Checking Gateway health =="
curl -fsS "${GATEWAY_URL}/actuator/health" | jq . || curl -fsS "${GATEWAY_URL}/actuator/health"

echo "== Checking Keycloak is up =="
curl -fsS "${KEYCLOAK_URL}/" >/dev/null && echo "Keycloak OK"

echo "== Checking Swagger endpoints (may require auth depending on config) =="
for p in /api/swagger-ui/wellness/ /api/swagger-ui/goals/ /api/swagger-ui/events/ ; do
  echo "  - ${GATEWAY_URL}${p}"
done

echo "Smoke test complete."
