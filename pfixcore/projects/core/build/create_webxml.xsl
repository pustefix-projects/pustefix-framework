<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns="http://java.sun.com/xml/ns/javaee"
                xmlns:jee="http://java.sun.com/xml/ns/javaee"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
                xmlns:ci="java:org.pustefixframework.config.customization.PropertiesBasedCustomizationInfo"
                exclude-result-prefixes="cus p ci jee">

  <!-- Config files that should be loaded by the ApplicationContext,
       separated by spaces                                           -->
  <xsl:param name="configfiles"/>

  <xsl:param name="projectname"/>
  <xsl:param name="warmode"/>
  
  <xsl:param name="projectsfile"/>
  <xsl:param name="commonprojectsfile"/>
  
  <xsl:param name="customizationinfo"/>

  <xsl:variable name="common" select="document(concat('file://', $commonprojectsfile))/p:projects" />

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="/p:project-config/p:application/p:web-xml//jee:web-app">
        <xsl:variable name="wxt">
          <xsl:apply-templates select="/p:project-config/p:application/p:web-xml/*"/>
        </xsl:variable>
        <web-app>
          <xsl:copy-of select="$wxt/jee:web-app/@*"/>
          <xsl:apply-templates select="$wxt/jee:web-app/jee:icon|$wxt/jee:web-app/jee:display-name|$wxt/jee:web-app/jee:description|$wxt/jee:web-app/jee:distributable|$wxt/jee:web-app/jee:context-param|$wxt/jee:web-app/jee:filter|$wxt/jee:web-app/jee:filter-mapping|$wxt/jee:web-app/jee:listener"/>
          <xsl:call-template name="create-servlet-definitions"/>
          <xsl:apply-templates select="$wxt/jee:web-app/jee:servlet"/>
          <xsl:call-template name="create-servlet-mappings"/>
          <xsl:apply-templates select="$wxt/jee:web-app/jee:servlet-mapping"/>
          <xsl:choose>
            <xsl:when test="$wxt/jee:web-app/jee:session-config">
              <xsl:apply-templates select="$wxt/jee:web-app/jee:session-config"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create-session-config"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:apply-templates select="$wxt/jee:web-app/jee:mime-mapping|$wxt/jee:web-app/jee:welcome-file-list|$wxt/jee:web-app/jee:error-page|$wxt/jee:web-app/jee:taglib|$wxt/jee:web-app/jee:resource-env-ref|$wxt/jee:web-app/jee:resource-ref|$wxt/jee:web-app/jee:security-constraint|$wxt/jee:web-app/jee:login-config|$wxt/jee:web-app/jee:security-role|$wxt/jee:web-app/jee:env-entry|$wxt/jee:web-app/jee:ejb-ref|$wxt/jee:web-app/jee:ejb-local-ref"/>
        </web-app>
      </xsl:when>
      <xsl:otherwise>
        <web-app>
          <xsl:call-template name="create-servlet-definitions"/>
          <xsl:call-template name="create-servlet-mappings"/>
          <xsl:call-template name="create-session-config"/>
        </web-app>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="create-servlet-definitions">
    <xsl:call-template name="create-dispatcher-servlet"/>
  </xsl:template>
    
  <xsl:template name="create-servlet-mappings">
    <xsl:apply-templates mode="servlet-mappings" select="/p:project-config/p:application/*"/>
    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>/xml/deref/*</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>/*</url-pattern>
    </servlet-mapping>
  </xsl:template>
  
  <xsl:template name="create-session-config">
    <session-config>
      <session-timeout>60</session-timeout>
    </session-config>
  </xsl:template>
  
  <xsl:template mode="serlvet-mappings" match="p:choose">
    <xsl:variable name="matches" select="p:when[ci:evaluateXPathExpression($customizationinfo,@test)]"/>
    <xsl:choose>
      <xsl:when test="count($matches)=0">
        <xsl:apply-templates select="p:otherwise/node()" mode="servlet-mappings"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$matches[1]/node()" mode="servlet-mappings"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template mode="servlet-mappings" match="text()">
    <xsl:call-template name="text"/>
  </xsl:template>
  
  <xsl:template mode="servlet-mappings" match="*">
    <!-- Ignore anything not matched explicitly -->
  </xsl:template>
  
  <xsl:template mode="servlet-mappings" match="p:direct-output-service|p:context-xml-service">
    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern><xsl:value-of select="p:path/text()"/>/*</url-pattern>
    </servlet-mapping>
  </xsl:template>
  
  <xsl:template name="create-dispatcher-servlet">
    <servlet>
      <servlet-name>dispatcher</servlet-name>
      <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
      <init-param>
        <param-name>contextClass</param-name>
        <param-value>org.pustefixframework.container.spring.beans.PustefixWebApplicationContext</param-value>
      </init-param>
      <init-param>
        <param-name>contextConfigLocation</param-name>
        <param-value><xsl:value-of select="$configfiles"/></param-value>
      </init-param>
      <load-on-startup>1</load-on-startup>
    </servlet>
  </xsl:template>
  
  <xsl:template name="create_dispatcher_mappings">
    <xsl:for-each select="$project/p:application/p:direct-output-service|$project/p:application/p:context-xml-service">
      <servlet-mapping>
        <servlet-name>dispatcher</servlet-name>
        <url-pattern><xsl:value-of select="p:path/text()"/>/*</url-pattern>
      </servlet-mapping>
    </xsl:for-each>
    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>/xml/deref/*</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>/*</url-pattern>
    </servlet-mapping>
  </xsl:template>
  
  <xsl:template match="servlet[not(preceding-sibling::servlet)]">
    <xsl:call-template name="create-servlet-definitions"/>
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="servlet-mapping[not(preceding-sibling::servlet-mapping)]">
    <xsl:call-template name="create-servlet-mappings"/>
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="p:choose">
    <xsl:variable name="matches" select="p:when[ci:evaluateXPathExpression($customizationinfo,@test)]"/>
    <xsl:choose>
      <xsl:when test="count($matches)=0">
        <xsl:apply-templates select="p:otherwise/node()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="$matches[1]/node()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text()" name="text">
    <xsl:value-of select="ci:replaceVariables($customizationinfo,.)"/>
  </xsl:template>

  <xsl:template match="jee:*">
    <xsl:element name="{local-name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates/>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>