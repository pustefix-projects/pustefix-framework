<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ci="java:org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo"
                xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
                version="1.1">
  
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="UTF-8" indent="no"/>
  
  <xsl:param name="__customization_info"/>
  <xsl:param name="java_home"/>
  
  <xsl:variable name="customizationinfo" select="$__customization_info"/>
  
  <xsl:template match="/">
    <xsl:variable name="tree">
      <xsl:apply-templates mode="customization" select="self::node()"/>
    </xsl:variable>
#
# NOTE: this is only a sample file, suited for a single machine without any clustering.
#       You may need to tweak this file and copy it somewhere else so your changes will not
#       get lost.
#
    <xsl:variable name="jvmroute"><xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jvmroute/node()"/></xsl:variable>
    <xsl:variable name="jkmount"><xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:jkmount/node()"/></xsl:variable>
    <xsl:variable name="port"><xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:connector-port/node()"/></xsl:variable>

&lt;Proxy balancer://<xsl:value-of select="$jkmount"/>&gt;
  # In a clustered environment add a BalancerMember entry for every host participating in the cluster
  BalancerMember ajp://<xsl:value-of select="$tree/p:global-config/p:http-server/p:tomcat/p:jkhost/text()"/>:<xsl:choose><xsl:when test="not(string($port) = '')"><xsl:value-of select="$port"/></xsl:when><xsl:otherwise>8009</xsl:otherwise></xsl:choose> <xsl:if test="not($jvmroute='')"> route=<xsl:value-of select="$jvmroute"/></xsl:if>
&lt;/Proxy&gt;
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
