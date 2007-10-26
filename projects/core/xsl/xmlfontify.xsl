<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:callback="xalan://de.schlund.pfixcore.util.TransformerCallback">

  <xsl:param name="__context__"/>
  <xsl:param name="__navitree"/>
  <xsl:param name="navitree" select="$__navitree"/>

  <xsl:template match="/">
    <html>
      <head>
        <title>Last DOM</title>
        <style type="text/css">
          body {font-family: monospace; }
          .bracket            {color: #0000cc;}
          .attribute          {color: #0000cc;}
          .tag                {color: #dd5522;}
          .value              {color: #22aa00;}
          .comment            {color: #666666;}
          .dimmed             {color: #aaaaaa;}
          .error              {background-color: #ffff00;}
          .datatable {border-spacing:0px;color:#000000;padding-left:20px;}
          .datatable td {padding:4px;}
          .datatable th {padding:4px;text-align:left;font-weight:normal;border-bottom: 1px solid black;}
          .rowsep {border-bottom: 1px dotted #888888;}
        </style>
      </head>
      <body>
        <h1>XML data:</h1>
        <xsl:apply-templates mode="static_disp" select="/"/>
        <h1>Page status:</h1>
        <table cellpadding="4" cellspacing="0" style="padding-left:20px;">
        <tr>
        <td style="border-bottom: 1px solid black;">Page name</td>
        <td style="border-bottom: 1px solid black;">Handler</td>
        <td style="border-bottom: 1px solid black;">Visited?</td>
        <td style="border-bottom: 1px solid black;">Accessible?</td></tr>
        <xsl:call-template name="render_pages">
          <xsl:with-param name="thepages" select="$navitree/page"/>
        </xsl:call-template>
        </table>
        <xsl:call-template name="render_iwrappers"/>
        <br/>
      </body>
    </html>
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
    <xsl:variable name="acc_retval" select="callback:isAccessible($__context__, string(@name))"/>
<!--    <xsl:variable name="acc_retval">1</xsl:variable>-->
    <xsl:variable name="visited"> 
      <xsl:choose>
        <xsl:when test="$vis_retval = 1">&#8226;</xsl:when>
        <xsl:when test="$vis_retval = -1"><span style="color:#aaaaaa;">?</span></xsl:when>
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
        <xsl:otherwise>
        <span style="color: #cc3333;">false</span></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <tr>
      <td>  
        <xsl:value-of select="$ind"/><xsl:value-of select="@name"/>
      </td>
      <td><span style="color:#aaaaaa;"><xsl:value-of select="@handler"/></span></td>
      <td align="center"><xsl:copy-of select="$visited"/></td>
      <td align="center"><xsl:copy-of select="$visible"/></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="render_iwrappers">
    <h1>IWrappers:</h1>
    <table cellspacing="0" class="datatable">
      <tr>
        <th>Prefix</th>
        <th>Param name</th>
        <th>Occurrence</th>
        <th>Frequency</th>
        <th>Type</th>
      </tr>
      <xsl:for-each select="callback:getIWrappers($__context__,/,'')/iwrappers/iwrapper">
        <xsl:variable name="iwrp" select="callback:getIWrapperInfo($__context__,/,'',@prefix)/iwrapper"/>
        <xsl:variable name="prefix" select="@prefix"/>
        <xsl:for-each select="$iwrp/param">
          <tr>
            <td>
              <xsl:if test="position()=last()"><xsl:attribute name="class">rowsep</xsl:attribute></xsl:if>
              <xsl:value-of select="$prefix"/>
            </td>
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
          </tr>
        </xsl:for-each> 
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="*" mode="static_disp">
    <xsl:param name="ind">  </xsl:param>
    <xsl:param name="break">true</xsl:param>
    <xsl:param name="bold">true</xsl:param>
    <xsl:variable name="dim">
      <xsl:choose>
<!--         <xsl:when test="ancestor-or-self::navigation[1] and generate-id(ancestor-or-self::navigation[1]) = generate-id(/formresult/navigation)">true</xsl:when> -->
        <xsl:when test="ancestor-or-self::pageflow[1] and generate-id(ancestor-or-self::pageflow[1]) = generate-id(/formresult/pageflow)">true</xsl:when>
        <xsl:when test="ancestor-or-self::formhiddenvals[1] and generate-id(ancestor-or-self::formhiddenvals[1]) = generate-id(/formresult/formhiddenvals)">true</xsl:when>
        <xsl:when test="generate-id(current()) = generate-id(/formresult/formerrors)">true</xsl:when>
        <xsl:when test="ancestor-or-self::formvalues[1] and generate-id(ancestor-or-self::formvalues[1]) = generate-id(/formresult/formvalues)">true</xsl:when>
        <xsl:when test="ancestor-or-self::iwrappergroups[1] and generate-id(ancestor-or-self::iwrappergroups[1]) = generate-id(/formresult/iwrappergroups)">true</xsl:when>
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
    <xsl:variable name="tagclass"> 
      <xsl:choose> 
        <xsl:when test="$dim = 'false'">
          <xsl:choose>
            <xsl:when test="$error = 'false'">tag</xsl:when>
            <xsl:otherwise>tag error</xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>dimmed</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="attrclass">
      <xsl:choose> 
        <xsl:when test="$dim = 'false'">attribute</xsl:when>
        <xsl:otherwise>dimmed</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="valueclass">
      <xsl:choose>
        <xsl:when test="$dim = 'false'">value</xsl:when>
        <xsl:otherwise>dimmed</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:if test="$break='false'">
      <br/>
    </xsl:if>
    <xsl:value-of select="$ind"/>
    <span class="bracket">
      <xsl:text>&lt;</xsl:text>
    </span>
    <span class="{$tagclass}">
      <xsl:if test="$bold = 'true' and $dim = 'false'">
        <xsl:attribute name="style">font-weight: bold;</xsl:attribute>
      </xsl:if>
    <xsl:value-of select="name()"/></span>
    <xsl:for-each select="@*">
      <xsl:text> </xsl:text>
      <span class="{$attrclass}"><xsl:value-of select="name()"/></span>
      <xsl:text>="</xsl:text><span class="{$valueclass}"><xsl:value-of select="."/></span>
      <xsl:text>"</xsl:text>
    </xsl:for-each>
    <span class="bracket"><xsl:if test="count(./node()) = 0">/</xsl:if>&gt;</span>
    <xsl:apply-templates mode="static_disp">
      <xsl:with-param name="bold">
        <xsl:choose>
          <xsl:when test="count(ancestor::node()) &gt; 1">false</xsl:when>
          <xsl:otherwise>true</xsl:otherwise>
        </xsl:choose>
        </xsl:with-param>
        <xsl:with-param name="ind">
        <xsl:value-of select="$ind"/>    </xsl:with-param>
      <xsl:with-param name="break">false</xsl:with-param>
    </xsl:apply-templates>
    <xsl:if test="not(count(./node()) = 0)">
      <xsl:if test="count(./*) &gt; 0">
        <br/>
        <xsl:value-of select="$ind"/>
      </xsl:if>
      <span class="bracket">&lt;/</span>
      <span class="{$tagclass}">
      <xsl:if test="$bold = 'true' and $dim = 'false'">
        <xsl:attribute name="style">font-weight: bold;</xsl:attribute>
      </xsl:if>
      <xsl:value-of select="name()"/></span>
      <span class="bracket">&gt;</span>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="static_disp">
    <xsl:value-of select="normalize-space(current())"/>
  </xsl:template>

  <xsl:template match="comment()" mode="static_disp">
    <br/> <span class="comment">&lt;!--<xsl:value-of select="."/>--&gt;</span>
  </xsl:template>

  

</xsl:stylesheet>
