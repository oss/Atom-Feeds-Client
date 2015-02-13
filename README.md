Atom-Feeds-Client
=================

Requires:
    openjdk-7
    maven  >= 3.2.5
    tomcat >= 7.054

How to compile for the first time:
    mvn clean validate compile package

For the Oauth-Maven-Project - Execute Project with:
    mvn compile exec:java -Dexec.classpathScope=compile
    -Dlog4j.configuration=file:./src/main/resources/log4j.properties

For the Test OAuth2v1 Project - Execute Project with:
    mvn clean compile tomcat7:deploy
