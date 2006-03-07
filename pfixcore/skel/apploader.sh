#!/bin/sh

export CLASSPATH=`ls -1 lib/pfixcore-*.jar`
java -Dapploader.script="$0" de.schlund.pfixxml.loader.CommandClient $@
