LANG=C

include Makefile.local

################################################################
# normally, there's no need to change anything below this line #
################################################################
export CLASSPATH = $(shell ./bin/setClassPath.sh `pwd`)
TC4_BUILDPATH    = ${CLASSPATH}:${PWD}/example/servletconf/tomcat/common/lib/servlet.jar:${JDK_HOME}/jre/lib/rt.jar
JS_BUILDPATH     = ${CLASSPATH}:${PWD}/lib/jserv/jsdk.jar:${JDK_HOME}/jre/lib/rt.jar
BUILDPATH        = ${JS_BUILDPATH}

export JAVAC      = jikes -g +D ${JIKESOPTIONS}
export JAVA       = java

export JAVADOC    = ${JDK_HOME}/bin/javadoc
export JAR        = ${JDK_HOME}/bin/jar

export VERSION    = $(shell head -n 1 META-INF/$(PROJECT).version | grep -E '^[^\s]+')
export JARAUTOTAG = AUTO_TAG_$(shell  date +%s)

JAVADOCSUBDIRS = $(subst $(SRCROOT)/,,$(SUBDIRS))


ALLWRAPPERS = $(shell find $(SUBDIRS) -maxdepth 1 -name "*.iwrp")
TRFBUILD      = de.schlund.pfixcore.util.MultiTransform
TRFBUILDCLASS = de/schlund/pfixcore/util/MultiTransform.class

PFIXSREQCLASS = de/schlund/pfixxml/PfixServletRequest.class

ALLJAVA     = $(shell find $(SUBDIRS) -maxdepth 1 -name "*.java") $(ALLWRAPPERS:.iwrp=.java)
JS_ALLJAVA  = $(shell find $(JS_SUBDIRS) -maxdepth 1 -name "*.java")
TC4_ALLJAVA = $(shell find $(TC4_SUBDIRS) -maxdepth 1 -name "*.java")

.PHONY : java-common java-tomcat java-jserv clean doc docpriv cleandoc dist notag clean-rebuild example

all: compile example jar
	@echo "Install jars in tomcat-lib directory!"
	@rm -f example/servletconf/tomcat/lib/*.jar
	@(cp dist/*.jar example/servletconf/tomcat/lib/)
	@(cd example/servletconf/tomcat/lib/; ln -s ../../../../lib/*.jar .)

compile:  $(BUILDDIR)/$(PFIXSREQCLASS) $(BUILDDIR)/$(TRFBUILDCLASS) java-tomcat java-jserv java-common

java-common: generate_src
	@echo
	@echo "*** Building all *.java-files..."
	@${JAVAC} -classpath ${BUILDPATH} \
                  -d $(shell pwd)/${BUILDDIR} -sourcepath $(shell pwd)/${SRCROOT} $(ALLJAVA)
	@echo "*** ...Done!"

java-tomcat:
	@echo
	@echo "*** Building tomcat java-files..."
	@${JAVAC} -classpath ${TC4_BUILDPATH} \
                  -d $(shell pwd)/${BUILDDIR} -sourcepath $(shell pwd)/${TC4_SRCROOT} $(TC4_ALLJAVA)
	@echo "*** ...Done!"

java-jserv:
	@echo
	@echo "*** Building jserv  java-files..."
	@${JAVAC} -classpath ${JS_BUILDPATH} \
                  -d $(shell pwd)/${BUILDDIR} -sourcepath $(shell pwd)/${JS_SRCROOT} $(JS_ALLJAVA)
	@echo "*** ...Done!"

# generate generator...
$(BUILDDIR)/$(TRFBUILDCLASS): $(SRCROOT)/$(TRFBUILDCLASS:.class=.java)
	@echo
	@echo "*** Building wrapper builder..."
	@${JAVAC} -classpath ${BUILDPATH} \
                 -d $(shell pwd)/${BUILDDIR} -sourcepath $(shell pwd)/${SRCROOT} $<
	@echo "*** ...Done!"

$(BUILDDIR)/$(PFIXSREQCLASS): $(SRCROOT)/$(PFIXSREQCLASS:.class=.java)
	@echo
	@echo "*** Building PfixServletRequest..."
	@${JAVAC} -classpath ${BUILDPATH} \
                 -d $(shell pwd)/${BUILDDIR} -sourcepath $(shell pwd)/${SRCROOT} $<
	@echo "*** ...Done!"

# generate IWrappers...
generate_src:
	@echo
	@echo "*** Generating wrapper java-files..."
	@$(JAVA) $(TRFBUILD) -s $(SRCROOT)/ -a iwrp -x example/core/build/iwrapper.xsl $(ALLWRAPPERS)
	@echo "*** ...Done!"

example:
	cd example && make -f Makefile  all

clean: 
	@echo "*** Removing all under '$(BUILDDIR)'..."
	@rm -rf $(BUILDDIR)/*
	@echo "*** Removing generated files..."
	@rm -f $(ALLWRAPPERS:.iwrp=.java)
	@echo "*** Removing all files matching: *~ ..."
	@find $(SUBDIRS) -type f -name "*~" | xargs rm -f
	@find $(JS_SUBDIRS) -type f -name "*~" | xargs rm -f
	@find $(TC4_SUBDIRS) -type f -name "*~" | xargs rm -f
	@cd example && make -f Makefile clean
	@rm -f example/servletconf/tomcat/lib/*.jar


realclean: clean cleandoc

clean-rebuild:
	make clean
	make all

echo-classpath-jserv:
	@echo ${CLASSPATH}:${PWD}/lib/jserv/jsdk.jar

echo-classpath-tomcat:
	@echo ${CLASSPATH}

generate: all
	@cd example && make -f Makefile generate

# javadoc with only interfaces
doc:
	@(if ! test -d $(JAVADOCDIR); then mkdir $(JAVADOCDIR); fi)    
	${JAVADOC} -locale en_US -use -splitindex \
	${JAVADOC_GENERALOPTS}

# javadoc with private fields/methods
docpriv:
	@(if ! test -d $(JAVADOCDIR); then mkdir $(JAVADOCDIR); fi)    
	${JAVADOC} -locale en_US -private -use -splitindex \
	${JAVADOC_GENERALOPTS}

cleandoc:
	@echo "*** Removing Java-Docs files ..."
	@rm -rf $(JAVADOCDIR)/*

dist:   _tag clean compile jar
	@cp META-INF/CVS_AUTOTAG example/core/AUTOTAG
	@(cd example && rm -f ../dist/$(PROJECT)-data-*.tar.gz && make stylesheets && tar cvzf ../dist/$(PROJECT)-data-$(VERSION).tar.gz `find core -type f | grep -v CVS`) 
	@rm example/core/AUTOTAG

_tag:
	@echo "*** tag cvs with ${JARAUTOTAG}!"
	@(cvs tag ${JARAUTOTAG})
	$(shell echo ${JARAUTOTAG} > META-INF/CVS_AUTOTAG) 

data:
	@(cd example && rm -f ../dist/$(PROJECT)-data-*.tar.gz && make stylesheets && tar cvzf ../dist/$(PROJECT)-data-$(VERSION).tar.gz `find core -type f | grep -v CVS`) 

notag:	_notag compile jar
	@rm -f example/core/AUTOTAG
	@(cd example && rm -f ../dist/$(PROJECT)-data-*.tar.gz && make stylesheets && tar cvzf ../dist/$(PROJECT)-data-$(VERSION).tar.gz `find core -type f | grep -v CVS`) 


_notag:
	@echo "*** Remove autotag, because we don't tag in cvs!"
	$(shell echo no-tag-set > META-INF/CVS_AUTOTAG) 

jar:
	@echo "Building $(PROJECT).jar in directory dist."
	@(if ! test -d dist; then mkdir dist; fi)
	@(rm -f dist/*.jar)	
	@${JAR} cf dist/$(PROJECT)-$(VERSION).jar `find META-INF -type f | grep -v "CVS/"`
	@(cd $(BUILDDIR); ${JAR} uf ../dist/$(PROJECT)-${VERSION}.jar *)
	@(cd res; ${JAR} uf ../dist/$(PROJECT)-${VERSION}.jar `find . -type f | grep -v "CVS/"`)
