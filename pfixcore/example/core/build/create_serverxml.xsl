<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  
  <xsl:template match="projects">
    <xsl:param name="tomcat_defaulthost">
      <xsl:apply-templates select="/projects/common/tomcat/defaulthost/node()"/>
    </xsl:param>
    <xsl:param name="tomcat_jvmroute">
      <xsl:apply-templates select="/projects/common/tomcat/jvmroute/node()"/>
    </xsl:param>
    <xsl:param name="debug">
      <xsl:apply-templates select="/projects/common/tomcat/debug/node()"/>
    </xsl:param>
    
    <Server port="8005" shutdown="SHUTDOWN">
      <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      
      <Service name="Tomcat-Standalone">
	
	<Connector className="org.apache.ajp.tomcat4.Ajp13Connector" enableLookups="false" port="8009"
		   minProcessors="200" maxProcessors="750" acceptCount="100">
	  <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
	</Connector>
	<Engine name="Standalone">
	  <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
	  <xsl:attribute name="defaultHost">
	    <xsl:value-of select="normalize-space($tomcat_defaulthost)"/>
	  </xsl:attribute>
	  <xsl:attribute name="jvmRoute">
	    <xsl:value-of select="normalize-space($tomcat_jvmroute)"/>
	  </xsl:attribute>
	  
	  <Logger className="org.apache.catalina.logger.FileLogger" prefix="catalina_log." suffix=".txt" timestamp="true"/>
	  
	  <xsl:apply-templates select="/projects/project"/>

	</Engine>
	
      </Service>

    </Server>
    
    
  </xsl:template>

  <xsl:template match="project">
    <xsl:param name="debug">
      <xsl:apply-templates select="/projects/common/tomcat/debug/node()"/>
    </xsl:param>
    
    <Host>
      <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      <xsl:attribute name="name">
	<xsl:apply-templates select="servername/node()"/>
      </xsl:attribute>
      <Alias><xsl:apply-templates select="serveralias/node()"/></Alias>
      <Valve className="org.apache.catalina.valves.AccessLogValve"
	     directory="logs" prefix="access_log." suffix=".txt" pattern="common"/>
      
      <Logger className="org.apache.catalina.logger.FileLogger"
	      directory="logs" prefix="log." suffix=".txt"	timestamp="true"/>
      
      <Context path="/xml" debug="20" crossContext="true" cookies="false">
	<xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
	<xsl:attribute name="docBase">
	  <xsl:value-of select="$docroot"/>/servletconf/tomcat/webapps/<xsl:apply-templates select="@name"/>
	</xsl:attribute>
      </Context>
    </Host>
      
  </xsl:template>
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
