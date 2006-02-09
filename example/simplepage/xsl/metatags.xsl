<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">
<!-- 
	Here you can say how the page should look like. The relation is declared
	in the depend.xml.in
 -->

  <xsl:template match="quadtext">
    <table>
      <tr>
        <td style="background-color: #ffaaaa;"><basictext><xsl:apply-templates/></basictext></td>
        <td><basictext><xsl:apply-templates/></basictext></td>
      </tr>
      <tr>
        <td><basictext><xsl:apply-templates/></basictext></td>
        <td style="background-color: #ffaaaa;"><basictext><xsl:apply-templates/></basictext></td>
      </tr>
    </table>
  </xsl:template>


</xsl:stylesheet>

