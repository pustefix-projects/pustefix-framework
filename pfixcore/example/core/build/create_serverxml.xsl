<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:ext="xalan://de.schlund.pfixcore.util.XsltTransformer"
		>

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  
  <xsl:param name="setup"/>
  
  <xsl:variable name="debug">
    <xsl:apply-templates select="/projects/common/tomcat/debug/node()"/>
  </xsl:variable>

  <xsl:template match="projects">
    <xsl:variable name="adminport">
      <xsl:apply-templates select="/projects/common/tomcat/adminport/node()"/>
    </xsl:variable>
    <xsl:variable name="tomcat_defaulthost">
      <xsl:apply-templates select="/projects/common/tomcat/defaulthost/node()"/>
    </xsl:variable>
    <xsl:variable name="tomcat_jvmroute">
      <xsl:apply-templates select="/projects/common/tomcat/jvmroute/node()"/>
    </xsl:variable>
    <Server port="8005" shutdown="SHUTDOWN">
      <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      <xsl:if test="not(string($adminport) = '')"><xsl:attribute name="port"><xsl:value-of select="$adminport"/></xsl:attribute></xsl:if>
      
      <Service name="Tomcat-Standalone">

      <xsl:call-template name="create-connector"/>

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

  <xsl:template name="create-connector">
    <xsl:variable name="jkport">
      <xsl:apply-templates select="/projects/common/tomcat/connectorport/node()"/>
    </xsl:variable>
    <xsl:variable name="minprocessors">
      <xsl:apply-templates select="/projects/common/tomcat/minprocessors/node()"/>
    </xsl:variable>
    <xsl:variable name="maxprocessors">
      <xsl:apply-templates select="/projects/common/tomcat/maxprocessors/node()"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$setup = 'embedded'">
	    <Connector port="8009" enableLookups="false" acceptCount="100" minProcessors="5" maxProcessors="20" protocol="AJP/1.3">
          <xsl:if test="not(string($jkport) = '')"><xsl:attribute name="port"><xsl:value-of select="$jkport"/></xsl:attribute></xsl:if>
          <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
          <xsl:if test="not(string($minprocessors)='')"><xsl:attribute name="minProcessors"><xsl:value-of select="$minprocessors"/></xsl:attribute></xsl:if>
          <xsl:if test="not(string($maxprocessors)='')"><xsl:attribute name="maxProcessors"><xsl:value-of select="$maxprocessors"/></xsl:attribute></xsl:if>
	    </Connector>
      </xsl:when>
      <xsl:when test="$setup = 'standalone'">
        <Connector port="8080"
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" redirectPort="8443" acceptCount="100"
               connectionTimeout="20000"
               disableUploadTimeout="true">
          <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
          <xsl:if test="not(string($minprocessors)='')"><xsl:attribute name="minProcessors"><xsl:value-of select="$minprocessors"/></xsl:attribute></xsl:if>
          <xsl:if test="not(string($maxprocessors)='')"><xsl:attribute name="maxProcessors"><xsl:value-of select="$maxprocessors"/></xsl:attribute></xsl:if>
        </Connector>
        <Connector port="8443" 
               maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
               enableLookups="false" disableUploadTimeout="true"
               acceptCount="100" debug="0" scheme="https" secure="true"
               clientAuth="false" sslProtocol="TLS" keystorePass="changeit">
          <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
          <xsl:if test="not(string($minprocessors)='')"><xsl:attribute name="minProcessors"><xsl:value-of select="$minprocessors"/></xsl:attribute></xsl:if>
          <xsl:if test="not(string($maxprocessors)='')"><xsl:attribute name="maxProcessors"><xsl:value-of select="$maxprocessors"/></xsl:attribute></xsl:if>
        </Connector>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">unkown setup: <xsl:value-of select="$setup"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="project">
    <xsl:variable name="active">
    	<xsl:apply-templates select="active/node()"/>
    </xsl:variable>
    <xsl:if test="normalize-space($active) = 'true'">
 		<Host xmlValidation="true" unpackWARs="false" autoDeploy="false">
		  <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
		  <xsl:attribute name="name">
		    <xsl:apply-templates select="servername/node()"/>
		  </xsl:attribute>
		  <xsl:call-template name="create_tomcat_aliases">
			<xsl:with-param name="all_aliases"><xsl:apply-templates select="serveralias/node()"/></xsl:with-param>
		  </xsl:call-template>
		  <Valve className="org.apache.catalina.valves.AccessLogValve"
			 directory="logs" prefix="access_log." suffix=".txt" pattern="common"/>
		  
		  <Logger className="org.apache.catalina.logger.FileLogger"
			  directory="logs" prefix="log." suffix=".txt"	timestamp="true"/>
		  
		  <xsl:call-template name="create_context_list"/>
		</Host>
    </xsl:if>
  </xsl:template>

  <xsl:template name="create_context_list">
	<xsl:call-template name="create_context">
	  <xsl:with-param name="path">/xml</xsl:with-param>
	  <xsl:with-param name="docBase"><xsl:value-of select="$docroot"/>/servletconf/tomcat/webapps/<xsl:apply-templates select="@name"/></xsl:with-param>
	</xsl:call-template>
    <xsl:choose>
      <xsl:when test="$setup = 'embedded'">
        <!-- nothing but the above context -->
      </xsl:when>
      <xsl:when test="$setup = 'standalone'">
	    <xsl:call-template name="create_std_context_opt">
	      <xsl:with-param name="rel_path"><xsl:apply-templates select="@name"/>/img</xsl:with-param>
	    </xsl:call-template>
	    <xsl:call-template name="create_std_context">
	      <xsl:with-param name="rel_path">core/img</xsl:with-param>
	    </xsl:call-template>
	    <xsl:call-template name="create_std_context">
	      <xsl:with-param name="rel_path">core/editor/img</xsl:with-param>
	    </xsl:call-template>
	    <xsl:call-template name="create_std_context">
	      <xsl:with-param name="rel_path">common/img</xsl:with-param>
	    </xsl:call-template>
	    <xsl:call-template name="create_std_context">
	      <xsl:with-param name="rel_path">core/script</xsl:with-param>
	    </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message terminate="yes">unkown setup: <xsl:value-of select="$setup"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="create_std_context_opt">
    <xsl:param name="rel_path"/>
    <xsl:if test="ext:exists(concat($docroot, '/', $rel_path))">
      <xsl:call-template name="create_std_context">
	    <xsl:with-param name="rel_path"><xsl:value-of select="$rel_path"/></xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>

  <xsl:template name="create_std_context">
    <xsl:param name="rel_path"/>
    <xsl:call-template name="create_context">
	  <xsl:with-param name="path">/<xsl:value-of select="$rel_path"/></xsl:with-param>
	  <xsl:with-param name="docBase"><xsl:value-of select="$docroot"/>/<xsl:value-of select="$rel_path"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="create_context">
    <xsl:param name="path"/>
    <xsl:param name="docBase"/>
    <Context crossContext="true" cookies="false">
	    <xsl:attribute name="path"><xsl:value-of select="$path"/></xsl:attribute>
	    <xsl:attribute name="docBase"><xsl:value-of select="$docBase"/></xsl:attribute>
	    <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
    </Context>
  </xsl:template>
  
  <xsl:template name="create_tomcat_aliases">
    <xsl:param name="all_aliases"/>
    <xsl:variable name="alias_string" select="normalize-space($all_aliases)"/>
    <xsl:choose>
      <xsl:when test="not(contains($alias_string, ' '))">
        <Alias><xsl:value-of select="$alias_string"/></Alias>
      </xsl:when>
      <xsl:otherwise>
       <Alias><xsl:value-of select="substring-before($alias_string,' ')"/></Alias>
       <xsl:call-template name="create_tomcat_aliases">
         <xsl:with-param name="all_aliases" select="substring-after($alias_string, ' ')"/>
       </xsl:call-template> 
      </xsl:otherwise> 
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>



<!--
Local Variables:
mode: xml
End:
-->
