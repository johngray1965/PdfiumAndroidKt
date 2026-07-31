#! /bin/bash
#
# Publish all four modules (pdfiumandroid, api, arrow, core) as a SINGLE Maven Central Portal deployment
# via nmcp aggregation, instead of publish-on-central firing one deployment per module (which hit the
# Portal's per-month deployment limit). The upload is USER_MANAGED — after this runs, review and release
# the single deployment from https://central.sonatype.com (or set publishingType = "AUTOMATIC" in the root
# build.gradle.kts to skip the manual step).

set -uo pipefail

# Guard: never publish a non-release version. gitSemVer emits a bare version (e.g. 3.0.4-alpha01) only on a
# clean, tagged commit; off-tag or dirty commits produce dev builds like 2.0.1-dev1w-34a067c. One of those
# reached Central once — and Central is immutable — so refuse anything that isn't a real release. Legit
# pre-release tags (-alpha/-beta/-rc) are allowed; only -dev / -SNAPSHOT / dirty are blocked.
VERSION=$(./gradlew -q --no-configuration-cache properties 2>/dev/null | grep '^version:' | sed 's/^version:[[:space:]]*//' || true)
echo "Resolved version: ${VERSION:-<unknown>}"
case "${VERSION:-}" in
    "")
        echo "Could not resolve the project version; aborting." >&2
        exit 1 ;;
    *-dev* | *-SNAPSHOT | *dirty*)
        echo "Refusing to publish non-release version '$VERSION'." >&2
        echo "Publish only from a clean, tagged commit (check 'git status' / 'git tag' — gitSemVer emits the" >&2
        echo "bare version there). Pass --force below only if you REALLY mean it." >&2
        [ "${1:-}" = "--force" ] || exit 1
        echo "--force given; proceeding anyway." >&2 ;;
esac

echo "Publishing $VERSION as one aggregated Central Portal deployment (USER_MANAGED — release it at central.sonatype.com)..."
./gradlew clean publishAggregationToCentralPortal --no-configuration-cache
