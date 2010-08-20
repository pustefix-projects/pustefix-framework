<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:rex="java:de.schlund.pfixxml.RenderExtensionSaxon1"
                exclude-result-prefixes="rex">

  <xsl:param name="__rendercontext__"/>

  <xsl:param name="component_href"/>
  <xsl:param name="component_part"/>
  <xsl:param name="component_module"/>
  <xsl:param name="component_search"/>

  <xsl:template name="__render_start__">
    <xsl:if test="rex:renderStart($__rendercontext__)"/>
  </xsl:template>

  <xsl:template match="pfx:component">
    <xsl:call-template name="pfx:include">
      <xsl:with-param name="href"><xsl:value-of select="$component_href"/></xsl:with-param>
      <xsl:with-param name="part"><xsl:value-of select="$component_part"/></xsl:with-param>
      <xsl:with-param name="module"><xsl:value-of select="$component_module"/></xsl:with-param>
      <xsl:with-param name="search"><xsl:value-of select="$component_search"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:render">
    <ixsl:call-template name="pfx:render">
      <ixsl:with-param name="href"><xsl:value-of select="@href"/></ixsl:with-param>
      <ixsl:with-param name="part"><xsl:value-of select="@part"/></ixsl:with-param>
      <ixsl:with-param name="module"><xsl:value-of select="@module"/></ixsl:with-param>
      <ixsl:with-param name="search"><xsl:value-of select="@search"/></ixsl:with-param>
    </ixsl:call-template>
  </xsl:template>
  
  <xsl:template name="pfx:render">
    <xsl:param name="href"/>
    <xsl:param name="part"/>
    <xsl:param name="module"/>
    <xsl:param name="search"/>
    <xsl:param name="mode">output</xsl:param>
    <xsl:choose>
      <xsl:when test="$mode='output'">
        <xsl:if test="rex:render($__target_gen, $href, $part, $module, $search, node(), $__context__, $__rendercontext__, true())"/>
      </xsl:when>
      <xsl:when test="$mode='copy'">
        <xsl:copy-of select="rex:render($__target_gen, $href, $part, $module, $search, node(), $__context__, $__rendercontext__, false())"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
