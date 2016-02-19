# ROEST
A ROS-REST bridge.

## Requirements
* Java JDK5

## Configuration
Default configuration:

    roest-ros/src/main/resources/config.default.xml

Custom configuration:

roest-ros/src/main/resources/config.xml

For example:

    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <configuration>
            <uri>http://127.0.0.1:11311</uri>
            <topicNames>
                    <topicName>/topic/name/to/be/mapped/one</topicName>
                    <topicName>/topic/name/to/be/mapped/two</topicName>
            </topicNames>
    </configuration>
* uri
    * HTTP URI of the ROS Master to be used
* topicNames / topicName
    * List of topic names to be mapped. By default all topics are mapped.

## Usage
Currently the Gradle Wrapper is used to start the application.

    ./gradlew appRun

Explore the HTTP interface at:

    http://localhost:8081/roest-webapp