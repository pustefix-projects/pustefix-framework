<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:template match="projects">
    <xsl:param name="jvm_opts">
      <xsl:apply-templates select="/projects/common/jserv/jvmopts"/>
    </xsl:param>

<xsl:if test="not(normalize-space($jvm_opts) = '')">
wrapper.bin.parameters=<xsl:value-of select="normalize-space($jvm_opts)"/>
</xsl:if>
wrapper.class=org.apache.jserv.JServ
wrapper.protocol=ajpv12
bindaddress=<xsl:value-of select="$fqdn"/>
port=8007
security.selfservlet=true
security.maxConnections=50
security.allowedAddresses=<xsl:value-of select="$fqdn"/>
security.authentication=false
log=true
log.file=<xsl:value-of select="$docroot"/>/servletconf/jserv/logs/jserv.log
log.timestamp=true
log.dateFormat=[dd/MM/yyyy HH:mm:ss:SSS zz]
log.channel.servletException=true
log.channel.jservException=true
log.channel.warning=true
log.channel.servletLog=true
log.channel.critical=true
zones=<xsl:call-template name="project_list"/><xsl:text>
</xsl:text>
<xsl:call-template name="project_props"/>
  </xsl:template>

  <xsl:template name="project_list">
    <xsl:for-each select="/projects/project">
      <xsl:value-of select="@name"/>
      <xsl:if test="following-sibling::project">,</xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="project_props">
    <xsl:for-each select="/projects/project">
      <xsl:value-of select="@name"/>.properties=<xsl:value-of select="$docroot"/>/servletconf/jserv/<xsl:value-of select="@name"/>.prop
    </xsl:for-each>
  </xsl:template>
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
