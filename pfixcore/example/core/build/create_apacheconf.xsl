<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:template match="projects">
    <xsl:apply-templates select="./project"/>
  </xsl:template>

  <xsl:template match="port">
    <xsl:variable name="currentprj" select="ancestor::project"></xsl:variable>
&lt;VirtualHost <xsl:apply-templates select="$currentprj/virtualhost/node()"/>:<xsl:value-of select="@number"/>&gt;

ServerName <xsl:apply-templates select="$currentprj/servername/node()"/>
ServerAlias <xsl:apply-templates select="$currentprj/serveralias/node()"/>

RewriteEngine on

<xsl:if test="$currentprj/defpath/node()">
  RewriteRule ^/$ <xsl:choose>
    <xsl:when test="@ssl = 'true'">https</xsl:when><xsl:otherwise>http</xsl:otherwise>
  </xsl:choose>://%{SERVER_NAME}<xsl:apply-templates select="$currentprj/defpath/node()"/> [NC,R,L]
</xsl:if>

<xsl:if test="@ssl ='true'">
  SSLEngine on
  <xsl:apply-templates select="sslcrt"/>
  <xsl:apply-templates select="sslkey"/>
</xsl:if>

<xsl:if test="$container = 'tomcat'">
  JkMount /xml/* <xsl:apply-templates select="/projects/common/tomcat/jkmount/node()"/>
</xsl:if>

<xsl:apply-templates select="$currentprj/errordoc"/>

<xsl:apply-templates select="$currentprj/documentroot"/>

<xsl:apply-templates select="/projects/common/apache/passthrough"/>

<xsl:apply-templates select="$currentprj/literalapache/node()"/>

<xsl:if test="/projects/common/apache/apachelogdir">
ErrorLog   <xsl:apply-templates select="/projects/common/apache/apachelogdir"/>/error_log
CustomLog  <xsl:apply-templates select="/projects/common/apache/apachelogdir"/>/access_log combined
</xsl:if>

&lt;Location /&gt;
Options ExecCGI FollowSymLinks
AllowOverride None
&lt;/Location&gt;

DirectoryIndex index.cgi
AddHandler cgi-script .cgi
&lt;/VirtualHost&gt;
  </xsl:template>

  
  <xsl:template match="project">
    <xsl:param name="active">
      <xsl:apply-templates select="active/node()"/>
    </xsl:param>

    <xsl:if test="normalize-space($active) = 'true'">
      <xsl:apply-templates select="ports/*"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="sslcrt">
    <xsl:if test="node()">
      SSLCertificateFile <xsl:apply-templates select="node()"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="sslkey">
    <xsl:if test="node()">
      SSLCertificateKeyFile <xsl:apply-templates select="node()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="documentroot">
    <xsl:if test="node()">
      DocumentRoot <xsl:apply-templates select="node()"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="errordoc">
      ErrorDocument <xsl:value-of select="@error"/><xsl:text> </xsl:text><xsl:apply-templates select="./node()"/>
  </xsl:template>
  
  <xsl:template match="passthrough">
    <xsl:variable name="pass"><xsl:apply-templates select="node()"/></xsl:variable>
      Alias        /<xsl:value-of select="$pass"/><xsl:text> </xsl:text><xsl:value-of select="$docroot"/>/<xsl:value-of select="$pass"/>
  </xsl:template>
      
  
    
</xsl:stylesheet>

<!--
Local Variables:
mode: xml
End:
-->
