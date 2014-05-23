<?xml version="1.0" encoding="UTF-8"?>
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

  <xsl:template match="errmsg">
    <xsl:param name="cols">
      <xsl:choose>
        <xsl:when test="@cols"><xsl:value-of select="@cols"/></xsl:when>
        <xsl:otherwise>2</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <pfx:checkfield>
      <pfx:name>
        <xsl:choose>
          <xsl:when test="@name"><xsl:value-of select="@name"/></xsl:when>
          <xsl:otherwise><xsl:apply-templates select="./name/node()"/></xsl:otherwise>
        </xsl:choose>
      </pfx:name>
      <pfx:error>
        <tr>
          <td colspan="{$cols}" class="PfxError" bgcolor="#ffffff">
            <i><pfx:scode/></i>
          </td>
        </tr>
      </pfx:error>
    </pfx:checkfield>
  </xsl:template>

  
</xsl:stylesheet>


