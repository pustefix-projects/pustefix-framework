#!/bin/sh

java -classpath .:`find ./lib -name "*.jar" -printf "%p:"` TypeTestClient $*

