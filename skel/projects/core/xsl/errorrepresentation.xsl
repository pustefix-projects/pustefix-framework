<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template match="/">
    <html>
      <head>
        <title>Error</title>
      </head>
      <body>
        <br/><br/>
        <div align="center">
          <table cellpadding="2" cellspacing="0" style="background-color: #aaaacc; border: 1px solid black">
            <tr>
              <td align="center" bgcolor="#cc0000" colspan="2">
                <span style="color:#ffffff; font-weight: bold">XML/XSLT Error!</span>
              </td>
            </tr>
            <xsl:apply-templates/>
          </table>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="error">
    <tr>
      <td bgcolor="#dd9999" colspan="2" align="center">
        <b><xsl:value-of select="@type"/></b>
      </td>
    </tr>
    <xsl:apply-templates/>
  </xsl:template>

  
  <xsl:template match="info">
    <tr>
      <xsl:attribute name="bgcolor">
        <xsl:choose>
          <xsl:when test="count(preceding-sibling::info) mod 2 = 0">#aaaacc</xsl:when>
          <xsl:otherwise>#ccccee</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <td><b><xsl:value-of select="@key"/>:</b></td>
      <td><xsl:value-of select="@value"/></td>
    </tr>
  </xsl:template>
  
</xsl:stylesheet>