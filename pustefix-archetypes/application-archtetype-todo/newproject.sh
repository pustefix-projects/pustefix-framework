#!/bin/sh

docroot=$PWD/projects/

mylogjar=`ls lib/log*.jar;`
mysaxonjar=`ls lib/saxon6*.jar;`
mypfixcore=`ls lib/pfixcore*.jar;`

java -Dpustefix.docroot=${docroot} -classpath $mypfixcore:$mylogjar:$mysaxonjar:build de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
