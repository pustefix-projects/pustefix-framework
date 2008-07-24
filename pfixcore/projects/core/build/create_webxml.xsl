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

  <xsl:variable name="common-temp" select="document(concat('file://', $commonprojectsfile))" />
  <xsl:variable name="common">
    <xsl:apply-templates select="$common-temp" mode="customization"/>
  </xsl:variable>
  
  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"/>
  
  <xsl:template match="/">
    <xsl:variable name="tree">
      <xsl:apply-templates mode="customization" select="self::node()"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$tree/p:project-config/p:application/p:web-xml/jee:web-app">
        <xsl:for-each select="$tree/p:project-config/p:application/p:web-xml/jee:web-app">
          <web-app>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates select="jee:icon|jee:display-name|jee:description|jee:distributable|jee:context-param|jee:filter|jee:filter-mapping|jee:listener"/>
            <xsl:call-template name="create-servlet-definitions"/>
            <xsl:apply-templates select="jee:servlet"/>
            <xsl:call-template name="create-servlet-mappings">
              <xsl:with-param name="tree" select="$tree"/>
            </xsl:call-template>
            <xsl:apply-templates select="jee:servlet-mapping"/>
            <xsl:choose>
              <xsl:when test="jee:session-config">
                <xsl:apply-templates select="jee:session-config"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="create-session-config"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="jee:mime-mapping|jee:welcome-file-list"/>
            <xsl:call-template name="create-error-pages">
              <xsl:with-param name="tree" select="$tree"/>
            </xsl:call-template>
            <xsl:apply-templates select="jee:error-page"/>
            <xsl:apply-templates select="jee:taglib|jee:resource-env-ref|jee:resource-ref|jee:security-constraint|jee:login-config|jee:security-role|jee:env-entry|jee:ejb-ref|jee:ejb-local-ref"/>
          </web-app>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <web-app>
          <xsl:call-template name="create-servlet-definitions"/>
          <xsl:call-template name="create-servlet-mappings">
            <xsl:with-param name="tree" select="$tree"/>
          </xsl:call-template>
          <xsl:call-template name="create-session-config"/>
          <xsl:call-template name="create-error-pages">
            <xsl:with-param name="tree" select="$tree"/>
          </xsl:call-template>
        </web-app>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="create-servlet-definitions">
    <xsl:call-template name="create-dispatcher-servlet"/>
  </xsl:template>
    
  <xsl:template name="create-servlet-mappings">
    <xsl:param name="tree"/>
    <xsl:for-each select="$tree/p:project-config/p:application/p:context-xml-service|$tree/p:project-config/p:application/p:direct-output-service">
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
      <url-pattern>/xml/*</url-pattern>
    </servlet-mapping>
    <servlet-mapping>
      <servlet-name>dispatcher</servlet-name>
      <url-pattern>/*</url-pattern>
    </servlet-mapping>
  </xsl:template>
  
  <xsl:template name="create-error-pages">
    <xsl:param name="tree"/>
    <xsl:for-each select="$tree/p:project-config/p:application/p:error-pages/p:error">
      <xsl:variable name="code" select="@code"/>
      <xsl:if test="not($tree/p:project-config/p:application/p:web-xml/jee:web-app/jee:error-page/jee:error-code[text()=$code])">
        <error-page>
          <error-code><xsl:value-of select="@code"/></error-code>
          <location><xsl:value-of select="text()"/></location>
        </error-page>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template name="create-session-config">
    <session-config>
      <session-timeout>60</session-timeout>
    </session-config>
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
  
  <xsl:template match="p:choose" mode="customization">
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

  <xsl:template match="text()" mode="customization">
    <xsl:value-of select="ci:replaceVariables($customizationinfo,.)"/>
  </xsl:template>
  
  <xsl:template mode="customization" match="*">
    <xsl:element name="{name()}" namespace="{namespace-uri()}">
      <xsl:copy-of select="./@*"/><xsl:apply-templates mode="customization"/>
    </xsl:element>
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