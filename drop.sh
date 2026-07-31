#! /bin/bash
#
# Drop (delete) an un-released Maven Central Portal deployment by its deployment ID.
#
# Works ONLY while the deployment has not been published (states VALIDATED, PENDING, VALIDATING, FAILED).
# Once it is PUBLISHED to Maven Central it is permanent and cannot be deleted.
#
# Usage:
#   ./drop.sh <deploymentId>            # checks state first, refuses if already published
#   ./drop.sh <deploymentId> --force    # skip the state check and DELETE immediately
#
# Credentials (Central Portal token) are read from, in order:
#   1. env  CENTRAL_PORTAL_USERNAME / CENTRAL_PORTAL_PASSWORD
#   2. ~/.gradle/gradle.properties keys  mavenCentralPortalUsername / mavenCentralPortalPassword

set -euo pipefail

DEPLOYMENT_ID="${1:-}"
FORCE="${2:-}"
BASE="https://central.sonatype.com/api/v1/publisher"

if [ -z "$DEPLOYMENT_ID" ]; then
    echo "Usage: $0 <deploymentId> [--force]" >&2
    exit 1
fi

prop() { # read a key=value from ~/.gradle/gradle.properties
    grep -E "^[[:space:]]*$1[[:space:]]*=" "$HOME/.gradle/gradle.properties" 2>/dev/null |
        head -1 | sed -E "s/^[^=]*=[[:space:]]*//; s/[[:space:]]*$//"
}

USER_NAME="${CENTRAL_PORTAL_USERNAME:-$(prop mavenCentralPortalUsername)}"
PASSWORD="${CENTRAL_PORTAL_PASSWORD:-$(prop mavenCentralPortalPassword)}"

if [ -z "$USER_NAME" ] || [ -z "$PASSWORD" ]; then
    echo "Missing credentials. Set CENTRAL_PORTAL_USERNAME/PASSWORD, or add" >&2
    echo "mavenCentralPortalUsername / mavenCentralPortalPassword to ~/.gradle/gradle.properties." >&2
    exit 1
fi

TOKEN=$(printf '%s:%s' "$USER_NAME" "$PASSWORD" | base64 | tr -d '\n')
AUTH="Authorization: Bearer $TOKEN"

if [ "$FORCE" != "--force" ]; then
    echo "Checking deployment $DEPLOYMENT_ID ..."
    STATUS_JSON=$(curl -s -X POST -H "$AUTH" "$BASE/status?id=$DEPLOYMENT_ID")
    STATE=$(printf '%s' "$STATUS_JSON" | python3 -c '
import sys, json
try:
    print(json.load(sys.stdin).get("deploymentState", "?"))
except Exception:
    print("?")
' 2>/dev/null)
    echo "  state: $STATE"
    case "$STATE" in
        PUBLISHING | PUBLISHED)
            echo "Refusing: deployment is $STATE (already released to Central — permanent, cannot be deleted)." >&2
            exit 1 ;;
        "?" | "")
            echo "Could not read state (bad deployment ID or auth?). Response was:" >&2
            printf '  %s\n' "$STATUS_JSON" >&2
            echo "If you're sure, re-run with --force to DELETE anyway." >&2
            exit 1 ;;
    esac
fi

echo "Dropping deployment $DEPLOYMENT_ID ..."
CODE=$(curl -s -o /tmp/drop_resp.txt -w "%{http_code}" -X DELETE -H "$AUTH" "$BASE/deployment/$DEPLOYMENT_ID")
if [ "$CODE" = "204" ] || [ "$CODE" = "200" ]; then
    echo "Dropped OK (HTTP $CODE)."
else
    echo "Drop failed (HTTP $CODE):" >&2
    cat /tmp/drop_resp.txt >&2 2>/dev/null; echo >&2
    rm -f /tmp/drop_resp.txt
    exit 1
fi
rm -f /tmp/drop_resp.txt
