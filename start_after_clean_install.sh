#!/bin/bash
./gradlew clean installDist && ./roest-server/build/install/roest-server/bin/roest-server
