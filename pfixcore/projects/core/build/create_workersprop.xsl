<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ci="java:org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo"
                xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
                version="1.1">
  
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:param name="__customization_info"/>
  <xsl:param name="java_home"/>
  
  <xsl:variable name="customizationinfo" select="$__customization_info"/>
  <xsl:variable name="tree">
    <xsl:apply-templates mode="customization" select="/"/>
  </xsl:variable>
  <xsl:variable name="port">
    <xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:connectorport/node()"/>
  </xsl:variable>
  
  <xsl:template match="/">
#
# NOTE: this is only a sample file, suited for a single machine without any clustering.
#       You may need to tweak this file and copy it somewhere else so your changes will not
#       get lost.
#

workers.tomcat_home=<xsl:value-of select="$docroot"/>/servletconf/tomcat
workers.java_home=<xsl:value-of select="$java_home"/>
worker.list=<xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jkmount/node()"/>

ps=/
worker.<xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jkmount/node()"/>.host=<xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jkhost/node()"/>
worker.<xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jkmount/node()"/>.port=<xsl:choose><xsl:when test="not(string($port) = '')">
<xsl:value-of select="$port"/></xsl:when><xsl:otherwise>8009</xsl:otherwise></xsl:choose>
worker.<xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jkmount/node()"/>.type=ajp13
  </xsl:template>
  
  <xsl:template mode="customization" match="p:choose">
    <xsl:variable name="matches" select="p:when[ci:evaluateXPathExpression($customizationinfo,@test)]"/>
    <xsl:choose>
      <xsl:when test="count($matches)=0">
        <xsl:apply-templates select="p:otherwise/node()" mode="customization"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$matches[1]/node()" mode="customization"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template mode="customization" match="text()">
    <xsl:value-of select="ci:replaceVariables($customizationinfo,.)"/>
  </xsl:template>
  
  <xsl:template mode="customization" match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates mode="customization"/>
    </xsl:element>
  </xsl:template>
  
</xsl:stylesheet>
