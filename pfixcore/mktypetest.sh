#!/bin/sh

javac -classpath .:`find ./lib -name "*.jar" -printf "%p:"` TypeTestClient.java
#javac -classpath .:`find ./example/servletconf/tomcat/shared/lib -name "*.jar" -printf "%p:"` CalcClient2.java

