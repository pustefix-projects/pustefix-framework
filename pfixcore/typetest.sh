#!/bin/sh

TESTSTUBLIB=example/.webservice/webservice
java -classpath .:build:$TESTSTUBLIB:`find ./lib -name "*.jar" -printf "%p:"` TypeTestClient $*

