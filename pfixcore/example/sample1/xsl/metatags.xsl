<?xml version="1.0" encoding="ISO-8859-1"?><xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:cus="http://www.schlund.de/pustefix/customize" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">
  
  <xsl:param name="page"/>
  <xsl:param name="navigation"><cus:navigation/></xsl:param>
  <xsl:param name="navitree" select="document(concat('file://',$navigation))/make/navigation"/>
  
  <xsl:param name="docroot"><cus:docroot/></xsl:param>
  <xsl:param name="lang"><cus:lang/></xsl:param>
  <xsl:param name="product"><cus:product/></xsl:param>
  
  <xsl:include href="core/xsl/include.xsl"/>
  <xsl:include href="core/xsl/utils.xsl"/>
  <xsl:include href="core/xsl/default_copy.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="overviewinfo">
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
