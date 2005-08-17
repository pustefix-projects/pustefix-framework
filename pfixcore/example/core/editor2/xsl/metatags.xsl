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

  <xsl:template match="displayauxfiles_of_target">
    <ixsl:if test="/formresult/currenttargetinfo/auxfileinfo/auxfile">
      <table><tr><td class="editor_main_emph">Additional dependencies:</td></tr></table>
      <table width="100%" class="editor_box">
        <ixsl:for-each select="/formresult/currenttargetinfo/auxfileinfo/auxfile">
          <ixsl:variable name="class">
            <ixsl:choose>
              <ixsl:when test="(number(@count) mod 2) = 0">editor_even_row</ixsl:when>
              <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
            </ixsl:choose>
          </ixsl:variable>
          <tr>
            <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
            <td nowrap="nowrap">
              <ixsl:value-of select="@path"/>
            </td>
          </tr>
        </ixsl:for-each>
      </table>
      <br/>
    </ixsl:if>
  </xsl:template>

</xsl:stylesheet>


