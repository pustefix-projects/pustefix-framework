<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <!-- for filtering out duplicate Alias directives -->
  <xsl:key name="passthroughKey" match="/projects/common/apache/passthrough | /projects/project/passthrough" use="text()"/>
  
  <xsl:template match="projects">
    <xsl:apply-templates select="./project"/>
  </xsl:template>

  <xsl:template match="port">
    <xsl:variable name="currentprj" select="ancestor::project"></xsl:variable>
&lt;VirtualHost <xsl:apply-templates select="$currentprj/virtualhost/node()"/>:<xsl:value-of select="@number"/>&gt;

ServerName <xsl:apply-templates select="$currentprj/servername/node()"/>
<xsl:if test="count($currentprj/serveralias/node()) > 0">
ServerAlias <xsl:apply-templates select="$currentprj/serveralias/node()"/>
</xsl:if>

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

&lt;IfDefine PFX_USE_JK&gt;
JkMount /xml/* <xsl:apply-templates select="/projects/common/tomcat/jkmount/node()"/>
<xsl:for-each select="$currentprj/jkmount">
JkMount <xsl:choose><xsl:when test="@url"><xsl:value-of select="@url"/><xsl:text> </xsl:text></xsl:when><xsl:otherwise>/xml/*<xsl:text> </xsl:text></xsl:otherwise></xsl:choose> <xsl:apply-templates select="./node()"/>
</xsl:for-each>
&lt;/IfDefine&gt;

&lt;IfDefine PFX_USE_PROXY_AJP&gt;
ProxyPass /xml/ balancer://<xsl:apply-templates select="/projects/common/tomcat/jkmount/node()"/>/xml/ nofailover=On stickysession=jsessionid
<xsl:for-each select="$currentprj/jkmount">
ProxyPass <xsl:choose><xsl:when test="@url"><xsl:value-of select="substring-before(@url, '*')"/><xsl:text> </xsl:text></xsl:when><xsl:otherwise>/xml/<xsl:text> </xsl:text></xsl:otherwise></xsl:choose> balancer://<xsl:apply-templates select="./node()"/><xsl:choose><xsl:when test="@url"><xsl:value-of select="substring-before(@url, '*')"/><xsl:text> </xsl:text></xsl:when><xsl:otherwise>/xml/<xsl:text> </xsl:text></xsl:otherwise></xsl:choose> nofailover=On stickysession=jsessionid
</xsl:for-each>
Include <xsl:value-of select="$docroot"/>/servletconf/tomcat/ajp.conf
&lt;/IfDefine&gt;

&lt;IfDefine !PFX_USE_PROXY_AJP&gt;
&lt;IfDefine !PFX_USE_JK&gt;
&lt;IfModule proxy_ajp_module&gt;
ProxyPass /xml/ balancer://<xsl:apply-templates select="/projects/common/tomcat/jkmount/node()"/>/xml/ nofailover=On stickysession=jsessionid
<xsl:for-each select="$currentprj/jkmount">
ProxyPass <xsl:choose><xsl:when test="@url"><xsl:value-of select="substring-before(@url, '*')"/><xsl:text> </xsl:text></xsl:when><xsl:otherwise>/xml/<xsl:text> </xsl:text></xsl:otherwise></xsl:choose> balancer://<xsl:apply-templates select="./node()"/><xsl:choose><xsl:when test="@url"><xsl:value-of select="substring-before(@url, '*')"/><xsl:text> </xsl:text></xsl:when><xsl:otherwise>/xml/<xsl:text> </xsl:text></xsl:otherwise></xsl:choose> nofailover=On stickysession=jsessionid
</xsl:for-each>
Include <xsl:value-of select="$docroot"/>/servletconf/tomcat/ajp.conf
&lt;/IfModule&gt;
&lt;IfModule !proxy_ajp_module&gt;
JkMount /xml/* <xsl:apply-templates select="/projects/common/tomcat/jkmount/node()"/>
<xsl:for-each select="$currentprj/jkmount">
JkMount <xsl:choose><xsl:when test="@url"><xsl:value-of select="@url"/><xsl:text> </xsl:text></xsl:when><xsl:otherwise>/xml/*<xsl:text> </xsl:text></xsl:otherwise></xsl:choose> <xsl:apply-templates select="./node()"/>
</xsl:for-each>
&lt;/IfModule&gt;
&lt;/IfDefine&gt;
&lt;/IfDefine&gt;

<xsl:apply-templates select="$currentprj/errordoc"/>

<xsl:apply-templates select="$currentprj/documentroot"/>

<xsl:apply-templates select="/projects/common/apache/passthrough"/>

<!-- CAUTION: don't use 'select="$currentprj/passthrough"' here -->
<!-- for beeing able to reference all resources from all projects/products -->
<xsl:apply-templates select="/projects/project/passthrough"/>

<xsl:apply-templates select="$currentprj/literalapache/node()"/>

<xsl:if test="/projects/common/apache/apachelogdir">
ErrorLog   <xsl:apply-templates select="/projects/common/apache/apachelogdir"/>/error_log
CustomLog  <xsl:apply-templates select="/projects/common/apache/apachelogdir"/>/access_log combined
</xsl:if>

&lt;Location /&gt;
Options ExecCGI
<xsl:apply-templates select="$currentprj/literalauth"/>
&lt;/Location&gt;
DirectoryIndex index.cgi
AddHandler cgi-script .cgi
&lt;/VirtualHost&gt;
      
  </xsl:template>

  <xsl:template match="extendedvhost">
    <xsl:copy-of select="."/>
  </xsl:template>
    
  
  <xsl:template match="project">
    <xsl:param name="active">
      <xsl:apply-templates select="active/node()"/>
    </xsl:param>

    <xsl:if test="normalize-space($active) = 'true'">
      <xsl:apply-templates select="ports/*"/>
      <xsl:apply-templates select="extendedvhost"/>   
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
  
  <!-- predicate is for filtering out duplicate Alias directives -->
  <xsl:template match="passthrough[generate-id() = generate-id( key('passthroughKey', text())[1] )]">
    <xsl:variable name="pass"><xsl:apply-templates select="node()"/></xsl:variable>
      Alias        /<xsl:value-of select="$pass"/><xsl:text> </xsl:text><xsl:value-of select="$docroot"/>/<xsl:value-of select="$pass"/>
  </xsl:template>
  <!-- supress copy of duplicate passthrough elements -->
  <xsl:template match="passthrough"/>

</xsl:stylesheet>

