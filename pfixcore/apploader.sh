#!/bin/sh
java -Dapploader.script="$0" -classpath build de.schlund.pfixxml.loader.CommandClient $@ 
