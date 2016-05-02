<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="cus">

  <!-- NOTE: this stylesheet is included by other stylesheets using this functionality. Don't use it on it's own. -->
  
  <xsl:param name="docroot"/>
  <xsl:param name="logroot"/>
  <xsl:param name="uid"/>
  <xsl:param name="machine"/>
  <xsl:param name="fqdn"/>
  <xsl:param name="mode"/>

  <xsl:template match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates/></xsl:element>
  </xsl:template>

  <!-- replace the docroot-tag with the docroot-parameter-value -->
  <xsl:template match="cus:docroot"><xsl:value-of select="$docroot"/><xsl:text>/</xsl:text></xsl:template>
  <xsl:template match="cus:logroot"><xsl:value-of select="$logroot"/><xsl:text>/</xsl:text></xsl:template>

  <xsl:template match="cus:uid"><xsl:value-of select="$uid"/></xsl:template>
  <xsl:template match="cus:machine"><xsl:value-of select="$machine"/></xsl:template>
  <xsl:template match="cus:fqdn"><xsl:value-of select="$fqdn"/></xsl:template>

  <!-- include other files -->
  <xsl:template match="cus:include">
    <xsl:param name="href"><xsl:value-of select="@name"/></xsl:param>
    <xsl:param name="path">
      <xsl:choose>
        <xsl:when test="starts-with($href, 'classpath:') or starts-with($href, 'module:')">
          <xsl:value-of select="$href"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="concat($docroot, '/', $href)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:apply-templates select="document($path)/node()"/>
  </xsl:template>

  <xsl:template match="cus:attribute">
    <xsl:attribute name="{@name}"><xsl:apply-templates/></xsl:attribute>
  </xsl:template>
  
  <xsl:template match="cus:test"><xsl:apply-templates/></xsl:template>

  <xsl:template match="cus:choose" name="choose">
    <xsl:param name="in" select="./cus:test"/>
    <xsl:variable name="result">
      <xsl:call-template name="testit">
        <xsl:with-param name="thenode" select="$in[position()=1]"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$result = 'true'"><xsl:apply-templates select="$in[position()=1]"/></xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="choose">
          <xsl:with-param name="in" select="$in[position() != 1]"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="testit">
    <xsl:param name="thenode"/>
    <xsl:choose> 
      <xsl:when test="$thenode/@bool = 'or'">
      <!-- or -->
        <xsl:if test="$thenode/@uid = $uid
                or $thenode/@machine = $machine or $thenode/@mode = $mode">true</xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <!-- and -->
        <xsl:if test="($thenode/@uid = $uid or not($thenode/@uid))
                and ($thenode/@machine = $machine or not($thenode/@machine))
                and ($thenode/@mode = $mode or not($thenode/@mode))">true</xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
