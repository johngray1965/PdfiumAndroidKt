#! /bin/bash

rm -rf ~/.m2/repository/io/legere/pdfium*
./gradlew publishToMavenLocal
pushd ~/.m2/repository/io/legere/pdfiumandroid/2.0.0.beta18
unzip pdfiumandroid-2.0.0.beta18-javadoc.jar
open index.html
popd
