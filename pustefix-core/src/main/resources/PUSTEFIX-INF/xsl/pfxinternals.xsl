<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback">

  
  <xsl:param name="__contextpath"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Pustefix internals</title>
        <style type="text/css">
          body {font-family: monospace; padding: 5px; }
          table.info {padding-left: 20px; border-spacing:0px;}
          table.info th,td {text-align:left; padding:4px;}
          table.info td {color: #666666; font-weight: normal;}
          table.info th {color: #000000; font-weight: normal;}
          div.info {padding-left: 15px;}
        </style>
      </head>
      <body>
        <h1>Pustefix internals</h1>
        <div class="info">
          <a href="#envinfo">Environment properties</a><br/>
          <a href="#frameworkinfo">Framework information</a><br/>
          <a href="#moduleinfo">Loaded modules</a><br/>
          <a href="#actions">Actions</a><br/>
          <a href="#messages">Messages</a><br/>
          <a name="envinfo"/>
        </div>
      
        <a name="envinfo"/>
        <h2>Environment properties:</h2>
        <xsl:variable name="properties" select="java:getProperties()" xmlns:java="de.schlund.pfixxml.config.EnvironmentProperties"/>
        <table class="info">
        <tr>
        <th>fqdn</th>
        <td><xsl:value-of select="java:getProperty($properties,'fqdn')" xmlns:java="java.util.Properties"/></td>
        </tr>
        <tr>
        <th>machine</th>
        <td><xsl:value-of select="java:getProperty($properties,'machine')" xmlns:java="java.util.Properties"/></td>
        </tr>
        <tr>
        <th>mode</th>
        <td><xsl:value-of select="java:getProperty($properties,'mode')" xmlns:java="java.util.Properties"/></td>
        </tr>
        <tr>
        <th>uid</th>
        <td><xsl:value-of select="java:getProperty($properties,'uid')" xmlns:java="java.util.Properties"/></td>
        </tr>
        </table>
      
        <a name="frameworkinfo"/>
        <h2>Framework information:</h2>
        <table class="info">
        <tr>
        <th>Pustefix version</th>
        <td><xsl:value-of select="callback:getFrameworkVersion()"/></td>
        </tr>
        </table>
      
        <a name="moduleinfo"/>
        <h2>Loaded modules:</h2>
        <table class="info">
        <xsl:variable name="modules" select="callback:getLoadedModules(.)"/>
        <xsl:for-each select="$modules/modules/module">
          <tr><th><xsl:value-of select="@name"/></th></tr>
        </xsl:for-each>
        </table>
        
        <a name="actions"/>
        <h2>Actions:</h2>
        <div class="info">
          <a href="{$__contextpath}/xml/develinfo?action=reload">Schedule webapp reload</a>
        </div>
        
        <xsl:apply-templates select="/develinfo/messages"/>
      
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="messages">
    <a name="messages"/>
    <h2>Messages:</h2>
    <table class="info">
      <tr>
        <th>Date</th>
        <th>Level</th>
        <th>Message</th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>

  <xsl:template match="message">
    <tr>
      <td><xsl:value-of select="@date"/></td>
      <td><xsl:value-of select="@level"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>