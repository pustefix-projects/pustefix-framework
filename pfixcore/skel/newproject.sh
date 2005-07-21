#!/bin/sh

mylogjar=`ls lib/log*.jar;`
mycorejar=`ls lib/pfixcore*.jar`
myxmljar=`ls lib/xml*.jar;`
myxercesjar=`ls lib/xerces*.jar;`
mysaxonjar=`ls lib/saxon*.jar;`

java -Dnewprjprops="$PWD/projects/common/conf/newproject.prop" -classpath $mylogjar:$mysaxonjar:$myxmljar:$myxercesjar:$mycorejar de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
