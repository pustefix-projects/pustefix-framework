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
            pfx:isVisible() either needs a parameter or has to be called in the right context!
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
            pfx:isVisited() either needs a parameter or has to be called in the right context!
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
  
  <func:function name="pfx:requiresToken">
    <xsl:param name="pageName"/>
    <func:result select="callback:requiresToken($__context__,$pageName)"/>
  </func:function>
  
  <func:function name="pfx:getIWrapperInfo">
    <xsl:param name="pageName"/>
    <xsl:param name="prefix"/>
    <func:result select="callback:getIWrapperInfo($__context__,/,$pageName,$prefix)"/>
  </func:function>
  
  <func:function name="pfx:hasRole">
    <xsl:param name="roleName"/>
    <func:result select="callback:hasRole($__context__,$roleName)"/>
  </func:function>
  
  <func:function name="pfx:condition">
    <xsl:param name="conditionId"/>
    <func:result select="callback:checkCondition($__context__,$conditionId)"/>
  </func:function>
  
  <func:function name="pfx:checkAuthorization">
    <xsl:param name="pageName"/>
    <func:result select="callback:checkAuthorization($__context__, $pageName)"/>
  </func:function>
  
  <func:function name="pfx:isAuthorized">
    <xsl:param name="pageName"/>
    <func:result select="callback:isAuthorized($__context__,$pageName)"/>
  </func:function>
  
</xsl:stylesheet>