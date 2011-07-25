<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.1"
                exclude-result-prefixes="xsl cus gen" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:gen="xalan://de.schlund.pfixxml.targets.TargetGenerator"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:output method="xml" encoding="UTF-8" indent="no"/>
  
  <xsl:param name="product"/>
  <xsl:param name="lang"/>
  <xsl:param name="__target_gen"/>
  
  <xsl:param name="stylesheets_to_include"/>
  <xsl:param name="exclude_result_prefixes"/>
  
  <xsl:param name="config_document" select="gen:getConfigDocument($__target_gen)"/>

  <xsl:template match="cus:custom_xsl">
    <xsl:call-template name="gen_xsl_import">
      <xsl:with-param name="ssheets"><xsl:value-of select="normalize-space($stylesheets_to_include)"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="gen_xsl_import">
    <xsl:param name="ssheets"/>
    <xsl:variable name="first">
      <xsl:value-of select="normalize-space(substring-before(concat($ssheets, ' '), ' '))"/>
    </xsl:variable>
    <xsl:variable name="rest">
      <xsl:value-of select="normalize-space(substring-after($ssheets, ' '))"/>
    </xsl:variable>
    <xsl:if test="$first != ''">
      <xsl:element name="xsl:import">
        <xsl:attribute name="href"><xsl:value-of select="$first"/></xsl:attribute>
        </xsl:element><xsl:text>
      </xsl:text>
    </xsl:if>
    <xsl:if test="$rest != ''">
      <xsl:call-template name="gen_xsl_import">
        <xsl:with-param name="ssheets">
          <xsl:value-of select="$rest"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="/xsl:stylesheet/xsl:template[@match='/']/ixsl:stylesheet">
    <xsl:variable name="complete_exclude_result_prefixes" xmlns:prj="http://www.pustefix-framework.org/2008/namespace/project-config">
      <xsl:for-each select="$config_document/make/namespaces/namespace-declaration[@exclude-result-prefix='true']">
        <xsl:value-of select="@prefix"/>
        <xsl:text>:</xsl:text>
        <xsl:value-of select="@url"/>
        <xsl:text> </xsl:text>
      </xsl:for-each>
      <xsl:value-of select="$exclude_result_prefixes"/>
    </xsl:variable>
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:attribute name="exclude-result-prefixes">
        <xsl:value-of select="./@exclude-result-prefixes"/>
        <xsl:if test="normalize-space($complete_exclude_result_prefixes) != ''">
          <xsl:text> </xsl:text>
          <xsl:call-template name="gen_namespace_prefixes">
            <xsl:with-param name="list"><xsl:value-of select="normalize-space($complete_exclude_result_prefixes)"/></xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:attribute>
      <xsl:if test="normalize-space($complete_exclude_result_prefixes) != ''">
        <xsl:call-template name="gen_dummy_attributes">
          <xsl:with-param name="list"><xsl:value-of select="normalize-space($complete_exclude_result_prefixes)"/></xsl:with-param>
        </xsl:call-template>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>


  <xsl:template name="gen_dummy_attributes">
    <xsl:param name="list"/>
    <xsl:variable name="first">
      <xsl:value-of select="normalize-space(substring-before(concat($list, ' '), ' '))"/>
    </xsl:variable>
    <xsl:variable name="rest">
      <xsl:value-of select="normalize-space(substring-after($list, ' '))"/>
    </xsl:variable>
    <xsl:variable name="prefix"><xsl:value-of select="substring-before($first, ':')"/></xsl:variable>
    <xsl:variable name="namespace"><xsl:value-of select="substring-after($first, ':')"/></xsl:variable>
    <xsl:attribute name="{$prefix}:dummyattribute" namespace="{$namespace}"></xsl:attribute>
    <xsl:if test="$rest != ''">
      <xsl:call-template name="gen_dummy_attributes">
        <xsl:with-param name="list">
          <xsl:value-of select="$rest"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="gen_namespace_prefixes">
    <xsl:param name="list"/>
    <xsl:variable name="first">
      <xsl:value-of select="normalize-space(substring-before(concat($list, ' '), ' '))"/>
    </xsl:variable>
    <xsl:variable name="rest">
      <xsl:value-of select="normalize-space(substring-after($list, ' '))"/>
    </xsl:variable>
    <xsl:variable name="prefix"><xsl:value-of select="substring-before($first, ':')"/></xsl:variable>
    <xsl:value-of select="$prefix"/><xsl:text> </xsl:text>
    <xsl:if test="$rest != ''">
      <xsl:call-template name="gen_namespace_prefixes">
        <xsl:with-param name="list">
          <xsl:value-of select="$rest"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template match="cus:product">
    <xsl:value-of select="$product"/>
  </xsl:template>

  <xsl:template match="cus:lang">
    <xsl:value-of select="$lang"/>
  </xsl:template>


</xsl:stylesheet>

<!--
Local Variables:
mode: xsl
End:
-->
