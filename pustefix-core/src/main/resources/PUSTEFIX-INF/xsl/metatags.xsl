<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <xsl:import href="module://pustefix-core/xsl/default_copy.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/include.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/utils.xsl"/>
  <xsl:import href="module://pustefix-core/xsl/render.xsl"/>

  <cus:custom_xsl/>

  <xsl:param name="page"/>
  <xsl:param name="__navitree"/>
  <xsl:param name="navitree" select="$__navitree"/>
  
  <!--
    Define __contextpath despite it's only evaluated/needed at runtime, cause Saxon2
    doesn't allow the usage of undefined variables/params, even if they aren't evaluated
  -->
  <xsl:param name="__contextpath">$__contextpath</xsl:param>
  
  <xsl:param name="lang"><cus:lang/></xsl:param>
  <xsl:variable name="product"><cus:product/></xsl:variable>
  
</xsl:stylesheet>
