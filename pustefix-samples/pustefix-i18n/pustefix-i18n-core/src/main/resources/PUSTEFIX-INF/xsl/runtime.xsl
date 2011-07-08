<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:variable name="envprops" select="java:getProperties()" xmlns:java="de.schlund.pfixxml.config.EnvironmentProperties"/>
  <xsl:variable name="fqdn" select="java:getProperty($envprops,'fqdn')" xmlns:java="java.util.Properties"/>
  
</xsl:stylesheet>
