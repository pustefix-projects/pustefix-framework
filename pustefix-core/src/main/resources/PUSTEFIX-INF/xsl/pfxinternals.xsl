<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="__contextpath"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Pustefix internals</title>
        <style type="text/css">
          body {
            font-family: monospace;
            padding: 5px; 
            background: #fff; 
          }
          a:link {
            color: #000;
          }
          a:visited {
            color: #444;
          }
          a:hover {
            color: #888;
          }
          a:active {
            color: #777;
          }
          a:focus {
            color: #666;
          }
          div.section {
            background: #fff;
            padding: 5px;
            padding-left: 10px;
          }
          div.title {
            background: #999;
            color: #fff; 
            font-size: 130%; 
            font-style:italic; 
            padding: 3px; 
            padding-left: 10px; 
            margin-top: 15px;
            -moz-border-radius: 10px;
            -webkit-border-radius: 10px;
            -khtml-border-radius: 10px;
            border-radius: 10px;
            border: 0px solid #333;
          }
          div.header {
            background: #000; 
            color: #fff; 
            font-size: 170%; 
            font-style:italic; 
            padding: 3px; 
            padding-left: 10px;
            -moz-border-radius: 10px;
            -webkit-border-radius: 10px;
            -khtml-border-radius: 10px;
            border-radius: 10px;
            border: 0px solid #333;
            margin-bottom: 5px;
          }
          table.navi {
            border-spacing: 0px;
          }
          table.navi td {
            padding-right: 20px;
          }
          table.actions {
            border-spacing: 0px;
          }
          table.actions td {
            padding-right: 20px;
          }
          table.info {
            border-spacing:0px;
          }
          table.info th,td {
            text-align:left; 
            padding-left: 0px; 
            padding-right: 10px; 
            padding-top: 3px; 
            padding-bottom: 3px;
          }
          table.info td {
            color: #000000;
            font-weight: normal;
          }
          table.info th {
            color: #000000; 
            font-weight: bold;
          }
          table.info td.num {
            text-align:right;
          }
          table.info th.title {
            font-weight: bold; 
            padding-bottom: 5px;
          }
          div.info {
            padding-left: 15px; 
            padding-top: 2px; 
            padding-bottom: 2px;
          }
          table.layout {
            border-spacing:0px;
          }
          table.layout td {
            padding-right: 30px;
            vertical-align: top;
          }
          table.barchart {
            border-spacing:0px; 
            padding:0px; 
            margin:0px;
            margin-top: 5px;
          }
          table.barchart td {
            height: 20px; 
            padding:0px; 
            margin:0px;
          }
          table.barchart td hr {
            padding:0px; 
            margin:0px; 
            border: 0px;
          }
          div.info {
            font-size: 80%;
          }
          span.liveclasses {
            color: green;
            font-size: 150%;
            padding-left: 3px;
            padding-right: 20px;
          }
          span.liveresources {
            color: green;
           	font-size: 150%;
           	padding-left: 10px;
          }
        </style>
        <script type="text/javascript">

          function removeCookies() {
            var d = 0;
            var c = document.cookie.split(";");
            for(var i=0; i &lt; c.length; i++) {
              var e = c[i].indexOf("=");
              var n = e > -1 ? c[i].substr(0,e) : c[i];
              if(n != "") {
                document.cookie = n + "=;path=<xsl:value-of select="$__contextpath"/>;expires=Thu, 01 Jan 1970 00:00:00 GMT";
                d++;
              }
            }
            alert("Removed " + d + " cookie" + (d==1 ? "" : "s"));
          }
          
        </script>
      </head>
      <body>
      
        <div class="header">Pustefix internals</div>
      
        <div class="section">
          <table class="navi">
            <tr>
              <td><a href="#framework">Framework information</a></td>   
              <td><a href="#modules">Loaded modules</a></td>
            </tr>
            <tr>
              <td><a href="#environment">Environment properties</a></td>
              <td><a href="#actions">Actions</a></td>
            </tr>
            <tr>
              <td><a href="#jvm">JVM information</a></td>
              <td><a href="#messages">Messages</a></td>
            </tr>
          </table>
        </div>
      
        <a name="framework"/>
        <div class="title">Framework information</div>
        <div class="section">
          <table class="info">
            <tr>
              <th>Pustefix version:</th>
              <td><xsl:value-of select="/pfxinternals/framework/@version"/></td>
            </tr>
          </table>
        </div>
      
        <a name="environment"/>
        <div class="title">Environment properties</div>
        <div class="section">
          <table class="info">
            <tr>
              <th>fqdn:</th>
              <td><xsl:value-of select="/pfxinternals/environment/properties/property[@name='fqdn']"/></td>
            </tr>
            <tr>
              <th>machine:</th>
              <td><xsl:value-of select="/pfxinternals/environment/properties/property[@name='machine']"/></td>
            </tr>
            <tr>
              <th>mode:</th>
              <td><xsl:value-of select="/pfxinternals/environment/properties/property[@name='mode']"/></td>
            </tr>
            <tr>
              <th>uid:</th>
              <td><xsl:value-of select="/pfxinternals/environment/properties/property[@name='uid']"/></td>
            </tr>
          </table>
        </div>
        
        <a name="jvm"/>
        <div class="title">JVM information</div>
        <div class="section">
          <table class="layout">
            <xsl:variable name="max">
              <xsl:for-each select="/pfxinternals/jvm/memory">
                <xsl:sort select="@max" data-type="number" order="descending" />
                <xsl:if test="position()=1">
                  <xsl:value-of select="@max" />
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="factor" select="384 div $max"/>
            <tr>
              <td>
                <xsl:apply-templates select="/pfxinternals/jvm/memory[@type='heap']">
                  <xsl:with-param name="title">Heap memory usage</xsl:with-param>
                  <xsl:with-param name="factor" select="$factor"/>
                </xsl:apply-templates>
              </td>
              <td>
                <xsl:apply-templates select="/pfxinternals/jvm/memory[@type='permgen']">
                  <xsl:with-param name="title">PermGen space usage</xsl:with-param>
                  <xsl:with-param name="factor" select="$factor"/>
                </xsl:apply-templates>
              </td>
              <td>
                <table class="info">
                  <tr>
                    <th>Garbage collector</th>
                    <th>Collections</th>
                    <th>Time</th>
                  </tr>
                  <xsl:for-each select="/pfxinternals/jvm/gc">
                    <tr>
                      <td><xsl:value-of select="@name"/></td>
                      <td class="num"><xsl:value-of select="@count"/></td>
                      <td class="num"><xsl:value-of select="format-number(@time div 1000, '0.0')"/>s</td>
                    </tr>
                  </xsl:for-each>
                </table>
              </td>
            </tr>
          </table>
        </div>
        
        <a name="modules"/>
        <div class="title">Loaded modules</div>
        <div class="section">
          <xsl:apply-templates select="/pfxinternals/modules"/>
        </div>
        
        <a name="actions"/>
        <div class="title">Actions</div>
        <div class="section">
          <table class="actions">
            <tr>
              <td><a href="{$__contextpath}/pfxinternals?action=reload">Schedule webapp reload</a></td>
              <td><a href="javascript:removeCookies()">Remove cookies</a></td>
            </tr>
            <tr>   
              <td><a href="{$__contextpath}/pfxinternals?action=invalidate">Invalidate all running sessions</a></td>
            </tr>
          </table>
        </div>
        
        <xsl:apply-templates select="/pfxinternals/messages"/>
      
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="memory">
    <xsl:param name="title"/>
    <xsl:param name="factor"/>
    <table class="info">
      <tr>
        <th class="title" colspan="2"><xsl:value-of select="$title"/>:</th>
      </tr>
      <tr>
        <th>Used:</th>
        <td class="num"><xsl:value-of select="format-number(@used div 1024 div 1024, '0.0')"/> M</td>
      </tr>
      <tr>
        <th>Committed:</th>
        <td class="num"><xsl:value-of select="format-number(@committed div 1024 div 1024, '0.0')"/> M</td>
      </tr>
      <tr>
        <th>Max:</th>
        <td class="num"><xsl:value-of select="format-number(@max div 1024 div 1024, '0.0')"/> M</td>
      </tr>
    </table>
    <table class="barchart">
      <tr>
        <td><hr style="background:red; width: {@used * $factor}px; height:20px;"/></td>
        <td><hr style="background:yellow; width: {(@committed - @used) * $factor}px; height:20px;"/></td>
        <td><hr style="background:green; width: {(@max - @committed) * $factor}px; height:20px;"/></td>
      </tr>
    </table>
  </xsl:template>
  
  <xsl:template match="modules">
    <table class="info">
      <xsl:variable name="rows" select="ceiling(count(module) div 3)"/>
      <xsl:for-each select="module[position() &lt;= $rows]">
        <xsl:variable name="pos" select="position()"/>
        <tr>
          <td>
            <xsl:apply-templates select="."/>
          </td>
          <td> 
            <xsl:choose>
	          <xsl:when test="../module[$pos + $rows]">
	            <xsl:apply-templates select="../module[$pos + $rows]"/>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
          </td>
          <td>
            <xsl:choose>
	          <xsl:when test="../module[$pos + $rows +$rows]">
	            <xsl:apply-templates select="../module[$pos + $rows + $rows]"/>
              </xsl:when>
              <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="module">
    <xsl:value-of select="@name"/>
    <xsl:choose>
      <xsl:when test="@url">
        <span class="liveresources" title="Live resources in {@url}">&#11089;</span>
      </xsl:when>
      <xsl:otherwise>
        <span class="liveresources" title="No live resources">&#11090;</span>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="@classurl">
        <span class="liveclasses" title="Live classes in {@classurl}">&#11089;</span>
      </xsl:when>
      <xsl:otherwise>
        <span class="liveclasses" title="No live classes">&#11090;</span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="messages">
    <a name="messages"/>
    <div class="title">Messages</div>
    <div class="section">
      <table class="info">
        <tr>
          <th>Date</th>
          <th>Level</th>
          <th>Message</th>
        </tr>
        <xsl:apply-templates/>
      </table>
    </div>
  </xsl:template>

  <xsl:template match="message">
    <tr>
      <td><xsl:value-of select="@date"/></td>
      <td><xsl:value-of select="@level"/></td>
      <td><xsl:apply-templates/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>