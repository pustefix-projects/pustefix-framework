#!/bin/sh

mylogjar=`ls lib/log*.jar;`
mycorejar=`ls lib/pfixcore*.jar`

java -Dnewprjprops="$PWD/projects/common/conf/newproject.prop" -classpath $mylogjar:$mycorejar de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
