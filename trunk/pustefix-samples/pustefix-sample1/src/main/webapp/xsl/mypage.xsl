<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:param name="page"/>
    <xsl:param name="__sessionIdPath"/>

    <xsl:output method="html"/>
    
    <xsl:template match="/">
      <html>
        <head>
          <title><xsl:value-of select="$page"/></title>
        </head>
        <body>
          <h1>mypage</h1>
          <a href="/{$__sessionIdPath}">Home</a>
        </body>
      </html>
    </xsl:template>

</xsl:stylesheet>