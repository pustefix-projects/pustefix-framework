#!/bin/sh

javac -classpath .:example/.webservice/webservice:build:`find ./lib -name "*.jar" -printf "%p:"` example/.webservice/webservice/de/schlund/pfixcore/example/webservices/*.java
javac -classpath .:build:example/.webservice/webservice:`find ./lib -name "*.jar" -printf "%p:"` TypeTestClient.java
#javac -classpath .:`find ./example/servletconf/tomcat/shared/lib -name "*.jar" -printf "%p:"` CalcClient2.java

