LANG=C

######################################
# Makefile for JNI part of AppLoader #
######################################

CC=$(shell which cc 2>/dev/null)

JAVAH=${JDK_HOME}/bin/javah
JAVA_INC=${JDK_HOME}/include
JAVA_INCLIN=${JAVA_INC}/linux

PFX_BLDDIR=../build
PFX_SRCDIR=../src
PFX_SOURCES= \
	${PFX_SRCDIR}/de/schlund/pfixxml/loader/ObjectBuilder.java 
PFX_CLASSES=$(subst /,.,$(subst .java,,$(subst ${PFX_SRCDIR}/,,${PFX_SOURCES})))

JNI_BLDDIR=build
JNI_INCDIR=include
JNI_SRCDIR=src
JNI_PKGDIR=jni
JNI_SOURCES=$(subst /,_,$(subst .java,.c,$(subst ${PFX_SRCDIR}/,,${PFX_SOURCES})))
JNI_LIB=$(JNI_BLDDIR)/libapploader.so
JNI_PKG=jni.tar.gz

.PHONY : headers lib dist pkg clean

$(JNI_LIB): $(PFX_SOURCES)
	@make dist 

init:
	@if [ ! -d ${JNI_BLDDIR} ]; then mkdir ${JNI_BLDDIR}; fi
	@if [ ! -d ${JNI_INCDIR} ]; then mkdir ${JNI_INCDIR}; fi

headers:
	@echo "*** Creating JNI header files"
	${JAVAH} -classpath ${PFX_BLDDIR} -d ${JNI_INCDIR} ${PFX_CLASSES}

ifneq ($(CC),)
lib: headers
	@echo "*** Creating shared library"
	cd ${JNI_SRCDIR} && ${CC} -I${JAVA_INC} -I${JAVA_INCLIN} -I../${JNI_INCDIR} -fPIC ${JNI_SOURCES} -shared -o ../${JNI_LIB}
else
lib: headers
	@echo "!!!!! WARNING !!!!! Can't build shared library due to missing C-compiler."
endif

ifneq ($(CC),)
dist: clean init lib pkg
else
dist: clean init lib pkg
	@echo "!!!!! WARNING !!!!! Distribution is built, but it can't be assured to compile/work correctly."
endif

pkg:  
	@echo "*** Creating distribution"
	@mkdir ${JNI_PKGDIR}
	@cp -a ${JNI_SRCDIR} ${JNI_PKGDIR}
	@cp Makefile.pkg ${JNI_PKGDIR}/Makefile
	@tar cfz ${JNI_PKG} ${JNI_PKGDIR}

clean:
	@echo "*** Cleaning up"
	@rm -f ${JNI_INCDIR}/*.h
	@rm -f ${JNI_LIB}
	@rm -rf ${JNI_PKGDIR}
	@rm -f ${JNI_PKG}
