<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="__contextpath"/>

  <xsl:key name="priokey" match="/pfxinternals/modules/defaultsearch/module" use="@priority"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Pustefix internals</title>
        <style type="text/css">
          body {
            font-family: monospace;
            padding: 5px; 
            background: #ddd; 
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
            display: none;
            background: #fff;
            padding: 10px;
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
            border: 1px solid #ccc;
          }
          div.title {
            background: #777;
            color: #fff; 
            font-size: 130%; 
            font-style:italic; 
            padding: 3px; 
            padding-left: 10px; 
            margin-top: 15px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            border: 0px solid #333;
            cursor: pointer;
          }
          div.header {
            background: #000; 
            color: #fff; 
            font-size: 170%; 
            font-style:italic; 
            padding: 3px; 
            padding-left: 10px;
            border-top-left-radius: 10px;
            border-top-right-radius: 10px;
            border: 0px solid #333;
            margin-bottom: 0px;
          }
          div.navisection {
            padding:10px;
            background: #fff;
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
          }
          div.navisection div {
            padding-bottom:10px;
          }
          div.navisection a {
            padding:10px; 
            padding-left:0px;
            font-size: 125%;
          }
          div.navisection span {
            text-decoration: underline;
            padding: 0px;
            cursor: pointer;
            font-size: 85%;
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
          div.subtitle {
            font-weight:bold;
            padding-top: 10px;
            padding-left: 10px;
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
          
          function toggle(id) {
            var selem = document.getElementById("s_"+id);
            var telem = document.getElementById("t_"+id);
            if(selem.style.display == 'none' || selem.style.display == '') {
              selem.style.display = 'block';
              telem.innerHTML= "-" + telem.innerHTML.substring(1);
            } else {
              selem.style.display = 'none';
              telem.innerHTML= "+" + telem.innerHTML.substring(1);
            }
            saveState();
          }
          
          function expand() {
            var elems = document.getElementsByTagName('div');
            for(var i=0; i &lt; elems.length; i++) {
              if(elems[i].className == 'title') {
                elems[i].innerHTML= "-" + elems[i].innerHTML.substring(1);
              } else if(elems[i].className == 'section') {
                elems[i].style.display = 'block';
              }
            }
            saveState();
          }
          
          function collapse() {
            var elems = document.getElementsByTagName('div');
            for(var i=0; i &lt; elems.length; i++) {
              if(elems[i].className == 'title') {
                elems[i].innerHTML= "+" + elems[i].innerHTML.substring(1);
              } else if(elems[i].className == 'section') {
                elems[i].style.display = 'none';
              }
            }
            saveState();
          }
          
          function activate(id) {
            var telem = document.getElementById("t_"+id);
            var selem = document.getElementById("s_"+id);
            if(selem.style.display == 'none' || selem.style.display == '') {
              selem.style.display = 'block';
              telem.innerHTML= "+" + telem.innerHTML.substring(1);
            }
            saveState();
          }
          
          function saveState() {
            var elems = document.getElementsByTagName('div');
            var expanded = [];
            for(var i=0; i &lt; elems.length; i++) {
              if(elems[i].className == 'section' &amp;&amp; elems[i].style.display == 'block') {
                expanded.push(elems[i].id.substring(2));
              }
            }
            var val = ("" + expanded).replace(/,/g,"#");
            document.cookie = "pfxinternal_expanded=" + val + ";path=" + location.pathname;
          }
          
          function restoreState() {
            var regexp = /\s*pfxinternal_expanded\s*=\s*([^;]+)\s*/;
            regexp.exec(document.cookie);
            var value = RegExp.$1;
            if(value) {
              var expanded = value.split("#");
              for(var i=0; i &lt; expanded.length; i++) {
              toggle(expanded[i]);
              }
            }
          }
          
          window.onload = restoreState; 
        </script>
      </head>
      <body>
      
        <div class="header">Pustefix internals</div>
      
        <div class="navisection">
          <div>
          <a href="#framework" onclick="activate('framework')">Framework information</a>
          <a href="#environment" onclick="activate('environment')">Environment properties</a>
          <a href="#jvm" onclick="activate('jvm')">JVM information</a>
          <a href="#cache" onclick="activate('cache')">Cache statistics</a>
          <a href="#modules" onclick="activate('modules')">Loaded modules</a>
          <a href="#actions" onclick="activate('actions')">Actions</a>
          <a href="#messages" onclick="activate('messages')">Messages</a>
          </div>
          <div>
          <span onclick="expand()">Expand all</span> / <span onclick="collapse()">Collapse all</span>
          </div>
        </div>
      
        <a name="framework"/>
        <div id="t_framework" class="title" onclick="toggle('framework')">+ Framework information</div>
        <div id="s_framework" class="section">
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
      
        <a name="environment"/>
        <div id="t_environment" class="title" onclick="toggle('environment')">+ Environment properties</div>
        <div id="s_environment" class="section">
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
        <div class="title" id="t_jvm" onclick="toggle('jvm')">+ JVM information</div>
        <div class="section" id="s_jvm">
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
        
        <a name="cache"/>
        <div class="title" id="t_cache" onclick="toggle('cache')">+ Cache statistics</div>
        <div class="section" id="s_cache">
          <table>
            <tr>
          <xsl:apply-templates select="/pfxinternals/cachestatistic/cache">
            <xsl:with-param name="title">Heap memory usage</xsl:with-param>
          </xsl:apply-templates>
            </tr>
          </table>
        </div>
        
        <a name="modules"/>
        <div class="title" id="t_modules" onclick="toggle('modules')">+ Loaded modules</div>
        <div class="section" id="s_modules">
          <xsl:apply-templates select="/pfxinternals/modules"/>
        </div>
        
        <a name="actions"/>
        <div class="title" id="t_actions" onclick="toggle('actions')">+ Actions</div>
        <div class="section" id="s_actions">
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
    <div class="title" id="t_messages" onclick="toggle('messages')">+ Messages</div>
    <div class="section" id="s_messages">
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