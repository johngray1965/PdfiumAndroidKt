#! /bin/bash

./gradlew clean publishAllPublicationsToProjectLocalRepository zipMavenCentralPortalPublication releaseMavenCentralPortalPublication --no-configuration-cache

