<xsl:stylesheet version="1.0"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
<!--  <xsl:include href="create_lib.xsl"/> -->

  <xsl:param name="prohibitEdit"/>
  <xsl:param name="docroot"/>


  <xsl:template match="/">
    <stylesheetdoc>
      <xsl:apply-templates/>
    </stylesheetdoc>
  </xsl:template>
 
 <xsl:template match="cus:documentation">
    <xsl:variable name="templatenode" select="ancestor::xsl:template[position() = 1]"/>
    <xsl:variable name="templatetype">
      <xsl:choose>
        <xsl:when test="string($templatenode/@match) = ''">PRIVATE</xsl:when>
        <xsl:otherwise>PUBLIC</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <template_doc type="{$templatetype}">          
      <xsl:attribute name="name">
        <xsl:choose>
          <xsl:when test="$templatetype = 'PRIVATE'">
            <xsl:value-of select="$templatenode/@name"/>_<xsl:value-of select="$templatetype"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$templatenode/@match"/>_<xsl:value-of select="$templatetype"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <!--              <xsl:message>Matched doc node</xsl:message> -->
      <xsl:apply-templates mode="doc"/>
      <!-- <xsl:copy-of select="./node()"/>-->
    </template_doc>
  </xsl:template>

  <xsl:template match="*" mode="doc">
    <xsl:element name="{name()}" namespace="{namespace-uri()}"> 
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates mode="doc"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="text()"/>
  
  
</xsl:stylesheet>

<!-- 
Local Variables: 
mode: xml 
End: 
--> 
