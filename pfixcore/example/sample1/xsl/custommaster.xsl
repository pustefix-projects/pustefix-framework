<xsl:stylesheet version="1.0"
                exclude-result-prefixes="xsl cus" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:output method="xml" encoding="ISO-8859-1" indent="no"/>

  <xsl:param name="docroot"/>
  <xsl:param name="product"/>
  <xsl:param name="lang"/>
  <xsl:param name="__target_gen"/>
  
  <!-- ******************************************
  Customize below
  ******************************************-->
  
  <xsl:template match="cus:custom_xsl">
    <!-- Add all the stuff you want at that place in the customized master sheet -->
  </xsl:template>
   
  <xsl:template match="cus:custom_ixsl">
    <!-- Add all the stuff you want at that place in the customized master sheet -->
    <!-- Are you really sure you want to add anything here? -->
  </xsl:template>

  <xsl:template match="cus:final-output-method">
    <ixsl:output encoding="iso-8859-1" indent="no">
      <xsl:copy-of select="./node()"/>
    </ixsl:output>
  </xsl:template>
  

  <!-- No customizeable parts below -->
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="xsl:include | ixsl:include">
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
mode: xml
End:
-->