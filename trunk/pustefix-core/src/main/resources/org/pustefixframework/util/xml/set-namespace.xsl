<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:param name="namespace"/>
  <xsl:param name="schema"/>
  
  <xsl:template match="*">
    <xsl:element name="{local-name()}" namespace="{$namespace}">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="/*">
    <xsl:element name="{local-name()}" namespace="{$namespace}" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <xsl:copy-of select="@*[not(name()='xsi:schemaLocation')]"/>
      <xsl:if test="$schema and @xsi:schemaLocation">
        <xsl:attribute name="xsi:schemaLocation"><xsl:value-of select="$namespace"/><xsl:text> </xsl:text> <xsl:value-of select="$schema"/></xsl:attribute>
      </xsl:if>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="comment()">
    <xsl:copy/>
  </xsl:template>

</xsl:stylesheet>
