<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
       xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
       xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" 
       xmlns:pfx="http://www.schlund.de/pustefix/core" 
       version="1.0">

  <xsl:template match="show-error">
    <pfx:checkfield name="{@field}">
      <pfx:error>
        <span class="error"><pfx:scode/></span>
      </pfx:error>
    </pfx:checkfield>
  </xsl:template>
  
  <xsl:template match="show-messages">
    <pfx:checkmessage>
      <pfx:messageloop>
        <div>
          <xsl:attribute name="class">{$pfx_class}</xsl:attribute>
          <pfx:scode/>
        </div>
      </pfx:messageloop>
    </pfx:checkmessage>
  </xsl:template>

</xsl:stylesheet>