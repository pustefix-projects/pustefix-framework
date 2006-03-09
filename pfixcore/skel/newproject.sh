#!/bin/sh

if [[ -d $PWD/projects ]] ; then \
  docroot=$PWD/projects
elif [[ -d $PWD/example ]] ; then \
  docroot=$PWD/example
else \
  echo "Neither \"projects\" nor \"example\" directory exist in \"$PWD\"!"
  exit 1
fi

mylogjar=`ls lib/log*.jar;`
mysaxonjar=`ls lib/saxon*.jar;`

java -Dpustefix.docroot=${docroot} -classpath $mylogjar:$mysaxonjar:build de.schlund.pfixcore.util.basicapp.basics.InitNewPfixProject
