LANG=C

include Makefile.local

################################################################
# normally, there's no need to change anything below this line #
################################################################
export CLASSPATH  = $(shell ./bin/setClassPath.sh `pwd`)
export JAVAC      = jikes -g +D ${JIKESOPTIONS}
export JAVA       = java
export JAVADOC    = ${JDK_HOME}/bin/javadoc
export JAR        = ${JDK_HOME}/bin/jar
export VERSION    = $(shell head -n 1 META-INF/$(PROJECT).version | grep -E '^[^\s]+')
export JARAUTOTAG = AUTO_TAG_$(shell  date +%s)

BUILDPATH     = ${CLASSPATH}:${PWD}/example/servletconf/tomcat/common/lib/servlet.jar:${JDK_HOME}/jre/lib/rt.jar
ALLWRAPPERS   = $(shell find $(SUBDIRS) -maxdepth 1 -name "*.iwrp")
TRFBUILD      = de.schlund.pfixcore.util.MultiTransform
TRFBUILDCLASS = de/schlund/pfixcore/util/MultiTransform.class

PFIXSREQCLASS = de/schlund/pfixxml/PfixServletRequest.class

ALLJAVA     = $(shell find $(SUBDIRS) -maxdepth 1 -name "*.java") $(ALLWRAPPERS:.iwrp=.java)

.PHONY : java-common java-tomcat clean doc docpriv cleandoc dist notag clean-rebuild

all: compile dev 
	@cd example && make -f Makefile all

generate: compile dev
	@cd example && make -f Makefile generate

compile:  $(BUILDDIR)/$(PFIXSREQCLASS) $(BUILDDIR)/$(TRFBUILDCLASS) java-tomcat java-common

java-common: generate_src
	@echo
	@echo "*** Building all *.java-files..."
	@${JAVAC} -classpath ${BUILDPATH} \
                  -d $(shell pwd)/${BUILDDIR} -sourcepath $(shell pwd)/${SRCROOT} $(ALLJAVA)
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

clean: 
	@echo "*** Removing all under '$(BUILDDIR)'..."
	@rm -rf $(BUILDDIR)/*
	@echo "*** Removing generated files..."
	@rm -f $(ALLWRAPPERS:.iwrp=.java)
	@echo "*** Removing all files matching: *~ ..."
	@find $(SUBDIRS) -type f -name "*~" | xargs rm -f
	@cd example && make -f Makefile clean
	@rm -f example/servletconf/tomcat/lib/*.jar

realclean: clean cleandoc

clean-rebuild:
	make clean
	make all

echo-classpath:
	@echo ${CLASSPATH}

doc:
	@(if ! test -d $(JAVADOCDIR); then mkdir $(JAVADOCDIR); fi)    
	${JAVADOC} -locale en_US -use -splitindex \
	${JAVADOC_GENERALOPTS}

cleandoc:
	@echo "*** Removing Java-Docs files ..."
	@rm -rf $(JAVADOCDIR)/*

dist:   _dist clean compile jar data skeleton
	@rm -f example/core/CVS_AUTOTAG
	@rm -f skel/CVS_AUTOTAG

notag:	_notag clean compile jar data skeleton
	@rm -f example/core/CVS_AUTOTAG
	@rm -f skel/CVS_AUTOTAG

_dist:
	@echo "*** tag cvs with ${JARAUTOTAG}!"
	@(cvs tag ${JARAUTOTAG})
	$(shell echo ${JARAUTOTAG} > META-INF/CVS_AUTOTAG) 

_notag:
	@echo "*** Remove autotag, because we don't tag in cvs!"
	$(shell echo no-tag-set > META-INF/CVS_AUTOTAG) 

skeleton:
	@echo "Building $(PROJECT)-skel.jar in directory dist."
	cp META-INF/CVS_AUTOTAG skel/CVS_AUTOTAG
	cp -a lib/*.jar skel/lib/
	cp dist/$(PROJECT)-${VERSION}.jar dist/$(PROJECT)-data-$(VERSION).tar.gz skel/lib
	@echo "...copying the tomcat installation files into the skeleton distribution..."  
	@(cd example/servletconf/tomcat; tar cf - `find bin common server -type f | grep -v "/CVS/"` | \
		tar xf - -C ../../../skel/projects/servletconf/tomcat) 
	@rm -f dist/$(PROJECT)-skel-*.tar.gz
	tar cvzf dist/$(PROJECT)-skel-$(VERSION).tar.gz  `find skel -type f | grep -v CVS`
	@rm -f skel/lib/*.jar
	@rm -f skel/lib/$(PROJECT)-data-$(VERSION).tar.gz
	@(cd skel/projects/servletconf/tomcat; rm -rf bin common server)

data:
	@echo "Building $(PROJECT)-data.jar in directory dist."
	@cp META-INF/CVS_AUTOTAG example/core/CVS_AUTOTAG
	@(cd example && rm -f ../dist/$(PROJECT)-data-*.tar.gz && make stylesheets && \
		tar cvzf ../dist/$(PROJECT)-data-$(VERSION).tar.gz `find core -type f | grep -v CVS`) 

jar:
	@echo "Building $(PROJECT).jar in directory dist."
	@(if ! test -d dist; then mkdir dist; fi)
	@(rm -f dist/*.jar)	
	@${JAR} cvf dist/$(PROJECT)-$(VERSION).jar `find META-INF -type f | grep -v "CVS/"`
	@(cd $(BUILDDIR); ${JAR} uvf ../dist/$(PROJECT)-${VERSION}.jar *)
	@(cd res; ${JAR} uvf ../dist/$(PROJECT)-${VERSION}.jar `find . -type f | grep -v "CVS/"`)

dev:
	@echo "Setting link to $(BUILDDIR)/de in example/servletconf/tomcat/classes."
	@(cd example/servletconf/tomcat/; if ! test -d classes; then mkdir classes; fi)
	@(cd example/servletconf/tomcat/classes; if ! test -h de; then ln -s ../../../../$(BUILDDIR)/de; fi)
	@echo "Building $(PROJECT)-res.jar in tomcat's lib directory."
	@rm -f example/servletconf/tomcat/lib/*.jar
	@${JAR} cf example/servletconf/tomcat/lib/$(PROJECT)-$(VERSION)-res.jar `find META-INF -type f | grep -v "CVS/"`
	@(cd res; ${JAR} uf ../example/servletconf/tomcat/lib/$(PROJECT)-${VERSION}-res.jar `find . -type f | grep -v "CVS/"`)
	@echo "Install jars in tomcat-lib directory!"
	@(cd example/servletconf/tomcat/lib/; ln -s ../../../../lib/*.jar .)
