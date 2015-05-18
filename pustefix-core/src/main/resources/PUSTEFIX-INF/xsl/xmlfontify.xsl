<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core" 
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback"
                xmlns:pxsl="http://pustefixframework.org/org.pustefixframework.xslt.ExtensionElements"
                extension-element-prefixes="pxsl" version="1.1">

  <xsl:param name="__context__"/>
  <xsl:param name="__sitemap"/>
  <xsl:param name="sitemap" select="$__sitemap"/>
  <xsl:param name="__target_gen"/>
  <xsl:param name="__contextpath"/>
  <xsl:param name="__querystring"/>

  <xsl:template match="/">
<xsl:choose>
  <xsl:when test="contains($__querystring,'pagestatus')">
    <table cellpadding="4" cellspacing="0" style="padding-left:20px;">
      <tr>
        <td style="border-bottom: 1px solid black;">Page name</td>
        <td style="border-bottom: 1px solid black;">Alias</td>
        <td style="border-bottom: 1px solid black;">Visited?</td>
        <td style="border-bottom: 1px solid black;">Accessible?</td>
        <td style="border-bottom: 1px solid black;">Authorized?</td>
      </tr>
      <xsl:call-template name="render_pages">
        <xsl:with-param name="thepages" select="$sitemap/page"/>
      </xsl:call-template>
    </table>
  </xsl:when>
  <xsl:otherwise>
    <html>
      <head>
        <title>Last DOM</title>
        <style type="text/css">
          body { font-family: monospace; }
          .datatable { border-spacing: 0px; color: #000000; padding-left: 20px; }
          .datatable td { padding: 4px; }
          .datatable th { padding: 4px; text-align: left; font-weight: normal; border-bottom: 1px solid black; }
          .rowsep { border-bottom: 1px dotted #888888; }
          table.info { padding-left: 20px; }
          table.info th, td { text-align: left; padding: 4px; }
          table.info td {color: #666666; font-weight: normal;}
          table.info th {color: #000000; font-weight: normal;}
          .assistent { background: #eeeeee; position: fixed; left: 0; top: 0; right: 0; width: 100%; border-bottom: 2px solid #000000; padding: 5px; z-index: 1; }
          .assistent input#xpath { border: 1px solid #ccc; padding: 2px; font-family: sans; font-size: 16px; width: 700px; margin-right: 15px; }
          .assistent input#xpath.valid { background: url("/modules/pustefix-core/img/valid.png") no-repeat scroll 8px 4px white; padding-left: 30px; }
          .assistent input#xpath.invalid { background: url("/modules/pustefix-core/img/invalid.png") no-repeat scroll 8px 4px white; padding-left: 30px; }
          .assistent div#autocompletion { display: none; position: absolute; background: #ffffff; left: 180px; border: 1px solid #cccccc; border-top: none; margin-top: -4px; padding: 5px; width: 688px; }
          .assistent a { color: #000000; }
          .assistent label { margin-left: 5px; margin-right: 10px; }
          ul a:hover { text-decoration: underline; cursor: pointer; }
          pre.errors {display:none; position:absolute; background:#FF9999; color:#000; border:red; border-radius: 10px; padding: 10px;}

          .error {
            background-color: #ffff00;
          }

          .pretty-print {
            font-family: monospace;
            font-size: 13px;
          }

          .indent {
            margin-left: 1em;
          }

          .xml-tag-name {
            color: #dd5522;
            font-weight: bold;
          }

          .xml-attribute-name {
            color: #0000cc;
          }

          .xml-attribute-value {
            color: #22aa00;
          }

          .xml-comment {
            color: #666666;
          }

          .dimmed .xml-tag-name,
          .dimmed .xml-attribute-name,
          .dimmed .xml-attribute-value,
          .dimmed .xml-comment {
            color: #aaaaaa;
          }

          .collapsible {          
            position: relative;
          }

          .collapsible > .xml-tag {
            cursor: hand;
          }

          .collapsible:before {
            width: 0; 
            height: 0; 
            border-top: 4px solid transparent;
            border-bottom: 4px solid transparent;            
            border-left: 4px solid #bebdbd;
            position: absolute;
            content: '';
            left: -15px;
            top: 4px;
          }

          .collapsible > .xml-line,
          .collapsible > .xml-line-end,
          .collapsible.expanded > .xml-tag > .xml-tag-end {
            display: none;
          }

          .collapsible.expanded > .xml-line,
          .collapsible.expanded> .xml-line-end {
            display: block;
          }

          .collapsible.expanded:before {
            width: 0; 
            height: 0; 
            border-left: 4px solid transparent;
            border-right: 4px solid transparent;
            border-top: 4px solid #bebdbd;
            position: absolute;
            content: '';
            left: -15px;
            top: 6px;
          }

          .collapsible.active:before {
            border-left-color: #000;
          }

          .collapsible.active.expanded:before {
            border-left-color: transparent;
            border-top-color: #000;
          }

          #dom-tree-clone-wrapper-bottom,
          #dom-tree-clone-wrapper-top {
            position: absolute;
            background-color: #fff;
            top: 0;
            left: auto;
            z-index: 2;
            width: 99%;
          }

          #dom-tree-clone-blocker-top,
          #dom-tree-clone-blocker-bottom {
            background-color: #fff;
            opacity: 0.75;
            position: absolute;
            z-index: 1;
            width: 99%;
            top: 0;
            left: 0;
          }
          
          #pagestatus_loader {
            padding-left: 10px;
            text-decoration: underline;
            cursor: pointer;
          }
          @keyframes color_change {
            from { color: #000; }
            to { color: #aaa; }
          }
          #pagestatus_progress {
            padding-left: 10px;
            -webkit-animation: color_change 1s infinite alternate;
            -moz-animation: color_change 1s infinite alternate;
            -ms-animation: color_change 1s infinite alternate;
            -o-animation: color_change 1s infinite alternate;
            animation: color_change 1s infinite alternate;
          }
        </style>
        <script type="text/javascript">
          function toggleErrors(errElem, id) {
            var elem = document.getElementById(id);
            if(elem.style.display=='block') {
              elem.style.display='none';
              errElem.innerHTML="[+]";
            } else {
              elem.style.display='block';
              errElem.innerHTML="[-]";
            }
          }
          function pageStatusCallback() {
            document.getElementById('pagestatus_content').innerHTML = this.responseText;
          }
          function loadPageStatus() {
            document.getElementById('pagestatus_content').innerHTML = "&lt;div id='pagestatus_progress'&gt;Loading page status...&lt;/div&gt;";
            var req = new XMLHttpRequest();
            req.onload = pageStatusCallback;
            var href = window.location.href;
            var ind = href.indexOf("#");
            if(ind > 0) {
              href = href.substring(0,ind);
            }
            href += "&amp;pagestatus"
            req.open("GET", href, true);
            req.send();
          }
        </script>
      </head>
      <body>
        <div id="dom-tree-clone-blocker-top" />
        <div id="dom-tree-clone-blocker-bottom" />
        <div id="dom-tree-clone-wrapper-top">
          <div id="dom-tree-clone-top" class="pretty-print" />
        </div>
        <div id="dom-tree-clone-wrapper-bottom">
          <div id="dom-tree-clone-bottom" class="pretty-print" />
        </div>
        <div class="assistent"> 
          <label for="xpath">XPath-Expression [<a href="http://www.w3schools.com/xpath/xpath_syntax.asp" target="_blank">?</a>]</label> <input id="xpath" class="valid" value="/formresult" />
          <a href="javascript:void(0);" id="expand">Expand all</a> | <a href="javascript:void(0);" id="collapse">Collapse all</a> | <a href="#XMLData">XML data</a> | <a href="#IWrappers">IWrappers</a> | <a href="#PageStatus">Page status</a>
          <div id="autocompletion"></div>
        </div>
        <br /><br />
        <h1 id="XMLData">XML data:</h1>
        <div id="dom-tree" class="pretty-print">
          <xsl:apply-templates mode="static_disp" select="/"/>
        </div>
        <xsl:call-template name="render_iwrappers">
          <xsl:with-param name="tree" select="/"/>
        </xsl:call-template>
        <xsl:call-template name="render_roles"/>
        <br/>
        <h1 id="PageStatus">Page status:</h1>
        <div id="pagestatus_content">
          <div id="pagestatus_loader" onclick="loadPageStatus();return false;">Load page status</div>
        </div>
        <br/><br/>
        <script type="text/javascript" src="{$__contextpath}/modules/pustefix-core/script/dom.js"></script>
      </body>
    </html>
  </xsl:otherwise>
</xsl:choose>
  </xsl:template>

  <xsl:template name="render_pages">
    <xsl:param name="thepages"/>
    <xsl:param name="ind"/>
    <xsl:for-each select="$thepages">
      <xsl:call-template name="render_page">
        <xsl:with-param name="ind" select="$ind"/>
      </xsl:call-template>
      <xsl:call-template name="render_pages">
        <xsl:with-param name="thepages" select="./page"/>
        <xsl:with-param name="ind"><xsl:value-of select="$ind"></xsl:value-of>&#160;&#160;</xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>
 
  <xsl:template name="render_page">
    <xsl:param name="ind"/>
    <xsl:variable name="vis_retval" select="callback:isVisited($__context__, string(@name))"/>
    <xsl:variable name="acc_retval">
      <pxsl:fail-safe>
        <xsl:value-of select="callback:isAccessible($__context__, $__target_gen, string(@name))"/>
      </pxsl:fail-safe>
    </xsl:variable>
    <xsl:variable name="auth_retval" select="callback:checkAuthorization($__context__, string(@name))"/>
<!--    <xsl:variable name="acc_retval">1</xsl:variable>-->
    <xsl:variable name="visited"> 
      <xsl:choose>
        <xsl:when test="$vis_retval = 1">&#9745;</xsl:when>
        <xsl:when test="$vis_retval = -1"><span style="color:#aaaaaa;">?</span></xsl:when>
        <xsl:otherwise>&#9744;</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="visible">
      <xsl:choose>
        <xsl:when test="$acc_retval = 1">
        <span style="color: #33cc33;">true</span>
        </xsl:when>
        <xsl:when test="$acc_retval = -1">
        <span style="color: #aaaaaa;">?</span>
        </xsl:when>
        <xsl:when test="$acc_retval = 0">
          <span style="color: #cc3333;">false</span>
        </xsl:when>
        <xsl:otherwise>
          <span style="color: red;">error
            <span style="cursor: pointer;" title="Show exception" onclick="toggleErrors(this, 'error_{generate-id()}')">[+]</span>
          </span>
          <pre id="error_{generate-id()}" class="errors"><xsl:value-of select="$acc_retval"/></pre>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="authorized">
      <xsl:choose>
        <xsl:when test="$auth_retval = 1">
          <span style="color: #33cc33;">&#11043;</span>&#160;
        </xsl:when>
        <xsl:when test="$auth_retval = 2">
          <span style="color: #cccc33;">&#11043;</span>&#160;
        </xsl:when>
        <xsl:when test="$auth_retval = 3">
          <span style="color: #cc3333;">&#11043;</span>&#160;
        </xsl:when>
        <xsl:otherwise>
          <span style="color: #cccccc;">&#11043;</span>&#160;
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <tr>
      <td>  
        <xsl:value-of select="$ind"/><xsl:value-of select="@name"/>
      </td>
      <td>
        <xsl:value-of select="@alias"/>
      </td>
      <td align="center" style="font-family: sans;"><xsl:copy-of select="$visited"/></td>
      <td align="center"><xsl:copy-of select="$visible"/></td>
      <td align="center"><xsl:copy-of select="$authorized"/></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="render_iwrappers">
    <xsl:param name="tree"/>
    <xsl:variable name="iwrappers" select="callback:getIWrappers($__context__,/,'')"/>
    <xsl:if test="count($iwrappers/iwrappers/iwrapper) > 0">
      <br/>
      <h1 id="IWrappers">IWrappers:</h1>
      <table cellspacing="0" class="datatable">
        <tr>
          <th><b>Parameter</b></th>
          <th><b>Occurrence</b></th>
          <th><b>Frequency</b></th>
          <th><b>Type</b></th>
          <th><b>checkactive?</b></th>
          <th><b>active?</b></th>               
        </tr>
        <xsl:for-each select="$iwrappers/iwrappers/iwrapper">
          <xsl:variable name="iwrp" select="callback:getIWrapperInfo($__context__,/,'',@prefix)/iwrapper"/>
          <xsl:variable name="prefix" select="@prefix"/>
          <xsl:variable name="class" select="@class"/>
          <tr style="background-color:#eeeeee;">
            <td colspan="4">&#160;<small><b>&#8227; <xsl:value-of select="$prefix"/></b> (<xsl:value-of select="$class"/>)</small></td>
            <td align="center" style="font-family: sans;">
              <xsl:choose>
                <xsl:when test="@checkactive = 'false'">&#9745;</xsl:when>
                <xsl:otherwise>&#9744;</xsl:otherwise>
              </xsl:choose>
            </td>
            <td align="center" style="font-family: sans;">
              <xsl:choose>
                <xsl:when test="$tree/formresult/wrapperstatus/wrapper[@prefix=$prefix]/@active = 'true'">&#9745;</xsl:when>
                <xsl:otherwise>&#9744;</xsl:otherwise>
              </xsl:choose>
            </td>
          </tr> 
          <xsl:if test="not($iwrp/param)">
            <tr><td class="rowsep" colspan="5" align="center"><small>w/o parameters</small></td></tr>
          </xsl:if>
          <xsl:for-each select="$iwrp/param">
            <tr>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
                <xsl:value-of select="@name"/>
              </td>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
                <xsl:value-of select="@occurrence"/>
              </td>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
                <xsl:value-of select="@frequency"/>
              </td>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
                <xsl:value-of select="@type"/>
              </td>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>&#160;         
              </td>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>&#160;
              </td>
              <td>
                <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>&#160;
              </td>
            </tr>
          </xsl:for-each> 
        </xsl:for-each>
      </table>
    </xsl:if>
    <xsl:if test="count($iwrappers/iwrappers/actions) > 0">
      <br/>
      <h1>Actions:</h1>
      <table class="datatable" cellspacing="1">
        <tr>
          <th><b>Name</b></th>
          <th><b>submit</b></th>
          <th><b>retrieve</b></th>
          <th><b>pageflow</b></th>
          <th><b>forcestop</b></th>
          <th><b>jumptopage</b></th>
          <th><b>jumptopageflow</b></th>  
        </tr>
      <xsl:for-each select="$iwrappers/iwrappers/actions/action">
        <tr valign="top">
          <td class="rowsep">
            <b><xsl:value-of select="@name"/></b>
          </td>
          <td class="rowsep">
            <xsl:if test="not(./submit/string)">&#160;</xsl:if>
            <xsl:for-each select="./submit/string">
              <xsl:value-of select="./text()"/><br/>
            </xsl:for-each>
          </td>
          <td class="rowsep">
            <xsl:if test="not(./retrieve/string)">&#160;</xsl:if>
            <xsl:for-each select="./retrieve/string">
              <xsl:value-of select="./text()"/><br/>
            </xsl:for-each>
          </td>
          <td class="rowsep">
            <xsl:if test="not(./@pageFlow)">&#160;</xsl:if>
            <xsl:value-of select="@pageFlow"/>
          </td>
          <td class="rowsep">
            <xsl:if test="not(./@forceStop)">&#160;</xsl:if>
            <xsl:value-of select="@forceStop"/>
          </td>
          <td class="rowsep">
            <xsl:if test="not(./@jumpToPage)">&#160;</xsl:if>
            <xsl:value-of select="@jumpToPage"/>
          </td>
          <td class="rowsep">
            <xsl:if test="not(./@jumpToPageFlow)">&#160;</xsl:if>
            <xsl:value-of select="@jumpToPageFlow"/>
          </td>
        </tr>
      </xsl:for-each>
      </table>
    </xsl:if> 
  </xsl:template>

  <xsl:template name="render_roles">
    <xsl:variable name="roles" select="callback:getAllDefinedRoles($__context__,/)"/>
    <xsl:if test="$roles/roles/role">
      <br/>
      <h1>Roles:</h1>
      <table cellspacing="0" class="datatable">
        <tr>
          <th>Name</th>
          <th>Active?</th>
          <th>Initial?</th>
        </tr>
        <xsl:for-each select="$roles/roles/role">
        <tr>
          <td>
            <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
            <span>
              <xsl:value-of select="@name"/>
            </span>
          </td>
          <td align="center" style="font-family: sans;">
            <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
            <xsl:choose>
              <xsl:when test="@current = 'true'">&#9745;</xsl:when>
              <xsl:otherwise>&#9744;</xsl:otherwise>
            </xsl:choose>
          </td>
          <td align="center" style="font-family: sans;">
            <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
            <xsl:choose> 
              <xsl:when test="@initial = 'true'">&#9899;</xsl:when>
              <xsl:otherwise>&#9898;</xsl:otherwise> 
            </xsl:choose>
          </td>
        </tr>
        </xsl:for-each>
      </table>
    </xsl:if>
  </xsl:template>

  <xsl:template match="node()" mode="static_disp">
    <xsl:variable name="dim">
      <xsl:choose>
        <xsl:when test="ancestor-or-self::wrapperstatus[1] and generate-id(ancestor-or-self::wrapperstatus[1]) = generate-id(/formresult/wrapperstatus)">true</xsl:when>
        <xsl:when test="ancestor-or-self::pageflow[1] and generate-id(ancestor-or-self::pageflow[1]) = generate-id(/formresult/pageflow)">true</xsl:when>
        <xsl:when test="ancestor-or-self::tenant[1] and generate-id(ancestor-or-self::tenant[1]) = generate-id(/formresult/tenant)">true</xsl:when>
        <xsl:when test="ancestor-or-self::formhiddenvals[1] and generate-id(ancestor-or-self::formhiddenvals[1]) = generate-id(/formresult/formhiddenvals)">true</xsl:when>
        <xsl:when test="generate-id(current()) = generate-id(/formresult/formerrors)">true</xsl:when>
        <xsl:when test="ancestor-or-self::formvalues[1] and generate-id(ancestor-or-self::formvalues[1]) = generate-id(/formresult/formvalues)">true</xsl:when>
        <xsl:when test="ancestor-or-self::iwrapperinfo[1] and generate-id(ancestor-or-self::iwrapperinfo[1]) = generate-id(/formresult/iwrapperinfo)">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="error">
      <xsl:choose>
        <xsl:when test="name() = 'formerrors' and count(./node()) &gt; 0">true</xsl:when>
        <xsl:when test="ancestor::formerrors">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="position">
      <xsl:if test="count(parent::node()/child::*[name() = name(current())]) &gt; 1">
        <xsl:value-of select="count(preceding-sibling::node()[name() = name(current())]) + 1" />
      </xsl:if>
    </xsl:variable>

    <div>
      <xsl:attribute name="data-xpath">
        <xsl:text>/</xsl:text><xsl:value-of select="name()" />
        <xsl:choose>
          <xsl:when test="@id">
            <xsl:text>[@id = '</xsl:text><xsl:value-of select="@id" /><xsl:text>']</xsl:text>
          </xsl:when>
          <xsl:when test="number($position)">
            <xsl:text>[</xsl:text><xsl:value-of select="$position" /><xsl:text>]</xsl:text>
          </xsl:when>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="class">
        <xsl:text>xml-line indent</xsl:text>
        <xsl:if test="*">
          <xsl:text> collapsible expanded</xsl:text>
        </xsl:if>
        <xsl:if test="$dim = 'true'">
          <xsl:text> dimmed</xsl:text>
        </xsl:if>  
      </xsl:attribute>
      <span class="xml-tag">
        <xsl:text>&lt;</xsl:text>
        <!-- tag name -->
        <span>
          <xsl:attribute name="class">
            <xsl:text>xml-tag-name</xsl:text>
            <xsl:if test="$error = 'true'">
              <xsl:text> error</xsl:text>
            </xsl:if>    
          </xsl:attribute>
          <xsl:value-of select="name()" />
        </span>
        <!-- tag attributes -->
        <xsl:apply-templates select="@*" mode="static_disp" />
        <!-- end tag -->
        <xsl:choose>
          <xsl:when test="count(./node()) = 0">
            <xsl:text> /</xsl:text>
          </xsl:when>
          <xsl:when test="not(./text() or ./comment())">
            <span class="xml-tag-end">
              <xsl:text> </xsl:text>
              <span>
                <xsl:attribute name="class">
                  <xsl:text>xml-tag-name</xsl:text>
                  <xsl:if test="$error = 'true'">
                    <xsl:text> error</xsl:text>
                  </xsl:if>     
                </xsl:attribute>
                <xsl:text>/</xsl:text>
              </span>
            </span>
          </xsl:when>
        </xsl:choose>
        <xsl:text>&gt;</xsl:text>
      </span>
      <xsl:if test="count(./node()) &gt; 0">
        <xsl:apply-templates mode="static_disp" />
        <span>
          <xsl:attribute name="class">
            <xsl:text>xml-tag</xsl:text>
            <xsl:if test="*">
              <xsl:text> xml-line-end</xsl:text>
            </xsl:if>
          </xsl:attribute>
          <xsl:text>&lt;</xsl:text>
          <span class="xml-tag-name">/<xsl:value-of select="name()" /></span>
          <xsl:text>&gt;</xsl:text>
        </span>
      </xsl:if>
    </div>   
  </xsl:template>
  
  <xsl:template match="text()" mode="static_disp">
    <xsl:value-of select="normalize-space(current())"/>
  </xsl:template>

  <xsl:template match="@*" mode="static_disp">
    <span class="xml-attribute">
      <xsl:text> </xsl:text>
      <span class="xml-attribute-name">
        <xsl:attribute name="data-xpath">/@<xsl:value-of select="name()" /></xsl:attribute>
        <xsl:value-of select="name()" />
      </span>
      <xsl:text>="</xsl:text>
      <span class="xml-attribute-value">
        <xsl:if test="not(name() = 'id')">
          <xsl:attribute name="data-xpath">[@<xsl:value-of select="name()" /> = '<xsl:value-of select="." />']</xsl:attribute>
        </xsl:if>
        <xsl:value-of select="." />
      </span>
      <xsl:text>"</xsl:text>
    </span>
  </xsl:template>

  <xsl:template match="comment()" mode="static_disp">
    <span class="xml-comment">
      &lt;!--<xsl:value-of select="."/>--&gt;
    </span>
  </xsl:template>

  
</xsl:stylesheet>
