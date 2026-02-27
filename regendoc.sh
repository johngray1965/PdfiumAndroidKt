#! /bin/bash

version=2.0.0.beta21
rm -rf ~/.m2/repository/io/legere/pdfium*
./gradlew publishToMavenLocal
pushd ~/.m2/repository/io/legere/pdfiumandroid/$version
unzip pdfiumandroid-${version}-javadoc.jar
open index.html
popd
