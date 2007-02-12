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
      
      <Listener className="org.apache.catalina.core.AprLifecycleListener"/>
      <Listener className="org.apache.catalina.mbeans.ServerLifecycleListener"/>
      <Listener className="org.apache.catalina.mbeans.GlobalResourcesLifecycleListener"/>
      <Listener className="org.apache.catalina.storeconfig.StoreConfigLifecycleListener"/>

      <GlobalNamingResources>
        <Resource name="UserDatabase" auth="Container" type="org.apache.catalina.UserDatabase"
                  description="User database that can be updated and saved"
                  factory="org.apache.catalina.users.MemoryUserDatabaseFactory"
                  pathname="conf/tomcat-users.xml" />
      </GlobalNamingResources>

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
          <!-- TODO Logger className="org.apache.catalina.logger.FileLogger" prefix="catalina_log." suffix=".txt" timestamp="true"/ -->
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
      <Connector maxHttpHeaderSize="8192"
                 maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
                 enableLookups="false" acceptCount="100"
                 connectionTimeout="20000" disableUploadTimeout="true" 
                 useBodyEncodingForURI="true">
        <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
        <xsl:attribute name="port"><xsl:value-of select="$portbase+80"/></xsl:attribute>
        <xsl:attribute name="redirectport"><xsl:value-of select="$portbase+443"/></xsl:attribute>
      </Connector>
      <Connector maxHttpHeaderSize="8192"
                 maxThreads="150" minSpareThreads="25" maxSpareThreads="75"
                 enableLookups="false" disableUploadTimeout="true"
                 acceptCount="100" scheme="https" secure="true"
                 clientAuth="false" sslProtocol="TLS" keystoreFile="conf/keystore" keystorePass="secret"
                 useBodyEncodingForURI="true">
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
      <Host xmlValidation="false" unpackWARs="false" autoDeploy="false">
        <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
        <xsl:attribute name="name">
          <xsl:apply-templates select="servername/node()"/>
        </xsl:attribute>
        <xsl:if test="webapps[@hostbased='true']">
          <xsl:attribute name="appBase">webapps_<xsl:value-of select="@name"/></xsl:attribute>
        </xsl:if>
        <xsl:call-template name="create_tomcat_aliases">
          <xsl:with-param name="all_aliases"><xsl:apply-templates select="serveralias/node()"/></xsl:with-param>
        </xsl:call-template>

        <Valve className="org.apache.catalina.valves.AccessLogValve"
               directory="logs" prefix="access_log." suffix=".txt" pattern="%h %l %u %t &quot;%r&quot; %s %b %D" resolveHosts="false"/>
        
        <!-- TODO: Logger className="org.apache.catalina.logger.FileLogger"
                directory="logs" prefix="log." suffix=".txt"	timestamp="true"/ -->
        
        <xsl:call-template name="create_context_list">
          <xsl:with-param name="defpath"></xsl:with-param>
          <xsl:with-param name="hostbased"><xsl:value-of select="webapps/@hostbased"/></xsl:with-param>
        </xsl:call-template>
      </Host>
    </xsl:if>
  </xsl:template>

  <xsl:template name="create_context_list">
    <xsl:param name="defpath"/>
    <xsl:param name="hostbased"/>
    <xsl:call-template name="create_context">
      <xsl:with-param name="cookies">false</xsl:with-param>
      <xsl:with-param name="path"><xsl:value-of select="$defpath"/></xsl:with-param>
      <xsl:with-param name="docBase">webapps/<xsl:apply-templates select="@name"/></xsl:with-param>
      <xsl:with-param name="staticDocBase">
        <xsl:if test="$standalone = 'true'">
          <xsl:if test="documentroot">
            <xsl:variable name="abs_path"><xsl:apply-templates select="documentroot/node()"/></xsl:variable>
            <xsl:choose>
              <xsl:when test="ext:exists($abs_path)">
                <xsl:value-of select="$abs_path"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:message>CAUTION: documentroot does not exist: <xsl:value-of select="$abs_path"/></xsl:message>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:if>
        </xsl:if>
      </xsl:with-param>
      <xsl:with-param name="hostbased" select="$hostbased"/>
    </xsl:call-template>
    <xsl:if test="$standalone = 'true'">
      <xsl:apply-templates select="passthrough">
        <xsl:with-param name="hostbased" select="$hostbased"/>
      </xsl:apply-templates>
      <xsl:apply-templates select="/projects/common/apache/passthrough">
        <xsl:with-param name="hostbased" select="$hostbased"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <xsl:template match="passthrough">
    <xsl:param name="hostbased"/>
    <xsl:variable name="rel_path" select="normalize-space(./node())"/>
    <xsl:variable name="abs_path" select="concat($docroot, '/', $rel_path)"/>
    <xsl:choose>
      <xsl:when test="ext:exists($abs_path)">
        <xsl:call-template name="create_context">
          <xsl:with-param name="path">/<xsl:value-of select="$rel_path"/></xsl:with-param>
          <xsl:with-param name="docBase">../../<xsl:value-of select="$rel_path"/></xsl:with-param>
          <xsl:with-param name="cookies">false</xsl:with-param>
          <xsl:with-param name="hostbased" select="$hostbased"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:message>CAUTION: passthrough path not found: <xsl:value-of select="$abs_path"/></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="create_context">
    <xsl:param name="path"/>
    <xsl:param name="docBase"/>
    <xsl:param name="cookies"/>
    <xsl:param name="staticDocBase"/>
    <xsl:param name="hostbased"/>
    <Context crossContext="true">
      <xsl:attribute name="cookies"><xsl:value-of select="$cookies"/></xsl:attribute>
      <xsl:attribute name="path"><xsl:value-of select="$path"/></xsl:attribute>
      <xsl:attribute name="docBase"><xsl:if test="$hostbased='true'">../</xsl:if><xsl:value-of select="$docBase"/></xsl:attribute>
      <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
      <!-- switch off session serialization -->
      <Manager  pathname=""/>
      <xsl:if test="$staticDocBase and not($staticDocBase = '')">
        <Parameter name="staticDocBase" value="{$staticDocBase}"/>
      </xsl:if>
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

