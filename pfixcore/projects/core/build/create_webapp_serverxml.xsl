<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ext="xalan://de.schlund.pfixcore.util.XsltTransformer"
                >

  <xsl:param name="standalone">true</xsl:param>
  <xsl:param name="portbase"/>
  <xsl:param name="trusted"/>
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  
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
    <Server shutdown="SHUTDOWN">
      <xsl:attribute name="port">
        <xsl:choose>
          <xsl:when test="string($adminport) = ''">
            <xsl:value-of select="$portbase+5"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$adminport"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      
      <Listener className="org.apache.catalina.mbeans.ServerLifecycleListener" debug="0"/>
      <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener" debug="0"/>
      <!--
      <GlobalNamingResources>
        <Resource name="UserDatabase" auth="Container" type="org.apache.catalina.UserDatabase"
                  description="User database that can be updated and saved">
        </Resource>
        <ResourceParams name="UserDatabase">
          <parameter>
            <name>factory</name>
            <value>org.apache.catalina.users.MemoryUserDatabaseFactory</value>
          </parameter>
          <parameter>
            <name>pathname</name>
            <value>conf/tomcat-users.xml</value>
          </parameter>
        </ResourceParams>
      </GlobalNamingResources>
      -->
      <Service name="Catalina">
        <xsl:call-template name="create-connector"/>
        <Engine name="Catalina">
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
    <Connector enableLookups="false" acceptCount="100" maxThreads="150" minSpareThreads="25" maxSpareThreads="75" useBodyEncodingForURI="true" protocol="AJP/1.3">		
      <xsl:attribute name="port">
        <xsl:choose>
          <xsl:when test="string($jkport) = ''">
            <xsl:value-of select="$portbase+9"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$jkport"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      
      <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      <xsl:if test="not(string($minprocessors)='')"><xsl:attribute name="minSpareThreads"><xsl:value-of select="$minprocessors"/></xsl:attribute></xsl:if>
      <xsl:if test="not(string($minprocessors)='')"><xsl:attribute name="maxSpareThreads"><xsl:value-of select="$minprocessors"/></xsl:attribute></xsl:if>
      <xsl:if test="not(string($maxprocessors)='')"><xsl:attribute name="maxThreads"><xsl:value-of select="$maxprocessors"/></xsl:attribute></xsl:if>
    </Connector>
    <xsl:if test="$standalone = 'true'">
      <Connector maxThreads="150" minSpareThreads="25" maxSpareThreads="75" useBodyEncodingForURI="true"
                 enableLookups="false" acceptCount="100"
                 connectionTimeout="20000"
                 disableUploadTimeout="true">
        <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
        <xsl:attribute name="port"><xsl:value-of select="$portbase+80"/></xsl:attribute>
        <xsl:attribute name="redirectport"><xsl:value-of select="$portbase+443"/></xsl:attribute>
      </Connector>
      <Connector maxThreads="150" minSpareThreads="25" maxSpareThreads="75" useBodyEncodingForURI="true"
                 enableLookups="false" disableUploadTimeout="true"
                 acceptCount="100" debug="0" scheme="https" secure="true"
                 clientAuth="false" sslProtocol="TLS" keystoreFile="conf/keystore" keystorePass="secret">
        <xsl:attribute name="port"><xsl:value-of select="$portbase+443"/></xsl:attribute>
        <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      </Connector>
    </xsl:if>
  </xsl:template>

  <xsl:template match="project">
    <xsl:variable name="active">
      <xsl:apply-templates select="active/node()"/>
    </xsl:variable>
    <xsl:if test="normalize-space($active) = 'true'">
      <xsl:variable name="virtual-base"><xsl:value-of select="$docroot"/>/<xsl:value-of select="@name"/></xsl:variable>  
      <xsl:if test="ext:exists($virtual-base)">
	      <Host xmlValidation="true" unpackWARs="false" autoDeploy="false">
	        <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
	        <xsl:attribute name="name">
	          <xsl:apply-templates select="servername/node()"/>
	        </xsl:attribute>
	        <xsl:attribute name="appBase"><xsl:value-of select="$virtual-base"/></xsl:attribute>
	        <xsl:call-template name="create_tomcat_aliases">
	          <xsl:with-param name="all_aliases"><xsl:apply-templates select="serveralias/node()"/></xsl:with-param>
	        </xsl:call-template>
	        <Valve className="org.apache.catalina.valves.AccessLogValve"
	               directory="logs" prefix="access_log." suffix=".txt" pattern="%h %l %u %t &quot;%r&quot; %s %b %D"/>
	        
	        <Logger className="org.apache.catalina.logger.FileLogger"
	                directory="logs" prefix="log." suffix=".txt"	timestamp="true"/>
	      </Host>
	      </xsl:if>
    </xsl:if>
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

