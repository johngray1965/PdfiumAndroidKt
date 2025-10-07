#! /bin/bash

if [ -z "$JRELEASER_SIGNING_PUBLIC_KEY" ]; then
  echo "❌ ERROR: JRELEASER_SIGNING_PUBLIC_KEY is not set."
  echo "-----------------------------------------------------"
  echo "Please set the required environment variables for signing."
  echo "You can use the following commands:"
  echo ""
  echo "  export JRELEASER_SIGNING_PUBLIC_KEY=\$(gpg --export --armor YOUR_KEY_ID)"
  echo "  export JRELEASER_SIGNING_SECRET_KEY=\$(gpg --export-secret-keys --armor YOUR_KEY_ID)"
  echo "  export JRELEASER_SIGNING_PASSPHRASE='your-secret-gpg-passphrase'"
  echo ""
  echo "Replace YOUR_KEY_ID and the passphrase with your actual credentials."
  echo "-----------------------------------------------------"
  exit 1 # Exit with an error code
fi

# Check for the secret key variable
if [ -z "$JRELEASER_SIGNING_SECRET_KEY" ]; then
  echo "❌ ERROR: JRELEASER_SIGNING_SECRET_KEY is not set."
  echo "--> Please check the instructions above and ensure all three variables are exported."
  exit 1
fi

# Check for the passphrase variable
if [ -z "$JRELEASER_SIGNING_PASSPHRASE" ]; then
  echo "❌ ERROR: JRELEASER_SIGNING_PASSPHRASE is not set."
  echo "--> Please check the instructions above and ensure all three variables are exported."
  exit 1
fi

echo "✅ JReleaser signing environment variables are set. Proceeding with release..."
echo ""

./gradlew clean
mkdir -p pdfiumandroid/arrow/build/jreleaser
mkdir -p pdfiumandroid/build/target/staging-deploy
mkdir -p pdfiumandroid/arrow/build/target/staging-deploy
./gradlew publish 
./gradlew jreleaserFullRelease
./gradlew :pdfiumandroid:arrow:jreleaserRelease
