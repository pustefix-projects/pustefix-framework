<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>
  
  <xsl:param name="port"><xsl:apply-templates select="/projects/common/tomcat/connectorport/node()"/></xsl:param>

  <xsl:template match="/">
#
# NOTE: this is only a sample file, suited for a single machine without any clustering.
#       You may need to tweak this file and copy it somewhere else so your changes will not
#       get lost.
#

&lt;IfDefine PFX_USE_JK&gt;
   <xsl:call-template name="jkconf"/>
&lt;/IfDefine&gt;

&lt;IfDefine !PFX_USE_PROXY_AJP&gt;
&lt;IfDefine !PFX_USE_JK&gt;
&lt;IfModule !proxy_ajp_module&gt;
   <xsl:call-template name="jkconf"/>
&lt;/IfModule&gt;
&lt;/IfDefine&gt;
&lt;/IfDefine&gt;

 </xsl:template>
 
 <xsl:template name="jkconf">
JkWorkersFile <xsl:value-of select="$docroot"/>/servletconf/tomcat/workers.prop
JkLogFile     <xsl:apply-templates select="/projects/common/apache/apachelogdir"/>/mod_jk.log
JkLogLevel    <xsl:apply-templates select="/projects/common/tomcat/loglevel/node()"/>
 </xsl:template>
 
</xsl:stylesheet>
