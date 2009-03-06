<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
>
  
  <xsl:import href="defaultcopy.xsl"/>
  <xsl:import href="customization.xsl"/>
  
  <xsl:param name="targetNamespace">http://www.schlund.de/pustefix/customize</xsl:param>
  <xsl:param name="targetPrefix">cus</xsl:param>
    
  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="cus:choose">
    <cus:choose>
      <xsl:call-template name="choose"/>
    </cus:choose>
  </xsl:template>
  
  <xsl:template match="/project/servlet/propfile|/projects/project/servlet/propfile" priority="1">
    <propfile>
      <xsl:choose>
        <xsl:when test="substring-after(./text(), substring-before(./text(), '.conf.xml')) = '.conf.xml'">
          <xsl:message terminate="yes">
            File already contains references to config files with new schema.
            Please check manually, whether a migration is necessary.
          </xsl:message>
        </xsl:when>
        <xsl:when test="parent::node()/class/text() = 'org.pustefixframework.webservices.WebServiceServlet'"><xsl:value-of select="./text()"/>.xml</xsl:when>
        <xsl:otherwise><xsl:value-of select="substring-before(./text(), '.prop')"/>.conf.xml</xsl:otherwise>
      </xsl:choose>
    </propfile>
  </xsl:template>
  
  <xsl:template match="/projects/common/commonpropfile">
    <xsl:choose>
      <xsl:when test="text() = 'common/conf/pustefix.prop'">
        <commonpropfile>common/conf/pustefix.xml</commonpropfile>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          You are using a special common propfile (not common/conf/pustefix.prop)
          so the conversion of the name could not be accomplished automatically
          and personal review of the central projects configuration file is
          needed.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="/projects/project/servlet[@name='init']/propfile" priority="2">
    <xsl:choose>
      <xsl:when test="text() = 'common/conf/factory.prop'">
        <propfile>common/conf/factory.xml</propfile>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>
          You are using a special configuration file (not common/conf/factory.prop)
          for the factory initialization servlet, so the conversion of the name 
          could not be accomplished automatically and manual review of the central 
          projects configuration file is strongly advised.
        </xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
