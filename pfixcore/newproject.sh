#!/bin/sh

mylogjar=`ls lib/log*.jar;`
myxmljar=`ls lib/xml*.jar;`
myxercesjar=`ls lib/xerces*.jar;`
mysaxonjar=`ls lib/saxon*.jar;`

java -Dnewprjprops="$PWD/example/common/conf/newproject.prop" -classpath $mylogjar:$mysaxonjar:$myxmljar:$myxercesjar:build de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
