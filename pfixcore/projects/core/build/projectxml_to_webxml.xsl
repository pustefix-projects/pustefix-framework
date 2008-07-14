<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
  version="1.1"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:web="http://java.sun.com/xml/ns/j2ee"
  xmlns:p="http://www.pustefix-framework.org/2008/namespace/project-config"
>
  
  <!-- Config files that should be loaded by the ApplicationContext,
       separated by spaces                                           -->
  <xsl:param name="__config_files"/>
  
  <!-- Config file containing global options -->
  <xsl:param name="__global_config_file"/>
  
  <xsl:template match="/">
    
    <web:web-app xmlns:xsi="http:///www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd" version="2.4">
      <xsl:attribute name="id"><xsl:value-of select="project/name/text()"/></xsl:attribute>
      
      <web:display-name><xsl:value-of select="project/name/text()"/></web:display-name>
      
      <web:servlet>
        <web:servlet-name>dispatcher</web:servlet-name>
        <web:servlet-class>org.springframework.web.servlet.DispatcherServlet</web:servlet-class>
        <web:init-param>
          <web:param-name>contextClass</web:param-name>
          <web:param-value>org.pustefixframework.container.spring.beans.PustefixWebApplicationContext</web:param-value>
        </web:init-param>
        <web:init-param>
          <web:param-name>contextConfigLocation</web:param-name>
          <web:param-value><xsl:value-of select="$__config_files"/></web:param-value>
        </web:init-param>
        <web:load-on-startup>1</web:load-on-startup>
      </web:servlet>
      
      <xsl:for-each select="p:project-config/p:application/p:direct-output-service|p:project-config/p:application/p:context-xml-service">
        <web:servlet-mapping>
          <web:servlet-name>dispatcher</web:servlet-name>
          <web:url-pattern><xsl:value-of select="p:path/text()"/>/*</web:url-pattern>
        </web:servlet-mapping>
      </xsl:for-each>
      
      <web:servlet-mapping>
        <web:servlet-name>dispatcher</web:servlet-name>
        <web:url-pattern>/xml/deref/*</web:url-pattern>
      </web:servlet-mapping>
      
      <web:servlet-mapping>
        <web:servlet-name>dispatcher</web:servlet-name>
        <web:url-pattern>/*</web:url-pattern>
      </web:servlet-mapping>
      
    </web:web-app>
    
  </xsl:template>
  
</xsl:stylesheet>
