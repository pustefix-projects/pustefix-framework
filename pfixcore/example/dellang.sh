find . -type f | grep "/txt/" | grep "\.xml$" > ALLINC
find . -type f | grep "/dyntxt/" |grep "\.xml$" >> ALLINC
export CLASSPATH=`ls \`pwd\`/../lib/*.jar | xargs echo | sed -e 's/ /:/g'`:`pwd`/../build
export LANG=C

java -mx384M de.schlund.pfixcore.util.DelLang `pwd` ALLINC

