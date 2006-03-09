#!/bin/sh

mylogjar=`ls lib/log*.jar;`
mysaxonjar=`ls lib/saxon*.jar;`

java -Dnewprjprops="$PWD/projects/common/conf/newproject.prop" -classpath $mylogjar:$mysaxonjar:build de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
