#!/bin/zsh

# A small example program for using the new getopt(1) program.
# This program will only work with bash(1)
# An similar program using the tcsh(1) script language can be found
# as parse.tcsh

# Example input and output (from the bash prompt):
# ./parse.bash -a par1 'another arg' --c-long 'wow!*\?' -cmore -b " very long "
# Option a
# Option c, no argument
# Option c, argument `more'
# Option b, argument ` very long '
# Remaining arguments:
# --> `par1'
# --> `another arg'
# --> `wow!*\?'

AH_BN=$(basename $0)

function usage() {
cat << EOF
usage: $AH_BN [options]
  -h, --help
  -c, --copy          copies all .prop.in to non-existing .prop.in.orig (see --remove)
  -C, --convert       converts all .prop.in.orig to .prop.in (overwriting existing)
  -d, --docroot=DDIR
      --debug         enable debug output (no debug output yet)
      --diff          diffs all .prop.orig to .prop files
      --diffsort      sorts and diffs all .prop.orig.sorted to .prop.sorted files
  -D, --dump          show all files that would be processed
      --pattern=PAT   egreps PAT against filenames of docroot relative .prop.in filenames
                      E.g.: someapp/conf/help.prop.in
                      Performs operations only on matching filenames
      --project=VER   modifies DOCROOT/**/project.xml.in by replacing
                      <class>de.schlund.pfixxml.DirectOutputServ[^<]*</class> with
                      VER=old: <class>de.schlund.pfixxml.DirectOutputServlet</class>
                      VER=new: <class>de.schlund.pfixxml.DirectOutputServer</class>
  -p, --propconv=PDIR
  -r, --remove        removes all .prop(.in).orig, .prop(.in).orig.sorted
  -R, --restore       copies all .prop.in.orig to .prop.in (overwriting existing)
  -s, --suffix=SUFFIX the suffix to use for copies, diffs and so on, default ".orig"       
  -t, --todo          greps all .prop.in for stuff to convert manually (// TODO_PROPCONV)

EOF
}

if [ $# -eq 0 ]; then
  usage
  exit 0
fi

# Note that we use `"$@"' to let each command-line parameter expand to a
# separate word. The quotes around `$@' are essential!
# We need TEMP as the `eval set --' would nuke the return value of getopt.
TEMP=`getopt -o hcCd:Dp:rRs:t --long help,copy,convert,docroot:,debug,diff,diffsort,dump,pattern:,project:,propconv:,remove,suffix:,restore,todo,c-long:: \
     -n "$AH_BN" -- "$@"`
RET=$?
#echo TEMP=">>>"$TEMP"<<<"
if [ $RET != 0 ] ; then usage; echo "Terminating..." >&2 ; exit 1 ; fi

# Note the quotes around `$TEMP': they are essential!
eval set -- "$TEMP"

while true ; do
    case "$1" in
        -h|--help)      usage ; shift ; exit 0 ;;
        -c|--copy)      copy="true" ; shift ;;
        -C|--convert)   convert="true" ; shift ;;
        -d|--docroot)   d="$2" ; docroot="$d" ; shift 2 ;;
           --diff)      diff="true" ; shift ;;
           --diffsort)  diffsort="true" ; shift ;;
        -D|--dump)      dump="true" ; shift ;;
           --debug)     debug="1" ; shift ;;
        -p|--propconv)  p="$2" ; propconv="$p" ; shift 2 ;;
           --pattern)   pattern="true"; pat="$2" ; shift 2 ;;
           --project)   project="true"; projectver="$2" ; shift 2 ;;
        -r|--remove)    remove="true" ; shift ;;
	    -s|--suffix)    suf="$2"; suffix="true" ; shift 2 ;;
        -R|--restore)   restore="true" ; shift ;;
        -t|--todo)      todo="true" ; shift ;;
        -DUMMY|--DUMMY-long)
                        # c has an optional argument. As we are in quoted mode,
                        # an empty parameter will be generated if its optional
                        # argument is not found.
            case "$2" in
                "") echo "Option c, no argument"; shift 2 ;;
                *)  echo "Option c, argument \`$2'" ; shift 2 ;;
            esac ;;
        --) shift ; break ;;
        *) echo "Internal error!" ; exit 1 ;;
    esac
done
#echo "Remaining arguments:"
#for arg do echo '--> '"\`$arg'" ; done

if [ "$suffix" != "true" ]; then
  suf=".orig"
fi

if [ "$docroot" = "" ]; then
  #d=/home/adam/workspace/pfixcore/example
  #docroot="$d"
  #echo guessing docroot=$docroot
  echo no docroot. exiting.
  usage
  exit 30
  
fi

if [ "$propconv" = "" ]; then
  #p=/home/adam/workspace/pfixcore/stuff/propconv
  #propconv="$p"
  #echo guessing propconv=$propconv
  echo no propconv. exiting.
  usage
  exit 30
fi


# xslt stylesheet file [file ...]
function xslt {
  XSL="$1" ; shift
  xsltproc "$XSL" "$@"
}

export PATH=$PATH:"$p"/bin

function saxon {
java ${SAXON_OPTS} -mx120M -cp $p/lib/saxon6_5_3.jar -Djavax.xml.transform.TransformerFactory=com.icl.saxon.TransformerFactoryImpl com.icl.saxon.StyleSheet "$@"
}

# $pat extended regexp pattern
# $1 filename
function matches {
  if [ -z "$1" ]; then
  	echo matches: no filename given. exiting.
  	exit 40
  fi
  if [ "$pattern" = "true"  ]; then
  	echo "$1" | grep -q -E "${pat}"
  	return $?
  else
  	true
  fi
}

#set -x
# d-relative, caution: --pattern will not be applied to $allprops
allprops=(`cd "$d" && find . -name "*.prop.in"`)
# d-relative wo .in
dwoincontextprops=(`xslt $p/xsl/findcontextxmlserverprops.xsl $(find $d -name "project.xml.in")`)
# d-relative wo .in
dwoindirectprops=(`xslt $p/xsl/finddirectoutputservletprops.xsl $(find $d -name "project.xml.in")`)

for i in $dwoincontextprops
do
  if matches $i.in ; then
  	# if .prop.in is for contextxmlserver, but formulated as standardprops
    if ! grep -q '<prop  *name="xmlserver.depend.xml" *>' "$d/${i}.in" ; then
      contextprops=(${contextprops} "./${i}.in")
    fi
  fi
done
#echo contextprops="${contextprops}"

for i in $dwoindirectprops
do
  if matches $i.in ; then
    directprops=(${directprops} "./${i}.in")
  fi
done
#echo directprops="${directprops}"

# dwoinallprops allprops without .in  
for i in $allprops
do
  if matches "$i" ; then
    dwoinallprops=(${dwoinallprops} "${i%.in}")
  fi
done

# standardprops = allprops - ( directprops + contextprops )
for i in $allprops
do
  if matches "$i" && ! echo $directprops $contextprops | grep -q $i
  then
    standardprops=(${standardprops} "$i")
  fi
done



if [ "$dump" = "true" ]; then
  #echo allprops
  #for i in $allprops
  #do
  #  echo $i
  #done
  
  #echo dwoinallprops
  #for i in $dwoinallprops
  #do
  #  echo $i
  #done
  
  echo directprops
  for i in $directprops
  do
    echo $i
  done
  
  echo contextprops
  for i in $contextprops
  do
    echo $i
  done
  
  echo standardprops
  for i in $standardprops
  do
    echo $i
  done
fi

# diff .prop${suf} to .prop
if [ "$diff" = "true" ]; then
  for i in $dwoinallprops
  do
  	echo -e "DIFFERENCES $i${suf} $i"
    diff $d/$i${suf} $d/$i
  done
fi

# diffsort .prop${suf}.sorted to .prop.sorted
if [ "$diffsort" = "true" ]; then
  for i in $dwoinallprops
  do
  	echo -e "DIFFERENCES $i${suf}.sorted $i.sorted"
  	sort --output=$d/$i.sorted $d/$i
  	sort --output=$d/$i${suf}.sorted $d/$i${suf}
    diff $d/$i${suf}.sorted $d/$i.sorted
  done
fi

# remove all .prop.in${suf}
if [ "$remove" = "true" ]; then
  for i in $dwoinallprops
  do
    if [ -e $d/$i.in${suf} ]; then
      rm -v $d/$i.in${suf}
    fi
    if [ -e $d/$i${suf} ]; then
      rm -v $d/$i${suf}
    fi
    if [ -e $d/$i${suf}.sorted ]; then
      rm -v $d/$i${suf}.sorted
    fi
    if [ -e $d/$i.sorted ]; then
      rm -v $d/$i.sorted
    fi
  done
fi

# copy .prop.in to .prop.in${suf}
if [ "$copy" = "true" ]; then
  for i in $dwoinallprops
  do
    if [ ! -e $d/$i.in${suf} ]; then
      cp -av $d/$i.in $d/$i.in${suf}
    fi
    if [ -e $d/$i ]; then
      cp -av $d/$i $d/$i${suf}
    else
      echo "WARNING: could not create '${suf}'. File does not exist: $d/$i"
    fi
  done
fi

# copy .prop.in${suf} to .prop.in
if [ "$restore" = "true" ]; then
  for i in $dwoinallprops
  do
    cp -av $d/$i.in${suf} $d/$i.in
    if [ -e $d/$i${suf} ]; then
      cp -av $d/$i${suf} $d/$i
    fi
  done
fi

# $1 roottag
# $2-* docroot relative prop.in
function convert {
  if [ $# -lt 2 ]; then
    echo $0: \"$@\" too few arguments: continue.
    return
  fi
  roottag=$1
  shift
  for i in "$@"
  do
    #echo saxon -o $d/$i $d/$i${suf} $p/xsl/propconv.xsl roottag="$roottag"
    echo -e "converting <properties> to <$roottag>\t$i${suf}"
    # does not work properly :-( shopt -s -o pipefail
    saxon $d/$i${suf} $p/xsl/propconv.xsl roottag="$roottag" | $p/bin/xmlindent -o $d/$i -i 2 -nas -nae -nbs -nbe
    AH_RET=$?
    #shopt -u -o pipefail
    #echo AH_RET=$AH_RET
    if [ $AH_RET -ne 0 ]; then
      saxon returned $AH_RET. exiting.
      exit 10
    fi
  done
}

function todo {
  for i in $allprops
  do
     grep --context=3 --with-filename "// TODO_PROPCONV" $d/$i
  done
}

if [ "$todo" = "true" ]; then
	todo
fi

if [ "$project" = "true" ]; then
	if [ "$projectver" = "old" ]; then
		replace="let"
	else
		replace="er"
	fi
	find $d -name "project.xml.in" -exec $p/bin/ssed -ie 's/<class>de\.schlund\.pfixxml\.DirectOutputServ[^<]*<\/class>/<class>de\.schlund\.pfixxml\.DirectOutputServ'"$replace"'<\/class>/g' {} \;
fi

if [ "$convert" = "true" ]; then
  test -n "$standardprops" && convert standardprops $standardprops
  test -n "$directprops" && convert directoutputserver $directprops
  test -n "$contextprops" && convert contextxmlserver $contextprops
  (cd $d && grep -q "// TODO_PROPCONV" $allprops)
  AH_RET=$?
  if [ $AH_RET -eq 0 ]; then
    echo Some elements could not be converted automatically
    echo Run $AH_BN --todo to see the files and locations to convert manually
    echo "These locations are marked with the string \"// TODO_PROPCONV\""
  else
    echo "All files could be converted automatically"
  fi
fi


