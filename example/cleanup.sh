#!/bin/sh

export CLASSPATH=`ls \`pwd\`/../lib/*.jar | xargs echo | sed -e 's/ /:/g'`:`pwd`/../build
export LANG=C


java -mx384M de.schlund.pfixcore.util.Cleanup 


