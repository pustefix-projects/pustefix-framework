<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">

    <xsl:param name="page"/>

    <xsl:namespace-alias stylesheet-prefix="ixsl" result-prefix="xsl"/>
    <xsl:output method="xml" encoding="UTF-8" indent="yes"/>

    <xsl:template match="document">
      <ixsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

        <ixsl:param name="page"/>
        <ixsl:param name="__sessionIdPath"/>

        <ixsl:output method="html"/>
    
        <ixsl:template match="/">
          <html>
            <head>
              <title><xsl:value-of select="$page"/></title>
            </head>
            <body>
              <xsl:apply-templates/>
            </body>
          </html>
        </ixsl:template>

      </ixsl:stylesheet>
    
    </xsl:template>
    
    <xsl:template match="link">
      <a href="{@to}{{$__sessionIdPath}}"><xsl:apply-templates/></a>
    </xsl:template>
    
   <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
    

</xsl:stylesheet>