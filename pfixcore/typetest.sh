#!/bin/sh

TESTSTUBLIB=example/.webservice/webservice
java -classpath .:$TESTSTUBLIB:`find ./lib -name "*.jar" -printf "%p:"` TypeTestClient $*

