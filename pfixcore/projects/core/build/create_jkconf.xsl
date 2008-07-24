<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ci="java:org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo"
                xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
                version="1.1">
  
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:param name="__customization_info"/>
  
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

&lt;IfDefine PFX_USE_JK&gt;
   <xsl:call-template name="jkconf"/>
&lt;/IfDefine&gt;

&lt;IfDefine !PFX_USE_PROXY_AJP&gt;
&lt;IfDefine !PFX_USE_JK&gt;
&lt;IfModule !proxy_ajp_module&gt;
   <xsl:call-template name="jkconf"/>
&lt;/IfModule&gt;
&lt;/IfDefine&gt;
&lt;/IfDefine&gt;

  </xsl:template>
  
  <xsl:template name="jkconf">
    <xsl:variable name="logpath">
      <xsl:choose>
        <xsl:when test="starts-with(normalize-space($tree/p:global-config/p:http-server/p:apache/p:logdir/text()), 'pfixroot:')">
          <xsl:value-of select="$docroot"/><xsl:value-of select="substring-after(normalize-space($tree/p:global-config/p:http-server/p:apache/p:logdir/text()), 'pfixroot:')"/>
        </xsl:when>
        <xsl:when test="starts-with(normalize-space($tree/p:global-config/p:http-server/p:apache/p:logdir/text()), 'file:')">
          <xsl:value-of select="substring-after(normalize-space($tree/p:global-config/p:http-server/p:apache/p:logdir/text()), 'file:')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($tree/p:global-config/p:http-server/p:apache/p:logdir/text())"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
JkWorkersFile <xsl:value-of select="$docroot"/>/servletconf/tomcat/workers.prop
JkLogFile     <xsl:value-of select="$logpath"/>/mod_jk.log
JkLogLevel    <xsl:apply-templates select="$tree/p:global-config/p:http-server/p:tomcat/p:loglevel/node()"/>
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
