#!/bin/sh

if [ "$1" = "pfixcore" ]
then
  java -Dnewprjprops="$PWD/example/common/conf/newproject.prop" -classpath build:lib/log4j-1.2.8.jar de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
else
  java -Dnewprjprops="$PWD/projects/common/conf/newproject.prop" -classpath lib/pfixcore-0.6.0-pre1.jar:lib/log4j-1.2.8.jar de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
fi