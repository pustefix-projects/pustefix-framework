<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:cus="http://www.schlund.de/pustefix/customize"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">


  <xsl:template name="runtimemsg">
    If you see this message, the generation of XSL includes works.<br/><br/>
    The information shown below is included using the customizestylesheet.xsl
    during the generation of the included stylesheet:<br/>
    Product: <cus:product/><br/>
    Language: <cus:lang/><br/>
  </xsl:template>
  
</xsl:stylesheet>
