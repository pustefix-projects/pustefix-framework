<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:param name="prjname"/>
  
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>

  <xsl:template match="/">
    <xsl:apply-templates select="/projects/project[@name = $prjname]"/>
  </xsl:template>

  <xsl:template match="project">
autoreload.classes=true
autoreload.file=true
init.timeout=10000
destroy.timeout=10000
session.useCookies=false
session.timeout=14400000
session.checkFrequency=30000
    <xsl:if test="./servlet[@autostartup='true']">
servlets.startup=<xsl:call-template name="servlet_init"/>
    </xsl:if>
singleThreadModelServlet.initialCapacity=1
singleThreadModelServlet.incrementCapacity=2
singleThreadModelServlet.maximumCapacity=100

servlets.default.initArgs=servlet.commonpropfile=<xsl:apply-templates select="/projects/common/commonpropfile/node()"/>

    <xsl:for-each select="./servlet">
      <xsl:variable name="servletclass"><xsl:apply-templates select="./class/node()"/></xsl:variable>
      <xsl:variable name="servletprop"><xsl:apply-templates select="./propfile/node()"/></xsl:variable>
servlet.<xsl:value-of select="@name"/>.code=<xsl:value-of select="$servletclass"/>
      <xsl:if test="not(string ($servletprop) = '')">
servlet.<xsl:value-of select="@name"/>.initArgs=servlet.propfile=<xsl:value-of select="$servletprop"/> 
      </xsl:if>
    </xsl:for-each>
  </xsl:template>


  <xsl:template name="servlet_init">
    <xsl:for-each select="./servlet[@autostartup='true']">
      <xsl:value-of select="@name"/>
      <xsl:if test="following-sibling::servlet[@autostartup='true']">,</xsl:if>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
