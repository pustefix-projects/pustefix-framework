#!/bin/sh

mylogjar=`ls lib/log*.jar;`

java -Dnewprjprops="$PWD/example/common/conf/newproject.prop" -classpath $mylogjar:build de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
