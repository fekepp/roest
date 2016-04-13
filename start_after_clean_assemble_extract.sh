#!/bin/bash
./gradlew clean assemble && unzip -d roest-server/build/distributions/ roest-server/build/distributions/roest-server-*.zip && ./roest-server/build/distributions/roest-server-*/bin/roest-server
