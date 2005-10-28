<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:template match="staticdisplay" name="staticname">
    <ixsl:if>
      <xsl:attribute name="test"><xsl:value-of select="@path"/></xsl:attribute>
      <table width="100%" style="background: #ffffff; border-style: ridge; border-width: 2px;">
        <tr>
          <td><ixsl:apply-templates mode="static_disp">
              <xsl:attribute name="select"><xsl:value-of select="@path"/></xsl:attribute>
            </ixsl:apply-templates>
          </td>
        </tr>
      </table>
    </ixsl:if>
  </xsl:template>

</xsl:stylesheet>


