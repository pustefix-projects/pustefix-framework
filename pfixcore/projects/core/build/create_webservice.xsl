<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:ws="http://pustefix.sourceforge.net/wsconfig200401">

  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>

  <xsl:param name="docroot"/>
  <xsl:param name="uid"/>
  <xsl:param name="machine"/>
  <xsl:param name="fqdn"/>
  <xsl:param name="mode"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="text()"></xsl:template>

  <xsl:template match="ws:webservice-config">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="ws:webservice-global">
    <xsl:apply-templates>
      <xsl:with-param name="wspref">webservice-global</xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="ws:webservice">
    <xsl:value-of select="concat('webservice.',@name,'.name=',@name)"/><xsl:text>&#xa;</xsl:text>
    <xsl:apply-templates>
		<xsl:with-param name="wspref" select="concat('webservice.',@name)"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*">
    <xsl:param name="wspref"/>
	<xsl:variable name="pref" select="concat($wspref,'.',name())"/>
    <xsl:if test="not(normalize-space(text())='')">
      <xsl:value-of select="concat($pref,'=',normalize-space(text()))"/><xsl:text>&#xa;</xsl:text>
    </xsl:if>
    <xsl:for-each select="@*">
      <xsl:value-of select="concat($pref,'.',name(),'=',normalize-space(.))"/><xsl:text>&#xa;</xsl:text>
    </xsl:for-each>
    <xsl:apply-templates>
      <xsl:with-param name="wspref" select="$pref"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="ws:param">
    <xsl:param name="wspref"/>
    <xsl:variable name="pref" select="concat($wspref,'.',name(),'.',@name)"/>
    <xsl:value-of select="concat($wspref,'.',name(),'.',@name,'=',@value)"/><xsl:text>&#xa;</xsl:text>
  </xsl:template>

  <xsl:template match="ws:choose">
	<xsl:param name="wspref"/>
	<xsl:apply-templates>
		<xsl:with-param name="wspref" select="$wspref"/>
	</xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="ws:test">
    <xsl:param name="wspref"/>
	<xsl:if test="(@mode and @mode=$mode) or (not(@mode) and not(ancestor::ws:choose/ws:test[@mode=$mode]))">
      <xsl:apply-templates>
        <xsl:with-param name="wspref" select="$wspref"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
