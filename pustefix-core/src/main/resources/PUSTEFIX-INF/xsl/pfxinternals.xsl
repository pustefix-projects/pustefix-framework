<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:enc="java.net.URLEncoder" version="1.0">

  <xsl:param name="__contextpath"/>
  <xsl:param name="category"/>
  
  <xsl:key name="priokey" match="/pfxinternals/modules/defaultsearch/module" use="@priority"/>

  <xsl:template match="/">
  <xsl:message><xsl:copy-of select="/"/></xsl:message>
    <html>
      <head>
        <title>Pustefix internals</title>
        <style type="text/css">
          body {
            margin: 0;
            padding: 0;
            font-family: monospace;
            background: #fff url(../../modules/pustefix-core/img/background.png) 0 0 repeat-x;
          }
          div.header {
	        clear: both;
	        font-family: sans-serif;
	        min-width: 1024px;
          }
          div.header div.pagetitle {
            float: left;
            padding-left: 60px;
            padding-top: 30px;
            font-size: 36px;
            color: #fff;
            font-weight: 100;
          }
          div.header div.logo {
            float: left;
            padding-left: 10px;
          }
          
          
          div.content {
            clear: both;
           margin-left: 20px;
             margin-right: 20px;
             padding-top: 10px;
             min-width: 1024px;
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
            padding: 15px;
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
            border: 1px solid #ccc;
          }
          div.navi {
            margin-top: 10px;
            padding:6px;
            padding-left:0px;
            line-height: 150%;
            min-width: 1024px;
            font-family: sans-serif;
          }
          div.navi span {
            background: #666;
            padding:7px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            border: 1px solid #444;
            border-bottom: 0px solid #fff;
          }
          div.navi span.active {
            background: #999;
            padding:7px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            border: 1px solid #888;
            border-bottom: 0px solid #fff;
          }
          div.navi span a {
            text-decoration: none;
            color: white;
            font-family: sans-serif;
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
            color: #000; 
            font-weight: bold;
          }
          table.info td.num {
            text-align:right;
          }
          table.info th.title {
            font-weight: bold;
            color: #666;
            padding-bottom: 5px;
          }
          table.props {
            border-spacing: 0px;
            border: 0px solid #aaa;
            margin-bottom: 15px;
          }
          table.props th.caption {
            font-weight: bold;
            color: #222;
            border:0px;
            padding-left: 0px;
            padding-bottom: 6px;
          }
          table.props td, table.props th {
            text-align: left;
     		padding: 4px;
     		font-weight: normal;
     		border-top: 1px solid #ddd;
          }
          table.props tr.odd {
            background: #ededed;
          }
          table.props tr.even {
            background: #f3f3f3;
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
          div.subtitle {
            font-weight:bold;
            padding-top: 10px;
            padding-left: 10px;
          }
          img.live {
            vertical-align: middle;
            margin-left: 8px;
          }
          td.mod {
            border: 1px solid #ccc;
            border-radius: 5px;
            padding: 5px;
            background-color: #e4e4e4;
          }
          div.mod {
            border: 1px solid #ccc;
            border-radius: 5px;
            padding: 5px;
            background-color: #e4e4e4;
            margin: 5px;
          }
          table.defsearch {
            border-spacing: 10px;
            font-size: 85%;
          }
          table.defsearch td {padding-right: 0px;}
          
          <xsl:if test="$category='targets'">
          ul.targets {
            list-style-type: none; margin: 20px;
            border-left: 1px dotted black; margin:10px; padding:5px; margin-left:40px;
          }
          ul.targets li {
            margin: 10px;
          }
          span.target {
            
          }
          span.virtual {
            border: 1px solid black; border-radius: 10px; padding: 4px; margin: 6px;
          }
          span.leaf {
            border: 1px solid black; border-radius: 2px; padding: 4px; margin: 6px;
          }
          span.xml {
            border-color: #cc6666;
            background: #cc6666;
          }
          span.xsl {
            border-color: #9ecd9a;
            background: #9ecd9a;
          }
          span.xml_leaf{
            border-color: #cc6666;
            background: #cc6666;
          }
          span.xsl_leaf {
            border-color: #9ecd9a;
            background: #9ecd9a;
          }
          a.target {
            color:black;
            text-decoration:none;
          }
            span.inc {
            border-color: #bbb;
            background: #bbb;
            border: 1px solid #bbb; border-radius: 2px; padding-top: 3px; padding-bottom: 3px; margin: 0;
            margin-right: 5px;
          }
          span.inc_path {
            background: #bbb;
            padding: 3px;
            padding-right: 5px;
          }
          span.inc_part {
            background: #ddd;
            padding: 3px;
            padding-left: 5px;
          }
          div.deps {border: 1px dashed #aaa; border-radius: 5px; margin-top:20px;}
          table.targets tr td {
            vertical-align: top;
          }
          table.templates {
            border-spacing: 1px;
            border: 0px solid #bbb;
            font-size: 80%;
            padding-left: 40px;
          }
          table.templates th {
            background: #d2d2d2;
            font-weight: normal;
          }
          table.templates tr.odd {
            background: #ededed;
          }
          table.templates tr.even {
            background: #f3f3f3;
          }
          table.templates td, table.templates th {
            padding: 3px;
            padding-left: 5px;
            padding-right: 5px;
            border-top: 1px solid #ddd;
          }
          </xsl:if>
          
        </style>
        <script type="text/javascript">

          function removeCookies() {
            var d = 0;
            var c = document.cookie.split(";");
            for(var i=0; i &lt; c.length; i++) {
              var e = c[i].indexOf("=");
              var n = e > -1 ? c[i].substr(0,e) : c[i];
              if(n != "") {
                var path = location.pathname;
                while(path.length>0) {
                  document.cookie = n + "=;path=" + path + ";expires=Thu, 01 Jan 1970 00:00:00 GMT";
                  var ind = path.lastIndexOf('/');
                  if(ind > -1) {
                    path = path.substring(0, ind);
                  }
                }
                document.cookie = n + "=;path=/;expires=Thu, 01 Jan 1970 00:00:00 GMT";
                d++;
              }
            }
            alert("Removed " + d + " cookie" + (d==1 ? "" : "s"));
          }
          
         <xsl:if test="$category='targets'">
         function viewTarget(value) {
           var url = window.location.href;
           var ind = url.indexOf('?');
           if(ind > 0) url = url.substring(0, ind);
           url = url + "?target=" + encodeURIComponent(value);
           window.location.href = url;
         }
         </xsl:if>
          
        </script>
      </head>
      <body>
      
        <div class="header">
          <div class="logo"><img class="logo" src="{$__contextpath}/modules/pustefix-core/img/logo.png"/></div>
          <div class="pagetitle">Pustefix internals</div>
        </div>
    
        <div class="content">
          <div class="navi">
            <span><xsl:if test="$category='framework'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="framework">Framework</a></span>
            <span><xsl:if test="$category='environment'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="environment">Environment</a></span>
            <span><xsl:if test="$category='jvm'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="jvm">JVM</a></span>
            <span><xsl:if test="$category='system'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="system">System</a></span>
            <span><xsl:if test="$category='cache'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="cache">Cache</a></span>
            <span><xsl:if test="$category='modules'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="modules">Modules</a></span>
            <span><xsl:if test="$category='targets'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="targets">Targets</a></span>
            <span><xsl:if test="$category='actions'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="actions">Actions</a></span>
            <span><xsl:if test="$category='messages'"><xsl:attribute name="class">active</xsl:attribute></xsl:if><a href="messages">Messages</a></span>
          </div>
          
      
          <xsl:if test="not($category) or $category='framework'">
        <div class="section">
          <table class="info">
            <tr>
              <th>Pustefix version:</th>
              <td><xsl:value-of select="/pfxinternals/framework/@version"/></td>
            </tr>
            <tr>
              <th>SCM URL:</th>
              <td><a href="{/pfxinternals/framework/@scmurl}"><xsl:value-of select="/pfxinternals/framework/@scmurl"/></a></td>
            </tr>
            <tr>
              <th>Website:</th>
              <td><a href="http://pustefix-framework.org">http://pustefix-framework.org</a></td>
            </tr>
          </table>
        </div>
        </xsl:if>
      
        <xsl:if test="not($category) or $category='environment'">
        <div class="section">
          <table class="props">
            <tr>
              <th class="caption" colspan="2">Pustefix environment properties:</th>
            </tr>
            <xsl:for-each select="/pfxinternals/environment/properties/property">
              <xsl:sort select="@name"/>
              <tr>
                <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1">odd</xsl:when><xsl:otherwise>even</xsl:otherwise></xsl:choose></xsl:attribute>
                <th><xsl:value-of select="@name"/></th>
                <td><xsl:value-of select="."/>&#160;</td>
              </tr>
            </xsl:for-each>
          </table>
          <table class="props">
            <tr>
              <th class="caption" colspan="2">System properties:</th>
            </tr>
            <xsl:for-each select="/pfxinternals/system-properties/property">
              <xsl:sort select="@name"/>
              <tr>
                <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1">odd</xsl:when><xsl:otherwise>even</xsl:otherwise></xsl:choose></xsl:attribute>
                <th><xsl:value-of select="@name"/></th>
                <td><xsl:value-of select="."/>&#160;</td>
              </tr>
            </xsl:for-each>
          </table>
        </div>
        </xsl:if>
        
        <xsl:if test="not($category) or $category='jvm'">
        <div class="section">
          <table class="info">
            <tr>
              <th>Java version:</th>
              <td><xsl:value-of select="/pfxinternals/jvm/@version"/></td>
            </tr>
            <tr>
              <th>Jave home:</th>
              <td><xsl:value-of select="/pfxinternals/jvm/@home"/></td>
            </tr>
            <tr>
              <th>VM arguments:</th>
              <td>
                <xsl:for-each select="/pfxinternals/jvm/arguments/argument">
                  <xsl:value-of select="."/><xsl:text> </xsl:text>
                </xsl:for-each>
              </td>
            </tr>
          </table>
          <br/>
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
        </xsl:if>
        
        <xsl:if test="not($category) or $category='system'">
        <div class="section">
          <table class="layout">
            <xsl:variable name="max">
              <xsl:for-each select="/pfxinternals/system/*[name()='memory' or name()='swap']">
                <xsl:sort select="@total" data-type="number" order="descending" />
                <xsl:if test="position()=1">
                  <xsl:value-of select="@total" />
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="factor" select="384 div $max"/>
            <tr>
              <xsl:for-each select="/pfxinternals/system/*[name()='memory']">
              <td>
                <xsl:call-template name="show_free_memory">
                  <xsl:with-param name="title">Memory</xsl:with-param>
                  <xsl:with-param name="factor" select="$factor"/>
                </xsl:call-template>
              </td>
              </xsl:for-each>
              <xsl:for-each select="/pfxinternals/system/*[name()='swap']">
              <td>
                <xsl:call-template name="show_free_memory">
                  <xsl:with-param name="title">Swap space</xsl:with-param>
                  <xsl:with-param name="factor" select="$factor"/>
                </xsl:call-template>
              </td>
              </xsl:for-each>
            </tr>
          </table>
          <br/>
          <xsl:for-each select="/pfxinternals/system">
            <table class="info">
              <tr>
                <th class="title" colspan="2">System load:</th>
              </tr>
              <tr>
                <th>Load average for last minute:</th>
                <td class="num"><xsl:value-of select="format-number(@load, '0.00')"/></td>
              </tr>
              <tr>
                <th>Full load (available processors):</th>
                <td class="num"><xsl:value-of select="@processors "/></td>
             </tr>
           </table>
           <table class="barchart">
             <tr>
               <xsl:variable name="max">
                 <xsl:choose>
                   <xsl:when test="@load &gt; @processors"><xsl:value-of select="@load"/></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@processors"/></xsl:otherwise>
                 </xsl:choose>
               </xsl:variable>
               <xsl:variable name="nload">
                 <xsl:choose>
                   <xsl:when test="@load &gt; @processors"><xsl:value-of select="@processors"/></xsl:when>
                   <xsl:otherwise><xsl:value-of select="@load"/></xsl:otherwise>
                 </xsl:choose>
               </xsl:variable>
               <xsl:variable name="oload">
                 <xsl:choose>
                   <xsl:when test="@load &gt; @processors"><xsl:value-of select="@load - @processors"/></xsl:when>
                   <xsl:otherwise>0</xsl:otherwise>
                 </xsl:choose>
               </xsl:variable>
               <xsl:variable name="factor" select="256 div $max"/>
               <td><hr style="background:red; width: {$nload * $factor}px; height:20px;"/></td>
               <td><hr style="background:green; width: {(@processors - $nload) * $factor}px; height:20px;"/></td>
               <td><hr style="background:darkred; width: {$oload * $factor}px; height:20px;"/></td>
             </tr>
           </table>
         </xsl:for-each>
          <br/>
          <xsl:for-each select="/pfxinternals/system/filedescriptors">
            <table class="info">
              <tr>
                <th class="title" colspan="2">File descriptors:</th>
              </tr>
              <tr>
                <th>Open:</th>
                <td class="num"><xsl:value-of select="@open"/></td>
              </tr>
              <tr>
                <th>Max:</th>
                <td class="num"><xsl:value-of select="@max"/></td>
             </tr>
           </table>
           <table class="barchart">
             <tr>
               <xsl:variable name="factor" select="256 div @max"/>
               <td><hr style="background:red; width: {@open * $factor}px; height:20px;"/></td>
               <td><hr style="background:green; width: {(@max - @open) * $factor}px; height:20px;"/></td>
             </tr>
           </table>
         </xsl:for-each>
        </div>
        </xsl:if>
        
        <xsl:if test="not($category) or $category='cache'">
        <div class="section">
          <table>
            <tr>
          <xsl:apply-templates select="/pfxinternals/cachestatistic/cache">
            <xsl:with-param name="title">Heap memory usage</xsl:with-param>
          </xsl:apply-templates>
            </tr>
          </table>
        </div>
        </xsl:if>
        
        <xsl:if test="not($category) or $category='modules'">
        <div class="section">
          <xsl:apply-templates select="/pfxinternals/modules"/>
        </div>
        </xsl:if>
        
        <xsl:if test="not($category) or $category='targets'">
        <div class="section">
          <xsl:apply-templates select="/pfxinternals/targets"/>
        </div>
        </xsl:if>
        
        <xsl:if test="not($category) or $category='actions'">
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
        </xsl:if>
        
        <xsl:if test="not($category) or $category='messages'">
        <xsl:apply-templates select="/pfxinternals/messages"/>
        </xsl:if>
        
        </div>
      
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
  
  <xsl:template name="show_free_memory">
    <xsl:param name="title"/>
    <xsl:param name="factor"/>
    <table class="info">
      <tr>
        <th class="title" colspan="2"><xsl:value-of select="$title"/>:</th>
      </tr>
      <tr>
        <th>Used:</th>
        <td class="num"><xsl:value-of select="format-number((@total - @free) div 1024 div 1024, '0.0')"/> M</td>
      </tr>
      <tr>
        <th>Total:</th>
        <td class="num"><xsl:value-of select="format-number(@total div 1024 div 1024, '0.0')"/> M</td>
      </tr>
    </table>
    <table class="barchart">
      <tr>
        <td><hr style="background:red; width: {(@total - @free) * $factor}px; height:20px;"/></td>
        <td><hr style="background:green; width: {@free * $factor}px; height:20px;"/></td>
      </tr>
    </table>
  </xsl:template>
  
  <xsl:template match="cache">
    <xsl:param name="title"/>
    <td>
    <table class="info">
      <tr>
        <th class="title" colspan="2">Cache '<xsl:value-of select="@id"/>':</th>
      </tr>
      <tr>
        <th>Size/capacity:</th>
        <td class="num"><xsl:value-of select="@size"/>/<xsl:value-of select="@capacity"/></td>
      </tr>
    </table>
    <table class="barchart">
       <xsl:variable name="max">
              <xsl:for-each select="../cache">
                <xsl:sort select="@capacity" data-type="number" order="descending" />
                <xsl:if test="position()=1">
                  <xsl:value-of select="@capacity" />
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="factor" select="200 div $max"/>
      <tr>
        <td><hr style="background:#bbb; width: {@size * $factor}px; height:20px;"/></td>
        <td><hr style="background:#ddd; width: {(@capacity - @size) * $factor}px; height:20px;"/></td>
      </tr>
    </table>
    <table class="info">
      <tr>
        <th>Hit rate:</th>
        <td class="num"><xsl:value-of select="@hitrate"/>%</td>
      </tr>
    </table>
    <table class="barchart">
            <xsl:variable name="factor">2</xsl:variable>
      <tr>
        <td><hr style="background:green; width: {@hitrate * $factor}px; height:20px;"/></td>
        <td><hr style="background:red; width: {(100 - @hitrate) * $factor}px; height:20px;"/></td>
      </tr>
    </table>
    </td>
  </xsl:template>
  
  <xsl:template match="modules">
    <table class="info" style="border-spacing: 10px">
      <xsl:variable name="rows" select="ceiling(count(module) div 3)"/>
      <xsl:for-each select="module[position() &lt;= $rows]">
        <xsl:variable name="pos" select="position()"/>
        <tr>
          <td class="mod">
            <xsl:apply-templates select="."/>
          </td>
          <td class="mod"> 
            <xsl:choose>
	          <xsl:when test="../module[$pos + $rows]">
	            <xsl:apply-templates select="../module[$pos + $rows]"/>
              </xsl:when>
              <xsl:otherwise><xsl:attribute name="class"/></xsl:otherwise>
            </xsl:choose>
          </td>
          <td class="mod">
            <xsl:choose>
	          <xsl:when test="../module[$pos + $rows +$rows]">
	            <xsl:apply-templates select="../module[$pos + $rows + $rows]"/>
              </xsl:when>
              <xsl:otherwise><xsl:attribute name="class"/></xsl:otherwise>
            </xsl:choose>
          </td>
        </tr>
      </xsl:for-each>
    </table>
    <div class="subtitle">Default search chain: </div>
    <table class="defsearch"><tr>
      <td>
      <div class="mod">webapp</div>
      </td>
      <xsl:for-each select="defaultsearch/module[generate-id() = generate-id(key('priokey', @priority)[1])]">
        <td style="font-size: 200%; padding: 0px;">&#x21D2;</td>
        <td>
        <xsl:for-each select="key('priokey', @priority)">
        <div class="mod"><xsl:value-of select="@name"/><br/><xsl:value-of select="@filter"/></div>
        </xsl:for-each>
        </td>
      </xsl:for-each>
      
    </tr></table>
  </xsl:template>

  <xsl:template match="module">
    <xsl:value-of select="@name"/>
    <xsl:choose>
      <xsl:when test="@url">
        <img class="live" src="{$__contextpath}/modules/pustefix-core/img/changes-allow.png" title="Live resources in {@url}"/>
      </xsl:when>
      <xsl:otherwise>
        <img class="live" src="{$__contextpath}/modules/pustefix-core/img/changes-prevent.png" title="No live resources"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="@classurl">
        <img class="live" src="{$__contextpath}/modules/pustefix-core/img/changes-allow-grey.png" title="Live classes in {@classurl}"/>
      </xsl:when>
      <xsl:otherwise>
        <img class="live" src="{$__contextpath}/modules/pustefix-core/img/changes-prevent-grey.png" title="No live classes"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="messages">
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
  
  <xsl:template match="targets">
      <div>
        <xsl:apply-templates select="targetlist"/>
      </div>
      <table>
       <tr>
       <td valign="top">
        <ul>
            <xsl:apply-templates select="/pfxinternals/targets/target"/>
          </ul>
      </td>
      <td valign="top">
        <xsl:if test="/pfxinternals/targets/templates/template">
          <table class="templates" >
               <tr>
                <th align="left">Match pattern</th>
                <th align="left">Template name</th>
                <th align="left">System ID</th>
              </tr>
    	 	  <xsl:for-each select="/pfxinternals/targets/templates/template[@match and not(@match='*' or starts-with(@match, '/'))]">
                <xsl:sort select="@match"/>
                <xsl:sort select="@name"/>
                <xsl:sort select="../@url"/>
                <tr>
                  <xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 2 = 1">odd</xsl:when><xsl:otherwise>even</xsl:otherwise></xsl:choose></xsl:attribute>
                  <td><xsl:value-of select="@match"/>&#160;</td>
                  <td><xsl:value-of select="@name"/>&#160;</td>
                  <td><xsl:value-of select="../@url"/></td>
                </tr>
              </xsl:for-each>
              <xsl:variable name="tmpcnt" select="count(/pfxinternals/targets/templates/template[@match and not(@match='*' or starts-with(@match, '/'))])"/>
              <xsl:for-each select="/pfxinternals/targets/templates/template[not(@match)]">
                <xsl:sort select="@match"/>
                <xsl:sort select="@name"/>
                <xsl:sort select="../@url"/>
                <tr>
                  <xsl:attribute name="class"><xsl:choose><xsl:when test="($tmpcnt + position()) mod 2 = 1">odd</xsl:when><xsl:otherwise>even</xsl:otherwise></xsl:choose></xsl:attribute>
                  <td><xsl:value-of select="@match"/>&#160;</td>
                  <td><xsl:value-of select="@name"/>&#160;</td>
                  <td><xsl:value-of select="../@url"/></td>
                </tr>
              </xsl:for-each>
    	    </table>
    	    </xsl:if>
          </td></tr>
        </table>
         
  </xsl:template>

  <xsl:template match="targetlist">
    <select name="target" size="1" onChange="viewTarget(this.value)">
      <xsl:for-each select="target">
        <option><xsl:if test="@key = /pfxinternals/targets/target/@key"><xsl:attribute name="selected">true</xsl:attribute></xsl:if><xsl:value-of select="@key"/></option>
      </xsl:for-each>
    </select>
  </xsl:template>
  
  <xsl:template match="target">
    <li style="padding:3px;">
      <div class="target">
      <span class="target">
        <xsl:choose>
          <xsl:when test="target"><xsl:attribute name="class">virtual <xsl:value-of select="@type"/></xsl:attribute></xsl:when>
          <xsl:otherwise><xsl:attribute name="class">leaf <xsl:value-of select="@type"/></xsl:attribute></xsl:otherwise>
        </xsl:choose>
        <a class="target" href="?target={@key}"><xsl:value-of select="@key"/></a>
      </span>
      <a href="{$__contextpath}/pfxinternals?action=download&amp;resource={enc:encode(@resource,'utf-8')}">
      <img class="download" src="{$__contextpath}/modules/pustefix-core/img/download.png" title="Download/Open"/>
      </a>
      </div>
      <xsl:if test="target">
        <xsl:if test="@type='xsl'">
      		
      	</xsl:if>
        <ul class="targets">
          <xsl:apply-templates/>
        </ul>
      </xsl:if>
      
      
    </li>
  </xsl:template>
  
    <xsl:template match="dependencies">
     <div class="deps">
     <xsl:apply-templates/>
     </div>
     
  </xsl:template>
  
  <xsl:template match="file">
    <li class="dep file">
      <xsl:value-of select="@path"/>
    </li>
  </xsl:template>
  
  <xsl:template match="include">
    <li class="dep include">
    <span class="inc"><span class="inc_path"><xsl:value-of select="@path"/></span><span class="inc_part"><xsl:value-of select="@part"/></span></span>
     <a href="{$__contextpath}/pfxinternals?action=download&amp;resource={enc:encode(@path,'utf-8')}">
      <img class="download" src="{$__contextpath}/modules/pustefix-core/img/download.png" title="Download/Open"/>
      </a>
    </li>
  </xsl:template>
  
  <xsl:template match="image">
    <li class="dep image">
      <span class="inc"><span class="inc_path"><xsl:value-of select="@path"/></span></span>
      <a href="{$__contextpath}/pfxinternals?action=download&amp;resource={enc:encode(@path,'utf-8')}">
        <img class="download" src="{$__contextpath}/modules/pustefix-core/img/download.png" title="Download/Open"/>
      </a>
    </li>
  </xsl:template>
  
</xsl:stylesheet>