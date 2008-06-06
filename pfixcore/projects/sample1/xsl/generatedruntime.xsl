<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">


  <xsl:template name="runtimemsg">
    If you see this message, the generation of XSL includes works.<br/><br/>
    The information shown below is included via &lt;pfx:include&gt;
    during the generation of the included <b>stylesheet</b>:<br/>
    <hr/>
    <pfx:include href="sample1/txt/navigation.xml" part="test"/>
    <hr/>
  </xsl:template>
  
</xsl:stylesheet>
