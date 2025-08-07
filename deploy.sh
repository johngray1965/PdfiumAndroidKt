#! /bin/bash

./gradlew clean
mkdir -p pdfiumandroid/arrow/build/jreleaser
mkdir -p pdfiumandroid/build/target/staging-deploy
mkdir -p pdfiumandroid/arrow/build/target/staging-deploy
./gradlew publish 
./gradlew jreleaserFullRelease
./gradlew :pdfiumandroid:arrow:jreleaserRelease
