<xsl:stylesheet version="1.0"
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
  
  <xsl:param name="skinning_stylesheets"/>
  <xsl:param name="runtime_stylesheets"/>

  <xsl:param name="outputmethod">html</xsl:param>
  <xsl:param name="outputencoding">iso-8859-1</xsl:param>
  <xsl:param name="outputdoctype-public"/>
  <xsl:param name="outputdoctype-system"/>

  <xsl:template match="cus:final-output-method">
    <ixsl:output indent="no">
      <xsl:attribute name="method">
        <xsl:value-of select="$outputmethod"/>
      </xsl:attribute>
      <xsl:attribute name="encoding">
        <xsl:value-of select="$outputencoding"/>
      </xsl:attribute>
      <xsl:if test="not($outputdoctype-system = '')">
        <xsl:attribute name="doctype-system">
          <xsl:value-of select="$outputdoctype-system"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="not($outputdoctype-public = '')">
        <xsl:attribute name="doctype-public">
          <xsl:value-of select="$outputdoctype-public"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:copy-of select="./node()"/>
    </ixsl:output>
  </xsl:template>
  
  <xsl:template match="cus:custom_xsl">
    <xsl:call-template name="gen_xsl_include">
      <xsl:with-param name="ns">xsl</xsl:with-param>
      <xsl:with-param name="ssheets"><xsl:value-of select="normalize-space($skinning_stylesheets)"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="cus:custom_ixsl">
    <xsl:call-template name="gen_xsl_include">
      <xsl:with-param name="ns">ixsl</xsl:with-param>
      <xsl:with-param name="ssheets"><xsl:value-of select="normalize-space($runtime_stylesheets)"/></xsl:with-param>
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
mode: xsl
End:
-->
