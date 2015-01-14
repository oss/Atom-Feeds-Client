Atom-Feeds-Client
=================

Requires:
openjdk-7
maven 3.2.5

How to compile for the first time:
mvn clean validate compile package

How to run the program:
mvn compile exec:java -Dexec.classpathScope=compile
 -Dlog4j.configuration=file:./src/main/resources/log4j.properties

