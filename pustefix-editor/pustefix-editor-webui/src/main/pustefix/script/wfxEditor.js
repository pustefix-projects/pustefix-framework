//#****************************************************************************
//#
//#****************************************************************************
function wfxEditor( config ) {
  
  if (typeof config == "undefined") {
    // new (default) configuration
    this.config = new wfxEditor.Config();
  } else {
    // existing configuration
    this.config = config;
  }
  
  this._editor  = null;   // HTMLElement.window
  this._toolbar = null;   // wfxToolbar object
  this._linebar = null;   // window
  
  this._designMode = false;
  this._editMode = "wysiwyg";
  
  this._doc  = null;
  
  this._timerToolbar = null; //XXX
  this._timestamp = null;

  this.skipRehighlighting   = true;
  this.needRehighlighting   = 0;
  this.cancelRehighlighting = false;
  this.insideRehighlighting = false;

  this.countRangeMarkers = 0;

  this._showLine   = document.getElementById("wfxed_line");
  this._showColumn = document.getElementById("wfxed_column");
  this._showMsg    = document.getElementById("wfxed_msg");
  this._dbg        = document.getElementById("dbg");

  this.showStatus = false;
  this.syntaxrehighlighting = this._showLine;

  this._scrollTop = 0;

  this._linepx    = 16;
  this._linebarheight = null;
  this._lineStart = -1;
  this._linenumber = 1;

  this._content_tag;
  this._content_src;
  this._content_col;

  this.undo = [];
  this.currentUndo = -1;

  if( wfx.is_ie ) {
    this.newline = '\r\n';
  } else {
    this.newline = '\n';
  }
}

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.Config = function () {

  this.maxLines = 3333;   // max lines for Moz.

  this.maxUndo = 100;

  this.doctype = '<!' + 'DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">';
  this.html = '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">';
  this.head = '<meta http-equiv="Content-type" content="text/html; charset=UTF-8" /><title></title>';

  this.style_linenumbers = '<style type="text/css">' + 
    'body,pre { font-family: monospace; font-size: 13px !important; margin:0px !important; background-color:#eeeeee; cursor:default }\n' + 
    'pre { text-align:right; }\n' +
    '</style>';

  this.style_source = '<style type="text/css">' + 
    'body,pre { font-family: monospace; font-size: 13px !important; margin:0px !important}\n' + 
    'p    { margin-top: 0px !important; margin-bottom: 0px !important; }\n' + 
    'a { text-decoration: none }\n' + 
    '.string      { color:#118800; font-weight:normal }\n' + 
    '.htmltag     { color:#0000ff; font-weight:normal }\n' + 
    '.htmltagname { color:#ffaa44; font-weight:bold }\n' + 
    '.htmlendtag  { color:#0000ff; font-weight:normal }\n' + 
    '.pfxtag      { color:#0000ff; font-weight:normal }\n' + 
    '.pfxtagname  { color:#0000aa; font-weight:bold }\n' + 
    '.pfxendtag   { color:#0000ff; font-weight:normal }\n' + 
    '.xsltag      { color:#0000ff; font-weight:normal }\n' + 
    '.xsltagname  { color:#dd0000; font-weight:bold }\n' + 
    '.xslendtag   { color:#0000ff; font-weight:normal }\n' + 
    '.ixsltag     { color:#0000ff; font-weight:normal }\n' + 
    '.ixsltagname { color:#cc44aa; font-weight:bold }\n' + 
    '.ixslendtag  { color:#0000ff; font-weight:normal }\n' + 
    '.entity      { color:#cc0000; font-weight:normal }\n' + 
    '.comment     { color:#aaaaaa; font-weight:normal }\n' + 
    '.cdata       { color:#009999; font-weight:normal }\n' + 
    '</style>';

  this.width = "auto";
  this.height = "auto";

  // the next parameter specifies whether the toolbar should be included
  // in the size or not.
  this.sizeIncludesToolbar = true;

  // style included in the iframe document
  this.pageStyle = "body { background-color: #00f; font-family: verdana,sans-serif; }";
  if (typeof _editor_url != "undefined") {
    this.editorURL = _editor_url;
  } else {
    this.editorURL = "";
  }

  // URL-s
  this.imgURL = "images/";
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.registerToolbar = function( wfxToolbar ) {
  
  this._toolbar = wfxToolbar;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.dbg = function() {

  //  this._dbg.value += "\n---getHTML()------------------------------------------------------------------------------------\n" + this.getHTML();

  this._dbg.value += "\n---col2tag.getHTML()----------------------------------------------------------------------------\n" + this.col2tag(this.getHTML());

  //  this._dbg.value += "\n---tag2src.col2tag.getHTML()--------------------------------------------------------------------\n" + this.tag2src(this.col2tag(this.getHTML()));
																   
  //  this._dbg.value += "\n---src2col.tag2src.col2tag.getHTML()------------------------------------------------------------\n" + this.src2col(this.tag2src(this.col2tag(this.getHTML())));
  
  this._dbg.value += "\n================================================================================================\n\n";
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.setRangeMarker = function() {

  var sel = this._getSelection();
  var rng = this._createRange(sel);

  this.countRangeMarkers++;

  if( wfx.is_ie ) {

    rngStart = rng.duplicate();
    rngStart.collapse(true);
    rngStart.pasteHTML('<span id="sfxStart' + this.countRangeMarkers + '"></span>');

    //    alert("sel.type:" + sel.type);
    if( sel.type == "Text" ) {
      rngEnd   = rng.duplicate();
      rngEnd.collapse(false);
      rngEnd.pasteHTML('<span id="sfxEnd' + this.countRangeMarkers + '"></span>');
    }

  } else {

    var startContainer, endContainer, startOffset, endOffset;
    
    startContainer = rng.startContainer;
    endContainer   = rng.endContainer;
    startOffset    = rng.startOffset;
    endOffset      = rng.endOffset;
    
    //    alert( "startContainer:>>>"+ startContainer.nodeValue + "<<<\n" +
    //	   "endContainer:>>>"+ endContainer.nodeValue + "<<<\n" +
    //	   "startOffset:"+ startOffset + "\n" +
    //	   "endOffset:"+ endOffset);

    var mydoc  = this._doc.createDocumentFragment();
    var mynode;

    mynode = this._doc.createElement("span");
    mynode.id = "sfxStart" + this.countRangeMarkers;
    mydoc.appendChild(mynode);
    //    mynode = this._doc.createComment("sfxStart");
    //    mydoc.appendChild(mynode);

    if( !sel.isCollapsed) {
      mynode = this._doc.createElement("span");
      mynode.id = "sfxEnd" + this.countRangeMarkers;
      mydoc.appendChild(mynode);

      var rngEnd = this._doc.createRange();
      rngEnd.setStart( endContainer, endOffset );
      rngEnd.setEnd(   endContainer, endOffset );
      rngEnd.insertNode(   mydoc.lastChild );
    }

    var rngStart = this._doc.createRange();
    rngStart.setStart( startContainer, startOffset );
    rngStart.setEnd(   startContainer, startOffset );
    rngStart.insertNode( mydoc.firstChild );
  }

  //  this.dbg();
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.changeRangeMarker = function(buf) {

  //  var arr = buf.match( /<span id="sfxStart"><\/span>/g );
  //  if( arr instanceof Array ) {
  //    if( arr.length > 1 ) {
  //      this._dbg.value += "A" + arr.length + " ";
  //    }
  //  }

  var rule_marker = new RegExp( '<span id="(sfx(Start|End)' + 
				this.countRangeMarkers + ')"><\/span>', "g" );
  buf = buf.replace( rule_marker, "[[[sfx$2]]]");
  //  alert(this.countRangeMarkers + "\n" + buf);

  buf = buf.replace( /<span id="(sfx.+?)"><\/span>/g, "");
  return buf;


  //  if( this.countRangeMarkers > 1 ) {
  //    buf = buf.replace( /<span id="sfx(Start|sfxEnd)"><\/span>/g, "[[[$1]]]");
  //  } else {
  return buf.replace( /<span id="(sfx.+?)"><\/span>/g, "[[[$1]]]");
  //  }
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.removeChangedRangeMarker = function(buf) {

  return buf.replace( /\[\[\[sfx.*?\]\]\]/g, "" );
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.rehighlight = function() {

  this.skipRehighlighting = true;
  
  var content;

  //----------------
  // setRangeMarker
  //----------------

  this.setRangeMarker();

  //-----------------------
  // get content (col2tag)
  //-----------------------
 
  content = this.col2tag(this.getHTML());

  //  alert(wfxEditor.str2chr(content));

  //------------
  // getOffsets
  //------------
    
  var ret = new Array();
  this.getOffsets( content, ret );
  
  var offsetStart         = ret[0];
  var offsetNewlinesStart = ret[1];
  var offsetEnd           = ret[2];
  var offsetNewlinesEnd   = ret[3];
  var posEnd              = ret[4];
  //---------------------------------------------------------------------------

  try {

    //--------------------------
    // removeChangedRangeMarker
    //--------------------------

    content = this.removeChangedRangeMarker(content);

    //--------------------------------------------
    // transform content in step 1 of 2 (tag2src)
    //--------------------------------------------

    content = this.tag2src(content);

    //--------------------------------------------
    // transform content in step 2 of 2 (src2col)
    //--------------------------------------------

    content = this.src2col(content);

    //-------------------------
    // set content (innerHTML)
    //-------------------------
    
    content = this.prepareContent( content );
    
    this._doc.body.innerHTML = '<pre>' + content + '</pre>';
  } catch(e) {
    alert("Exception:\n" + e);
  }

  //------------
  // setOffsets
  //------------

  this.setOffsets( [ offsetStart, offsetNewlinesStart, 
		     offsetEnd,   offsetNewlinesEnd,
		     posEnd ] );
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.isWellFormedXML = function( xml, err ) {

  if( xml.indexOf("<?xml ") != 0 ) {
    // prepend XML declaration
    xml = '<?xml version="1.0" encoding="utf-8" ?>\n' +
      '<sfxroot ' + de_schlund_pfixcore_editor_namespace_decl + '>\n' + xml + '\n</sfxroot>\n';
  }

  if( typeof err == "undefined" ) {
    return false;
  } else if( ! err instanceof Array ) {
    err = new Array();
  }

  //  alert(wfxEditor.str2chr(xml));
  var xDoc = null;
  try {
    if(window.DOMParser) {
      xDoc = new DOMParser().parseFromString( xml, "text/xml" );
    } else if (window.ActiveXObject) {
      xDoc       = new ActiveXObject("Microsoft.XMLDOM");
      xDoc.async = false;
      xDoc.loadXML(xml);
    } else {
      return false;
    }
  } catch(e) {
    alert("Exception:\n" + e);
    return false;
  }

  if( !xDoc ) {
    return false;
  }

  if( xDoc.documentElement && xDoc.documentElement.nodeName == "parsererror" ) {
    var msg = xDoc.documentElement.firstChild.nodeValue;

    var rule_errReason = /^[^\n]*?:\s*([^\0]*?)\n[^\0]*$/;
    var rule_errLine   = /^[^\n]*\n[^\n]*\n\D+(\d+)[^\0]*$/;
    var rule_errColumn = /^[^\n]*\n[^\n]*\n[^\,]*\,\D+(\d+)[^\0]*$/;
    var rule_errSource = /^([^\0]*?)[\s\-]*\^$/;

    if( rule_errReason.test(msg) ) {
      err["Reason"] = msg.replace( rule_errReason, "$1");
    }
    if( rule_errLine.test(msg) ) {
      err["Line"] = msg.replace( rule_errLine, "$1") - 3;   // XXX
    }
    if( rule_errColumn.test(msg) ) {
      err["Column"] = msg.replace( rule_errColumn, "$1");
    }
    if( xDoc.documentElement.lastChild.nodeName == "sourcetext" ) {
      err["Source"] = xDoc.documentElement.lastChild.firstChild.nodeValue;
      err["Source"] = err["Source"].replace( rule_errSource, "$1");
    }
    
    //    alert(wfxEditor.str2chr(msg));
    //    alert ( new XMLSerializer ().serializeToString( xDoc.documentElement ));
  }
  
  if( xDoc.parseError && xDoc.parseError.errorCode != 0) {

    var rule_errReason = /^([^\0]*?)[\r\n]*$/;

    err["Reason"] = xDoc.parseError.reason;
    err["Reason"] = err["Reason"].replace( rule_errReason, "$1");
    err["Line"]   = xDoc.parseError.line-1;
    err["Column"] = xDoc.parseError.linepos;
    err["Source"] = xDoc.parseError.srcText;
  }

  if( err["Line"] || err["Column"] || err["Reason"] || err["Source"] ) {
    return false;
  }
  
  return true;
}

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.initLineNumbers = function() {

  var body = '<body id="bodynode" onload="setUnselectable()">\n';

  var content = "<pre>"
  var maxLines;
  if( wfx.is_ie ) {
    maxLines = this._linebarheight;
  } else {
    maxLines = this.config.maxLines;
  }
  for( var i=1; i<maxLines; i++ ) {
    content += i + "&nbsp;\n";
  }
  content += "</pre>";

  var script = 
    '<script type="text/javascript">' + 
    'function stopEvent(ev) {' +
    '  if( typeof ev.preventDefault != "undefined" ) {' + 
    '    ev.preventDefault();' + 
    '    ev.stopPropagation();' + 
    '  } else {' + 
    '    ev.cancelBubble = true;' + 
    '    ev.returnValue = false;' + 
    '  }' + 
    '}' + 
    'function setUnselectable() {' + 
    '  if(window.document.attachEvent) {' +
    '	window.document.attachEvent( "onmousedown", stopEvent);' + 
    '	window.document.attachEvent( "onkeypress",  stopEvent);' + 
    '	window.document.body.attachEvent( "onclick",  stopEvent);' + 
    '	window.document.body.attachEvent( "onkeypress",  stopEvent);' + 
    '	window.document.body.attachEvent( "onfocus",     stopEvent);' + 
    '  }' + 
    '  if( window.addEventListener ) {' + 
    '	window.addEventListener( "mousedown", stopEvent, true);' + 
    '	window.addEventListener( "keypress",  stopEvent, true);' + 
    '	window.addEventListener( "focus",     stopEvent, true);' + 
    '  }' + 
    '}' + 
    '</script>';

  content = this.config.doctype + this.config.html + '<head>' + this.config.head + this.config.style_linenumbers + 
    script + '</head>' +  body + content + '</body></html>';

  var doc = this._linebar.document;

  doc.open();
  doc.writeln(content);
  doc.close();
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.tag2src = function( buf ) {

  // order is important

  // tags --> entities
  buf = buf.replace( /&/g, "&amp;" );
  buf = buf.replace( /</g, "&lt;" );
  buf = buf.replace( />/g, "&gt;" );
  //    buf = buf.replace( /"/g, "&quot;" );
  
  //buf = buf.replace( /(\r?)\n/g, '<br />$1\n' );

  if( wfx.is_ie ) {
    buf = buf.replace( /\r\n/g, '<br />' );
  } else {
    buf = buf.replace( /\n/g, '<br />' );
  }

  // colorize indentation
  //  buf = buf.replace( /<br \/>(\s{4})/, '<br /><span style="background-color:red">$1</span>' );

  //	buf = buf.replace( /&lt;\/P&gt;/g, '&lt;/P&gt;</span><br>' );

  return buf;
}
    
//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.src2col = function( buf ) {

  //  return buf;
  //  alert("src2col" + wfxEditor.str2chr(buf));

  var dbg_bench = 0;

  dbg_bench && bench( null, 10);

  dbg_bench && bench( null, 6);

  var rule_htmlTag        = /&lt;([\w\-]+\b.*?)&gt;/g;
  var rule_htmlTagName    = /&lt;(\/?[\w\-]+)\b(.*?)&gt;/g;
  var rule_htmlEndTag     = /&lt;(\/[\w\-]+)&gt;/g;

  //  var rule_pfxTag      = /&lt;(pfx\:\w+[\s\S]*?)&gt;/g;
  //  var rule_pfxTagName  = /&lt;(pfx\:\w+)\b(.*?)&gt;/g;
  //  var rule_pfxEndTag   = /&lt;(\/pfx\:\w+)&gt;/g;
  //
  //  var rule_xslTag      = /&lt;(xsl\:\w+[\s\S]*?)&gt;/g;
  //  var rule_xslTagName  = /&lt;(xsl\:\w+)\b(.*?)&gt;/g;
  //  var rule_xslEndTag   = /&lt;(\/xsl\:\w+)&gt;/g;
  //
  //  var rule_ixslTag     = /&lt;(ixsl\:\w+[\s\S]*?)&gt;/g;
  //  var rule_ixslTagName = /&lt;(ixsl\:\w+)\b(.*?)&gt;/g;
  //  var rule_ixslEndTag  = /&lt;(\/ixsl\:\w+)&gt;/g;

  var rule_colonTag        = /&lt;((pfx|ixsl|xsl)\:[\w\-]+[\s\S]*?)&gt;/g;
  var rule_colonTagName    = /&lt;(\/?(pfx|ixsl|xsl)\:[\w\-]+)\b(.*?)&gt;/g;
  var rule_colonEndTag     = /&lt;(\/(pfx|ixsl|xsl)\:[\w\-]+)&gt;/g;

  //  var rule_argval  = /(\"[^\0]*?\")/g;
  var rule_argval_nonglobal  = /=\s*([\"\'])[^\0]*?\1/;
  var rule_argval  = /(=\s*)(([\"\'])[^\0]*?\3)/g;
  //  var rule_argvalcondition  = /(=\s*)(([\"\'])[^\3]*?&[^\3]*?\3)/g;
  var rule_argvalcondition  = /(=\s*)(\"[^\"]*?&[^\"]*?\")/g;

  var rule_entity  = /(&amp;[^&]+?;)/g;
  var rule_comment = /(&lt;!--[^\0]*?--&gt;)/g;
  var rule_cdata   = /(&lt;!\[CDATA\[[^\0]*?\]\]&gt;)/g;
  var rule_script   = /&lt;script[^\0]*?&gt;([^\0]*?)&lt;\/script&gt;/g;

  dbg_bench && bench( "\n------------------------\n  rules", null, 6);

  //-------------------------
  // comments: preprocessing
  //-------------------------
  
  dbg_bench && bench( null, 6 );
  var comments = buf.match( rule_comment );
  if( comments instanceof Array ) {
    for( var i=0; i<comments.length; i++ ) {
      buf = buf.replace( new RegExp(comments[i].replace(/(\W)/g,'\\$1')), '&lt;!-- '+i+' --&gt;' );
    }
  }
  dbg_bench && bench( "  comments1", null, 6);

  dbg_bench && bench( null, 6 );
  var cdatas = buf.match( rule_cdata );
  if( cdatas instanceof Array ) {
    for( var i=0; i<cdatas.length; i++ ) {
      buf = buf.replace( new RegExp(cdatas[i].replace(/(\W)/g,'\\$1')), '&lt;!\[CDATA\[ '+i+' \]\]&gt;' );
    }
  }
  dbg_bench && bench( "  cdatas1", null, 6);

  dbg_bench && bench( null, 6 );
  var scripts = buf.match( rule_script );
  var scripts2;

  var scripttmp = new Array(3);
  if( scripts instanceof Array ) {
    for( var i=0; i<scripts.length; i++ ) {
      /(&lt;script[^\0]*?&gt;)([^\0]*?)(&lt;\/script&gt;)/.test(scripts[i]);
      scripttmp[0] = RegExp.$1;
      scripttmp[1] = RegExp.$2;
      scripttmp[2] = RegExp.$3;
      buf = buf.replace( new RegExp(scripts[i].replace(/(\W)/g,'\\$1')), scripttmp[0] + '&lt;!script '+i+' &gt;' + scripttmp[2]);
      scripts[i] = scripttmp[1];
    }
  }
  dbg_bench && bench( "  scripts1", null, 6);
  //---------------------------------------------------------------------------

  //--------------------------------------
  // attribute values containing brackets
  //--------------------------------------

  dbg_bench && bench( null, 6 );
  var argvals = buf.match( rule_argvalcondition );
  var oldexp, newstr;
  var res, pos = [];
  while( res = rule_argvalcondition.exec(buf) ) {
    argvals = res[0];
    pos[2] = res.index;

    pos[0] = buf.lastIndexOf("&lt;", pos[2]);
    pos[1] = buf.lastIndexOf("&gt;", pos[2]);
    pos[3] = buf.indexOf(    "&lt;", pos[2]+argvals.length);
    pos[4] = buf.indexOf(    "&gt;", pos[2]+argvals.length);

    if( (pos[0]>pos[1] && pos[3]>pos[4]) ) {
      // inside tag
      oldexp = new RegExp(argvals.replace(/(\W)/g,'\\$1'));
      newstr = argvals.replace(/&/g, '[[[sfxEntity]]]').replace(/\$/g, '$$$' );
      buf = buf.replace( oldexp, newstr );
    }
  }
  dbg_bench && bench( "  argvalcondition", null, 6);
  //---------------------------------------------------------------------------

  //------
  // tags
  //------

  if( !wfx.is_ie ) {
    dbg_bench && bench( null, 6 );
    var tmp, vals;
    var rule_tag = /(&lt;[\w\:\-]+\b.*?&gt;)/g;
    var tags, vals;

    if(  wfx.is_ie ) {
      var tagsin  = buf.match( rule_tag );
      var tagsout = buf.split( rule_tag );
      buf = "";
      //      alert( tagsin.length + "tagsin:\n" + tagsin.join("\n--------------------\n"));
      //      alert( tagsout.length + "tagsout:\n" + tagsout.join("\n--------------------\n"));
      var valsin, valsout;
      for( var j=0; j<tagsin.length; j++ ) {
	if( /^&lt;[^\0]*?&gt;$/.test(tagsin[j]) ) {

	  valsin  = tagsin[j].match( rule_argval );
	  valsout = tagsin[j].split( rule_argval );
	  if( valsin instanceof Array && valsout instanceof Array) {
	    tmp = "";
	    for( var l=0; l<valsin.length; l++ ) {
	      tmp += valsout[l] + '<span class="string">' + valsin[l] + '</span>';
	    }
	    tmp += valsout[l];
	    buf += tmp;
	  }
	}
      }
      buf += tagsout[j];
    } else {
    
      tags = buf.split( rule_tag );

      buf = "";
      //      alert( tags.join("\n--------------------\n"));
      for( var i=0; i<tags.length; i++ ) {
	if( /^&lt;[^\0]*?&gt;$/.test(tags[i]) ) {

	  vals = tags[i].split( rule_argval );
	  //	  alert( vals.join("\n--------------------\n"));
	  tmp = "";
	  if( vals instanceof Array) {
	    for( var j=0; j<vals.length-4; j=j+4 ) {
	      tmp += vals[j] + vals[j+1] + '<span class="string">' + vals[j+2] + '</span>';
	    }
	    tmp += vals[vals.length-1];
	    buf += tmp;
	  }
	} else {
	  buf += tags[i];
	}
      }
    }
    dbg_bench && bench( "  argval", null, 6);
  }

  if( wfx.is_ie ) {
    dbg_bench && bench( null, 6 );
    var pos;
    var arr = buf.match( rule_htmlTag );
    //    var arr = buf.match( rule_argval );
    if( arr instanceof Array ) {

      var oldexp, newexp, myexp;
      for( var i=0; i<arr.length; i++ ) {

	if( buf.indexOf(arr[i]) != -1 ) {
	  // only if not yet replaced (redundancy!)

	  if( rule_argval_nonglobal.test(arr[i]) ) {
	    
	    newexp = arr[i].replace( rule_argval, '$1<span class="string">$2</span>');
//	    if(newexp != arr[i]) {
//	    //   quotemeta
	    arr[i] = arr[i].replace( /(\W)/g, '\\$1' );
	    newexp = newexp.replace( /\$/g, '$$$' );  // escape $ for IE as $$
	    //	  alert( "oldexp:" + arr[i] + "\nnewexp:" + newexp);
	    myexp = new RegExp( arr[i], "g" );
	    buf = buf.replace( myexp, newexp );
	  }
	}
      }
    }
    dbg_bench && bench( "  argval", null, 6);
  }
  //---------------------------------------------------------------------------

  dbg_bench && bench( null, 6 );
  buf = buf.replace( rule_colonTag,     '<span class="$2tag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_colonEndTag,  '<span class="$2endtag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_colonTagName, '&lt;<span class="$2tagname">$1</span>$3&gt;' );
  dbg_bench && bench( "  colon", null, 6);

  dbg_bench && bench( null, 6 );
  buf = buf.replace( rule_htmlTag,    '<span class="htmltag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_htmlEndTag, '<span class="htmlendtag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_htmlTagName,'&lt;<span class="htmltagname">$1</span>$2&gt;' );
  
  // remove superfluous html tags, because they actually are xml tags
  //  buf = buf.replace( /&lt;<span class="htmltagname">(\w+)<\/span>:/g, "&lt;$1:");
  //  buf = buf.replace( /<span class="htmltag">(&lt;\w+\:\w+.*?&gt;)<\/span>/g, "$1");

  dbg_bench && bench( "  html", null, 6);

  dbg_bench && bench( null, 6 );
  buf = buf.replace( rule_entity,     '<span class="entity">$1</span>' );
  dbg_bench && bench( "  entities", null, 6 );

  //--------------------------
  // comments: postprocessing
  //--------------------------

  dbg_bench && bench( null, 6);
  if( scripts instanceof Array ) {
    for( var i=0; i<scripts.length; i++ ) {

      buf = buf.replace( new RegExp('&lt;!script '+i+' ([^\0]*?)&gt;'), '<span class="cdata">'+scripts[i].replace(/\$/g,'$$$')+'</span>' );
    }
  }
  dbg_bench && bench( "  scripts2", null, 6);

  dbg_bench && bench( null, 6);
  if( cdatas instanceof Array ) {
    for( var i=0; i<cdatas.length; i++ ) {

      buf = buf.replace( new RegExp('&lt;!\\[CDATA\\[ '+i+' \\]\\]&gt;'), '<span class="cdata">'+cdatas[i].replace(/\$/g,'$$$')+'</span>' );
    }
  }
  dbg_bench && bench( "  cdatas2", null, 6);

  dbg_bench && bench( null, 6);
  if( comments instanceof Array ) {
    for( var i=0; i<comments.length; i++ ) {
      buf = buf.replace( new RegExp('&lt;!-- '+i+' --&gt;'), '<span class="comment">'+comments[i].replace(/\$/g,'$$$')+'</span>' );
    }
  }
  dbg_bench && bench( "  comments2", null, 6);

  dbg_bench && bench( null, 6);
  buf = buf.replace(/\[\[\[sfxEntity\]\]\]/g, '&');
  dbg_bench && bench( "  argvalcondition2", null, 6);

  dbg_bench && bench( "src2col(intern)", null, 10);

  return buf;
}

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.col2tag = function( buf ) {

  // order is important

  if( wfx.is_ie ) {
    buf = buf.replace( /<br \/>/g, "\r\n");
    buf = buf.replace( /<pre \/>/g, "\r\n");
    buf = buf.replace( /<\/pre>([\r\n]*)<pre>/g, "$1\r\n");
  } else {
    buf = buf.replace( /<br \/>/g, "\n");
  }

  buf = this.changeRangeMarker(buf);

  // remove color spans
  buf = buf.replace( /<[^>]+>/g, ""  );

  // entities --> tags
  buf = buf.replace( /&lt;/gi,   "<" );
  buf = buf.replace( /&gt;/gi,   ">" );
  buf = buf.replace( /&amp;/gi,  "&" );
  buf = buf.replace( /&quot;/gi, "\"");
  
  if( wfx.is_ie ) {
    //      buf = buf.replace( /\r\n\r\n/g, "\r\n");
  }
  
  buf = buf.replace( /^[\r\n]*([^\r\n])/g, "$1");
  buf = buf.replace( /([^\r\n])[\r\n]*$/g, "$1");
  
  return buf;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.prepareContent = function( content ) {

  if(wfx.is_ie) {
    content = content.replace( /<br \/>/g, '\r\n' );
  }

  return content;
};

// the execCommand function (intercepts some commands and replaces them with
// our own implementation)
//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.execCommand = function(cmdID, UI, param) {
  var editor = this;	// for nested functions

  this.focusEditor();

  switch (cmdID.toLowerCase()) {
  case "undo":
    if( this.currentUndo-1 >= 0 ) {
      try {
	this._doc.body.innerHTML = '<pre>' + this.undo[this.currentUndo-1][0] + '</pre>';
	this.setOffsets( this.undo[this.currentUndo-1][1] );
	this.currentUndo--;
      } catch(e) {
	alert("Exception:\n" + e);
      }
    }
    break;
  case "redo":
    if( this.currentUndo+1 <= this.undo.length-1 ) {
      try {
	this._doc.body.innerHTML = '<pre>' + this.undo[this.currentUndo+1][0] + '</pre>';
	this.setOffsets( this.undo[this.currentUndo+1][1] );
	this.currentUndo++;
      } catch(e) {
	alert("Exception:\n" + e);
      }
    }
    break;
  default: 
    try { 
      //      alert("execCommand: " + cmdID + ", " + UI + ", " + param);
      this._doc.execCommand(cmdID, UI, param);
    } catch(e) {
    }
  }
  return false;
};

/***************************************************
 *  Category: EVENT HANDLERS
 ***************************************************/

/** A generic event handler for things that happen in the IFRAME's document.
 * This function also handles key bindings. */
wfxEditor.prototype._editorEvent = function(ev) {
  //  alert("_editorEvent()..." + ev.type);

  var editor = this;

  var keyEvent = (wfx.is_ie && ev.type == "keydown") || (ev.type == "keypress");
  //  var keyEvent = (ev.type == "keydown") || (ev.type == "keypress");
  //  this._dbg.value += ev.type + ", " + ev.keyCode;
  //---------------------------------------------------------------------------

  if( ev.type == "mousedown" || ev.type == "scroll" ) {
    // no other actions if mousedown (e.g. allow mouse selection in IE)
    //    return;
  }
  //---------------------------------------------------------------------------

  if (keyEvent && !(ev.type == "mousedown" || ev.type == "scroll") ) {
    // other keys here

    //    alert("keyEvent: " + (keyEvent==true) + "\n" + ev.type + ", " + 
    //	  "\nkeyCode: " + ev.keyCode + "\ncharCode: " + ev.charCode + 
    //	  "\nCtrl: " + (ev.ctrlKey==true) + "\nAlt: " + (ev.altKey==true) );

    //    this._dbg.value += "k" + ev.keyCode + " ";

    switch( ev.keyCode ) {
  
    case 0:
      this.skipRehighlighting = false;
      //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	
      //------
      // Ctrl
      //------

      if( ev.ctrlKey ) {
	switch( ev.charCode ) {
	case 97:   // Ctrl-A
	  //	  alert("Ctrl-A");
	  this.skipRehighlighting = true;
	  return;
	  break;
	case 99:   // Ctrl-C
	  this.skipRehighlighting = true;
	  return;
	  break;
	case 101:   // Ctrl-E
	  this.skipRehighlighting = true;
	  wfxEditor._stopEvent(ev);
	  return;
	  break;
	case 107:   // Ctrl-K
	  //	  alert("ctrl-k:\n" + wfxEditor.str2chr(this.getHTML()));
	  this.skipRehighlighting = true;
	  wfxEditor._stopEvent(ev);
	  //	  this.focusEditor();
	  return;
	  break;
	}
      }
      //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      //-----
      // Alt
      //-----

      if( ev.altKey ) {
	switch( ev.charCode ) {
	case 97:   // Alt-A
	  this.skipRehighlighting = true;
	  return;
	  break;
	}
      }
      //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

      this.needRehighlighting++;

      if( this.insideRehighlighting ) {
	//	alert("event1: insideRehighlighting ==> cancelRehighlighting");
	//	this._dbg.value += "event1 ";
	this.cancelRehighlighting = true;
      } else {
	this.cancelRehighlighting = false;
      }
      this._timestamp = (new Date()).getTime();
      break;

    case 65:
      if( ev.ctrlKey ) {
	// Ctrl-A
	this.needRehighlighting = 0;
	this.skipRehighlighting = true;
	return;
      }
      break;
	
    case 13: // KEY enter
      //      if (wfx.is_ie) {
      //	this.insertHTML("&lrm;");
      //	//	wfxEditor._stopEvent(ev);
      //      }

      this.skipRehighlighting = true;
      
      //      var indent = this.getIndentLevel();
      //      this._showMsg.value = "indent:" + indent;
      //      alert("indent:" + indent);

      break;
      
    case 9:

      //-----
      // TAB
      //-----

      this.cancelRehighlighting = true;
      this.skipRehighlighting = true;

      var cursor = this._doc.body.style.cursor;
      document.body.style.cursor  = "wait";
      this._doc.body.style.cursor = "wait";

      var content, buf, content2;
  
      //----------------
      // setRangeMarker
      //----------------

      this.setRangeMarker();

      //-----------------------
      // get content (col2tag)
      //-----------------------
 
      var optiTab = 0;

      if( optiTab ) {
	buf      = this.getHTML();

	buf = buf.replace( /^\n/, "");
	buf = buf.replace( /\n$/, "");

	content  = buf; 
	content  = this.changeRangeMarker(content);
      } else {
	content = this.col2tag(this.getHTML());
      }
      //alert("1:\n" + wfxEditor.str2chr(content));
      //-------------------------------------------------------------------------


      bench( null, 8 );

      //	content = this.indentCurrentLine( content, optiTab );
      content = this.indentCurrentRange( content, optiTab );

      //      alert("after indent:\n" + wfxEditor.str2chr(content));

      bench( "...indentCurrentRange", null, 8 );

      //      this._dbg.value = benchMsg;
      //-------------------------------------------------------------------------
      //-------------------------------------------------------------------------

      if( optiTab ) {
	buf     = content;
	content = this.col2tag(content);
      }
      //-------------------------------------------------------------------------

      //------------
      // getOffsets
      //------------
    
      //      alert("getOffsets:\n" + wfxEditor.str2chr(content));

      var ret = new Array();
      this.getOffsets( content, ret );

      var offsetStart         = ret[0];
      var offsetNewlinesStart = ret[1];
      var offsetEnd           = ret[2];
      var offsetNewlinesEnd   = ret[3];
      var posEnd              = ret[4];
	
      //	alert("offsetStart:" + offsetStart + ", offsetNewlinesStart:" + offsetNewlinesStart + "\noffsetEnd:" + offsetEnd + ", offsetNewlinesEnd:" +  offsetNewlinesEnd + "\nposEnd:" + posEnd);
      //-------------------------------------------------------------------------

      try {

	//--------------------------
	// removeChangedRangeMarker
	//--------------------------

	if( optiTab ) {
	  content = buf;
	}

	content = this.removeChangedRangeMarker(content);
	//	alert( wfxEditor.str2chr(content) );

	//--------------------------------------------
	// transform content in step 1 of 2 (tag2src)
	//--------------------------------------------

	if( !optiTab ) {
	  content = this.tag2src(content);
	}
	//--------------------------------------------
	// transform content in step 2 of 2 (src2col)
	//--------------------------------------------

	if( !optiTab ) {
	  content = this.src2col(content);
	}
	//-------------------------
	// set content (innerHTML)
	//-------------------------

	content = this.prepareContent( content );
	//	  alert(content);

	if( optiTab ) {
	  this._doc.body.innerHTML = '<pre>' + content + '</pre>';
	  //	  alert("innerHTML:\n" + wfxEditor.str2chr(content));
	} else {
	  this._doc.body.innerHTML = '<pre>' + content + '</pre>';
	  //	  alert("innerHTML:\n" + wfxEditor.str2chr('<pre>' + content + '</pre>'));
	}

	document.body.style.cursor  = cursor;
	this._doc.body.style.cursor = cursor;

      } catch(e) {
	alert("Exception:\n" + e);
      }

      //------------
      // setOffsets
      //------------

      this.setOffsets( [ offsetStart, offsetNewlinesStart, 
			 offsetEnd,   offsetNewlinesEnd,
			 posEnd ] );

      // Moz
      wfxEditor._stopEvent(ev);
      this.cancelRehighlighting = false;
      return;
      break;

    case 16: // Shift
    case 17: // Ctrl
    case 18: // Alt
    case 33: // PageUp
    case 34: // PageDown
    case 35: // End
    case 36: // Home
    case 37: // CursorLeft
    case 38: // CursorUp
    case 39: // CursorRight
    case 40: // CursorDown
      this.skipRehighlighting = true;
      if( this.insideRehighlighting ) {
	this.cancelRehighlighting = true;
      } else {
	this.cancelRehighlighting = false;
      }
      break;
    default:
      this.needRehighlighting++;
      this.skipRehighlighting = false;
      if( this.insideRehighlighting ) {
	//	alert("event2: insideRehighlighting ==> cancelRehighlighting");
	this.cancelRehighlighting = true;
	//	this._dbg.value += "event2 ";
      } else {
	this.cancelRehighlighting = false;
      }
      this._timestamp = (new Date()).getTime();
    }
  } else if(!(ev.type == "mousedown" || ev.type == "scroll")) {
    //    wfxEditor._stopEvent(ev);
  }
  //---------------------------------------------------------------------------

  //#  // update the toolbar state after some time
  //#  if (editor._timerToolbar) {
  //#    clearTimeout(editor._timerToolbar);
  //#  }
  //#  editor._timerToolbar = setTimeout
  //#    (function() {
  //#
  //#      if( 0 ) {
  //#
  //#	var sel = editor._getSelection();
  //#	var rng = editor._createRange(sel);
  //#
  //#	if(wfx.is_ie) {
  //#
  //#	  //----
  //#	  // IE
  //#	  //----
  //#
  //#	  var parNode   = rng.parentElement().nodeName;
  //#	  var className = rng.parentElement().className;
  //#	  var content;
  //#      
  //#	  if( parNode == "BODY" || parNode == "PRE" ) {
  //#
  //#	    // expand to the left till ">"
  //#	    while( rng.moveStart( "character", -1 ) &&
  //#		   rng.text.charAt(0)!=">" ) {
  //#	    }
  //#	    rng.moveStart( "character", 1 );
  //#
  //#	    // expand to the right till "<"
  //#	    while( rng.moveEnd( "character", 1 ) &&
  //#		   rng.text.charAt(rng.text.length-1)!="<" ) {
  //#	    }
  //#	    rng.moveEnd( "character", -1 );
  //#
  //#	    content = rng.text + "[" + parNode + "]";
  //#	
  //#	  } else if( className == "htmltag" ||
  //#		     className == "pfxtag"  ||
  //#		     className == "xsltag"  ||
  //#		     className == "ixsltag" ) {
  //#
  //#	    // look to the right
  //#	    rng.moveEnd( "character", 1);
  //#	    content = rng.text;
  //#
  //#	    if( content.charAt(0)== "=" ) {
  //#	      // before "=" <==> End Of AttrVar
  //#	      while( rng.moveStart( "character", -1 ) &&
  //#		     rng.text.charAt(0)!=" " ) {
  //#	      }
  //#	      rng.moveStart( "character", 1 );
  //#
  //#	      content = rng.text + "[EndOfAttrVar]";
  //#	      //	  rng.select();
  //#	    } else if( content.charAt(0)== ">" ) {
  //#	      // Before Closing Bracket
  //#	      content = "??? [BeforeClosingBracket]";
  //#	    } else if( content.charAt(0)== "<" ) {
  //#	      // Before Opening Bracket
  //#	      content = "??? [BeforeOpeningBracket]";
  //#	    } else {
  //#
  //#	      // look to the left
  //#	      rng.moveStart( "character", -1);
  //#	      rng.moveEnd( "character", -1);
  //#	      content = rng.text;
  //#	  
  //#	      if( content.charAt(0)== ">" ) {
  //#		// After Closing Bracket
  //#		content = "??? [AfterClosingBracket]";
  //#	      } else {
  //#
  //#		// look again to the right
  //#		rng.moveStart( "character", 1);
  //#		rng.moveEnd( "character", 1);
  //#		//	    content = rng.text;
  //#	  
  //#		rng.expand("word");
  //#
  //#		if( rng.text.charAt(0) == "=" ) {
  //#		  // before AttrVal
  //#		  content = "??? [BeforeAttrVal]";
  //#		} else {
  //#		  rng.moveEnd( "character", 1 );
  //#		  content = rng.text + "[InsideAttrVar]";
  //#		}
  //#	      }
  //#	    }
  //#	  } else if( 1 ) {
  //#	    content = rng.parentElement().innerHTML + "[???]";
  //#	  }
  //#
  //#	  editor._showMsg.value =
  //#	    "par.nodeName:" + rng.parentElement().nodeName + ", " +
  //#	    "(" + className + "), " + 
  //#	    "content:" + content;
  //#	} else {
  //#
  //#	  //-----
  //#	  // Moz
  //#	  //-----
  //#
  //#	  editor._showMsg.value = rng.startContainer.nodeValue;
  //#
  //#	  var parEl = rng.commonAncestorContainer.parentNode;
  //#	  editor._showMsg.value += " (" + parEl.className + ")";
  //#	}
  //#      }
  //#
  //#      //    editor.updateToolbar();
  //#      editor._timerToolbar = null;
  //#    }, 50);

  editor._timestamp = (new Date()).getTime();
};

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype.indentCurrentRange = function( content, optiTab ) {

  //  alert(wfxEditor.str2chr(content));

  var newline;
  var pos, posprev, count;
  var posEnd, posEndprev;

  //  alert( "indentCurrentRange():\ncontent:\n" + wfxEditor.str2chr(content));

  if( wfx.is_ie ) {
    newline = '\r\n';
  } else {
    newline = '\n';
  }
  if( optiTab ) {
    newline = '<br />';
  }
  //---------------------------------------------------------------------------

  //----------------
  // start of range
  //----------------

  if( (pos = content.indexOf("[[[sfxStart]]]")) == -1 ) {
    // error
    return content;
  }

  if( (pos = content.lastIndexOf(newline, pos)) == -1 ) {
    // no previous newline <==> in first line
    pos = 0; 
  }

  if( pos>0 ) {
    pos += newline.length;
  }
  //---------------------------------------------------------------------------

  //--------------
  // end of range
  //--------------

  if( (posEnd = content.indexOf("[[[sfxEnd]]]", pos)) == -1 ) {
    posEnd = pos;
  }

  if( (posEnd = content.indexOf(newline, posEnd)) == -1 ) {
    // no following newline <==> in last line
    posEnd = content.length; 
  } else {
    //    posEnd += newline.length;
  }
  //---------------------------------------------------------------------------

  var redux;
  if( optiTab ) {
    redux = this.col2tag(content).substring(0, posEnd);
  } else {
    redux = content.substring(0, posEnd);
  }

  //  alert( "redux [" + redux.length + "]:\n" + wfxEditor.str2chr(redux) );
  redux = this.removeChangedRangeMarker( redux );
  //  alert( "pos:" + pos);
  //  alert( "redux [" + redux.length + "]:\n" + wfxEditor.str2chr(redux) );

  bench( null, 9 );

  // single-/multiline comments: replace comments by inclosed newlines
  // and pad with spaces
  var rule_comment = /(<!--[^\0]*?-->)/g;
  var comments = redux.match( rule_comment );
  //  alert("# comment: " + comments.length );
  if( comments instanceof Array ) {
    var comment2newlines, padding, str;
    for( var i=0; i<comments.length; i++ ) {
      comment2newlines = comments[i].replace( /[^\r\n]/g, "");
      padding          = comments[i].length - comment2newlines.length - 3;
//      alert("comment [" + comments[i].length + "]:" + comments[i] +
//	    "comment2newlines [" + comment2newlines.length + "]:" + comment2newlines +
//	    "padding [" + padding + "]");
      str = "<";
      for( var j=0; j<padding; j++) {
	str += "!";
      }
      str += "/>";
      redux = redux.replace( new RegExp(comments[i].replace(/(\W)/g,'\\$1') + "([^\r\n]*)"), str+"$1"+comment2newlines);
    }
  }

  bench( "redux1", null, 9 );
  bench( null, 9 );

  var rule_tag = /(<[^\0]*?>)/g;
  var tags = redux.match( rule_tag );
  //  alert("# tags: " + tags.length );
  if( tags instanceof Array ) {
    var tag2newlines, padding, padChar, str, strStart, strEnd, strBlock;
    for( var i=0; i<tags.length; i++ ) {
      tag2newlines = tags[i].replace( /[^\n\r]/g, "");
      padding      = tags[i].length - tag2newlines.length - 2;
      strStart = "<";
      str      = "";
      strEnd   = ">";
      if( tags[i].search( /^<\// ) != -1 ) {
	strStart += "/";
	padding--;
      }
      if( tags[i].search( /\/>$/ ) != -1 ) {
	strEnd = "/" + strEnd;
	padding--;
      }
      for( var j=0; j<padding; j++) {
	str += ":";
      }
      if( tag2newlines.length ) {
	redux = redux.replace( new RegExp(tags[i].replace(/(\W)/g,'\\$1') + "([^\n]*)"), strStart + str + strEnd + "$1" + tag2newlines);
      }
    }
  }

//  alert( "pos:" + pos);
//  alert( "redux [" + redux.length + "]:\n" + wfxEditor.str2chr(redux) );

  bench( "redux2", null, 9 );
  bench( null, 9 );
  //  alert("redux (for first line):\n" + wfxEditor.str2chr(redux));

  wfxEditor.reduxes = redux.split( "\n" );
  if( wfx.is_ie ) {
    for( var i=0; i<wfxEditor.reduxes.length; i++ ) {
      wfxEditor.reduxes[i] = wfxEditor.reduxes[i].replace( /\r/g, "");
    }
  }

  //  alert( wfxEditor.str2chr( redux.substring( 0, pos ) + "***" + 
  //	 redux.substr( pos )) );

  wfxEditor.redux = redux.substring( 0, pos );
  //---------------------------------------------------------------------------

  if( pos>newline.length+1 && (posprev = content.lastIndexOf(newline, pos-newline.length-1)) != -1 ) {    posprev += newline.length;
  } else {
    // no previous newline
    posprev = 0;   // set to start of content
  }
  var firstprevline = content.substring( posprev, pos-newline.length );
  //  alert("posprev:"+ posprev + "\npos:" + pos);
  //---------------------------------------------------------------------------

  var contentLeading, contentTrailing;
  contentLeading  = content.substring( 0, pos );
  contentTrailing = content.substr(    posEnd );
  content         = content.substring( pos, posEnd );
  //---------------------------------------------------------------------------

  var offsetStart = content.indexOf("[[[sfxStart]]]");
  var offsetEnd   = content.indexOf("[[[sfxEnd]]]");

  content = this.removeChangedRangeMarker( content );

//  alert( wfxEditor.str2chr(contentLeading) + "\n**********************************************************************\n" +
//      	 wfxEditor.str2chr(content) + "\n**********************************************************************\n" +
//      	 wfxEditor.str2chr(contentTrailing) + "\n**********************************************************************\n" );

  wfxEditor.lines = content.split( "\n" );
  if( wfx.is_ie ) {
    for( var i=0; i<wfxEditor.lines.length; i++ ) {
      wfxEditor.lines[i] = wfxEditor.lines[i].replace( /\r/g, "");
    }
  }
  var linesLeading = wfxEditor.reduxes.length-wfxEditor.lines.length;

  var indentStart = this.getIndent(wfxEditor.lines[0]);
  var indentEnd   = this.getIndent(wfxEditor.lines[wfxEditor.lines.length-1]);

  wfxEditor.lines.unshift( firstprevline );

  if( offsetEnd != -1 ) {
    offsetEnd = wfxEditor.lines[wfxEditor.lines.length-1].length - (content.length - offsetEnd + 14);
  }

  wfxEditor.lastPrevlines = 0;
  wfxEditor.indentdiff = 0;

  bench( "pre-loop", null, 9 );
  bench( null, 9 );

  var i=0;
  do {
    content = this.indentCurrentLineInternal( i+1, linesLeading, optiTab );

    i++;
  } while( i<wfxEditor.lines.length-1 );

  bench( "loop", null, 9 );
  bench( null, 9 );

  wfxEditor.lines.shift();

  var indentnewStart = this.getIndent(wfxEditor.lines[0]);
  var indentnewEnd   = this.getIndent(wfxEditor.lines[wfxEditor.lines.length-1]);

  if( offsetStart <= indentStart ) {
    offsetStart = indentnewStart;
  } else {
    offsetStart += indentnewStart - indentStart;
  }

  if( offsetEnd != -1 ) {
    if( offsetEnd <= indentEnd ) {
      offsetEnd = indentnewEnd;
    } else {
      offsetEnd += indentnewEnd - indentEnd;
    }
  }

  if( offsetEnd != -1 ) {
    wfxEditor.lines[wfxEditor.lines.length-1] = wfxEditor.lines[wfxEditor.lines.length-1].substring( 0, offsetEnd) + "[[[sfxEnd]]]" + wfxEditor.lines[wfxEditor.lines.length-1].substr( offsetEnd );
  }

  wfxEditor.lines[0] = wfxEditor.lines[0].substring( 0, offsetStart) + "[[[sfxStart]]]" + wfxEditor.lines[0].substr( offsetStart );

  bench( "post-loop", null, 9 );
  bench( null, 9 );

  return contentLeading + wfxEditor.lines.join(newline) + contentTrailing;
};

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype.indentCurrentLineInternal = function( currentLine, linesLeading, optiTab ) {

  //  bench( null, 7 );

  var dbg = 0;
  //---------------------------------------------------------------------------

  //  alert("wfxEditor.redux:\n\n" + wfxEditor.str2chr(wfxEditor.redux)); 

  if( currentLine==1 && linesLeading==0 ) {
    // very first line

    return;
  }

  var insideComment = false;
  var posComment = wfxEditor.redux.lastIndexOf("<!--");
  if( posComment != -1 && wfxEditor.redux.indexOf("-->", posComment) == -1 ) {
    insideComment = true
      //    alert("inside comment");
  }

  var prevline = wfxEditor.lines[currentLine-1];
  var line     = wfxEditor.lines[currentLine];
  dbg && alert( "line:\n" + wfxEditor.str2chr(line));

  var prevlines = 0;
  if( (currentLine == 1 && line.search( /^[^<]*>.*$/ ) == -1) ||  
      (prevline.search( /^[^<]*>.*$/ ) != -1) || 
      (currentLine >  1 && prevline.search( /-->/ ) != -1) ) {
    // 1. first line to indent
    // 2. previous line starts with closing part of tag
    // 3. previous line contains closing part of comment

    var rx_trailing = /^[^\0]*?(\s*)$/;
    rx_trailing.test(wfxEditor.redux);
    prevlines = RegExp.$1.match(/\r?\n/g);
    prevlines = (prevlines instanceof Array ? prevlines.length : 0);

    dbg && alert("prevlines:" + prevlines);

    prevline = "";

    //    for( var j=0; j<prevlines; j++ ) {
    //      prevline += wfxEditor.reduxes[linesLeading+currentLine-1-prevlines+j] + this.newline;
    //    }
    prevline = wfxEditor.reduxes[linesLeading+currentLine-1-prevlines];
    //    alert( "linesLeading:" + linesLeading + "\ncurrentLine:" + currentLine + "\nprevlines:" + prevlines);
    //    alert("prevline (1) line " + (linesLeading+currentLine-1-prevlines) + ":\n" + prevline);
    //    alert("prevline (1) line " + (linesLeading+currentLine-1-prevlines) + ":\n" + wfxEditor.str2chr(prevline));
    //    alert(wfxEditor.str2chr(wfxEditor.reduxes.join("\n")));
  } else {
    prevlines = 0;
    prevline = wfxEditor.lines[currentLine-1];
    //    alert("prevline (2):\n" + prevline);
    //    alert("prevline (2):\n" + wfxEditor.str2chr(prevline));
  }
  //---------------------------------------------------------------------------

  // determine indentation of previous line

  var indentprev =  -1;
  while( prevline.charAt(++indentprev) == " " ) {
  }
  //  alert("spaces(prev):" + indentprev);
  //-------------------------------------------------------------------------

  var rule_htmlTag        = /<[\w\:\-]+[^\0]*?>/g;
  var rule_htmlEndTag     = /<\/[\w\:\-]+>/g;
  var rule_htmlSingleTag  = /<[\w\:\-]+[^>]*?\/>/g;
  var rule_comment        = /<!--[^\0]*?-->/g;

  var prevline = prevline.replace( rule_htmlSingleTag, "X");
  var prevline = prevline.replace( rule_comment, "X");

  dbg && alert("prevline (2):\n" + wfxEditor.str2chr(prevline));

  var count_tags    = prevline.match(rule_htmlTag);
  count_tags = (count_tags instanceof Array ? count_tags.length : 0);
  var count_endtags = prevline.match(rule_htmlEndTag);
  count_endtags = (count_endtags instanceof Array ? count_endtags.length : 0);
  var indentdiff = count_tags - count_endtags;
  //alert("count_tags:" + count_tags + "\ncount_endtags:" + count_endtags);
  //-------------------------------------------------------------------------

  // determine indentation of current line
	
  var postmp = 0;
  var indent =  0;
  while( line.charAt(postmp++) == " " ) {
    indent++;
  }
  //	alert("spaces(current):" + indent);
  //-------------------------------------------------------------------------

  var indentnew;

  if( insideComment ) {

    indentnew = indent + wfxEditor.indentdiff;

  } else {

    if( prevline.search( /^ *<\// ) != -1 ) {
      // previous line starts with endtag
      indentdiff++;
    }
    if( line.search( /^ *<\// ) != -1 ) {
      // current line starts with endtag
      indentdiff--;
    }
    
    if( prevline.search( /^(.*)(<\/?[\w\:\-\!]* *)[^>]*?$/ ) != -1 ) {
      // previous line ends with open tag
      
      indentnew = RegExp.$1.length + RegExp.$2.length;
    } else {
      
      dbg && alert("indentprev:" + indentprev + "\nindentdiff:" + indentdiff);
      indentnew = indentprev + indentdiff * wfxEditor.tabWidth;
      
      if(indentnew < 0) {
	indentnew = 0;
      }
    }

    wfxEditor.indentdiff = indentnew - indent;
  }
  //-------------------------------------------------------------------------
  
  dbg && alert("indent:" + indent + "\nindentnew:" + indentnew);

  var indentstr = "";
  for( var i=0; i<indentnew; i++) {
    indentstr += " ";
  }
  //-------------------------------------------------------------------------

  wfxEditor.reduxes[linesLeading+currentLine-1] = indentstr + wfxEditor.reduxes[linesLeading+currentLine-1].substr( indent);

  wfxEditor.lines[currentLine] = indentstr + wfxEditor.lines[currentLine].substr( indent);

  if( prevlines == 1 && (wfxEditor.lastPrevlines>1 || wfxEditor.lastPrevlines==0) ) {
    // reduce redux
    wfxEditor.redux = wfxEditor.lines[currentLine] + this.newline;
  } else {
    wfxEditor.redux += wfxEditor.reduxes[linesLeading+currentLine-1] + this.newline;
  }

  wfxEditor.lastPrevlines = prevlines;

//  bench( "indentCurrentLineInternal", null, 7 );
};

// focuses the iframe window.  returns a reference to the editor document.
wfxEditor.prototype.focusEditor = function() {
  setTimeout( function() {
    try { 
      this._editor.focus() 
	} catch(e) {}
  }, 50);
}

// retrieve the HTML
wfxEditor.prototype.getHTML = function(dbg) {

  bench( null, 3 );

  var html;
  if(wfx.is_ie) {

    //    html = wfxEditor.getHTML(this._doc.body, false, dbg);

    html = this._doc.body.innerHTML;
    bench( "getHTML (intrinsic)", null, 3 );

    html = html.replace( /\"/g, '&quot;');

    html = html.replace( /<SPAN (class|id)=(.+?)>/g, '<span $1="$2">');
    html = html.replace( /<\/SPAN>/g, '</span>' );
    html = html.replace( /<BR>/g, '<br />' );
    html = html.replace( /<(\/?)PRE>/g, '<$1pre>' );
    html = html.replace( /&nbsp;<\/pre>/g, '</pre>' );

    //    alert( html + "\n**********************************************************************\n" + html3 + "\n**********************************************************************\n" + html2);

    // remove bogus spans (introduced by Return at AfterClosingBracket)
    var rule_emptyspans = /<span class="[^\"]+?"><\/span>/g;    
    var i=0;
    while( html.match(rule_emptyspans) ) {
      i++;
      html = html.replace( rule_emptyspans, "" );
    }
    if( i>0 ) {
      //      alert("emptyspans:" + i );
    }

    html = html.replace( /<\/pre><pre>/g , "<br />" );
    
    // after paste from other applications (Word, ...)
    html = html.replace( /<P\b[^>]*>/g, "<p>");
    html = html.replace( /<\/?[A-Z][^>]*>/g, "");
    html = html.replace( /<\?[^>]*>/, "");
    html = html.replace( /<\/?o:p>/g, "");
    html = html.replace( /<\/span><p>/g, "<br />");

    //    alert( wfxEditor.str2chr(html));

    // after paste from other applications (WordPad, ...)
    html = html.replace( /<P>&nbsp;<\/P>((<P>&nbsp;<\/P>){0,})/g, function($0,$1) { if($1 == "") return "<br /><br />"; var res=""; for( var i=0; i<$1.match(/n/g).length+2; i++) res+="<br />"; return res; } );

    html = html.replace( /<P \/>/g, "<br />" );
    html = html.replace( /<\/P>((<br \/>)*)<P>/g, "<br />$1" );
    //    html = html.replace( /<P>/g, "" );
    //    html = html.replace( /<\/P>/g, "" );

    //    alert("getHTML() postprocessed:\n" + wfxEditor.str2chr(html));

    html = html.replace( /\r\n/g , "<br />" );

  } else {
    html = this._doc.body.innerHTML;

    // Windows (XXX: only as first char??)
    html = html.replace( /\r/g, "");

    html = html.replace( /^\n?<pre>\n?/g, "<pre>");
    html = html.replace( /\n?<\/pre>\n$/g, "</pre>");

    html = html.replace( /<br[^\>]*>/g, '<br />' );

    // Ctrl-A selects entire content including <pre>
    // ==> bring leading and trailing range markers inside <pre>
    html = html.replace( /^(.+?)\n?<pre>/, "<pre>$1");
    html = html.replace( /<\/pre>\n?(.+?)$/, "$1</pre>");

    // after Ctrl-A + Backspace to empty entire content, a Ctrl-V to paste new
    // content could be inserted after empty pre tags including wrong newlines
    html = html.replace( /^<pre><\/pre>([^\0]*)$/, "<pre>$1</pre>");
    html = html.replace( /\n/g, " ");
  }

  // untabify
  html = html.replace( /\x09/g, "  ");

  // untabify (IE); paste weirdness (Moz)
  html = html.replace( /&nbsp;/g, "  ");
  
  //  html = html.replace( /\u200E/g, '' );   // &#8206; == &lrm;

  bench( "getHTML (total)", null, 3 );
  //  alert( "getHTML():\n" + wfxEditor.str2chr(html));

  html = html.replace( /<\/pre>$/g, "");
  html = html.replace( /^<pre>/g, "");

  return html;
};

/***************************************************
 *  Category: UTILITY FUNCTIONS
 ***************************************************/

// selection & ranges

// returns the current selection object
wfxEditor.prototype._getSelection = function() {
  if (wfx.is_ie) {
    return this._doc.selection;
  } else {
    return this._editor.getSelection();
  }
};

// returns a range for the current selection
wfxEditor.prototype._createRange = function(sel) {
  if (wfx.is_ie) {
    return sel.createRange();
  } else {
    this.focusEditor();
    if (typeof sel != "undefined") {
      return sel.getRangeAt(0);
    } else {
      return this._doc.createRange();
    }
  }
};

// event handling

wfxEditor._addEvent = function(el, evname, func) {
  if (wfx.is_ie) {
    el.attachEvent("on" + evname, func);
  } else {
    el.addEventListener(evname, func, true);
  }
};

wfxEditor._addEvents = function(el, evs, func) {
  for (var i in evs) {
    wfxEditor._addEvent(el, evs[i], func);
  }
};

wfxEditor._removeEvent = function(el, evname, func) {
  if (wfx.is_ie) {
    el.detachEvent("on" + evname, func);
  } else {
    el.removeEventListener(evname, func, true);
  }
};

wfxEditor._removeEvents = function(el, evs, func) {
  for (var i in evs) {
    wfxEditor._removeEvent(el, evs[i], func);
  }
};

wfxEditor._stopEvent = function(ev) {
  if (wfx.is_ie) {
    ev.cancelBubble = true;
    ev.returnValue = false;
  } else {
    ev.preventDefault();
    ev.stopPropagation();
  }
};

wfxEditor.needsClosingTag = function(el) {
  //	var closingTags = " script style div span tr td tbody table em strong font a ";
  var closingTags = " span ";
  return (closingTags.indexOf(" " + el.tagName.toLowerCase() + " ") != -1);
};

// performs HTML encoding of some given string
wfxEditor.htmlEncode = function(str) {
  // we don't need regexp for that, but.. so be it for now.
  str = str.replace( /&/ig,    "&amp;");
  str = str.replace( /</ig,    "&lt;");
  str = str.replace( />/ig,    "&gt;");
  str = str.replace( /\x22/ig, "&quot;");
  // \x22 means '"' -- we use hex representation so that we don't disturb
  // JS compressors (well, at least mine fails.. ;)

  str = str.replace(/\xA9/ig, "&copy;");

  return str;
};

wfxEditor.str2chr = function( str ) {

  //*****************************************************************************
  //
  //*****************************************************************************
  var res = "";
  
  if( str == null ) {
    res = "[null]";
  } else {
    for( var i=0; i<str.length; i++) {
      res += str.charAt(i)+"("+str.charCodeAt(i)+")";
    }
  }

  return str + "\n---------------------------------------\n" + res;
  //  return "\n---------------------------------------\n" + res;
}

//*****************************************************************************
//
//*****************************************************************************
wfxEditor.prototype.restoreRange = function( offsetStart, offsetEnd, dbg ) {

  wfxEditor.currentOffset = 0;
  wfxEditor.reachedOffset = false;


//  var tree = document.createTreeWalker( this._bodynode.firstChild, NodeFilter.SHOW_TEXT, null, false);
//  
//  //  alert("...TreeWalker...");
//
//  var node;
//  var i=0;
//  while( !wfxEditor.reachedOffset && (node = tree.nextNode()) ) {
//    //    alert("node: " + node.nodeValue);
//    i++;
////    wfxEditor.currentOffset += node.nodeValue.length;
////    //    alert("currentOffset:" + wfxEditor.currentOffset);
////  
////    if( offsetStart != null && 
////	wfxEditor.offsetStart <0 && 
////	wfxEditor.currentOffset >= offsetStart ) {
////    
////      if( offsetEnd < 0 ) {
////	wfxEditor.reachedOffset = true;
////      }
////    
////      wfxEditor.nodeStart = node;
////    
////      var localOffset = offsetStart - wfxEditor.currentOffset + node.nodeValue.length;
////      //	alert("localOffset(start):" + localOffset);
////      wfxEditor.offsetStart = localOffset;
////    }
////  
////    if( offsetEnd >= 0 && 
////	wfxEditor.currentOffset >= offsetEnd ) {
////    
////      wfxEditor.reachedOffset = true;
////    
////      wfxEditor.nodeEnd = node;
////    
////      var localOffset = offsetEnd - wfxEditor.currentOffset + node.nodeValue.length;
////      wfxEditor.offsetEnd = localOffset;
////    }
//  }
//  
//  alert("i:" + i);

  wfxEditor.currentOffset = 0;
  wfxEditor.reachedOffset = false;

  //alert("this.restoreRange( ~, ~, " + offsetStart +", " + offsetEnd + " )");
  wfxEditor.restoreRange(this._doc.body, false, offsetStart, offsetEnd, dbg);

  if( wfxEditor.nodeStart == null ) {
    wfxEditor.nodeStart = this._doc.body.firstChild;
    wfxEditor.offsetStart = 0;
  }

  //  alert("...TreeWalker");

};

//*****************************************************************************
//
//*****************************************************************************
wfxEditor.prototype.restoreRangeStart = function( offsetNewlinesStart, offsetStart, dbg ) {

  wfxEditor.currentOffset = 0;
  wfxEditor.reachedOffset = false;

  //alert("this.restoreRange( ~, ~, " + offsetStart +", " + offsetEnd + " )");
  wfxEditor.restoreRangeStart(this._doc.body, false, offsetNewlinesStart, offsetStart, dbg);

  if( wfxEditor.nodeStart == null ) {
    wfxEditor.nodeStart = this._doc.body.firstChild;
    wfxEditor.offsetStart = 0;
  }
};

//*****************************************************************************
//
//*****************************************************************************
wfxEditor.prototype.restoreRangeByNewlines = function( offsetStart, offsetEnd, dbg ) {

  wfxEditor.currentOffset = 0;

  wfxEditor.restoreRangeByNewlines(this._doc.body, false, offsetStart, offsetEnd, dbg);
};

//*****************************************************************************
//
//*****************************************************************************
wfxEditor.restoreRange = function(root, outputRoot, offsetStart, offsetEnd, thisdbg) {
  
  //  alert("wfxEditor.restoreRange( ~, ~, " + offsetStart +", " + offsetEnd + " )");
  //  alert(root.nodeName);
  thisdbg = 0;

  switch (root.nodeType) {
  case 1: // Node.ELEMENT_NODE
    thisdbg && alert("case 1: Node.ELEMENT_NODE");
  case 11: // Node.DOCUMENT_FRAGMENT_NODE
    thisdbg && alert("case 11: Node.DOCUMENT_FRAGMENT_NODE");			
			var closed;
			var i;
			if (outputRoot) {
			  closed = (!(root.hasChildNodes() || wfxEditor.needsClosingTag(root)));
			}

			for (i = root.firstChild; i; i = i.nextSibling) {
			  wfxEditor.restoreRange(i, true, offsetStart, offsetEnd, thisdbg);
			  if( wfxEditor.reachedOffset ) {
			    return;
			  }
			}
			break;
  case 3: // Node.TEXT_NODE
    thisdbg && alert("case 3: Node.TEXT_NODE:" + root.data + "<<");

			wfxEditor.currentOffset += root.data.length;
			//    alert("currentOffset:" + wfxEditor.currentOffset);

			if( offsetStart != null && 
			    wfxEditor.offsetStart <0 && 
			    wfxEditor.currentOffset >= offsetStart ) {

			  if( offsetEnd < 0 ) {
			    wfxEditor.reachedOffset = true;
			  }
      
			  wfxEditor.nodeStart = root;

			  var localOffset = offsetStart - wfxEditor.currentOffset + root.data.length;
			  //	alert("localOffset(start):" + localOffset);
			  wfxEditor.offsetStart = localOffset;
			}

			if( offsetEnd >= 0 && 
			    wfxEditor.currentOffset >= offsetEnd ) {

			  wfxEditor.reachedOffset = true;
      
			  wfxEditor.nodeEnd = root;

			  var localOffset = offsetEnd - wfxEditor.currentOffset + root.data.length;
			  wfxEditor.offsetEnd = localOffset;
			}

			break;
  }

  return;
};

//*****************************************************************************
//
//*****************************************************************************
wfxEditor.restoreRangeByNewlines = function(root, outputRoot, offsetStart, offsetEnd, thisdbg) {
  
  thisdbg = 0;

  switch (root.nodeType) {
  case 1: // Node.ELEMENT_NODE
    thisdbg && alert("case 1: Node.ELEMENT_NODE");
  case 11: // Node.DOCUMENT_FRAGMENT_NODE
    thisdbg && alert("case 11: Node.DOCUMENT_FRAGMENT_NODE");
			
			if( root.tagName.toLowerCase() == "br" ) {
			  wfxEditor.currentOffset++;

			  if( wfxEditor.currentOffset == offsetStart ) {
			    wfxEditor.nodeStart = root;
			    //	alert("br! (start)");
			  }
			  if( wfxEditor.currentOffset == offsetEnd ) {
			    wfxEditor.nodeEnd = root;
			    //	alert("br! (end)");
			  }
			}

			var closed;
			var i;
			if (outputRoot) {
			  closed = (!(root.hasChildNodes() || wfxEditor.needsClosingTag(root)));
			}

			for (i = root.firstChild; i; i = i.nextSibling) {
			  wfxEditor.restoreRangeByNewlines(i, true, offsetStart, offsetEnd, thisdbg);
			}
			break;
  }

  return;
};

//*****************************************************************************
// 
//*****************************************************************************
wfxEditor.prototype.setRange = function( nodeStart, offsetStart, 
					 nodeEnd,   offsetEnd ) {

  //  alert("setRange( nodeStart.nodeType:" + nodeStart.nodeType +", offsetStart:"+ offsetStart +", "+ nodeEnd +", "+   offsetEnd + ")");

  //    this._dbg.value += "setRange( " + nodeStart +"(" + nodeStart.nodeValue + "), "+ offsetStart +", "+ nodeEnd +", "+   offsetEnd + ")\n";

  try {

    //  Moz only
    if( !wfx.is_ie ) {
	
      var rng = this._doc.createRange();

      if( nodeStart.nodeType == 1 ) {
	// element node (br)

	//	  if( nodeStart.nextSibling != null ) {
	//	    //	    rng.setStartBefore( nodeStart.nextSibling );
	//	    //	    rng.setEndAfter(   nodeStart.nextSibling );
	//	    rng.selectNode(nodeStart.nextSibling);
	//	  } else {
	rng.setStartAfter( nodeStart );
	rng.setEndAfter(   nodeStart );
	//	  }

      } else if( nodeStart.nodeType == 3 ) {
	// text node

	if(offsetStart > 0 ) {
	  rng.setStart( nodeStart, offsetStart);
	  rng.setEnd(   nodeStart, offsetStart);
	} else if(offsetStart == 0 ) {
	  rng.setStartBefore( nodeStart );
	  rng.setEndBefore(   nodeStart);
	}
      }

      //	try {
      //	  nodeEnd.nodeType;
      //	} catch(e) {
      //	  nodeEnd = null;
      //	}

      //	alert( typeof nodeEnd );

      if( nodeEnd != null ) {
	//	  alert(nodeEnd instanceof HTMLBRElement);

	if( nodeEnd.nodeType == 1 ) {
	  // element node (br)
	    
	  rng.setEndAfter( nodeEnd );
	} else  if( nodeEnd.nodeType == 3 ) {
	  // text node
	    
	  if( offsetEnd >= 0 ) {
	    rng.setEnd( nodeEnd, offsetEnd);
	  }
	}
      }

      var sel = this._editor.getSelection();

	
      try {
	// Moz bug: Ctrl-K ('kill-line') at the end of a line terminates 
	// the entire browser without a catchable exception
	// This is caused by a fatal call of sel.removeAllRanges(); 
	// ==> as an alternative: remove ranges one by one
	var oneRng;
	for( var i=0; i<sel.rangeCount; i++ ) {
	  oneRng = sel.getRangeAt(i);
	  sel.removeRange(oneRng);
	}
      } catch(e) {
	alert("Exception:\n" + e);
      }

      sel.addRange(rng);
    }
  } catch(e) {
    alert("Exception:\n" + e);
  }
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.submitContent = function( textarea, submit) {

  var newline;
  if( wfx.is_ie ) {
    newline = "\r\n";
  } else {
    newline = "\n";
  }

  var content = this.col2tag(this.getHTML());

  // make sure there are no markers left (==bug)
  content = this.removeChangedRangeMarker( content );

  //--------------------
  // XML wellformedness
  //--------------------

  var err = new Array();
  if( !this.isWellFormedXML( content, err) ) {
    
    if( err instanceof Array ) {
      alert("Sorry, this is not well-formed XML.\n\n" +
	    "errReason:" + err["Reason"] + "\n" + 
	    "errLine:"   + err["Line"]   + "(???)\n" +
	    "errSource:" + err["Source"] + "\n");
    }
    return;
  }
  //---------------------------------------------------------------------------

  var el;
  el = document.getElementById(textarea);
  if( el ) {
    el.value = content;
  }
  el = document.getElementById(submit);
  if( el ) {
    el.click();
  }
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.generate = function( target, content ) {

  var editor = this;	// we'll need "this" in some nested functions

  if( typeof target == "string" ) {
    // it's not HTMLElement but string "id"
    target = document.getElementById(target);
  }

  if( content && typeof content != "string" ) {
    // it's not string "content" but HTMLElement

    content = content.value;
  }
  
  // untabify
  content = content.replace( /\x09/g, "  ");

  content = this.tag2src( content );

  content = this.src2col( content );

  // replace &quot; inside real text with "
  content = content.replace( /<span class="entity">&amp;quot;<\/span>/g, '"' );

  var body = '<body id="bodynode">\n';
  // Moz: \n after body tag is required to prevent exception in rng.setStart

  content = this.prepareContent( content );

  this.currentUndo++; 
  this.undo[this.currentUndo] = [ content, [0, false, -1, false, -1]];

  content = '<pre>' + content + '</pre>';    
  
  //  content = doctype + html_header1 + style_source + html_header2 + content + html_footer;
  content = this.config.doctype + this.config.html + '<head>' + this.config.head + this.config.style_source + '</head>' + body + content + '</body></html>';

  this._editor = target.contentWindow;;
  wfxEditor._editor = this._editor;
  //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  
  this._doc      = this._editor.document;
  wfxEditor._doc = this._doc;
  this.parentURL = this._editor.parent.location.href;
  //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  var doc = this._doc;

  //  alert(content);

  this._doc.open();
  this._doc.writeln( content || '<html>\n<body>\nblank\n</body>\n<html>\n' );
  this._doc.close();

  function enableDesignMode() {
    try {
      if( wfx.is_ie ) {
        doc.body.contentEditable = true;
      } else {
	doc.designMode = "on";
        doc.execCommand("useCSS", false, true);
      }
      this._designMode = true;
    } catch(e) {
    }
  }
    if( !this._designMode ) {
      setTimeout( enableDesignMode, 1000 );
    }
  //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  
  wfxEditor._addEvents
    ( this._editor.document, 
      ["keydown", "keypress", "mousedown", "mouseup", "drag"],
      function (event) {
	return editor._editorEvent(wfx.is_ie ? 
				   editor._editor.event : event);
      } );

  wfxEditor._addEvents
    ( this._editor,
      ["scroll"],
      function (event) {
	return editor._editorEvent(wfx.is_ie ? 
				   editor._editor.event : event);
      } );
  
  //    editor.updateToolbar();

  this._bodynode      = doc.getElementById("bodynode");
  wfxEditor._bodynode = this._bodynode;

  this._linebar   = document.getElementById("wfxline01").contentWindow;
  this._linebarheight = Math.ceil( parseInt(document.getElementById("wfxline01").style.height) / this._linepx )+1;

  this.initLineNumbers();

  this.showStatus && ( this.syntaxrehighlighting.style.backgroundColor = "#00ff00");

  //  bench( "...generate()" );
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.startIntervalRehighlighting = function() {

  var editor = this;

  var dbg = 0;
  var dbgcancel = 0;
  
  var needHighlighting;

  this._timerRehighlight = setInterval( function() {

    //editor._dbg.value += ". ";
  
    if( editor.cancelRehighlighting ) {
      // editor._dbg.value += "cc ";
      return;
    }

    if( editor.insideRehighlighting ) {
      dbg && ( editor._dbg.value += "ci ");
      return;
    }

    if( editor.needRehighlighting == 0 ) {
      dbg && ( editor._dbg.value += "N ");
      return;
    }

    //    if( editor.skipRehighlighting ) {
    //      editor._dbg.value += "S ";
    //      return;
    //    }

    if( ((new Date()).getTime() - editor._timestamp) < wfxEditor.timeEvent ) {
      // editor._dbg.value += "K ";
      editor.showStatus && ( editor.syntaxrehighlighting.style.backgroundColor = "#ffff00");
      return;
    }

    editor.showStatus && ( editor.syntaxrehighlighting.style.backgroundColor = "#ff0000");

    editor.insideRehighlighting = true;

    needRehighlighting = editor.needRehighlighting;

    var content;

    var offsetStart, offsetNewlinesStart, offsetEnd, offsetNewlinesEnd, posEnd;

    bench( null, 1);
    //---------------------------------------------------------------------------

    setTimeout( function() {   // c0
      dbg && ( editor._dbg.value += "a0 ");

      //----------------
      // setRangeMarker
      //----------------
	
      bench( null, 2);
      editor.setRangeMarker();
      bench( "setRangeMarker", null, 2);

      sleepMSec( 1000 );
      //----------------------------------------------------------------------

      setTimeout( function() {   // c1
	dbg && ( editor._dbg.value += "a1 ");

	if( editor.cancelRehighlighting ) {
	  dbgcancel && ( editor._dbg.value += "c1 ");
	  editor.insideRehighlighting = false;
	  editor.cancelRehighlighting = false;
	  return;
	}

	//-------------
	// get content
	//-------------

	bench( null, 11);
	content = editor.getHTML();
	bench( "getHTML()", null, 11);

	sleepMSec( 1000 );
	//-------------------------------------------------------------------

	setTimeout( function() {   // c1
	  dbg && ( editor._dbg.value += "a1b ");
	  
	  if( editor.cancelRehighlighting ) {
	    dbgcancel && ( editor._dbg.value += "c1b ");
	    editor.insideRehighlighting = false;
	    editor.cancelRehighlighting = false;
	    return;
	  }

	  //---------
	  // col2tag
	  //---------
	  
	  bench( null, 2);
	  content = editor.col2tag(content);
	  bench( "col2tag", null, 2);


	  //    if( content == "[[[sfxStart]]]" ) {
	  //      // empty content
	  //      editor._doc.body.innerHTML = '<pre></pre>';
	  //      editor.insideRehighlighting = false;
	  //      return;
	  //    }

	  sleepMSec( 1000 );
	  //-------------------------------------------------------------------

	  setTimeout( function() {   // c2
	    dbg && ( editor._dbg.value += "a2 ");

	    if( editor.cancelRehighlighting ) {
	      dbgcancel && ( editor._dbg.value += "c2 ");
	      editor.insideRehighlighting = false;
	      editor.cancelRehighlighting = false;
	      return;
	    }
	    
	    //------------
	    // getOffsets
	    //------------

	    var ret = new Array();
	    
	    editor.getOffsets( content, ret );
	    
	    offsetStart         = ret[0];
	    offsetNewlinesStart = ret[1];
	    offsetEnd           = ret[2];
	    offsetNewlinesEnd   = ret[3];
	    posEnd              = ret[4];

	    if( editor.currentUndo == editor.config.maxUndo ) {
	      editor.storeUndo = editor.undo.splice( 1, 1);
	    } else {
	      editor.currentUndo++;
	    }
	    editor.undo[editor.currentUndo] = [ ret ];

	    //	    alert("offsetStart:" + offsetStart + ", offsetNewlinesStart:" + offsetNewlinesStart + "\noffsetEnd:" + offsetEnd + ", offsetNewlinesEnd:" +  offsetNewlinesEnd + "\nposEnd:" + posEnd);
	    //-----------------------------------------------------------------
	    
	    //#    var contentLeading="", contentTrailing="";
	    //#
	    //#    var newline;
	    //#    if( wfx.is_ie ) {
	    //#      newline = '\r\n';
	    //#    } else {
	    //#      newline = '\n';
	    //#    }
	    //#
	    //#    var posLeading;
	    //#    if( (posLeading = content.lastIndexOf(newline, offsetStart)) == -1 ) {
	    //#      posLeading = 0; 
	    //#    } else {
	    //#      posLeading += newline.length;
	    //#    }
	    //#
	    //#    contentLeading = content.substr( 0, posLeading );
	    //#    alert("contentLeading:\n" + wfxEditor.str2chr(contentLeading));
	    //#
	    //#    var posTrailing;
	    //#    if( (posTrailing = content.indexOf(newline, offsetStart)) == -1 ) {
	    //#      posTrailing = 0; 
	    //#    } else {
	    //#      posTrailing += newline.length;
	    //#    }
	    //#
	    //#    contentTrailing = content.substr( posTrailing );
	    //#    alert("contentTrailing:\n" + wfxEditor.str2chr(contentTrailing));
	    //#
	    //#    content = content.substring( posLeading, posTrailing );

	    //      content = editor.src2col(editor.tag2src(editor.removeChangedRangeMarker(content)));

	    sleepMSec( 1000 );
	    //-----------------------------------------------------------------

	    setTimeout( function() {   // c3
	      dbg && ( editor._dbg.value += "a3 ");

	      if( editor.cancelRehighlighting ) {
		dbgcancel && ( editor._dbg.value += "c3 ");
		editor.insideRehighlighting = false;
		editor.cancelRehighlighting = false;
		editor.undoRehighlightingCancelled();
		return;
	      }

	      //--------------------------
	      // removeChangedRangeMarker
	      //--------------------------

	      bench( null, 3);
	      content = editor.removeChangedRangeMarker(content);
	      bench( "removeChangedRangeMarker", null, 3);
	      
	      sleepMSec( 1000 );
	      //----------------------------------------------------------------

	      setTimeout( function() {   // c4
		dbg && ( editor._dbg.value += "a4 ");

		if( editor.cancelRehighlighting ) {
		  dbgcancel && ( editor._dbg.value += "c4 ");
		  editor.insideRehighlighting = false;
		  editor.cancelRehighlighting = false;
		  editor.undoRehighlightingCancelled();
		  return;
		}

		//--------------------------------------------
		// transform content in step 1 of 2 (tag2src)
		//--------------------------------------------

		bench( null, 4);
		content = editor.tag2src(content);
		bench( "tag2src", null, 4);

		//		editor._dbg.value = content.replace( /<br \/>/g, "\n");
		
		sleepMSec( 1000 );
		//-------------------------------------------------------------

		setTimeout( function() {   // c5
		  dbg && ( editor._dbg.value += "a5 ");

		  if( editor.cancelRehighlighting ) {
		    dbgcancel && ( editor._dbg.value += "c5 ");
		    editor.insideRehighlighting = false;
		    editor.cancelRehighlighting = false;
		    editor.undoRehighlightingCancelled();
		    return;
		  }

		  //--------------------------------------------
		  // transform content in step 2 of 2 (src2col)
		  //--------------------------------------------

		  bench( null, 5);
		  content = editor.src2col(content);
		  bench( "src2col", null, 5);

		  sleepMSec( 1000 );
		  //--------------------------------------------------------------------

		  setTimeout( function() {   // c6
		    dbg && ( editor._dbg.value += "a6 ");

		    if( editor.cancelRehighlighting ) {
		      dbgcancel && ( editor._dbg.value += "c6 ");
		      editor.insideRehighlighting = false;
		      editor.cancelRehighlighting = false;
		      editor.undoRehighlightingCancelled();
		      return;
		    }

		    //-------------------------
		    // set content (innerHTML)
		    //-------------------------
  
		    content = editor.prepareContent( content );

		    editor.undo[editor.currentUndo].unshift(content);
		    editor.undo.length = editor.currentUndo+1;

		    try {

		      //      alert("innerHTML=content:\n" + wfxEditor.str2chr(content));
		      bench( null, 6);
		      //#      editor._doc.body.innerHTML = '<pre>' + contentLeading + content + 
		      //#	contentTrailing + '</pre>';
		      editor._doc.body.innerHTML = '<pre>' + content + '</pre>';
		      bench( "innerHTML", null, 6);

		    } catch(e) {
		      alert("Exception:\n" + e);
		    }

		    //-------------------------------------------------------------------

		    //------------
		    // setOffsets
		    //------------

		    bench( null, 2);
		    editor.setOffsets( [ offsetStart, offsetNewlinesStart, 
					 offsetEnd,   offsetNewlinesEnd,
					 posEnd ] );
		    bench( "setOffsets", null, 2);
		    //-------------------------------------------------------------------

		    bench("rehighlight()", null, 1);

		    if( doBench ) {
		      editor._dbg.value += benchMsg;
		      benchMsg = "";
		    }

		    editor.countRangeMarkers = 0;

		    editor.showStatus && ( editor.syntaxrehighlighting.style.backgroundColor = "#00ff00");

		    editor.insideRehighlighting = false;
		    editor.cancelRehighlighting = false;
		    //      editor.skipRehighlighting = true;
		    //

		    if( needRehighlighting == editor.needRehighlighting ) {
		      editor.needRehighlighting = 0;
		    }

		    dbg && ( editor._dbg.value += "z6 ");
		  }, 0);
		  dbg && ( editor._dbg.value += "z5 ");
		}, 0);
		dbg && ( editor._dbg.value += "z4 ");
	      }, 0);
	      dbg && ( editor._dbg.value += "z3 ");
	    }, 0);
	    dbg && ( editor._dbg.value += "z2 ");
	  }, 0);
	  dbg && ( editor._dbg.value += "z1b ");
	}, 0);
	dbg && ( editor._dbg.value += "z1 ");
      }, 0);
      dbg && ( editor._dbg.value += "z0 ");
    }, 0);

  }, wfxEditor.timeInterval );


  //---------------------------------------------------------------------------
  
  //-----------
  // scrolling
  //-----------
  
  setInterval( function() {
    var scrollTop, scrollTopLines;
    if(wfx.is_ie) {
      scrollTop      = editor._doc.documentElement.scrollTop;
      scrollTopLines = editor._linebar.document.documentElement.scrollTop;
    } else {
      scrollTop      = editor._editor.pageYOffset;
      scrollTopLines = editor._linebar.pageYOffset;
    }

    if( scrollTop != editor._scrollTop || scrollTop != scrollTopLines ) {
      // scroll offset has changed

      if( wfx.is_ie ) {

	var lineStart = parseInt( scrollTop / editor._linepx );
	if( lineStart != editor._lineStart ) {
	// start line has changed

	var content = "<pre>";
	for( var i=0; i<editor._linebarheight; i++ ) {
	  content += (i+lineStart+1) + "&nbsp;\n";
	}
	content += "</pre>";
	editor._linebar.document.getElementById("bodynode").innerHTML = content;

	editor._lineStart = lineStart;
	}

	editor._scrollTop = scrollTop;

	scrollTop = scrollTop - editor._lineStart*editor._linepx;
      }

      // update linebar
      if(wfx.is_ie) {
	editor._linebar.document.documentElement.scrollTop = scrollTop;
      } else {
	editor._scrollTop = scrollTop;
	editor._linebar.scroll( 0, scrollTop );
      }
    }
  }, 100);
  //---------------------------------------------------------------------------

};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.undoRehighlightingCancelled = function() {
  
  if( this.currentUndo == this.maxUndo ) {
    this.undo.pop();
    this.undo.splice( 1, 0, this.storeUndo );
  } else {
    this.undo.length = this.currentUndo;
    this.currentUndo--;
  }
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.getOffsets = function( content, ret ) {

  //  alert("getOffsets(): content:\n" + wfxEditor.str2chr(content));

  //-------
  // start
  //-------

  var offsetStart = -1;
  var offsetEnd   = -1;
  // offsetNewlinesStart: true: br elements; false: text node
  var offsetNewlinesStart = false;
  var offsetNewlinesEnd   = false;

  var posStart = content.indexOf("[[[sfxStart]]]");
  if( posStart != -1 ) {
  
    // truncate buffer after marker position
    var buf = content.substr( 0, posStart );

    var newlines;

    // determine number of newlines
    newlines = buf.match( /\n/g );
    if(newlines instanceof Array) {
      newlines  = newlines.length;
    } else {
      newlines  = 0;
    }
    //    alert("newlines:" + newlines );

//*    offsetNewlinesStart = newlines;
//*
//*    var pos = buf.lastIndexOf("\n");
//*    offsetStart = buf.length-pos-1;
//*    alert( buf + "\nbuf.length: " + buf.length + "\npos: " + pos + "\noffsetStart: " + offsetStart);

    if( posStart && buf.substr(buf.length-1) == "\n" ) {
      // marker positioned immediately after newline
      
      offsetNewlinesStart = true;
      offsetStart = newlines;
    } else {
      // marker not positioned immediately after newline

      offsetStart = posStart - newlines;
    }
    //    bench( "...posStart", null, 2);
    //    alert("offsetStart:" + offsetStart + ", " + offsetNewlinesStart);
    
    //-----
    // end
    //-----

    var posEnd = content.indexOf("[[[sfxEnd]]]");
    if( posEnd != -1 ) {

      // truncate buffer after marker position
      buf = content.substr( 0, posEnd );
      
      var newlines;
      
      newlines = buf.match( /\n/g );
      if(newlines instanceof Array) {
	newlines  = newlines.length;
      } else {
	newlines  = 0;
      }

      if( posEnd && buf.substr(buf.length-1) == "\n" ) {
	// marker positioned immediately after newline
	
	offsetNewlinesEnd = true;
	offsetEnd = newlines;
      } else {
	// marker not positioned immediately after newline

	offsetEnd = posEnd - newlines - 14; // 14 == [[[sfxStart]]]".length
      }
      //      bench( "...posEnd", null, 2);
    }
  }

  ret[0] = offsetStart;
  ret[1] = offsetNewlinesStart;
  ret[2] = offsetEnd;
  ret[3] = offsetNewlinesEnd;
  ret[4] = posEnd;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.offsetFromNewlines = function( content, newlines ) {

  var pos   = -1;
  var count = 0;
  while( (count < newlines) && 
	 (pos = content.indexOf("\r\n", pos+1)) != -1 ) {
    count++;
  }
  pos = pos + 2 - newlines;
  //  alert("pos:" + pos);

  return pos;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.setOffsets = function( arg ) {

  var offsetStart         = arg[0];
  var offsetNewlinesStart = arg[1];
  var offsetEnd           = arg[2];
  var offsetNewlinesEnd   = arg[3];
  var posEnd              = arg[4];

  if( offsetStart != -1 ) {

    var rng;

    if( wfx.is_ie ) {

      rng = this._doc.body.createTextRange();
      rng.moveToElementText(this._bodynode);

      var content;      
      if( offsetNewlinesStart || offsetNewlinesEnd ) {
	// get content if at least one position is immediately after a newline
	content = rng.text;
      }

      rng.collapse(true);
      
      if( offsetEnd != -1 ) {
	if( offsetNewlinesEnd ) {
	  // transform offset from newlines to character position
	  offsetEnd = this.offsetFromNewlines( content, offsetEnd);
	}
	rng.moveEnd("character", offsetEnd);
      }

      if( offsetNewlinesStart ) {
	// transform offset from newlines to character position
	offsetStart = this.offsetFromNewlines( content, offsetStart);
      }
      rng.moveStart("character", offsetStart);
    
      //  rng.select();
      //      rng.moveEnd("character", offsetStart);
      //      alert(rng.htmlText);

      if( rng.type == "None" ) {
	rng.collapse(true);
      }
      rng.select();
    } else {

      //-----
      // Moz 
      //-----

      //      this._dbg.value += "\n\noffsetNewlinesStart:" + offsetNewlinesStart + ", offsetStart:" + offsetStart + "\noffsetNewlinesEnd:" + offsetNewlinesEnd + ", offsetEnd:" + offsetEnd + "\n\n";


      if(1) {
	if( offsetNewlinesStart && offsetNewlinesEnd ) {

	  this.restoreRangeByNewlines( offsetStart, offsetEnd, true );

	} else if( offsetNewlinesStart ) {
	  	  
	  this.restoreRangeByNewlines( offsetStart, -1, true );
	  
	  if( posEnd != -1 ) {
	    this.restoreRange( null, offsetEnd, true );
	  }
	} else if( offsetNewlinesEnd ) {

	  this.restoreRange( offsetStart, -1, true );
	  this.restoreRangeByNewlines( -1, offsetEnd, true );

	} else {

	  this.restoreRange( offsetStart, offsetEnd, true );
	}
      }

      if(0) { 
	var newlines = this._bodynode.getElementsByTagName("br");
	//	alert("newlines:" + newlines.length);
      
	//	wfxEditor.nodeStart = newlines[offsetNewlinesStart-1];	
	wfxEditor.nodeStart = newlines[0];
	wfxEditor.offsetStart = 0;

	var par = wfxEditor.nodeStart;
	while( par = par.parentNode ) {
	  //alert("parent:" + par.nodeName );
	}
      }

      //      bench( null, 120 );
      if( typeof wfxEditor.nodeStart != "undefined" ) {
	
	this.setRange( wfxEditor.nodeStart, wfxEditor.offsetStart, 
		       wfxEditor.nodeEnd,   wfxEditor.offsetEnd  );
      }
      //      bench( "...setRange", 120);
    }
  }

  wfxEditor.nodeStart   = null;
  wfxEditor.offsetStart = -1;
  wfxEditor.nodeEnd     = null;
  wfxEditor.offsetEnd   = -1;
};

function sleepMSec( msec ) {

  return;

  var t0 = (new Date()).getTime();
  var t1;

  while( (t1 = (new Date()).getTime()) && t1-t0<msec ) {
  }
}

function bench( msg, num, ref, reset ) {

  if( !doBench ) {
    return;
  }

  if( tt.length == 0 ) {
    tt[0] = (new Date()).getTime();
    msg = "tinit";
  }

  var keep = true;
  if( num == null || typeof num == "undefined" ) {
    num = tt.length;
    keep = false;
  }

  tt[num] = (new Date()).getTime();

  if( ref == null || typeof ref == "undefined" ) {
    ref = 0;
  }

  var tdiff = "[error]";
  if( num > ref ) {
    tdiff = tt[num] - tt[ref];
  }

  if( msg != null && typeof msg != "undefined" ) {
    benchMsg += msg + ":\n  tdiff(" + num + "-" + ref + "):" + tdiff + "ms\n";
  }

  if( !keep ) {
    tt.pop();
  }

};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.getIndent = function( line ) {

  /^(\s*)/.test(line);
  return RegExp.$1.length;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.getIndentLevel = function( linenumber ) {

  // default: current line
  linenumber = linenumber || this._linenumber;

  var content = this.col2tag(this.getHTML());
  //  alert("content:\n" + wfxEditor.str2chr(content));

  var newline;
  if( wfx.is_ie ) {
    newline = '\r\n';
  } else {
    newline = '\n';
  }
  
  var pos, count;
  count =  0;
  pos   = -1;
  while( (count<linenumber-1) && ((pos = content.indexOf(newline, pos+1)) != -1) ) {
    count++;
  }
  //  alert("count:" + count);
  
  count =  0;
  while( content.charAt(++pos) == " " ) {
    count++;
  }
  
  //  alert("spaces:" + count);

  return count;

};
//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.setValueFromSelection = function( id ) {

  var sel = this._getSelection();
  var rng = this._createRange(sel);
  
  var str;

  if( wfx.is_ie ) {

    str = rng.text;
    str = str.replace( /\r\n/g, "" );   // strip linebreaks

  } else {

    // without information on newlines
    str = rng.toString();
  }

  //  alert(wfxEditor.str2chr(str));

  var el;
  if( el = document.getElementById(id) ) {
    el.value = str;
  }
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.findAndReplace = function( fstr, rstr ) {

  //  alert("findAndReplace");

  var rng;
  var sel;
  var html;

  if( wfx.is_ie ) {

    rng = this._doc.body.createTextRange();

    rng.moveToElementText(this._bodynode);
    //     rng.collapse(true);

    //    alert(rng.innerText);

    html = rng.htmlText;

  } else {

    rng = this._doc.createRange();

    var startNode = this._doc.getElementById("bodynode");
    //    alert("startNode:" + (typeof startNode));
    if( typeof startNode != "undefined" ) {
      try {
        rng.setStart( startNode, 0);
        rng.setEnd( startNode, 1);
      } catch(e) {
	alert("Exception:\n" + e);
      }
    }
  
    // unfortunately without information on newlines
    //    alert(wfxEditor.str2chr(rng.toString()));
    html = rng.toString();
    html = html.replace( /\n/g, "" );   // strip newline (first char)

    if( this._editor.find( fstr ) ) {

    } else {

    }
  }

  //  alert(wfxEditor.str2chr(html));

}

//#****************************************************************************
//#
//#****************************************************************************

var doBench = false;
var benchMsg = "";
var tt = new Array();

//var insideRehighlighting = false;

wfxEditor._doc;
wfxEditor._bodynode;

wfxEditor.lines;
wfxEditor.reduxes;

wfxEditor.currentOffset = 0;
wfxEditor.reachedOffset = false;

wfxEditor.nodeStart   = null;
wfxEditor.offsetStart = -1;
wfxEditor.nodeEnd     = null;
wfxEditor.offsetEnd   = -1;

wfxEditor.tabWidth    = 2;

wfxEditor.timeInterval   = 500;   // msec of rehighlighting frequency
wfxEditor.timeEvent      = 500 ;   // minimum msec between (key)events
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------

// EOF
// Local variables: //
// c-basic-offset:2 //
// indent-tabs-mode:t //
// End: //
