<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">
  
  <xsl:template match="overviewinfo">
    <ixsl:value-of select="/formresult/adultinfo/info/text()"/>
    <table width="500" class="boxed" bgcolor="#ffcccc">
      <tr>
        <td colspan="3">
          <b>TShirt:</b>
        </td>
      </tr>
      <tr valign="top">
        <td>Size: <ixsl:value-of select="/formresult/tshirt/@size"/></td>
        <td>ColorNo: <ixsl:value-of select="/formresult/tshirt/@color"/></td>
        <td>
          <ixsl:for-each select="/formresult/tshirt/feature">
            <ixsl:value-of select="./text()"/><br/>
          </ixsl:for-each>
        </td>
      </tr>
    </table>
    <ixsl:if test="/formresult/adultinfo[@adult = 'true']">
      <br/>
      <table width="500" class="boxed" bgcolor="#ccccff">
        <tr>
          <td colspan="3">
            <b>Trousers:</b>
          </td>
        </tr>
        <tr valign="top">
          <td>Size: <ixsl:value-of select="/formresult/trouser/@size"/></td>
          <td>ColorNo: <ixsl:value-of select="/formresult/trouser/@color"/></td>
          <td>
            <ixsl:for-each select="/formresult/trouser/feature">
              <ixsl:value-of select="./text()"/><br/>
            </ixsl:for-each>
          </td>
        </tr>
      </table>
    </ixsl:if>
  </xsl:template>

</xsl:stylesheet>
