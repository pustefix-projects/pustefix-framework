<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:ext="xalan://de.schlund.pfixxml.util.XsltTest"
                version="1.1">

  <xsl:template match="import">
     <xsl:variable name="str"><![CDATA[<hello attr="foo"/>]]></xsl:variable>
     <xsl:variable name="doc" select="ext:toDocumentExtension(string($str))"/>
     <xsl:apply-templates select="$doc/node()"/>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:copy>
      <xsl:copy-of select="./@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
