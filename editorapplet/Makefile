
PROJECT = pfixeditorapplet
PROJECT_ROOT= $(shell pwd)
PFIXCORE_APPLETPATH=$(PROJECT_ROOT)/../example/core/script
#PFIXCORE_APPLETPATH=~/try

SRCROOT    = src
BUILDDIR   = build
JARDIR     = bin
RESDIR     = res

JAVADOCDIR = apidocs

EXTERNAL_JAVADOC =

PACKAGES =  de.schlund.pfixeditor.xml \
	    com.microstar.xml \
	    de.schlund.pfixeditor.editor \
	    de.schlund.pfixeditor.misc


SUBDIRS = $(subst .,/,$(PACKAGES))



################################################################
# normally, there's no need to change anything below this line #
################################################################

export JAVAC      = jikes -g +D ${JIKESOPTIONS}
#export JAVAC      = javac
#export JAVA       = java

export JAVADOC    = ${JDK_HOME}/bin/javadoc
export JAR        = ${JDK_HOME}/bin/jar

#export VERSION    = $(shell head -n 1 META-INF/$(PROJECT).version | grep -E '^[^\s]+')
export JARAUTOTAG = AUTO_TAG_$(shell  date +%s)

BUILDPATH     = $(PROJECT_ROOT)/$(BUILDDIR):${JDK_HOME}/jre/lib/rt.jar:./lib/java40.jar

JAVADOCSUBDIRS = $(subst $(SRCROOT)/,,$(SUBDIRS))
SRCDIRS        =$(patsubst %, $(SRCROOT)/%,$(SUBDIRS))


ALLJAVA     = $(shell find $(SRCDIRS) -maxdepth 1 -name "*.java") 

# The name of the jar file to install
JAR_FILE        = xmlEditorApplet.jar
JAR_MANIFEST    = $(JAR_FILE).manifest
SIGN_SCRIPT     = signScript

.PHONY : all compile jar install clean doc docpriv cleandoc

all: compile jar
	@echo "******* finished *******"
	@echo "type make install to install the applet in pustefix"

compile:  
	@(if ! test -d $(BUILDDIR); then mkdir $(BUILDDIR); fi)
	@(rm -fr $(BUILDDIR)/*)	
	@(cd $(PROJECT_ROOT)/$(SRCROOT))
	@echo
	@echo "*** Building all *.java-files..."
	@echo ${JAVAC}
	@echo ${BUILDPATH}
	@ ${JAVAC} -classpath ${BUILDPATH} \
                  -d $(PROJECT_ROOT)/${BUILDDIR} -sourcepath $(PROJECT_ROOT)/${SRCROOT} $(ALLJAVA)


                  

	@echo "*** ...Done!"

jar:
	@echo "Building $(JAR_FILE) in directory $(JARDIR)."
	@echo
	@(cd $(PROJECT_ROOT))
	@(if ! test -d $(JARDIR); then mkdir $(JARDIR); fi)
	@(rm -f $(JARDIR)/*.jar)	
	@(cd $(PROJECT_ROOT)/$(BUILDDIR); $(JAR) cvfm $(PROJECT_ROOT)/$(JARDIR)/$(JAR_FILE) $(PROJECT_ROOT)/$(RESDIR)/$(JAR_MANIFEST) *)

install: all
	@(cd $(PROJECT_ROOT)/$(JARDIR)/;rm -f $(SIGN_SCRIPT))
	@(cp $(PROJECT_ROOT)/$(RESDIR)/$(SIGN_SCRIPT) $(PROJECT_ROOT)/$(JARDIR)/ )
	@echo "Signing Java Code..."
	@(cd $(PROJECT_ROOT)/$(JARDIR)/;./$(SIGN_SCRIPT))
	@echo "...Done!"
	@echo "copy $(JAR_FILE) to $(PFIXCORE_APPLETPATH)"
	@(cp $(PROJECT_ROOT)/$(JARDIR)/$(JAR_FILE) $(PFIXCORE_APPLETPATH))

clean: 
	@echo "*** Removing all under '$(BUILDDIR)'..."
	@rm -rf $(PROJECT_ROOT)/$(BUILDDIR)/*
	@echo "*** Removing all under '$(JARDIR)'..."
	@rm -rf $(PROJECT_ROOT)/$(JARDIR)/*


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

