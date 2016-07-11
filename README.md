# ROEST
A ROS-REST bridge utilizing RDF as data model.

## Requirements
* Java 7

## Build everything
Everything = the .war, .jar, .zip, and .tar files.

    ./gradlew clean assemble

## Configuration
Default configuration:

    roest-ros/src/main/resources/config.default.xml

Custom configuration:

    ~/.roest/config.xml

For example:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <configuration>
            <host>127.0.0.1</host>
            <port>0</port>
            <masterUri>http://127.0.0.1:11311</masterUri>
            <queueExpirationTime>60</queueExpirationTime>
            <queueMaximalSize>600</queueMaximalSize>
            <topicNames>
                    <topicName>/topic/name/to/be/mapped/one</topicName>
                    <topicName>/topic/name/to/be/mapped/two</topicName>
            </topicNames>
    </configuration>

## Usage

### Server
Extract the distribution archive and use the provided start script.

    ./bin/roest-server

Explore the HTTP interface at:

    http://localhost:8081/

OR

Use the helper script to clean, build, assemble, extract, and start the server.

    start_after_clean_assemble_extract.sh

Explore the HTTP interface at:

    http://localhost:8081/

### WebApp
Use the assembled WAR file for deployment at an application server.

OR

Use the Gradle Wrapper to start a Jetty server and deploy the WebApp.

    ./gradlew appRun

Explore the HTTP interface at:

    http://localhost:8081/roest-api