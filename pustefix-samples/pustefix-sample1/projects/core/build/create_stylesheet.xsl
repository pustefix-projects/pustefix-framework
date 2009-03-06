<xsl:stylesheet version="1.0"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:include="xalan://de.schlund.pfixxml.IncludeDocumentExtension"
                xmlns:runtime="xalan://de.schlund.pfixxml.DependencyTracker"
                xmlns:geometry="xalan://de.schlund.pfixxml.ImageGeometry">

  <xsl:output method="xml" encoding="UTF-8" indent="no"/>

  <xsl:param name="prohibitEdit"/>
  <xsl:param name="docroot"/>

 

  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>

  <xsl:template match="*|@*">
   <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates select="node()"/>
   </xsl:copy>
  </xsl:template>
  
  <xsl:template match="cus:documentation" />
  
</xsl:stylesheet>

<!-- 
Local Variables: 
mode: xml 
End: 
--> 
