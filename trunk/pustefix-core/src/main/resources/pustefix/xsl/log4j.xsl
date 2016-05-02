<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:log4j="http://jakarta.apache.org/log4j/">
  
  <xsl:output method="xml" encoding="UTF-8" indent="yes" doctype-system="http://logging.apache.org/log4j/docs/api/org/apache/log4j/xml/log4j.dtd"/>
  <xsl:include href="lib.xsl"/>

  <xsl:template match="param">
    <param>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(@value)">
        <xsl:attribute name="value"><xsl:apply-templates select="./node()"/></xsl:attribute>
      </xsl:if>
    </param>
  </xsl:template>

  <xsl:template match="/">
    <log4j:configuration>
    <xsl:variable name="config">
      <xsl:apply-templates/>
    </xsl:variable>
    <xsl:apply-templates select="$config/renderer[not(following-sibling::renderer/@name=@name)]"/>
    <xsl:apply-templates select="$config/appender[not(following-sibling::appender/@name=@name)]"/>
    <xsl:apply-templates select="$config/category[not(following-sibling::category/@name=@name)]"/>
    <xsl:apply-templates select="$config/logger[not(following-sibling::logger/@name=@name)]"/>
    <xsl:apply-templates select="$config/root[last()]"/>
    </log4j:configuration>
  </xsl:template>
  
  <xsl:template match="includes">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="log4j:configuration">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
