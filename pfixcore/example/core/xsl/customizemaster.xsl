<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                exclude-result-prefixes="xsl cus" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:param name="docroot"/>
  <xsl:param name="product"/>
  <xsl:param name="lang"/>
  <xsl:param name="__target_gen"/>
  
  <xsl:param name="stylesheets_to_include"/>

  <xsl:template match="cus:custom_xsl">
    <xsl:call-template name="gen_xsl_include">
      <xsl:with-param name="ns">xsl</xsl:with-param>
      <xsl:with-param name="ssheets"><xsl:value-of select="normalize-space($stylesheets_to_include)"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="gen_xsl_include">
    <xsl:param name="ns"/>
    <xsl:param name="ssheets"/>
    <xsl:variable name="first">
      <xsl:value-of select="normalize-space(substring-before(concat($ssheets, ' '), ' '))"/>
    </xsl:variable>
    <xsl:variable name="rest">
      <xsl:value-of select="normalize-space(substring-after($ssheets, ' '))"/>
    </xsl:variable>
    <xsl:if test="$first != ''">
      <xsl:choose>
        <xsl:when test="$ns = 'xsl'">
          <xsl:element name="xsl:include">
            <xsl:attribute name="href"><xsl:value-of select="concat('file://',$docroot,'/',$first)"/></xsl:attribute>
          </xsl:element><xsl:text>
        </xsl:text>
        </xsl:when>
        <xsl:when test="$ns = 'ixsl'">
          <xsl:element name="ixsl:include">
            <xsl:attribute name="href"><xsl:value-of select="concat('file://',$docroot,'/',$first)"/></xsl:attribute>
          </xsl:element><xsl:text>
        </xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:message terminate="yes">
            **** Unknown Namespace: <xsl:value-of select="$ns"/>
          </xsl:message>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:if test="$rest != ''">
      <xsl:call-template name="gen_xsl_include">
        <xsl:with-param name="ns">
          <xsl:value-of select="$ns"/>
        </xsl:with-param>
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

  <xsl:template match="xsl:include | ixsl:include | xsl:import | ixsl:import">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:attribute name="href"><xsl:value-of select="concat('file://',$docroot,'/',@href)"/></xsl:attribute>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="cus:navigation">
    <xsl:value-of select="$__target_gen"/>
  </xsl:template>

  <xsl:template match="cus:docroot">
    <xsl:value-of select="$docroot"/>
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
