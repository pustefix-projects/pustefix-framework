<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:rex="java:de.schlund.pfixxml.RenderExtensionSaxon1"
                exclude-result-prefixes="rex">

  <xsl:param name="__rendercontext__"/>

  <xsl:param name="render_href"/>
  <xsl:param name="render_part"/>
  <xsl:param name="render_module"/>
  <xsl:param name="render_ctype"/>

  <xsl:template name="__render_start__">
    <xsl:if test="rex:renderStart($__rendercontext__)"/>
  </xsl:template>

  <xsl:template match="pfx:rendercontent">
    <xsl:choose>
      <xsl:when test="$render_ctype='text/javascript'">
        <pfx:compress type="javascript">
          <xsl:call-template name="pfx:include">
            <xsl:with-param name="href"><xsl:value-of select="$render_href"/></xsl:with-param>
            <xsl:with-param name="part"><xsl:value-of select="$render_part"/></xsl:with-param>
            <xsl:with-param name="module"><xsl:value-of select="$render_module"/></xsl:with-param>
            <xsl:with-param name="noedit">true</xsl:with-param>
          </xsl:call-template>
        </pfx:compress>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="pfx:include">
          <xsl:with-param name="href"><xsl:value-of select="$render_href"/></xsl:with-param>
          <xsl:with-param name="part"><xsl:value-of select="$render_part"/></xsl:with-param>
          <xsl:with-param name="module"><xsl:value-of select="$render_module"/></xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="pfx:render">
    <xsl:variable name="systemId" select="rex:getSystemId()"/>
    <ixsl:call-template name="pfx:render">
      <ixsl:with-param name="href">
        <xsl:choose>
          <xsl:when test="@href"><xsl:value-of select="@href"/></xsl:when>
          <xsl:when test="pfx:href"><xsl:apply-templates select="pfx:href/node()"/></xsl:when>
          <xsl:otherwise>
            <xsl:variable name="uri">
              <xsl:choose>
                <xsl:when test="contains($systemId,':')"><xsl:value-of select="substring-after($systemId,':')"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="$systemId"/></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:choose>
              <xsl:when test="starts-with($uri,'//')"><xsl:value-of select="substring-after(substring-after($uri,'//'),'/')"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="$uri"/></xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <ixsl:with-param name="part">
        <xsl:choose>
          <xsl:when test="@part"><xsl:value-of select="@part"/></xsl:when>
          <xsl:when test="pfx:part"><xsl:apply-templates select="pfx:part/node()"/></xsl:when>
          <xsl:otherwise><xsl:message>ERROR: Render include requires part specification.</xsl:message></xsl:otherwise>
        </xsl:choose>
      </ixsl:with-param>
      <xsl:if test="@module or pfx:module or starts-with($systemId,'module://')">
        <ixsl:with-param name="module">
          <xsl:choose>
            <xsl:when test="@module"><xsl:value-of select="@module"/></xsl:when>
            <xsl:when test="pfx:module"><xsl:apply-templates select="pfx:module/node()"/></xsl:when>
            <xsl:when test="starts-with($systemId,'module://')"><xsl:value-of select="substring-before(substring-after($systemId,'module://'),'/')"/></xsl:when>
          </xsl:choose>
        </ixsl:with-param>
      </xsl:if>
      <xsl:if test="@search">
        <ixsl:with-param name="search"><xsl:value-of select="@search"/></ixsl:with-param>
      </xsl:if>
    </ixsl:call-template>
  </xsl:template>
  
  <xsl:template name="pfx:render">
    <xsl:param name="href"/>
    <xsl:param name="part"/>
    <xsl:param name="module"/>
    <xsl:param name="search"/>
    <xsl:variable name="result" select="rex:render($__target_gen, $href, $part, $module, $search, ., $__context__, $__rendercontext__)"/>
    <xsl:if test="not($result)">
      <xsl:variable name="text">Missing render include: '<xsl:value-of select="$part"/>' in resource '<xsl:value-of select="$href"/>'
        <xsl:if test="$search='dynamic'"> dynamically searched </xsl:if><xsl:if test="$module">from '<xsl:value-of select="$module"/>'</xsl:if>
      </xsl:variable>
      <img src="{$__contextpath}/modules/pustefix-core/img/warning.gif" alt="{$text}" title="{$text}"/>
      <xsl:message>*** Render include not found:
        href = <xsl:value-of select="$href"/>
        module = <xsl:value-of select="$module"/>
        search = <xsl:value-of select="$search"/> 
        part = <xsl:value-of select="$part"/> ***</xsl:message>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
