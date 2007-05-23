<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:func="http://exslt.org/functions"
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback">
  
  <func:function name="pfx:isVisible">
    <xsl:param name="pagename"></xsl:param>
    <xsl:variable name="thepagename">
      <xsl:choose>
        <xsl:when test="$pagename">
          <xsl:value-of select="$pagename"/>
        </xsl:when>
        <xsl:when test="./@name and local-name() = 'page'">
          <xsl:value-of select="./@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            pfx:isVisible() either needs a paramter or has to be called in the right context!
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <func:result select="callback:isAccessible($__context__, $thepagename)"/>
  </func:function>
  
  <func:function name="pfx:isVisited">
    <xsl:param name="pagename"></xsl:param>
    <xsl:variable name="thepagename">
      <xsl:choose>
        <xsl:when test="$pagename">
          <xsl:value-of select="$pagename"/>
        </xsl:when>
        <xsl:when test="./@name and local-name() = 'page'">
          <xsl:value-of select="./@name"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            pfx:isVisited() either needs a paramter or has to be called in the right context!
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <func:result select="callback:isVisited($__context__, $thepagename)"/>
  </func:function>
  
  <func:function name="pfx:getToken">
    <xsl:param name="tokenName"/>
    <func:result select="callback:getToken($__context__,$tokenName)"/>
  </func:function>
  
</xsl:stylesheet>