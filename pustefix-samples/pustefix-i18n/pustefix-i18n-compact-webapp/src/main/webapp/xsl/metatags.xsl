<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet 
       xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
       xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" 
       xmlns:pfx="http://www.schlund.de/pustefix/core" 
       version="1.0">

  <xsl:variable name="envprops" select="java:getProperties()" xmlns:java="de.schlund.pfixxml.config.EnvironmentProperties"/>
  <xsl:variable name="fqdn" select="java:getProperty($envprops,'fqdn')" xmlns:java="java.util.Properties"/>
  
  <!-- example template showing how to attach error message to form field -->
  <xsl:template match="show-error">
    <pfx:checkfield name="{@field}">
      <pfx:error>
        <span class="error"><pfx:scode/></span>
      </pfx:error>
    </pfx:checkfield>
  </xsl:template>
  
</xsl:stylesheet>
