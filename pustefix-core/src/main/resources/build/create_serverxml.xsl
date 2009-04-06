<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:ci="java:org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo"
                xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
                xmlns:ext="xalan://de.schlund.pfixcore.util.XsltTransformer"
                >

  <xsl:param name="portbase"/>
  <xsl:param name="warmode">false</xsl:param>
  <xsl:param name="webappbase"/>
  <xsl:param name="commonprojectsfile"/>
  <xsl:param name="customizationinfo"/>
  <xsl:param name="webappmode">false</xsl:param>
  <xsl:param name="webapps"/>
  
  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
  
  <xsl:variable name="common-temp" select="document(concat('file://', $commonprojectsfile))" />
  <xsl:variable name="common">
    <xsl:apply-templates select="$common-temp" mode="customization"/>
  </xsl:variable>
  
  <xsl:variable name="tree">
    <xsl:apply-templates mode="customization" select="/"/>
  </xsl:variable>
  
  <xsl:variable name="debug">
    <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:debug/node()"/>
  </xsl:variable>

  <xsl:template match="/">
    <xsl:variable name="adminport">
      <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:adminport/node()"/>
    </xsl:variable>
    <xsl:variable name="tomcat_defaulthost">
      <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:defaulthost/node()"/>
    </xsl:variable>
    <xsl:variable name="tomcat_jvmroute">
      <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:jvmroute/node()"/>
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
          <xsl:apply-templates select="$tree/projects/p:project-config"/>
        </Engine>
      </Service>
    </Server>
  </xsl:template>

  <xsl:template name="create-connector">
    <xsl:variable name="jkport">
      <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:connectorport/node()"/>
    </xsl:variable>
    <xsl:variable name="minprocessors">
      <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:minprocessors/node()"/>
    </xsl:variable>
    <xsl:variable name="maxprocessors">
      <xsl:apply-templates select="$common/p:global-config/p:http-server/p:tomcat/p:maxprocessors/node()"/>
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
  </xsl:template>

  <xsl:template match="p:project-config">
    <xsl:variable name="active">
      <xsl:choose>
        <xsl:when test="normalize-space(p:project/p:enabled/text()) = 'false'">false</xsl:when>
        <xsl:otherwise>true</xsl:otherwise> 
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="applist" select="concat(' ',translate($webapps,',',' '),' ')"/>
    <xsl:if test="$active = 'true' and ( $webapps = '' or contains($applist,p:project/p:name/text()) )">
      <Host xmlValidation="false" unpackWARs="false" deployOnStartup="false" autoDeploy="false">
        <xsl:attribute name="debug"><xsl:value-of select="$debug"/></xsl:attribute>
        <xsl:attribute name="name">
          <!-- <xsl:apply-templates select="p:http-server/server-name/node()"/> -->
          <xsl:value-of select="p:http-server/p:server-name/text()"/>
        </xsl:attribute>
        <xsl:if test="p:http-server/p:tomcat/p:enable-extra-webapps/text() = 'true'">
          <xsl:attribute name="appBase">webapps_<xsl:value-of select="p:project/p:name/text()"/></xsl:attribute>
        </xsl:if>
        <xsl:call-template name="create_tomcat_aliases">
          <xsl:with-param name="all_aliases"><xsl:apply-templates select="p:http-server/p:server-alias/node()"/></xsl:with-param>
        </xsl:call-template>

        <Valve className="org.apache.catalina.valves.AccessLogValve"
               directory="logs" prefix="access_log." suffix=".txt" pattern="%h %l %u %t &quot;%r&quot; %s %b %D" resolveHosts="false"/>
        
        <!-- TODO: Logger className="org.apache.catalina.logger.FileLogger"
                directory="logs" prefix="log." suffix=".txt"	timestamp="true"/ -->
        
        <xsl:call-template name="create_context_list">
          <xsl:with-param name="defpath"></xsl:with-param>
          <xsl:with-param name="hostbased"><xsl:value-of select="p:http-server/p:tomcat/p:enable-extra-webapps/text()"/></xsl:with-param>
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
      <xsl:with-param name="docBase">
        <xsl:choose>
          <xsl:when test="$warmode = 'true'">
            <xsl:value-of select="$webappbase"/>/<xsl:apply-templates select="p:project/p:name/text()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>webapps/</xsl:text><xsl:apply-templates select="p:project/p:name/text()"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:with-param>
      <xsl:with-param name="staticDocBase">
        <xsl:if test="p:application/p:docroot-path/text()">
          <xsl:variable name="docroot" select="p:application/p:docroot-path/text()"/>
          <xsl:variable name="abs_path">
            <xsl:choose>
              <xsl:when test="starts-with(normalize-space(text()), 'pfixroot:')">
                <xsl:value-of select="$docroot"/><xsl:value-of select="substring-after(normalize-space(text()), 'pfixroot:')"/>
              </xsl:when>
              <xsl:when test="starts-with(normalize-space(text()), 'file:')">
                <xsl:value-of select="substring-after(normalize-space(text()), 'file:')"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="normalize-space(text())"/>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="ext:exists($abs_path)">
              <xsl:value-of select="$abs_path"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:message>CAUTION: documentroot does not exist: <xsl:value-of select="$abs_path"/></xsl:message>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:with-param>
      <xsl:with-param name="hostbased" select="$hostbased"/>
    </xsl:call-template>
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
      <xsl:if test="$webappmode='true'">
        <xsl:attribute name="allowLinking">true</xsl:attribute>
      </xsl:if>
      <!-- switch off session serialization -->
      <xsl:choose>
        <xsl:when test="$webappmode='true'">
          <Manager/>
        </xsl:when>
        <xsl:otherwise>
          <Manager  pathname=""/>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$staticDocBase and not($staticDocBase = '')">
        <Parameter name="staticDocBase" value="{$staticDocBase}"/>
      </xsl:if>
      <!-- Path to Pustefix docroot -->
      <Parameter name="pustefix.docroot" value="{$docroot}"/>
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
  
  <xsl:template mode="customization" match="p:choose">
    <xsl:variable name="matches" select="p:when[ci:evaluateXPathExpression($customizationinfo,@test)]"/>
    <xsl:choose>
      <xsl:when test="count($matches)=0">
        <xsl:apply-templates select="p:otherwise/node()" mode="customization"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$matches[1]/node()" mode="customization"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template mode="customization" match="text()">
    <xsl:value-of select="ci:replaceVariables($customizationinfo,.)"/>
  </xsl:template>
  
  <xsl:template mode="customization" match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates mode="customization"/>
    </xsl:element>
  </xsl:template>
  
</xsl:stylesheet>

