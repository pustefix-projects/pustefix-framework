#!/bin/sh

javac -classpath .:example/.webservice/webservice:build:`find ./lib -name "*.jar" -printf "%p:"` example/.webservice/webservice/TypeTest_pkg/*.java
javac -classpath .:example/.webservice/webservice:`find ./lib -name "*.jar" -printf "%p:"` TypeTestClient.java
#javac -classpath .:`find ./example/servletconf/tomcat/shared/lib -name "*.jar" -printf "%p:"` CalcClient2.java

