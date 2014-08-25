<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:func="http://exslt.org/functions"
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback"
                xmlns:rfh="java:org.pustefixframework.http.AbstractPustefixXMLRequestHandler$RegisterFrameHelper"
                xmlns:inf="java:de.schlund.pfixxml.LocationInfo"
                exclude-result-prefixes="pfx func callback rfh inf">
  
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
          <xsl:message terminate="yes">WARNING at '<xsl:value-of select="inf:getLocation()"/>':
            pfx:isVisible() either needs a parameter or has to be called in the right context!
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <func:result select="callback:isAccessible($__context__, $__target_gen, $thepagename)"/>
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
          <xsl:message terminate="yes">WARNING at '<xsl:value-of select="inf:getLocation()"/>':
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
 
  <func:function name="pfx:getTenantInfo">
    <func:result select="callback:getTenantInfo($__context__,$__target_gen,/)"/>
  </func:function>
  
  <func:function name="pfx:hasRole">
    <xsl:param name="roleName"/>
    <func:result select="callback:hasRole($__context__,$roleName)"/>
  </func:function>
  
  <func:function name="pfx:condition">
    <xsl:param name="conditionId"/>
    <func:result select="callback:checkCondition($__context__,$conditionId)"/>
  </func:function>
  
  <func:function name="pfx:authconstraint">
    <xsl:param name="authConstraintId"/>
    <func:result select="callback:checkAuthConstraint($__context__,$authConstraintId)"/>
  </func:function>
  
  <func:function name="pfx:checkAuthorization">
    <xsl:param name="pageName"/>
    <func:result select="callback:checkAuthorization($__context__, $pageName)"/>
  </func:function>
  
  <func:function name="pfx:isAuthorized">
    <xsl:param name="pageName"/>
    <func:result select="callback:isAuthorized($__context__,$pageName)"/>
  </func:function>

  <func:function name="pfx:isBot">
    <func:result select="callback:isBot($__context__)"/>
  </func:function>
  
  <func:function name="pfx:__addParams">
    <xsl:param name="params"/>
    <xsl:choose>
      <xsl:when test="$params=''">
        <func:result></func:result> 
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($params,'&amp;')">
            <func:result select="concat('?',substring-after($params,'&amp;'))"/>
          </xsl:when>
          <xsl:otherwise>
            <func:result select="concat('?',$params)"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </func:function>

  <func:function name="pfx:getFrameworkVersion">
    <func:result select="callback:getFrameworkVersion()"/>
  </func:function>
  
  <func:function name="pfx:__needsLastFlow">
    <xsl:param name="pageName"/>
    <xsl:param name="lastFlow"/>
    <func:result select="callback:needsLastFlow($__context__,$pageName,$lastFlow)"/>
  </func:function>
  
  <func:function name="pfx:__omitPage">
    <xsl:param name="pageName"/>
    <xsl:param name="language"><xsl:value-of select="$lang"/></xsl:param>
    <xsl:param name="altKey"><xsl:if test="$page=$pageName and $pageAlternative"><xsl:value-of select="$pageAlternative"/></xsl:if></xsl:param>
    <func:result select="callback:omitPage($__context__,$__target_gen,$pageName,$language,$altKey)"/>
  </func:function>
  
  <func:function name="pfx:getHomePage">
    <func:result select="callback:getHomePage($__context__,$__target_gen)"/>
  </func:function>
  
  <func:function name="pfx:getPageAlias">
    <xsl:param name="pageName"/>
    <xsl:param name="language"><xsl:value-of select="$lang"/></xsl:param>
    <func:result select="callback:getPageAlias($__target_gen,$pageName,$language)"/>
  </func:function>
  
  <func:function name="pfx:getDisplayPageName">
    <func:result select="callback:omitPage($__context__,$__target_gen,$page,$lang,$pageAlternative)"/>
  </func:function>
  
  <func:function name="pfx:getEnvProperty">
    <xsl:param name="prop"/>
    <func:result select="callback:getEnvProperty($prop)"/>
  </func:function>
  
  <func:function name="pfx:reuseDOM">
    <xsl:if test="rfh:registerFrame($__register_frame_helper__,'__renderinclude__')"/>
    <func:result select="concat($__reusestamp,'.__renderinclude__')"/>
  </func:function>
  
  <func:function name="pfx:freeDOM">
    <func:result select="rfh:unregisterFrame($__register_frame_helper__,'__renderinclude__')"/>
  </func:function>
  
  <func:function name="pfx:escapeJS">
    <xsl:param name="text"/>
    <func:result select="callback:escapeJS($text)"/>
  </func:function>
  
  <func:function name="pfx:getBean">
    <xsl:param name="name"/>
    <func:result select="callback:getBean($name)"/>
  </func:function>
  
  <func:function name="pfx:getResource">
    <xsl:param name="nodeName"/>
    <func:result select="callback:getResource($__context__, $nodeName, /)"/>
  </func:function>
  
</xsl:stylesheet>
