<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:include href="create_lib.xsl"/>
  <xsl:output method="text" encoding="ISO-8859-1" indent="no"/>

  <xsl:template match="/">
#
# NOTE: this is only a sample file, suited for a single machine without any clustering.
#       You may need to tweak this file and copy it somewhere else so your changes will not
#       get lost.
#

ApJServManual     <xsl:apply-templates select="/projects/common/jserv/jservmanual/node()"/>
ApJServProperties <xsl:value-of select="$docroot"/>/servletconf/jserv/jserv.prop
ApJServLogFile    <xsl:apply-templates select="/projects/common/apache/apachelogdir"/>/mod_jserv.log
ApJServDefaultProtocol ajpv12
ApJServDefaultHost <xsl:apply-templates select="/projects/common/jserv/jservhost/node()"/>
ApJServDefaultPort 8007
ApJServSecretKey DISABLED
ApJServMountCopy on
&lt;Location /jserv/&gt;
  SetHandler jserv-status
  order deny,allow
  deny from all
  allow from <xsl:apply-templates select="/projects/common/jserv/jservhost/node()"/>
&lt;/Location&gt;
  </xsl:template>
</xsl:stylesheet>
