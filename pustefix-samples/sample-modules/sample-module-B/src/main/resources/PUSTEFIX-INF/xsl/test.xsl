<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias" version="1.0">


<xsl:template match="foo">
XXXXXfooooooooooooooooooooooooooooooo
<pfx:include href="txt/common.xml" part="partA" search="dynamic" module="org.pustefixframework.samples.modules.sample-module-B"/>
</xsl:template>

</xsl:stylesheet>
