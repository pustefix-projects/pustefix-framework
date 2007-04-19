<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">

  <xsl:param name="maincontentpath"/>
  
  <xsl:template match="pfx:maincontent">
    <xsl:param name="noerror" select="@noerror"/>  
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path">
          <xsl:value-of select="@path"/>
        </xsl:when>
        <xsl:when test="string($maincontentpath) != ''"><xsl:value-of select="$maincontentpath"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$product"/>/txt/pages</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="prefix">
      <xsl:choose>
        <xsl:when test="@prefix">
          <xsl:value-of select="@prefix"/>
        </xsl:when>
        <xsl:otherwise>main_</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="postfix">
      <xsl:choose>
        <xsl:when test="@postfix">
          <xsl:value-of select="@postfix"/>
        </xsl:when>
        <xsl:otherwise>.xml</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="part">
      <xsl:choose>
        <xsl:when test="@part">
          <xsl:value-of select="@part"/>
        </xsl:when>
        <xsl:otherwise>content</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="pfx:include">
      <xsl:with-param name="href"><xsl:value-of select="$path"/>/<xsl:value-of select="$prefix"/><xsl:value-of select="$page"/><xsl:value-of select="$postfix"/></xsl:with-param>
      <xsl:with-param name="part" select="$part"/>
      <!-- this is tricky to understand --> 
      <xsl:with-param name="computed_inc">true</xsl:with-param>
      <xsl:with-param name="noerr" select="$noerror"/>      
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="pfx:editconsole">
    <xsl:if test="$prohibitEdit = 'no'">
      <xsl:choose>
        <xsl:when test="@classicmode='true'">
          <form target="_top">
            <table cellpadding="0" cellspacing="0" border="0">
              <tr>
                <td width="1">
                  <ixsl:choose>
                    <ixsl:when test="$__editmode='admin'">
                      <a target="_top">
                        <ixsl:attribute name="href">
                          <ixsl:value-of select="$__uri"/>?__editmode=none</ixsl:attribute>
                        <img border="0" alt="Disable edit mode" title="Switch edit mode OFF" src="{{$__contextpath}}/core/img/do_noedit.gif"/>
                      </a>
                    </ixsl:when>
                    <ixsl:otherwise>
                      <a target="_top">
                        <ixsl:attribute name="href">
                          <ixsl:value-of select="$__uri"/>?__editmode=admin</ixsl:attribute>
                        <img border="0" alt="Enable edit mode" title="Switch edit mode ON" src="{{$__contextpath}}/core/img/do_edit.gif"/>
                      </a>
                    </ixsl:otherwise>
                  </ixsl:choose>
                </td>
                <td align="left">
                  <a target="pfixcore_xml_source__">
                    <ixsl:attribute name="href">
                      <ixsl:value-of select="$__uri"/>?__xmlonly=1</ixsl:attribute>
                    <img border="0" alt="Show XML" title="Show last XML tree" src="{{$__contextpath}}/core/img/show_xml.gif"/></a>
                </td>
              </tr>
              <tr>
                <td nowrap="nowrap" colspan="2" style="font-family: Verdana,Sans; font-size: 10px; background-color: black; color: white; padding-left: 5px; padding-right: 2px;">
                  P: <ixsl:value-of select="$page"/>
                </td>
              </tr>
              <tr>
                <td nowrap="nowrap" colspan="2" style="font-family: Verdana,Sans; font-size: 10px; background-color: black; color: white; padding-left: 5px; padding-right: 2px;">
                  F: <ixsl:value-of select="$pageflow"/>
                </td>
              </tr>
            </table>
          </form>
        </xsl:when>
        <xsl:otherwise>
          <script type="text/javascript">
            var de_schlund_pfixcore_console_drag_start_x = 0;
            var de_schlund_pfixcore_console_drag_start_y = 0;
            var de_schlund_pfixcore_console_saved_width = 0;
            var de_schlund_pfixcore_console_drag_object = null;
            
            function de_schlund_pfixcore_console_drag_start(element, event) {
              var posx = document.all ? window.event.clientX : event.pageX;
              var posy = document.all ? window.event.clientY : event.pageY;
              de_schlund_pfixcore_console_drag_object = element;
              de_schlund_pfixcore_console_drag_start_x = posx - element.offsetLeft;
              de_schlund_pfixcore_console_drag_start_y = posy - element.offsetTop;
              if (typeof event.stopPropagation == "function") { 
                event.stopPropagation(); 
              } else if (typeof event.cancelBubble == "boolean") {
                event.cancelBubble = true;
              }
              if (typeof event.preventDefault == "function") {
                event.preventDefault();
              }
              // event.preventDefault for IE
              return false;
            }
            
            function de_schlund_pfixcore_console_drag_stop() {
              de_schlund_pfixcore_console_drag_object = null;
            }
            
            function de_schlund_pfixcore_console_drag_move(event) {
              var posx = document.all ? window.event.clientX : event.pageX;
              var posy = document.all ? window.event.clientY : event.pageY;
              if(de_schlund_pfixcore_console_drag_object != null) {
                de_schlund_pfixcore_console_drag_object.style.right = "";
                de_schlund_pfixcore_console_drag_object.style.left = (posx - de_schlund_pfixcore_console_drag_start_x) + "px";
                de_schlund_pfixcore_console_drag_object.style.top = (posy - de_schlund_pfixcore_console_drag_start_y) + "px";
                if (typeof event.stopPropagation == "function") { 
                  event.stopPropagation(); 
                } else if (typeof event.cancelBubble == "boolean") {
                  event.cancelBubble = true;
                }
                if (typeof event.preventDefault == "function") {
                  event.preventDefault();
                }
              }
              // event.preventDefault for IE
              return false;
            }
            
            function de_schlund_pfixcore_console_minimize(element, event) {
              element.style.visibility='hidden';
              element.style.left = element.offsetLeft + "px";
              element.style.right = "";
              var oldWidth = element.offsetWidth;
              de_schlund_pfixcore_console_saved_width = oldWidth;
              element.childNodes[1].style.display='none';
              element.childNodes[0].style.display='block';
              var newWidth = element.offsetWidth;
              element.style.left = (element.offsetLeft + oldWidth - newWidth) + "px";
              element.style.visibility='visible';
              if (typeof event.stopPropagation == "function") { 
                event.stopPropagation(); 
              } else if (typeof event.cancelBubble == "boolean") {
                event.cancelBubble = true;
              }
            }
            
            function de_schlund_pfixcore_console_maximize(element, event) {
              element.style.visibility='hidden';
              element.style.left = element.offsetLeft + "px";
              element.style.right = "";
              var oldWidth = element.offsetWidth;
              element.childNodes[0].style.display='none';
              element.childNodes[1].style.display='block';
              var newWidth = element.offsetWidth;
              if (de_schlund_pfixcore_console_saved_width > newWidth) {
                newWidth = de_schlund_pfixcore_console_saved_width;
              }
              element.style.left = (element.offsetLeft + oldWidth - newWidth) + "px";
              element.style.visibility='visible';
              if (typeof event.stopPropagation == "function") { 
                event.stopPropagation(); 
              } else if (typeof event.cancelBubble == "boolean") {
                event.cancelBubble = true;
              }
            }
            
            if (typeof document.addEventListener == "function") {
              document.addEventListener("mouseup", de_schlund_pfixcore_console_drag_stop, true);
              document.addEventListener("mousemove", de_schlund_pfixcore_console_drag_move, true);
            } else if (typeof window.attachEvent == "object") {
              document.attachEvent("onmouseup", de_schlund_pfixcore_console_drag_stop);
              document.attachEvent("onmousemove", de_schlund_pfixcore_console_drag_move);
            }
          </script>
          <div id="de_schlund_pfixcore_console_divelement" style="position:fixed;top:20px;right:20px;background-color:#aabbee;padding:5px;opacity:0.9;-moz-opacity:0.9;filter:alpha(opacity=90)">
            <div style="display:none">
              <div onMouseDown="de_schlund_pfixcore_console_drag_start(this.parentNode.parentNode, event);" style="text-align:right;">
                <img border="0" alt="Maximize" src="{{$__contextpath}}/core/img/console_maximize.gif" style="cursor:pointer;" onClick="de_schlund_pfixcore_console_maximize(this.parentNode.parentNode.parentNode, event);" onMouseDown="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true;"/>
                <img border="0" alt="Close" src="{{$__contextpath}}/core/img/console_close.gif" style="margin-left:5px;cursor:pointer;" onClick="this.parentNode.parentNode.parentNode.style.display='none';" onMouseDown="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true;"/>
              </div>
            </div>
            <div style="font-family:sans-serif;font-size:10pt;font-weight:bold;color:#ffffff;">
              <div onMouseDown="de_schlund_pfixcore_console_drag_start(this.parentNode.parentNode, event);" style="text-align:right;">
                <img border="0" alt="Minimize" src="{{$__contextpath}}/core/img/console_minimize.gif" style="cursor:pointer;" onClick="de_schlund_pfixcore_console_minimize(this.parentNode.parentNode.parentNode, event);" onMouseDown="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true;"/>
                <img border="0" alt="Close" src="{{$__contextpath}}/core/img/console_close.gif" style="margin-left:5px;cursor:pointer;" onClick="this.parentNode.parentNode.parentNode.style.display='none';" onMouseDown="if (event.stopPropagation) event.stopPropagation(); else if (typeof event.cancelBubble != 'undefined') event.cancelBubble = true;"/>
              </div>
              <div style="text-align:center;margin-top:5px">
                <ixsl:choose>
                  <ixsl:when test="$__editmode='admin'">
                    <a target="_top">
                      <ixsl:attribute name="href">
                        <ixsl:value-of select="$__uri"/>?__editmode=none</ixsl:attribute>
                      <img border="0" alt="Disable edit mode" title="Switch edit mode OFF" src="{{$__contextpath}}/core/img/console_editoff.gif"/>
                    </a>
                  </ixsl:when>
                  <ixsl:otherwise>
                    <a target="_top">
                      <ixsl:attribute name="href">
                        <ixsl:value-of select="$__uri"/>?__editmode=admin</ixsl:attribute>
                      <img border="0" alt="Enable edit mode" title="Switch edit mode ON" src="{{$__contextpath}}/core/img/console_editon.gif"/>
                    </a>
                  </ixsl:otherwise>
                </ixsl:choose>
                <a target="pfixcore_xml_source__">
                  <ixsl:attribute name="href">
                  <ixsl:value-of select="$__uri"/>?__xmlonly=1</ixsl:attribute>
                  <img border="0" alt="Show XML" title="Show last XML tree" src="{{$__contextpath}}/core/img/console_showxml.gif" style="margin-left:5px"/>
                </a>
                <xsl:if test="@webserviceconsole='true'">
                  <a target="pfixcore_web_service_monitor">
                    <ixsl:attribute name="href">
                      <ixsl:value-of select="concat($__contextpath, '/xml/webservice;',$__sessid,'?monitor')"/>
                    </ixsl:attribute>
                    <img border="0" alt="Webservice monitor" title="Show webservice monitor" src="{{$__contextpath}}/core/img/console_webservicemonitor.gif" style="margin-left:5px"/>
                  </a>
                  <a target="pfixcore_web_service_admin">
                    <ixsl:attribute name="href">
                      <ixsl:value-of select="concat($__contextpath, '/xml/webservice;',$__sessid,'?admin')"/>
                    </ixsl:attribute>
                    <img border="0" alt="Webservice admin" title="Show webservice admin" src="{{$__contextpath}}/core/img/console_webserviceadmin.gif" style="margin-left:5px"/>
                  </a>
                </xsl:if>
              </div>
              <div style="padding-left:5px;padding-right:5px">
                <span title="Page">P: <ixsl:value-of select="$page"/></span>
                <br/>
                <span title="Pageflow">F: <ixsl:value-of select="$pageflow"/></span>
                <br/>
                <span title="Variant">
                  V: 
                  <ixsl:choose>
                    <ixsl:when test="/formresult/@requested-variant">
                      <ixsl:value-of select="/formresult/@requested-variant"/>
                    </ixsl:when>
                    <ixsl:otherwise>
                      <span style="">None</span>
                    </ixsl:otherwise>
                  </ixsl:choose>
                </span>
              </div>
            </div>
            <script type="text/javascript">
              // Special hack for IE
              if (document.all) {
                de_schlund_pfixcore_console_divelement.style.position = "absolute";
              }
            </script>
          </div>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pfx:webserviceconsole">
    <xsl:if test="$prohibitEdit = 'no'">
      <span>
        Web service tools:
        <a target="pfixcore_web_service_monitor">
          <ixsl:attribute name="href">
            <ixsl:value-of select="concat($__contextpath, '/xml/webservice;',$__sessid,'?monitor')"/>
          </ixsl:attribute>
          Monitor
        </a>
        <a target="pfixcore_web_service_admin">
          <ixsl:attribute name="href">
            <ixsl:value-of select="concat($__contextpath, '/xml/webservice;',$__sessid,'?admin')"/>
          </ixsl:attribute>
          Admin
        </a>
      </span>
    </xsl:if>
  </xsl:template>

  <xsl:template match="pfx:blank">
    <img src="/core/img/blank.gif" width="1" height="1" border="0" alt="">
      <xsl:copy-of select="@*"/>
    </img>
  </xsl:template>
    
</xsl:stylesheet>
