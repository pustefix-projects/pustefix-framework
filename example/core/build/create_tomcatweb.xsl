<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:param name="prjname"/>

  <xsl:output method="xml" encoding="ISO-8859-1" indent="yes"
	      doctype-public="-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN"
	      doctype-system="http://java.sun.com/dtd/web-app_2_3.dtd"/>

  <xsl:include href="create_lib.xsl"/>

  <xsl:template match="/">
    <xsl:apply-templates select="/projects/project[@name = $prjname]"/>
  </xsl:template>

  <xsl:template match="project">
    <web-app>
      <xsl:for-each select="./servlet">
	<xsl:variable name="active_node">
	  <xsl:apply-templates select="./active/node()"/>
	</xsl:variable>
	<xsl:variable name="active">
	  <xsl:value-of select="normalize-space($active_node)"/>
	</xsl:variable>

	<xsl:if test="$active = 'true'">
	  <servlet>
	    <servlet-name><xsl:value-of select="@name"/></servlet-name>
	    <servlet-class><xsl:apply-templates select="class/node()"/></servlet-class>
	    <xsl:if test="/projects/common/commonpropfile/node()">
	      <init-param>
		<param-name>servlet.commonpropfile</param-name>
		<param-value><xsl:apply-templates select="/projects/common/commonpropfile/node()"/></param-value>
	      </init-param>
	    </xsl:if>
	    <xsl:if test="propfile/node()">     
	      <init-param>
		<param-name>servlet.propfile</param-name>
		<param-value><xsl:apply-templates select="propfile/node()"/></param-value>
	      </init-param>
	    </xsl:if>
	    
	    <xsl:if test="@autostartup = 'true'">
	      <load-on-startup>666</load-on-startup>
	    </xsl:if>
	    
	  </servlet>
	</xsl:if>
      </xsl:for-each>
    
      <xsl:for-each select="./servlet">
	<xsl:variable name="active_node">
	  <xsl:apply-templates select="./active/node()"/>
	</xsl:variable>
	<xsl:variable name="active">
	  <xsl:value-of select="normalize-space($active_node)"/>
	</xsl:variable>

	<xsl:if test="$active = 'true'">
	  <servlet-mapping>
	    <servlet-name><xsl:value-of select="@name"/></servlet-name>
	    <url-pattern>/<xsl:value-of select="@name"/>/*</url-pattern>
	  </servlet-mapping>
	</xsl:if>
      </xsl:for-each>


  <!-- if a 'sessiontimeout'-node exists use it, else use default -->
  <xsl:choose>
    <xsl:when test="./sessiontimeout">
      <session-config>
        <session-timeout><xsl:value-of select="./sessiontimeout"/></session-timeout>
      </session-config> 
    </xsl:when>
    <xsl:otherwise>
      <session-config>
        <session-timeout>60</session-timeout>
      </session-config>
    </xsl:otherwise>
  </xsl:choose>


      <xsl:for-each select="tomcat/error-page">
	<error-page>
	  <xsl:choose>
	    <xsl:when test="exception-type">
	      <exception-type><xsl:apply-templates select="exception-type/node()"/></exception-type>
	    </xsl:when>
	    <xsl:when test="error-code">
	      <error-code><xsl:apply-templates select="error-code/node()"/></error-code>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:message terminate="yes">one element "error-code" or "exception-type" is needed!</xsl:message>
	    </xsl:otherwise>
	  </xsl:choose>
	  <xsl:if test="not(location/node())">
	    <xsl:message terminate="yes">one element "location" is needed!</xsl:message>
	  </xsl:if>
	  <location><xsl:apply-templates select="location/node()"/></location>
	</error-page>
      </xsl:for-each>
      
    </web-app>
  </xsl:template>
  
</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
