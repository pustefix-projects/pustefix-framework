<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:pfx="http://www.schlund.de/pustefix/core"
                xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">
  
  <xsl:template match="staticdisplay" name="staticname">
    <ixsl:if>
      <xsl:attribute name="test"><xsl:value-of select="@path"/></xsl:attribute>
      <table width="100%" style="background: #ffffff; border-style: ridge; border-width: 2px;">
        <tr>
          <td><ixsl:apply-templates mode="static_disp">
              <xsl:attribute name="select"><xsl:value-of select="@path"/></xsl:attribute>
            </ixsl:apply-templates>
          </td>
        </tr>
      </table>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="productselection">
    <ul class="jobs">
      <ixsl:for-each select="/formresult/cr_editorsession/allproducts/product">
        <li>
          <pfx:button normalclass="editor_submit" page="prodselect" mode="force">
            <pfx:command  name="SELWRP">prodsel</pfx:command>
            <pfx:argument name="__pageflow">Editor</pfx:argument>
            <pfx:argument name="prodsel.Name"><ixsl:value-of select="./@name"/></pfx:argument>
            <ixsl:value-of select="./@comment"/>
          </pfx:button>
        </li>
      </ixsl:for-each>
    </ul>
  </xsl:template>


  <xsl:template match="displayallpages">
    <table width="100%" class="editor_sidebar_content">
      <ixsl:for-each select="/formresult/allpages//page">
        <tr>
          <td>
            <ixsl:if test="/formresult/currentpageinfo[@name = current()/@name]">
              <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
            </ixsl:if>
            <a>
              <ixsl:attribute name="name"><ixsl:value-of select="./@name"/></ixsl:attribute>
            </a>
            <ixsl:variable name="ind">
              <ixsl:for-each select="ancestor::page">&#160;&#160;&#160;&#160;&#160;</ixsl:for-each>
            </ixsl:variable>
            <ixsl:copy-of select="$ind"/>
            <pfx:button page="pages" mode="force">
              <pfx:argument name="psel.Page"><ixsl:value-of select="@name"/></pfx:argument>
              <pfx:anchor   frame="leftnavi"><ixsl:value-of select="@name"/></pfx:anchor>
              <ixsl:value-of select="@name"/>
            </pfx:button>
          </td>
          <td class="editor_dim" nowrap="nowrap" width="1%">[<ixsl:value-of select="@handler"/>]</td>
        </tr>
      </ixsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="displayallsessions">
    <table class="editor_box" cellspacing="0" width="100%" style="padding:0px;">
      <tr>
        <td class="editor_head">
          <b>SessionId</b>
        </td>
        <td class="editor_head">
          <b>Creation time</b>
        </td>
        <td class="editor_head">
          <b>Last Access</b>
        </td>
        <td class="editor_head">
          <b>No. of Req.</b>
        </td>
        <td class="editor_head">
          <b>Editor-User</b>
        </td>
      </tr>
      <ixsl:for-each select="/formresult/allsessions/session">
        <ixsl:variable name="class">
          <ixsl:choose>
            <ixsl:when test="(number(@num) mod 2) = 0">editor_even_row</ixsl:when>
            <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
          </ixsl:choose>
        </ixsl:variable>
        <tr>
          <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
          <td style="padding:1px;">
            <ixsl:value-of select="./@sessid"/>
          </td>
          <td style="padding:1px;">
            <ixsl:value-of select="./@createdtext"/>
          </td>
          <td style="padding:1px;">
            <ixsl:value-of select="./@lastaccesstext"/>
          </td>
          <td style="padding:1px;">
            <ixsl:value-of select="./@hits"/>
          </td>
          <td style="padding:1px;">
            <ixsl:choose>
              <ixsl:when test="./@editoruserid">
                <ixsl:variable name="alt">
                    <ixsl:value-of select="concat(@editorusername, ' (Phone: ', @editoruserphone, ')', ' -- Product: ', @editorproduct)"/>
                </ixsl:variable>
                <ixsl:value-of select="./@editoruserid"/>&#160;
                <pfx:image src="/core/editor/img/info.gif" style="vertical-align: middle">
                  <ixsl:attribute name="alt">
                    <ixsl:value-of select="$alt"/>
                  </ixsl:attribute>
                  <ixsl:attribute name="title">
                    <ixsl:value-of select="$alt"/>
                  </ixsl:attribute>
                </pfx:image>
              </ixsl:when>
              <ixsl:otherwise>
                N/A
              </ixsl:otherwise>
            </ixsl:choose>
          </td>
        </tr>
        <tr style="padding:0px;">
          <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
          <td colspan="5" style="padding:1px;" class="editor_small">
            <ixsl:for-each select="./step">
              <ixsl:if test="not(@counter = '1')"> -> </ixsl:if>
              <ixsl:value-of select="@stylesheet"/>
              <ixsl:if test="not(@mult = '1')">(<ixsl:value-of select="@mult"/>)</ixsl:if>
            </ixsl:for-each>
          </td>
        </tr>
      </ixsl:for-each>
      <tr>
        <td colspan="5">
          <hr/>
        </td>
      </tr>
      <tr>
        <td style="padding: 5px;" colspan="5" align="right">
          <pfx:button page="sessionmanager" normalclass="editor_submit" mode="force">Update</pfx:button>
        </td>
      </tr>
    </table>
  </xsl:template>


  <!-- Render all editor users in a table -->
  <xsl:template match="displayallusers">
    <table cellspacing="0" class="editor_box" width="100%" style="padding:0px;">
      <tr>
        <td class="editor_head">
          <b>UserId</b>
        </td>
        <td class="editor_head">
          <b>Username</b>
        </td>
        <td class="editor_head" colspan="2">
          <b>Sect/Phone</b>
        </td>
        <td align="right" class="editor_head">
          <b><pfx:label name="deluser.Id">Delete user</pfx:label></b>
        </td>
        </tr>
      <ixsl:for-each select="/formresult/allusers/user">
        <ixsl:variable name="class">
          <ixsl:choose>
            <ixsl:when test="(number(@count) mod 2) = 0">editor_even_row</ixsl:when>
            <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
          </ixsl:choose>
        </ixsl:variable>
        <tr>
          <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
          <td nowrap="nowrap" style="padding: 5px">
            <ixsl:value-of select="@id"/>
          </td> 
          <td style="padding: 5px">
            <ixsl:value-of select="@name"/>
          </td>
          <td style="padding: 5px">
            <ixsl:value-of select="@sect"/> (Phone: <ixsl:value-of select="@phone"/>)
          </td>
          <td align="right" style="padding: 5px">
            <pfx:button normalclass="editor_submit" page="editorusermanager" mode="force">
              <pfx:command  name="SELWRP">seluser</pfx:command>
              <pfx:argument name="seluser.User"><ixsl:value-of select="@id"/></pfx:argument>Edit</pfx:button>
          </td>
          <td align="right" style="padding: 5px">
            <pfx:xinp type="check" name="deluser.Id">
              <ixsl:attribute name="value"><ixsl:value-of select="@id"/></ixsl:attribute>
            </pfx:xinp>
          </td>
        </tr>
      </ixsl:for-each>
      <tr>
        <td style="padding: 1px;" colspan="6">
          <hr/>
        </td>
      </tr>
      <tr>
        <td style="padding: 5px;" colspan="6" align="right">
          <pfx:xinp class="editor_submit" type="submit" value="Delete selected">
            <pfx:command name="SELWRP">deluser</pfx:command>
          </pfx:xinp>
        </td>
      </tr>
    </table>
  </xsl:template>
  
  <xsl:template match="displayalllocks">
    <ixsl:choose>
      <ixsl:when test="/formresult/alllocks/lock">
        <table cellspacing="0" class="editor_box" width="100%" style="padding:0px;">
          <tr>
            <td class="editor_head">
              <b>Resource</b>
            </td>
            <td class="editor_head">
              <b>Product</b>
            </td>
            <td colspan="2" class="editor_head">
              <b>Locked by:</b>
            </td>
          </tr>
          <ixsl:for-each select="/formresult/alllocks/lock">
            <ixsl:variable name="class">
              <ixsl:choose>
                <ixsl:when test="(number(@count) mod 2) = 0">editor_even_row</ixsl:when>
                <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
              </ixsl:choose>
            </ixsl:variable>
            <tr>
              <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
              <td nowrap="nowrap" style="padding: 5px">
                <ixsl:value-of select="@auxpath"/>
                <ixsl:if test="@type = '[text]'">@<ixsl:value-of select="@auxpart"/>@<ixsl:value-of select="@auxproduct"/></ixsl:if>
              </td> 
              <td style="padding: 5px">
                <ixsl:value-of select="@product"/>
              </td>
              <td style="padding: 5px">
                <ixsl:value-of select="@username"/> (Phone: <ixsl:value-of select="@phone"/>)
              </td>
              <td align="right" style="padding: 5px">
                <pfx:button normalclass="editor_submit" page="lockmanager" mode="force">
                  <pfx:command  name="SELWRP">delete</pfx:command>
                  <pfx:argument name="delete.Id"><ixsl:value-of select="@id"/></pfx:argument>Delete</pfx:button>
              </td>
            </tr>
          </ixsl:for-each>
          <tr>
            <td style="padding: 1px;" colspan="4">
              <hr/>
            </td>
          </tr>
          <tr>
            <td style="padding: 5px;" colspan="4" align="right">
              <pfx:button page="lockmanager" normalclass="editor_submit" mode="force">Update</pfx:button>
            </td>
          </tr>
        </table>
      </ixsl:when>
      <ixsl:otherwise>
        <table class="editor_box" width="100%">
          <tr>
            <td align="center">
              <div class="editor_box editor_warn">No locks are currently registered.</div>
            </td>
          </tr>
          <tr>
            <td align="right">
              <br/><pfx:button page="lockmanager" normalclass="editor_submit" mode="force">Update</pfx:button>
            </td>
          </tr>
        </table>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>
  
  <xsl:template match="displayallimages">
    <table width="100%" class="editor_sidebar_content">
      <ixsl:for-each select="/formresult/allimages/directory">
        <tr>
          <td class="editor_sidebar_box" nowrap="nowrap">
            Directory: <b><ixsl:value-of select="@name"/></b>
          </td>
        </tr>
        <ixsl:for-each select="./image">
          <tr>
            <td nowrap="nowrap">
              <ixsl:if test="/formresult/currentimageinfo[@path = current()/@path]">
                <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
              </ixsl:if>
              <a>
                <ixsl:attribute name="name"><ixsl:value-of select="./@path"/></ixsl:attribute>
              </a>
              <ixsl:if test="@missing = 'true'"><span class="editor_missing_img">!&#160;</span></ixsl:if>
              <pfx:button page="images" frame="_top" target="_top" mode="force">
                <pfx:command  name="SELWRP">imgsel</pfx:command>
                <pfx:command  name="SELWRP">upload</pfx:command>
                <pfx:argument name="imgsel.Path"><ixsl:value-of select="./@path"/></pfx:argument>
                <pfx:anchor   frame="left_navi"><ixsl:value-of select="./@path"/></pfx:anchor>
                <ixsl:value-of select="@name"/>
              </pfx:button>
            </td>
          </tr>
        </ixsl:for-each>
        <ixsl:if test="following-sibling::directory">
          <tr>
            <td><blank style="display:block;" height="5px"/></td>
          </tr>
        </ixsl:if>
      </ixsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="displayallcommons">
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path"><xsl:value-of select="@path"/></xsl:when>
        <xsl:otherwise>/formresult/allcommons</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <table width="100%" class="editor_sidebar_content">
      <ixsl:for-each select="{$path}/directory">
        <tr>
          <td class="editor_sidebar_box"  nowrap="nowrap">
            Directory: <b><ixsl:value-of select="@name"/></b>
          </td>
        </tr>
        <ixsl:for-each select="./path">
          <tr>
            <td nowrap="nowrap">
              <a>
                <ixsl:attribute name="name"><ixsl:value-of select="@name"/></ixsl:attribute>
              </a>
              <b><ixsl:value-of select="substring-after(@name, concat(../@name, '/'))"/></b>
            </td>
          </tr>
          <ixsl:for-each select="./include">
            <ixsl:if test="not(following-sibling::include) or
                               following-sibling::include[position() = 1 and not(@path = current()/@path)] or
                               following-sibling::include[position() = 1 and not(@part = current()/@part)]">
              <tr>
                <td nowrap="nowrap">
                  <ixsl:if test="/formresult/currentcommoninfo[@path = current()/@path] and
                           /formresult/currentcommoninfo[@part = current()/@part]">
                    <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
                  </ixsl:if>
                  &#160;&#160;&#160;&#160;<pfx:button page="commons" mode="force">
                    <pfx:command  name="SELWRP">comsel</pfx:command>
                    <pfx:command  name="SELWRP">uplcom</pfx:command>
                    <pfx:argument name="comsel.Path"><ixsl:value-of select="./@path"/></pfx:argument>
                    <pfx:argument name="comsel.Part"><ixsl:value-of select="./@part"/></pfx:argument>
                    <pfx:anchor   frame="left_navi"><ixsl:value-of select="./@path"/></pfx:anchor>
                    <ixsl:value-of select="@part"/>
                  </pfx:button>
                </td>
              </tr>
            </ixsl:if>
          </ixsl:for-each>
        </ixsl:for-each>
        <ixsl:if test="following-sibling::directory">
          <tr>
            <td><blank style="display:block;" height="5px"/></td>
          </tr>
        </ixsl:if>
      </ixsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="displayallincludes">
    <xsl:variable name="path">
      <xsl:choose>
        <xsl:when test="@path"><xsl:value-of select="@path"/></xsl:when>
        <xsl:otherwise>/formresult/allincludes</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="target_page">
      <xsl:choose>
        <xsl:when test="@target"><xsl:value-of select="@target"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <table width="100%" class="editor_sidebar_content">
      <ixsl:for-each select="{$path}/directory">
        <tr>
          <td class="editor_sidebar_box"  nowrap="nowrap">
            Directory: <b><ixsl:value-of select="@name"/></b>
          </td>
        </tr>
        <ixsl:for-each select="./path">
          <tr>
            <td nowrap="nowrap">
              <a>
                <ixsl:attribute name="name"><ixsl:value-of select="@name"/></ixsl:attribute>
              </a>
              <b><ixsl:value-of select="substring-after(@name, concat(../@name, '/'))"/></b>
            </td>
          </tr>
          <ixsl:for-each select="./include">
            <tr>
              <td nowrap="nowrap">
                <ixsl:if test="/formresult/currentincludeinfo[@path    = current()/@path] and
                               /formresult/currentincludeinfo[@part    = current()/@part] and
                               /formresult/currentincludeinfo[@product = current()/@product]">
                  <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
                </ixsl:if>
                &#160;&#160;&#160;&#160;<pfx:button page="includes" mode="force">
                  <pfx:command  name="SELWRP" page="{$target_page}">incsel</pfx:command>
                  <pfx:command  name="SELWRP" page="{$target_page}">uplinc</pfx:command>
                  <pfx:argument name="incsel.Path"><ixsl:value-of select="./@path"/></pfx:argument>
                  <pfx:argument name="incsel.Part"><ixsl:value-of select="./@part"/></pfx:argument>
                  <pfx:anchor   frame="left_navi"><ixsl:value-of select="./@path"/></pfx:anchor>
                  <ixsl:value-of select="@part"/>
                </pfx:button>
              </td>
            </tr>
          </ixsl:for-each>
        </ixsl:for-each>
        <ixsl:if test="following-sibling::directory">
          <tr>
            <td><blank style="display:block;" height="5px"/></td>
          </tr>
        </ixsl:if>
      </ixsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="displaymatchingincludes">
    <xsl:variable name="path"><xsl:value-of select="@path"/></xsl:variable>
    <xsl:variable name="sel"><xsl:value-of select="@sel"/></xsl:variable>
    <xsl:variable name="upl"><xsl:value-of select="@upl"/></xsl:variable>
    <xsl:variable name="target_page">
      <xsl:choose>
        <xsl:when test="@target"><xsl:value-of select="@target"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$page"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <table width="100%" class="editor_sidebar_content">
      <ixsl:for-each select="{$path}/directory">
        <tr>
          <td colspan="2" class="editor_sidebar_box"  nowrap="nowrap">
            Directory: <b><ixsl:value-of select="@name"/></b>
          </td>
        </tr>
        <ixsl:for-each select="./path">
          <tr>
            <td colspan="2" nowrap="nowrap">
              <b><ixsl:value-of select="substring-after(@name, concat(../@name, '/'))"/></b>
            </td>
          </tr>
          <ixsl:for-each select="./include">
            <tr valign="top">
              <td nowrap="nowrap" style="padding-right: 10px;">
                <ixsl:if test="/formresult/currentincludeinfo[@path    = current()/@path] and
                               /formresult/currentincludeinfo[@part    = current()/@part] and
                               /formresult/currentincludeinfo[@product = current()/@product]">
                  <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
                </ixsl:if>
                &#160;&#160;&#160;&#160;<pfx:button page="{$target_page}" mode="force">
                  <pfx:command  name="SELWRP" page="{$target_page}"><xsl:value-of select="$sel"/></pfx:command>
                  <pfx:command  name="SELWRP" page="{$target_page}"><xsl:value-of select="$upl"/></pfx:command>
                  <pfx:argument name="{$sel}.Path"><ixsl:value-of select="./@path"/></pfx:argument>
                  <pfx:argument name="{$sel}.Part"><ixsl:value-of select="./@part"/></pfx:argument>
                  <ixsl:value-of select="@part"/>
                </pfx:button>
              </td>
              <td class="editor_odd_row editor_box">
                <i><ixsl:for-each select="./match">
                  <ixsl:value-of select="@pre"/><b><ixsl:value-of select="@match"/></b><ixsl:value-of select="@post"/>;&#160;
                  </ixsl:for-each></i>
              </td>
            </tr>
          </ixsl:for-each>
        </ixsl:for-each>
        <ixsl:if test="following-sibling::directory">
          <tr>
            <td><blank style="display:block;" height="5px"/></td>
          </tr>
        </ixsl:if>
      </ixsl:for-each>
    </table>
  </xsl:template>


  <xsl:template match="displayalltargets">
    <table width="100%" class="editor_sidebar_content">
      <ixsl:for-each select="/formresult/alltargets//target">
        <ixsl:variable name="counter"><ixsl:value-of select="count(ancestor::target)"/></ixsl:variable>
        <ixsl:variable name="childs"><ixsl:value-of select="count(./target)"/></ixsl:variable>
        <ixsl:if test="$counter = 0">
          <tr>
            <td>
              <a>
                <ixsl:attribute name="name"><ixsl:value-of select="@name"/></ixsl:attribute>
              </a>
              <ixsl:if test="preceding-sibling::target">
                <blank height="5"/>
              </ixsl:if>
            </td>
          </tr>
        </ixsl:if>
        <tr>
          <td>
            <ixsl:if test="/formresult/currenttargetinfo[@name = current()/@name]">
              <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
            </ixsl:if>
            <ixsl:variable name="ind">
              <ixsl:for-each select="ancestor::target">&#160;&#160;&#160;&#160;&#160;</ixsl:for-each>
            </ixsl:variable>
            <ixsl:copy-of select="$ind"/>
            <pfx:button page="targets" mode="force">
              <pfx:argument name="tsel.Target"><ixsl:value-of select="@name"/></pfx:argument>
              <pfx:anchor   frame="left_navi">
                <ixsl:choose>
                  <ixsl:when test="$counter = 0"><ixsl:value-of select="@name"/></ixsl:when>
                  <ixsl:otherwise>
                    <ixsl:value-of select="ancestor::target[position() = $counter]/@name"/>
                  </ixsl:otherwise>
                </ixsl:choose>
              </pfx:anchor>
              <span>
                <ixsl:choose>
                  <ixsl:when test="$counter = 0">
                    <ixsl:attribute name="style">font-weight: bold;</ixsl:attribute>
                  </ixsl:when>
                  <ixsl:when test="not(./target)">
                    <ixsl:attribute name="style">font-style: italic;</ixsl:attribute>
                  </ixsl:when>
                </ixsl:choose>
                <ixsl:value-of select="@name"/>
              </span>
            </pfx:button>
          </td>
        </tr>
      </ixsl:for-each>
    </table>
  </xsl:template>

  <xsl:template match="displaycurrentpage">
    <div class="editor_main_emph" align="right">
      [PAGE: <ixsl:value-of select="/formresult/currentpageinfo/@name"/>]
    </div>
  </xsl:template>
  
  <xsl:template match="displaycurrenttarget">
    <div class="editor_main_emph" align="right">
      [TARGET: <ixsl:value-of select="/formresult/currenttargetinfo/@name"/>]
    </div>
  </xsl:template>

 <xsl:template match="displaycurrentimage">
    <div class="editor_main_emph" align="right">
      [IMAGE: <ixsl:value-of select="/formresult/currentimageinfo/@name"/>]
    </div>
  </xsl:template>

  <xsl:template match="displaycurrentinclude">
    <div class="editor_main_emph" align="right">
      [INCLUDE: <ixsl:value-of select="/formresult/currentincludeinfo/@part"/>]
    </div>
  </xsl:template>

  <xsl:template match="displaycurrentcommon">
    <div class="editor_main_emph" align="right">
      [DYNINCLUDE: <ixsl:value-of select="/formresult/currentcommoninfo/@part"/>]
    </div>
  </xsl:template>

  <xsl:template match="displaycurrentuserforedit">
    <div class="editor_main_emph" align="right">
      [USERID: <ixsl:value-of select="/formresult/currentuserforedit/@id"/>]
    </div>
  </xsl:template>

  <xsl:template match="displaybranchoptions">
    <ixsl:if test="/formresult/currentincludeinfo/branchoptions/usedbyproduct[not(@name =
             /formresult/cr_editorsession/product/@name)]"> 
      <table><tr><td class="editor_main_emph">Edit this part in other Product:</td></tr></table>
      <table width="100%" class="editor_box">
        <ixsl:for-each select="/formresult/currentincludeinfo/branchoptions/usedbyproduct[not(@name = /formresult/cr_editorsession/product/@name)]">
          <tr>
            <td>
              <a target="_top">
                <ixsl:attribute name="href"><ixsl:value-of select="$__uri"/>?__page=includes&amp;extinc.Part=<ixsl:value-of select="/formresult/currentincludeinfo/@part"/>&amp;extinc.Path=<ixsl:value-of select="/formresult/currentincludeinfo/@path"/>&amp;extprod.Name=<ixsl:value-of select="./@name"/></ixsl:attribute>
                <ixsl:value-of select="./@comment"/>
              </a>
            </td>
          </tr>
        </ixsl:for-each>
      </table>
      <br/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="displayaffectedpages">
    <xsl:variable name="path"><xsl:value-of select="@path"/>/affectedpages/product</xsl:variable>
    <table><tr><td class="editor_main_emph">Affected pages:</td></tr></table>
    <ixsl:if test="{$path}/node()">
      <table width="100%" class="editor_box">
        <ixsl:if test="{$path}/page/@uptodate = 'false'">
          <tr>
            <td>
              <table class="editor_note" width="100%">
                <tr>
                  <td>
                    <u>Note</u>: pages printed in <span class="editor_page_old">this
                      style</span> are no longer up-to-date. They will be rebuild automatically over time.
                  </td>
                </tr>
              </table>
            </td>
          </tr>
        </ixsl:if>
        <ixsl:for-each select="{$path}">
          <ixsl:variable name="theproduct"><ixsl:value-of select="@name"/></ixsl:variable>
          <ixsl:if test="./node()">
            <tr>
              <td>
                <table><tr><td><b>Pages in product  "<i><ixsl:value-of select="@comment"/></i>":</b></td></tr></table>
                <table width="100%" class="editor_box">
                  <tr>
                    <td>
                      <ixsl:for-each select="./page">
                        <ixsl:choose>
                          <ixsl:when test="/formresult/cr_editorsession/product/@name = $theproduct">
                            <pfx:button page="pages" target="_top" frame="_top">
                              <pfx:argument name="psel.Page"><ixsl:value-of select="@name"/></pfx:argument>
                              <pfx:anchor   frame="left_navi"><ixsl:value-of select="@name"/></pfx:anchor>
                              <span>
                                <ixsl:if test="@uptodate = 'false'">
                                  <ixsl:attribute name="class">editor_page_old</ixsl:attribute>
                                </ixsl:if>
                                <ixsl:value-of select="@name"/></span>
                            </pfx:button>&#160;
                          </ixsl:when>
                          <ixsl:otherwise>
                            <pfx:button page="pages" target="_top" frame="_top">
                              <pfx:argument name="extprod.Name"><ixsl:value-of select="$theproduct"/></pfx:argument>
                              <pfx:argument name="psel.Page"><ixsl:value-of select="@name"/></pfx:argument>
                              <pfx:anchor   frame="left_navi"><ixsl:value-of select="@name"/></pfx:anchor>
                              <span>
                                <ixsl:if test="@uptodate = 'false'">
                                  <ixsl:attribute name="class">editor_page_old</ixsl:attribute>
                                </ixsl:if>
                                <ixsl:value-of select="@name"/>
                              </span>
                            </pfx:button>&#160;
                          </ixsl:otherwise>
                        </ixsl:choose>
                      </ixsl:for-each>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
          </ixsl:if>
        </ixsl:for-each>
      </table>
      <br/>
    </ixsl:if>
  </xsl:template>
  
  <xsl:template match="displaytarget_of_page">
    <table><tr><td class="editor_main_emph">XML/XSL dependency tree:</td></tr></table>
    <table width="100%" class="editor_box">
      <ixsl:for-each select="/formresult/currentpageinfo/targetinfo//target">
        <ixsl:variable name="count"><ixsl:value-of select="count(ancestor::target)"/></ixsl:variable>
        <ixsl:variable name="ind">
          <ixsl:for-each select="ancestor::target">&#160;&#160;&#160;&#160;&#160;</ixsl:for-each>
        </ixsl:variable>
        <tr>
          <td nowrap="nowrap">
            <ixsl:copy-of select="$ind"/>
            <pfx:button page="targets" target="_top" frame="_top">
              <pfx:argument name="tsel.Target"><ixsl:value-of select="@name"/></pfx:argument>
              <pfx:anchor   frame="left_navi">
                <ixsl:choose>
                  <ixsl:when test="$count = 0"><ixsl:value-of select="@name"/></ixsl:when>
                  <ixsl:otherwise><ixsl:value-of select="ancestor::target[position() = $count]/@name"/></ixsl:otherwise>
                </ixsl:choose>
              </pfx:anchor>
              <ixsl:value-of select="@name"/>
            </pfx:button>
          </td> 
          <td align="right">
            <ixsl:value-of select="@type"/>
          </td>
        </tr>
      </ixsl:for-each>
    </table>
    <br/>
  </xsl:template>
  
  <xsl:template match="displaytargetdetails">
    <ixsl:if test="/formresult/currenttargetinfo/@xmlsrc">
      <table><tr><td class="editor_main_emph">Parents:</td></tr></table>
      <table width="100%" class="editor_box">
        <tr>
          <td>
            <table><tr><td><b>
                    XML source: <pfx:button page="targets" mode="force">
                      <pfx:argument name="tsel.Target">
                        <ixsl:value-of select="/formresult/currenttargetinfo/@xmlsrc"/>
                      </pfx:argument>
                      <ixsl:value-of select="/formresult/currenttargetinfo/@xmlsrc"/>
                    </pfx:button>
                  </b></td></tr></table>
          </td> 
          <td>
            <table><tr><td><b>
                    XSL source: <pfx:button page="targets" mode="force">
                      <pfx:argument name="tsel.Target">
                        <ixsl:value-of select="/formresult/currenttargetinfo/@xslsrc"/>
                      </pfx:argument>
                      <ixsl:value-of select="/formresult/currenttargetinfo/@xslsrc"/>
                    </pfx:button>
                  </b></td></tr></table>
          </td>
        </tr>
      </table>
      <ixsl:if test="/formresult/currenttargetinfo/paraminfo/param">
        <br/>
        <table><tr><td class="editor_main_emph">Applied parameters:</td></tr></table>
        <table width="100%" class="editor_box">
          <ixsl:for-each select="/formresult/currenttargetinfo/paraminfo/param">
            <ixsl:variable name="class">
              <ixsl:choose>
                <ixsl:when test="(number(@count) mod 2) = 0">editor_even_row</ixsl:when>
                <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
              </ixsl:choose>
            </ixsl:variable>
            <tr>
              <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
              <td nowrap="nowrap">
                <ixsl:value-of select="@key"/>
              </td>
              <td nowrap="nowrap">
                <ixsl:value-of select="@value"/>
              </td>
            </tr>
          </ixsl:for-each>
        </table>
      </ixsl:if>
      <br/>
    </ixsl:if> 
  </xsl:template>

  <xsl:template match="displayauxfiles_of_target">
    <ixsl:if test="/formresult/currenttargetinfo/auxfileinfo/auxfile">
      <table><tr><td class="editor_main_emph">Additional dependencies:</td></tr></table>
      <table width="100%" class="editor_box">
        <ixsl:for-each select="/formresult/currenttargetinfo/auxfileinfo/auxfile">
          <ixsl:variable name="class">
            <ixsl:choose>
              <ixsl:when test="(number(@count) mod 2) = 0">editor_even_row</ixsl:when>
              <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
            </ixsl:choose>
          </ixsl:variable>
          <tr>
            <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
            <td nowrap="nowrap">
              <ixsl:value-of select="@path"/>
            </td>
          </tr>
        </ixsl:for-each>
      </table>
      <br/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="displayincludes">
    <xsl:variable name="thepath"><xsl:value-of select="@path"/></xsl:variable>
    <ixsl:if test="{$thepath}/includeinfo/include">
      <table><tr><td class="editor_main_emph">Used includes:</td></tr></table>
      <table width="100%" class="editor_box">
        <ixsl:for-each select="{$thepath}/includeinfo//include">
          <ixsl:variable name="ind">
            <ixsl:for-each select="ancestor::include">&#160;&#160;&#160;&#160;&#160;</ixsl:for-each>
          </ixsl:variable>
          <ixsl:variable name="class">
            <ixsl:choose>
              <ixsl:when test="(count(ancestor::include) mod 2) = 0">editor_even_row</ixsl:when>
              <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
            </ixsl:choose>
          </ixsl:variable>
          <tr>
            <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
            <td nowrap="nowrap">
              <ixsl:value-of select="$ind"/>
              <pfx:button page="includes" mode="force">
                <xsl:if test="not($page = 'includes')">
                  <xsl:attribute name="target">_top</xsl:attribute>
                  <xsl:attribute name="frame">_top</xsl:attribute>
                </xsl:if>
                <pfx:command  name="SELWRP" page="includes">incsel</pfx:command>
                <pfx:command  name="SELWRP" page="includes">uplinc</pfx:command>
                <pfx:argument name="incsel.Path"><ixsl:value-of select="@path"/></pfx:argument>
                <pfx:argument name="incsel.Part"><ixsl:value-of select="@part"/></pfx:argument>
                <pfx:anchor   frame="left_navi"><ixsl:value-of select="@path"/></pfx:anchor>
                <ixsl:value-of select="@part"/>
              </pfx:button>
            </td>
            <td>
              <ixsl:value-of select="@path"/>
            </td>
          </tr>
        </ixsl:for-each>
      </table>
      <br/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="displayimages">
    <xsl:variable name="thepath"><xsl:value-of select="@path"/></xsl:variable>
    <ixsl:if test="{$thepath}/imageinfo/image">
      <table><tr><td class="editor_main_emph">Used images:</td></tr></table>
      <table width="100%" class="editor_box_alpha" cellpadding="0" cellspacing="0"
             background="/core/editor/img/alpha.gif">
        <ixsl:for-each select="{$thepath}/imageinfo/image">
          <ixsl:variable name="class">
            <ixsl:choose>
              <ixsl:when test="(number(@count) mod 2) = 0">editor_even_row</ixsl:when>
              <ixsl:otherwise>editor_odd_row</ixsl:otherwise>
            </ixsl:choose>
          </ixsl:variable>
          <tr valign="top">
            <td width="50%" nowrap="nowrap">
              <ixsl:attribute name="class"><ixsl:value-of select="$class"/></ixsl:attribute>
              <pfx:button page="images" target="_top" frame="_top">
                <pfx:command  name="SELWRP" page="images">imgsel</pfx:command>
                <pfx:command  name="SELWRP" page="images">upload</pfx:command>
                <pfx:argument name="imgsel.Path"><ixsl:value-of select="@path"/></pfx:argument>
                <pfx:argument name="imgsel.Modtime"><ixsl:value-of select="@modtime"/></pfx:argument>
                <pfx:anchor   frame="left_navi"><ixsl:value-of select="@path"/></pfx:anchor>
                <ixsl:value-of select="@path"/>
              </pfx:button>
            </td>
            <td width="50%">
              <ixsl:choose>
                <ixsl:when test="@modtime = 0">
                  <span class="editor_missing_img_txt">MISSING IMAGE</span>
                </ixsl:when>
                <ixsl:otherwise>
                  <img border="0">
                    <ixsl:attribute name="src">
                      <ixsl:value-of select="@path"/>?<ixsl:value-of select="@modtime"/>
                    </ixsl:attribute>
                  </img>
                </ixsl:otherwise>
              </ixsl:choose>
            </td>
          </tr>
        </ixsl:for-each>
      </table>
      <br/>
    </ixsl:if>
  </xsl:template>

  <xsl:template match="displayincludedetails">
    <xsl:call-template name="displaypartdetails">
      <xsl:with-param name="type">include</xsl:with-param>
      <xsl:with-param name="select">incsel</xsl:with-param>
      <xsl:with-param name="upload">uplinc</xsl:with-param>
    </xsl:call-template>
  </xsl:template> 
  
  <xsl:template match="displaycommondetails">
    <xsl:call-template name="displaypartdetails">
      <xsl:with-param name="type">common</xsl:with-param>
      <xsl:with-param name="select">comsel</xsl:with-param>
      <xsl:with-param name="upload">uplcom</xsl:with-param>
    </xsl:call-template>
  </xsl:template> 

  <xsl:template name="displaypartdetails">
    <xsl:param name="type"/>
    <xsl:param name="select"/>
    <xsl:param name="upload"/>
      
    <pfx:forminput target="bottom" name="my_form">
      <pfx:xinp type="hidden" name="__anchor">
        <ixsl:attribute name="value">left_navi|<ixsl:value-of select="/formresult/current{$type}info/@path"/></ixsl:attribute>
      </pfx:xinp>
      
      <xsl:call-template name="handleformerrors">
        <xsl:with-param name="select" select="$select"/>
        <xsl:with-param name="upload" select="$upload"/>
      </xsl:call-template>
      
      <xsl:call-template name="showcurrentinclude">
        <xsl:with-param name="type" select="$type"/>
        <xsl:with-param name="docroot" select="$docroot"/>
      </xsl:call-template>
  
      <ixsl:choose>
        <ixsl:when test="/formresult/current{$type}info/lockinguser/user">
          <xsl:call-template name="handleuserlock">
            <xsl:with-param name="type" select="$type"/>
          </xsl:call-template>
        </ixsl:when>
        <ixsl:otherwise>
          <xsl:choose>
            <xsl:when test="$type = 'common'">
              <xsl:call-template name="partdetails_dyn">  
                <xsl:with-param name="type" select="$type"/>
                <xsl:with-param name="select" select="$select"/>
                <xsl:with-param name="upload" select="$upload"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:when test="$type = 'include'">
              <xsl:call-template name="partdetails_incl">
                <xsl:with-param name="type" select="$type"/>
                <xsl:with-param name="select" select="$select"/>
                <xsl:with-param name="upload" select="$upload"/>
              </xsl:call-template>
            </xsl:when>
          </xsl:choose>
        </ixsl:otherwise>
      </ixsl:choose>
    </pfx:forminput>
  </xsl:template>
 

   <xsl:template name="partdetails_incl"> 
      <xsl:param name="type"/>
      <xsl:param name="select"/>
      <xsl:param name="upload"/>
      
      <ixsl:variable name="edit_incl">
        <ixsl:choose>
          <ixsl:when test="/formresult/cr_editorsession/user/permissions/global/@admin = 'true'">true</ixsl:when>
          <ixsl:otherwise>
            <ixsl:value-of select="/formresult/cr_editorsession/user/permissions/project[@name = /formresult/cr_editorsession/product/@name]/@editIncludes"/>
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
     
    <ixsl:variable name="forbidden_inc">
      <ixsl:for-each select="/formresult/currentincludeinfo/affectedproducts/product">
        <ixsl:variable name="aff_inc" select="./@name"/>
        <ixsl:variable name="perm_aff_inc" select="/formresult/cr_editorsession/user/permissions/project[@name = $aff_inc]/@editIncludes"/>
        <ixsl:if test="$perm_aff_inc != 'true'">
          <li><ixsl:value-of select="./@name"/></li>
        </ixsl:if>
      </ixsl:for-each>
    </ixsl:variable>
    <ixsl:choose>
      <ixsl:when test="$forbidden_inc = ''">
        <ixsl:choose>
          <ixsl:when test="/formresult/currentincludeinfo/@product='default'">
            <xsl:call-template name="create_specific_branch">
              <xsl:with-param name="upload" select="$upload"/>
            </xsl:call-template>
          </ixsl:when>
          <ixsl:otherwise>
            <xsl:call-template name="handle_specific_branch">
              <xsl:with-param name="upload" select="$upload"/>
              <xsl:with-param name="type" select="$type"/>
            </xsl:call-template>
          </ixsl:otherwise>
        </ixsl:choose>
        <xsl:call-template name="show_upload">
          <xsl:with-param name="upload" select="$upload"/>
          <xsl:with-param name="type" select="$type"/>
        </xsl:call-template>
      </ixsl:when>
      <ixsl:otherwise>
        <ixsl:choose>
          <ixsl:when test="/formresult/cr_editorsession/user/permissions/project[@name = /formresult/cr_editorsession/product/@name]/@editIncludes = 'true'">
           <ixsl:call-template name="incl_perm_denied_usedby_other_prods_but_branch">
             <ixsl:with-param name="prods" select="$forbidden_inc"/>
           </ixsl:call-template>
           <br/>
           <xsl:call-template name="create_specific_branch">
             <xsl:with-param name="upload" select="$upload"/>
           </xsl:call-template>
          </ixsl:when>
          <ixsl:otherwise>
            <ixsl:call-template name="incl_perm_denied_usedby_other_prods_no_branch">
             <ixsl:with-param name="prods" select="$forbidden_inc"/>
            </ixsl:call-template>
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:otherwise>
    </ixsl:choose>
  </xsl:template>

   <xsl:template name="partdetails_dyn"> 
      <xsl:param name="type"/>
      <xsl:param name="select"/>
      <xsl:param name="upload"/>
      
      
      <ixsl:variable name="current_product" select="/formresult/cr_editorsession/product/@name"/>
      <ixsl:variable name="admin" select="/formresult/cr_editorsession/user/permissions/global/@admin"/>
      
      <ixsl:variable name="edit_dyn_default">
        <ixsl:choose>
          <ixsl:when test="$admin = 'true'">true</ixsl:when>
          <ixsl:otherwise>
            <ixsl:value-of select="/formresult/cr_editorsession/user/permissions/global/@editDefaults"/>
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
      <ixsl:variable name="edit_dyn_currprj">
        <ixsl:choose>
          <ixsl:when test="$admin = 'true'">true</ixsl:when>
          <ixsl:otherwise>
            <ixsl:value-of select="/formresult/cr_editorsession/user/permissions/project[@name = $current_product]/@editDefaults"/>
          </ixsl:otherwise>
        </ixsl:choose>
      </ixsl:variable>
     
      <ixsl:choose>
        <ixsl:when test="/formresult/currentcommoninfo/@product='default'">
          <ixsl:choose>
            <ixsl:when test="$edit_dyn_default = 'true'">
              <ixsl:choose>
                <ixsl:when test="$edit_dyn_currprj = 'true'">
                  <xsl:call-template name="create_specific_branch">
                    <xsl:with-param name="upload" select="$upload"/>
                  </xsl:call-template>
                  <xsl:call-template name="show_upload">
                    <xsl:with-param name="upload" select="$upload"/>
                    <xsl:with-param name="type" select="$type"/>
                  </xsl:call-template>
                </ixsl:when>
                <ixsl:otherwise><!-- edit_dyn_currprj != 'true' --> 
                  <xsl:call-template name="warning_spec_branch_not_allowed"/>
                  <xsl:call-template name="show_upload">
                    <xsl:with-param name="upload" select="$upload"/>
                    <xsl:with-param name="type" select="$type"/>
                  </xsl:call-template>
                </ixsl:otherwise>
              </ixsl:choose>
            </ixsl:when>
            <ixsl:otherwise><!-- edit_dyn_default != 'true' -->
              <ixsl:choose>
                <ixsl:when test="$edit_dyn_currprj = 'true'">
                  <xsl:call-template name="edit_default_branch_denied_but_specific"/>
                  <xsl:call-template name="create_specific_branch">
                    <xsl:with-param name="upload" select="$upload"/>
                  </xsl:call-template>
                </ixsl:when>
                <ixsl:otherwise><!-- edit_dyn_currprj != 'true' -->
                  <xsl:call-template name="edit_default_branch_denied"/>
                </ixsl:otherwise>
              </ixsl:choose>
            </ixsl:otherwise>
          </ixsl:choose>
        </ixsl:when>
        <ixsl:otherwise> <!--"/formresult/currentcommoninfo/@product!='default'"-->
          <ixsl:choose>
            <ixsl:when test="$edit_dyn_default = 'true'">
              <ixsl:choose>
                <ixsl:when test="$edit_dyn_currprj = 'true'">
                  <xsl:call-template name="handle_specific_branch">
                    <xsl:with-param name="upload" select="$upload"/>
                    <xsl:with-param name="type" select="$type"/>
                  </xsl:call-template>
                  <xsl:call-template name="show_upload">
                    <xsl:with-param name="upload" select="$upload"/>
                    <xsl:with-param name="type" select="$type"/>
                  </xsl:call-template>
                </ixsl:when>
                <ixsl:otherwise> <!-- $edit_dyn_currprj != 'true'-->
                  <xsl:call-template name="edit_specific_branch_denied"/>
                </ixsl:otherwise>
              </ixsl:choose>
            </ixsl:when>
            <ixsl:otherwise> <!-- $edit_dyn_default != 'true' -->
              <ixsl:choose>
                <ixsl:when test="$edit_dyn_currprj = 'true'">
                  <xsl:call-template name="handle_specific_branch">
                    <xsl:with-param name="upload" select="$upload"/>
                    <xsl:with-param name="type" select="$type"/>
                  </xsl:call-template>
                  <xsl:call-template name="show_upload">
                    <xsl:with-param name="upload" select="$upload"/>
                    <xsl:with-param name="type" select="$type"/>
                  </xsl:call-template>
                </ixsl:when>
                <ixsl:otherwise> <!-- $edit_dyn_currprj != 'true' -->
                  <xsl:call-template name="edit_specific_branch_denied"/>
                </ixsl:otherwise>
              </ixsl:choose> 
            </ixsl:otherwise>
          </ixsl:choose>
        </ixsl:otherwise>
      </ixsl:choose>
  </xsl:template>


  <xsl:template name="edit_specific_branch_denied">
    <xsl:call-template name="permission_denied">
      <xsl:with-param name="text" select="'You do not have the permission to edit the product specific branch.'"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="edit_default_branch_denied">
    <xsl:call-template name="permission_denied">
      <xsl:with-param name="text" select="'You have neither the permission to edit the default branch nor to create a product specific branch.'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="edit_default_branch_denied_but_specific">
    <xsl:call-template name="permission_denied">
      <xsl:with-param name="text" select="'You do not have the permission to edit the default branch, but you can create a specific branch.'"/>
    </xsl:call-template>
  </xsl:template>

 
  <xsl:template name="permission_denied">
    <xsl:param name="text"/>
    <table class="core_errorbox_table" width="100%">
      <tr valign="top">
        <td class="core_errorlabel_td">
          <img src="/core/img/error.gif"/>
        </td>
        <td class="core_errorlabel_text">
          Permission denied!
        </td>
        <td class="core_errortext" colspan="2">
          <xsl:value-of select="$text"/>
        </td>
      </tr>
    </table>
    <br/>
  </xsl:template>


  <xsl:template name="warning_spec_branch_not_allowed">
    <table class="core_errorbox_table" width="100%">
      <tr valign="top">
        <td class="core_errorlabel_td">
          <img src="/core/img/error.gif"/>
        </td>
        <td class="core_errorlabel_text">
          Warning!
        </td>
        <td class="core_errortext" colspan="2">
          You are not allowed to create a product specific branch of this dynamic include.
        </td>
      </tr>
    </table>
  </xsl:template>

  <xsl:template name="show_upload">
    <xsl:param name="upload"/>
    <xsl:param name="type"/>
    <pfx:checkactive prefix="{$upload}">
      <br/>
      <table class="editor_box" width="100%">
        <tr>
          <td colspan="2">
            <div id="wfxtb01" style="border:1px solid black"></div>
            <table cellpadding="0" cellspacing="0" width="100%" border="0">
              <tbody>
                <tr>
                  <td width="50"><iframe id="wfxline01" frameborder="0" style="width:50px; height:384px; border:1px solid #000000; background-color:#ffffff; float:left" src="about:blank" scrolling="no"></iframe></td>
                  <td><iframe id="wfxedit01" frameborder="0" style="width:100%; height:384px; border:1px solid #000000; background-color:#ffffff; float:right" src="about:blank" scrolling="yes"></iframe></td>
                </tr>
              </tbody>
            </table>
            <br />
            <input class="editor_submit" type="button" value="Upload Data" onclick="wfxed.submitContent('wfxta01', 'wfxsubmit01')" />
            <br />
            <div style="display:none">
              Line: <input type="text" id="wfxed_line" size="3" value="" />
              Scroll: <input type="text" id="wfxed_column" size="3" value="" />
              Message: <input type="text" id="wfxed_msg" size="100" value="" />
              <br />
              <textarea id="ta_src" rows="10" cols="130" style="display:none"></textarea>
              <textarea id="ta_col" rows="10" cols="130" style="display:none"></textarea>
							<textarea id="dbg" rows="25" cols="130" style="display:none"></textarea>
              <br />
              <pfx:xinp class="editor_textarea" wrap="off" type="area" id="wfxta01" name="{$upload}.Content"/>
            </div>
          </td>
        </tr>
        <tr>
          <td colspan="2" align="right">
            <pfx:xinp class="editor_submit"  type="submit" id="wfxsubmit01" name="Upload Data" value="Upload Data" style="display:none">
              <pfx:command  name="SELWRP"><xsl:value-of select="$upload"/></pfx:command>
              <pfx:argument name="{$upload}.HaveUpload">true</pfx:argument> 
              </pfx:xinp>
          </td>
        </tr>
        <ixsl:if test="/formresult/current{$type}info/backup/option">
          <tr>
            <td colspan="2"><hr/></td>
          </tr>
          <tr valign="top">
            <td>
              If you want to return to a previous version of the include, select one of the backups below.
            </td>
            <td nowrap="nowrap" align="right">
              <pfx:xinp type="dynselect" name="{$upload}.Backup" optionpath="/formresult/current{$type}info/backup"/>&#160;
              <pfx:xinp class="editor_submit" type="submit" value="Use Backup">
                <pfx:command  name="SELWRP"><xsl:value-of select="$upload"/></pfx:command>
                <pfx:argument name="{$upload}.HaveUpload">true</pfx:argument>
                <pfx:argument name="{$upload}.HaveBackup">true</pfx:argument>
              </pfx:xinp>
            </td>
          </tr>
        </ixsl:if>
      </table>
    </pfx:checkactive>
  </xsl:template>


  
  <xsl:template name="handleformerrors">
    <xsl:param name="select"/>
    <xsl:param name="upload"/>
    <ixsl:if test="/formresult/formerrors/error">
      <table width="100%">
        <tr>
          <td align="center">
            <ixsl:choose>
              <ixsl:when test="starts-with(/formresult/formerrors/error/@name, '{$select}')">
                <table class="core_errorbox_table" width="200">
                  <tr>
                    <td class="core_errorlabel_text">
                      Error!
                    </td>
                    <td class="core_errortext">
                      Unknown Include.
                    </td>
                  </tr>
                </table>
              </ixsl:when>
              <ixsl:when test="/formresult/formvalues/param[@name='{$upload}.ExceptionMsg']/node()">
                <table class="core_errorbox_table">
                  <tr>
                    <td class="core_errorlabel_text">
                      <ixsl:value-of select="/formresult/formvalues/param[@name='{$upload}.ExceptionMsg']"/>
                    </td>
                  </tr>
                </table><br/>
              </ixsl:when>
            </ixsl:choose>
          </td>
        </tr>
      </table><br/>
    </ixsl:if>
  </xsl:template>
 
  <xsl:template name="showcurrentinclude">
    <xsl:param name="type"/>
    <xsl:param name="docroot"/>
    <table width="100%">
      <tr>
        <td class="editor_main_emph">Current include:
          <ixsl:value-of select="/formresult/current{$type}info/@part"/>
          <span style="color: #9999cc">@</span>
          <ixsl:value-of select="/formresult/current{$type}info/@path"/>
          (Product: <ixsl:value-of select="/formresult/current{$type}info/@product"/>)
        </td>
      </tr>
    </table>
  </xsl:template>
  
  <xsl:template name="handleuserlock">
    <xsl:param name="type"/> 
    <table class="editor_box" width="100%">
      <ixsl:if test="/formresult/current{$type}info/lockinguser/user">
        <tr>
          <td align="right" class="editor_locked">
            [Locked by <ixsl:value-of select="/formresult/current{$type}info/lockinguser/user/@name"/>
            (<ixsl:value-of select="/formresult/current{$type}info/lockinguser/user/@sect"/>) - Phone:
            <ixsl:value-of select="/formresult/current{$type}info/lockinguser/user/@phone"/>]
            <input type="hidden" name="visible" value="false"/>
          </td>
        </tr>
      </ixsl:if>
    </table>
  </xsl:template>
    


  <xsl:template name="handle_specific_branch">
    <xsl:param name="type"/>
    <xsl:param name="upload"/>
    <table class="editor_box" width="100%"> 
      <tr>
        <td>
          This is the specific branch for product <b><ixsl:value-of select="/formresult/current{$type}info/@product"/></b>
          of the include part. You can delete this branch to use the default branch again.
        </td>
        <td nowrap="nowrap">
          <pfx:button normalclass="editor_submit" page="{$type}s" mode="force">
            <pfx:command  name="SELWRP">create</pfx:command>
            <pfx:command  name="SELWRP"><xsl:value-of select="$upload"/></pfx:command>
            <pfx:argument name="create.DoBranch">true</pfx:argument>
            <pfx:argument name="create.Type">delete</pfx:argument>
            Delete branch
          </pfx:button>
        </td>
      </tr>
    </table>
    <br/>
  </xsl:template>
  
  <xsl:template name="create_specific_branch">
    <xsl:param name="upload"/>
    <table class="editor_box" width="100%">
      <tr>
        <td colspan="2">
          <b>This is the default branch of the include.</b><br/>
          Do you want to create and edit a product specific branch of this part?
        </td>
      </tr>
      <tr>
        <td colspan="2">
          <table class="editor_box" width="100%">
            <tr valign="bottom">
              <td>
                <table>
                  <tr>
                    <td>
                      <pfx:xinp type="radio" name="create.Type" value="empty"/>
                    </td>
                    <td>
                      Create new, empty branch.
                    </td>
                  </tr>
                  <tr>
                    <td>
                      <pfx:xinp type="radio" name="create.Type" value="copy"/>
                    </td>
                    <td>
                      Create new branch and use the <i>default</i> branch as the initial content.
                    </td>
                  </tr>
                </table>
              </td>
              <td align="right" nowrap="nowrap">
                <pfx:xinp class="editor_submit" type="submit" value="Create Branch">
                  <pfx:command  name="SELWRP"><xsl:value-of select="$upload"/></pfx:command>
                  <pfx:command  name="SELWRP">create</pfx:command>
                  <pfx:argument name="create.DoBranch">true</pfx:argument>
                </pfx:xinp>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </xsl:template>


  <xsl:template name="include_perm_denied">
    <xsl:param name="type"/>
    <h1><ixsl:value-of select="/formresult/current{$type}info/@permission_info"/></h1>
  </xsl:template>
  
  <xsl:template match="displayimagedetails">
    <ixsl:if test="/formresult/currentimageinfo">
      <pfx:forminput enctype="multipart/form-data">
        <pfx:xinp type="hidden" name="__anchor">
          <ixsl:attribute name="value">left_navi|<ixsl:value-of select="/formresult/currentimageinfo/@path"/></ixsl:attribute>
        </pfx:xinp>
        <table width="100%">
          <tr>
            <td nowrap="nowrap" class="editor_main_emph">Current image: <ixsl:value-of select="/formresult/currentimageinfo/@path"/>
            </td>
          </tr>
        </table>
        <ixsl:choose>
          <ixsl:when test="/formresult/currentimageinfo/lockinguser/user">
            <xsl:call-template name="handleuserlock">
              <xsl:with-param name="type">image</xsl:with-param>
            </xsl:call-template>
          </ixsl:when>
          <ixsl:otherwise>
            <ixsl:variable name="forbidden_img">
              <ixsl:for-each select="/formresult/currentimageinfo/affectedproducts/product">
                <ixsl:variable name="aff_img" select="./@name"/>
                <ixsl:variable name="perm_aff_img" select="/formresult/cr_editorsession/user/permissions/project[@name = $aff_img]/@editImages"/>
                <ixsl:if test="$perm_aff_img != 'true'">
                  <li><ixsl:value-of select="./@name"/></li>
                </ixsl:if>
              </ixsl:for-each>
            </ixsl:variable>
            <ixsl:choose>
              <ixsl:when test="$forbidden_img = ''">
                <table width="100%" class="editor_box_alpha" cellspacing="0" background="/core/editor/img/alpha.gif">
                  <tr>
                    <td align="center" style="padding-top: 10px; padding-bottom: 10px;" colspan="3">
                      <ixsl:choose>
                        <ixsl:when test="/formresult/currentimageinfo/@modtime = 0">
                          <span class="editor_missing_img_txt">MISSING IMAGE</span>
                        </ixsl:when>
                        <ixsl:otherwise>
                          <img border="0">
                            <ixsl:attribute name="src">
                              <ixsl:value-of select="/formresult/currentimageinfo/@path"/>?<ixsl:value-of select="/formresult/currentimageinfo/@modtime"/>
                            </ixsl:attribute>
                          </img>
                        </ixsl:otherwise>
                      </ixsl:choose>
                    </td>
                  </tr>
                    <xsl:call-template name="show_image_upload"/>
                </table>
              </ixsl:when>
              <ixsl:otherwise>
                <ixsl:call-template name="image_perm_denied_usedby_other_prods">
                  <ixsl:with-param name="prods" select="$forbidden_img"/>
                </ixsl:call-template>
              </ixsl:otherwise>
            </ixsl:choose>
          </ixsl:otherwise>
        </ixsl:choose>
      </pfx:forminput>
    </ixsl:if>
  </xsl:template>

  <xsl:template name="show_image_upload">
    <pfx:checkactive prefix="upload">
        <tr valign="center" class="editor_odd_row">
          <td align="right">
            <pfx:label name="upload.HaveUpload">New Image:</pfx:label>
          </td>
          <td>
            <input type="file" name="upload.UplImage"/>
          </td>
          <td align="right">
            <pfx:xinp class="editor_submit" type="submit" value="Upload Image">
              <pfx:command  name="SELWRP">upload</pfx:command>
              <pfx:argument name="upload.HaveUpload">true</pfx:argument>
            </pfx:xinp>
          </td>
        </tr>
        <ixsl:if test="/formresult/currentimageinfo/backup/option">
          <tr class="editor_odd_row">
            <td colspan="3" align="right" nowrap="nowrap">
              <pfx:xinp type="dynselect" name="upload.Backup" optionpath="/formresult/currentimageinfo/backup"/>
              <pfx:xinp class="editor_submit" type="submit" value="Use Backup">
                <pfx:command  name="SELWRP">upload</pfx:command>
                <pfx:argument name="upload.HaveUpload">true</pfx:argument>
                <pfx:argument name="upload.HaveBackup">true</pfx:argument>
              </pfx:xinp>
            </td>
          </tr>
        </ixsl:if>
    </pfx:checkactive>
  </xsl:template>


  <xsl:template match="gendokumenu">
      <ixsl:choose>       
        <ixsl:when test="/formresult/all_documentation/stylesheet">
      <table width="100%" class="editor_sidebar_content">
        <ixsl:for-each select="/formresult/all_documentation/stylesheet">
          <tr><td nowrap="nowrap" class="editor_sidebar_box">
              <b><ixsl:value-of select="@file"/></b>
              <a><ixsl:attribute name="name"><ixsl:value-of select="@file"/></ixsl:attribute></a>
              </td></tr>
            <ixsl:variable name="file"><ixsl:value-of select="@file"/></ixsl:variable>

            <tr><td>
            <ul class="list">
          <ixsl:for-each select="template_doc">
          <ixsl:sort select="@id"/>
          <li>

            <ixsl:choose>
               <ixsl:when test="@doku='notfound'">
                    <span class="editor_missing_img">!&#160;</span>
                    <!--<ixsl:value-of select="./@value"/>-->
        <ixsl:choose>
        <ixsl:when test="@match != '' and @name != ''">
          <small>Match: </small><ixsl:value-of select="@match"/>
          <small> &#38; </small><br/>
          <small>Name: </small><ixsl:value-of select="@name"/>
        </ixsl:when>
        <ixsl:when test="@match != ''">
          <small>Match: </small><ixsl:value-of select="@match"/>
        </ixsl:when>
        <ixsl:when test="@name != ''">
          <small>Name: </small><ixsl:value-of select="@name"/>
        </ixsl:when>
        </ixsl:choose>
               </ixsl:when>
            <ixsl:otherwise>
                 <ixsl:if test="@active='true'">
                    <ixsl:attribute name="class">editor_sidebar_content_sel</ixsl:attribute>
                 </ixsl:if>
                      <pfx:button page="documentation" mode="force">
                      <pfx:argument name="select.Id"><ixsl:value-of select="./@id"/></pfx:argument>
                      <pfx:anchor   frame="left_navi"><ixsl:value-of select="$file"/></pfx:anchor>
                      <!--
                      <ixsl:value-of select="./@value"/>
                      -->
                      
        <ixsl:choose>
        <ixsl:when test="@match != '' and @name != ''">
          <small>Match: </small><ixsl:value-of select="@match"/>
          <small> &#38; </small><br/>
          <small>Name: </small><ixsl:value-of select="@name"/>
        </ixsl:when>
        <ixsl:when test="@match != ''">
          <small>Match: </small><ixsl:value-of select="@match"/>
        </ixsl:when>
        <ixsl:when test="@name != ''">
          <small>Name: </small><ixsl:value-of select="@name"/>
        </ixsl:when>
        </ixsl:choose>

                   </pfx:button>
            </ixsl:otherwise>
          </ixsl:choose>
          </li>
        </ixsl:for-each>
        </ul>
        </td></tr>
      </ixsl:for-each>
    </table>
    <br/>
      </ixsl:when>
      <ixsl:otherwise>
         <div class="editor_main_emph"><center>No Documentation found for this project</center></div>
      </ixsl:otherwise>
    </ixsl:choose>
    <br/>
  </xsl:template>


  <xsl:template match="docuShow">
    <div class="docframe">
    <ixsl:for-each select="/formresult/all_documentation/stylesheet/template_doc[@active='true']">

      <h1><ixsl:value-of select="stylesheet"/> | <span title="This user is responsible for this stylesheet. Please ask him/her if you have questions."><ixsl:value-of select="responsible"/></span></h1>

      <br clear="all"/>

      <div class="main">
        <h2>
        <!--
        <ixsl:value-of select="@value"/>
        -->

        <ixsl:choose>
        <ixsl:when test="@match != '' and @name != ''">
          <small>Match: </small><ixsl:value-of select="@match"/>
          <small> &#38; </small>
          <small>Name: </small><ixsl:value-of select="@name"/>
        </ixsl:when>
        <ixsl:when test="@match != ''">
          <small>Match: </small><ixsl:value-of select="@match"/>
        </ixsl:when>
        <ixsl:when test="@name != ''">
          <small>Name: </small><ixsl:value-of select="@name"/>
        </ixsl:when>
        </ixsl:choose>
        </h2>

        <p>
          <ixsl:value-of select="description"/>
        </p>
        
        <ixsl:if test="parent">
          <h3>Parent</h3>
          <p style="color:#990000">Must be a child of: <b><ixsl:value-of select="parent/name"/></b></p>
        </ixsl:if>

        <ixsl:if test="param">
          <h3>Parameter</h3>

          <ul>
            <ixsl:for-each select="param">
              <ixsl:sort select="@required" data-type="number" order="descending"/>
              <ixsl:sort select="name"/>
              
              <li>
                <ixsl:attribute name="class">
                  <ixsl:choose>
                    <ixsl:when test="(position() mod 2) = 0">low</ixsl:when>
                    <ixsl:otherwise>high</ixsl:otherwise>
                  </ixsl:choose>
                  <ixsl:if test="position()=last()"> last</ixsl:if>
                </ixsl:attribute>

                <h4>
                  <ixsl:attribute name="class">
                    <ixsl:if test="@required = '1'">required</ixsl:if>
                  </ixsl:attribute>

                  <ixsl:value-of select="name"/>
                </h4>

                <ixsl:if test="description">
                  <p><ixsl:apply-templates select="description/node()"/></p>
                </ixsl:if>
                

                <ixsl:if test="values">
                  <h5>Allowed Values</h5>

                  <ul>
                  <ixsl:for-each select="values/*">
                    <li>
                    <ixsl:choose>
                      <ixsl:when test="name() = 'int'">
                        <p>
                        <xsl:text>Numeric</xsl:text>
                        <ixsl:if test="@min|@max">
                          <xsl:text> [</xsl:text>
                            <ixsl:choose>
                              <ixsl:when test="@min"><xsl:value-of select="@min"/></ixsl:when>
                              <ixsl:otherwise>0</ixsl:otherwise>
                            </ixsl:choose>
                            <xsl:text> - </xsl:text>
                            <ixsl:choose>
                              <ixsl:when test="@max"><xsl:value-of select="@max"/></ixsl:when>
                              <ixsl:otherwise>32768</ixsl:otherwise>
                            </ixsl:choose>
                          <xsl:text>] </xsl:text>
                        </ixsl:if>
                        
                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>

                      <ixsl:when test="name() = 'string'">
                        <p>
                        <xsl:text>String</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      <ixsl:when test="name() = 'css'">
                        <p>
                        <xsl:text>CSS-Size</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      <ixsl:when test="name() = 'html'">
                        <p>
                        <xsl:text>HTML-Size</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      
                      <ixsl:when test="name() = 'path'">
                        <p>
                        <xsl:text>URI-Path</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      
                      <ixsl:when test="name() = 'xpath'">
                        <p>
                        <xsl:text>XPath</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>


                      <ixsl:when test="name() = 'oneof'">
                        <p>Choose from:</p>
                        <ul>
                        <ixsl:for-each select="option">
                          <li>
                            <ixsl:if test="@default='true'">
                              <ixsl:attribute name="class">default</ixsl:attribute>
                            </ixsl:if>
                            "<ixsl:apply-templates select="node()"/>"
                          </li>
                        </ixsl:for-each>
                        </ul>
                      </ixsl:when>

                    </ixsl:choose>
                    </li>
                  </ixsl:for-each>
                  </ul>
                </ixsl:if>



                <!-- ALT -->

                <ixsl:if test="possible_values/node()">
                  <h5>Allowed Values (Deprecated)</h5>
                  <p>
                    <ixsl:apply-templates select="possible_values/node()"/>
                  </p>
                </ixsl:if>

                <ixsl:if test="standard/node()">
                  <h5>Default (Deprecated)</h5>
                  <p>
                    <ixsl:apply-templates select="standard/node()"/>
                  </p>
                </ixsl:if>
              </li>

            </ixsl:for-each>
          </ul>
        </ixsl:if>
        
        <ixsl:if test="children">
          <h3>Children</h3>

          <ul>
            <ixsl:for-each select="children">
              <ixsl:sort select="@required" data-type="number" order="descending"/>
              <ixsl:sort select="name"/>

              <li>
                <ixsl:if test="position()=last()">
                  <ixsl:attribute name="class">last</ixsl:attribute>
                </ixsl:if>

                <h4>
                  <ixsl:attribute name="class">
                    <ixsl:if test="@required = '1'">required</ixsl:if>
                  </ixsl:attribute>

                  <ixsl:value-of select="name"/>
                </h4>

                <ixsl:if test="description">
                  <p><ixsl:apply-templates select="description/node()"/></p>
                </ixsl:if>
                
                
                
<!-- Das gleiche wie oben, Start -->

        <ixsl:if test="param">
          <h3>Parameter</h3>

          <ul>
            <ixsl:for-each select="param">
              <ixsl:sort select="@required" data-type="number" order="descending"/>
              <ixsl:sort select="name"/>
              
              <li>
                <ixsl:attribute name="class">
                  <ixsl:choose>
                    <ixsl:when test="(position() mod 2) = 0">low</ixsl:when>
                    <ixsl:otherwise>high</ixsl:otherwise>
                  </ixsl:choose>
                  <ixsl:if test="position()=last()"> last</ixsl:if>
                </ixsl:attribute>

                <h4>
                  <ixsl:attribute name="class">
                    <ixsl:if test="@required = '1'">required</ixsl:if>
                  </ixsl:attribute>

                  <ixsl:value-of select="name"/>
                </h4>

                <ixsl:if test="description">
                  <p><ixsl:apply-templates select="description/node()"/></p>
                </ixsl:if>
                

                <ixsl:if test="values">
                  <h5>Allowed Values</h5>

                  <ul>
                  <ixsl:for-each select="values/*">
                    <li>
                    <ixsl:choose>
                      <ixsl:when test="name() = 'int'">
                        <p>
                        <xsl:text>Numeric</xsl:text>
                        <ixsl:if test="@min|@max">
                          <xsl:text> [</xsl:text>
                            <ixsl:choose>
                              <ixsl:when test="@min"><xsl:value-of select="@min"/></ixsl:when>
                              <ixsl:otherwise>0</ixsl:otherwise>
                            </ixsl:choose>
                            <xsl:text> - </xsl:text>
                            <ixsl:choose>
                              <ixsl:when test="@max"><xsl:value-of select="@max"/></ixsl:when>
                              <ixsl:otherwise>32768</ixsl:otherwise>
                            </ixsl:choose>
                          <xsl:text>] </xsl:text>
                        </ixsl:if>
                        
                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>

                      <ixsl:when test="name() = 'string'">
                        <p>
                        <xsl:text>String</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      <ixsl:when test="name() = 'css'">
                        <p>
                        <xsl:text>CSS-Size</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      <ixsl:when test="name() = 'html'">
                        <p>
                        <xsl:text>HTML-Size</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      
                      <ixsl:when test="name() = 'path'">
                        <p>
                        <xsl:text>URI-Path</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>
                      
                      
                      <ixsl:when test="name() = 'xpath'">
                        <p>
                        <xsl:text>XPath</xsl:text>

                        <ixsl:if test="@default">
                          <xsl:text> ["</xsl:text>
                          <span class="default">
                            <ixsl:value-of select="@default"/>
                          </span>
                          <xsl:text>"] </xsl:text>
                        </ixsl:if>
                        </p>
                      </ixsl:when>


                      <ixsl:when test="name() = 'oneof'">
                        <p>Choose from:</p>
                        <ul>
                        <ixsl:for-each select="option">
                          <li>
                            <ixsl:if test="@default='true'">
                              <ixsl:attribute name="class">default</ixsl:attribute>
                            </ixsl:if>
                            "<ixsl:apply-templates select="node()"/>"
                          </li>
                        </ixsl:for-each>
                        </ul>
                      </ixsl:when>

                    </ixsl:choose>
                    </li>
                  </ixsl:for-each>
                  </ul>
                </ixsl:if>



                <!-- ALT -->

                <ixsl:if test="possible_values/node()">
                  <h5>Allowed Values (Deprecated)</h5>
                  <p>
                    <ixsl:apply-templates select="possible_values/node()"/>
                  </p>
                </ixsl:if>

                <ixsl:if test="standard/node()">
                  <h5>Default (Deprecated)</h5>
                  <p>
                    <ixsl:apply-templates select="standard/node()"/>
                  </p>
                </ixsl:if>
              </li>

            </ixsl:for-each>
          </ul>
        </ixsl:if>



<!-- Ende der Kopie -->




              </li>
            </ixsl:for-each>
          </ul>
        
        
        </ixsl:if>

        <ixsl:if test="/formresult/all_documentation/stylesheet/template_doc/example">
          <h3>Example</h3>
          
          <ul>
            <ixsl:for-each select="example">
              <li>

               <h4>Input</h4>
               <ixsl:apply-templates select="input/node()"/>

               <ixsl:if test="output/text()">
                  <h4>Output</h4>
                  <ixsl:apply-templates select="output/node()"/>
               </ixsl:if>

              </li>
            </ixsl:for-each>
          </ul>

        </ixsl:if>


      </div>
    </ixsl:for-each>
          </div>
  </xsl:template>
  

  <xsl:template match="displayerrortree">
    <xsl:param name="path"><xsl:value-of select="@errorpath"/></xsl:param>
    <table cellpadding="2" cellspacing="0" style="background-color: #aaaacc; border: 1px solid black">
      <tr>
        <td align="center" bgcolor="#cc0000" colspan="2">
          <span style="color:#ffffff; font-weight: bold"><xsl:apply-templates/></span>
        </td>
      </tr>
      <ixsl:for-each select="{$path}/error">
        <tr>
          <td bgcolor="#dd9999" colspan="2" align="center">
            <b><ixsl:value-of select="@type"/></b>
          </td>
        </tr>
        <ixsl:for-each select="./info">
          <tr>
            <ixsl:attribute name="bgcolor">
              <ixsl:choose>
                <ixsl:when test="count(preceding-sibling::info) mod 2 = 0">#aaaacc</ixsl:when>
                <ixsl:otherwise>#ccccee</ixsl:otherwise>
              </ixsl:choose>
            </ixsl:attribute>
            <td><b><ixsl:value-of select="@key"/>:</b></td>
            <td><ixsl:value-of select="@value"/></td>
          </tr>
        </ixsl:for-each>
      </ixsl:for-each>
    </table>
  </xsl:template>

</xsl:stylesheet>


