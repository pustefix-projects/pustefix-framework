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

  this.skipRehighlighting = true;
  //  this.cancelRehighlighting = true;
  //  this.insideRehighlighting = false;

  this._showLine   = document.getElementById("wfxed_line");
  this._showColumn = document.getElementById("wfxed_column");
  this._showMsg    = document.getElementById("wfxed_msg");
  this._dbg        = document.getElementById("dbg");

  this._ta_src = document.getElementById("ta_src");
  this._ta_col = document.getElementById("ta_col");

  this._scrollTop = 0;

  this._linepx    = 16;
  this._linebarheight = null;

  this._linenumber = 1;

  this._content_tag;
  this._content_src;
  this._content_col;
}

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

  if( wfx.is_ie ) {

    rngStart = rng.duplicate();
    rngStart.collapse(true);
    rngStart.pasteHTML('<span id="sfxStart"></span>');

    //    alert("sel.type:" + sel.type);
    if( sel.type == "Text" ) {
      rngEnd   = rng.duplicate();
      rngEnd.collapse(false);
      rngEnd.pasteHTML('<span id="sfxEnd"></span>');
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
    mynode.id = "sfxStart";
    mydoc.appendChild(mynode);
//    mynode = this._doc.createComment("sfxStart");
//    mydoc.appendChild(mynode);

    if( !sel.isCollapsed) {
      mynode = this._doc.createElement("span");
      mynode.id = "sfxEnd";
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

  return buf.replace( /<span id="(sfx.+?)"><\/span>/g, "[[[$1]]]");
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

  alert(wfxEditor.str2chr(content));

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
    '<sfxroot xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pfx="http://www.schlund.de/pustefix/core" xmlns:ixsl="http://www.w3.org/1999/XSL/TransformOutputAlias">\n' + xml + '\n</sfxroot>\n';
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
wfxEditor.prototype.updateLineNumbers = function() {

  var doctype =  '<!' + 'DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n';
  var html_header1 = '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">\n<head>\n' + 
  '<meta http-equiv="Content-type" content="text/html; charset=UTF-8" />\n<title></title>\n';
  var html_header2 = '</head>\n<body id="bodynode">\n<pre style="text-align:right">';
  var html_footer  = '</pre>\n</body>\n</html>\n';
  var style_source = '<style>\n' + 
  'body,pre { font-family: monospace; font-size: 13px; margin:0px; background-color:#eeeeee }\n' + 
  'td {text-align:right; font-weight:bold }\n' + 
  '</style>\n';
  
  var content = "";
  for( var i=1; i<=2222; i++ ) {
    content += i + "&nbsp;\n";
  }
  content = doctype + html_header1 + style_source + html_header2 + content + html_footer;

  var doc = this._linebar.document;
  doc.open();
  doc.writeln(content);
  doc.close();
}

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.Config = function () {

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

  bench( null, 10);

  bench( null, 6);
  var rule_htmlTag        = /&lt;(\w+\b.*?)&gt;/g;
  var rule_htmlTagName    = /&lt;(\/?\w+)\b(.*?)&gt;/g;
  var rule_htmlEndTag     = /&lt;(\/\w+)&gt;/g;

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
  var rule_colonEndTag     = /&lt;(\/(pfx|ixsl|xsl)\:\w+)&gt;/g;

  var rule_argval  = /(\"[^\0]*?\")/g;

  var rule_entity  = /(&amp;.+?;)/g;
  var rule_comment = /(&lt;!--[^\0]*?--&gt;)/g;
  bench( "\n------------------------\n  rules", null, 6);

  //-------------------------
  // comments: preprocessing
  //-------------------------
  
  bench( null, 6 );
  var comments = buf.match( rule_comment );
  if( comments instanceof Array ) {
    for( var i=0; i<comments.length; i++ ) {
      buf = buf.replace( new RegExp(comments[i]), '&lt;!-- '+i+' --&gt;' );
    }
  }
  bench( "  comments1", null, 6);
  //---------------------------------------------------------------------------

  //------
  // tags
  //------
  
  if(1) {
  bench( null, 6 );
  var pos;
  var arr = buf.match( rule_htmlTag );
  if( arr instanceof Array ) {
    var oldexp, newexp, myexp;
    for( var i=0; i<arr.length; i++ ) {

      if( buf.indexOf(arr[i]) != -1 ) {
	// only if not yet replaced (redundancy!)

	newexp = arr[i].replace( rule_argval, '<span class="string">$1</span>');
	if(newexp != arr[i]) {
	  // quotemeta
	  arr[i] = arr[i].replace( /(\W)/g, '\\$1' );
	  newexp = newexp.replace( /\$/g, '$$$' );  // escape $ for IE as $$
	  //	  alert( "oldexp:" + arr[i] + "\nnewexp:" + newexp);
	  myexp = new RegExp( arr[i], "g" );
	  buf = buf.replace( myexp, newexp );
	}
      }
    }
  }
  bench( "  argval", null, 6);
  }
  //---------------------------------------------------------------------------

  bench( null, 6 );
  buf = buf.replace( rule_colonTag,     '<span class="$2tag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_colonEndTag,  '<span class="$2endtag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_colonTagName, '&lt;<span class="$2tagname">$1</span>$3&gt;' );
  bench( "  colon", null, 6);

  bench( null, 6 );
  buf = buf.replace( rule_htmlTag,    '<span class="htmltag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_htmlEndTag, '<span class="htmlendtag">&lt;$1&gt;</span>' );
  buf = buf.replace( rule_htmlTagName,'&lt;<span class="htmltagname">$1</span>$2&gt;' );
  
  // remove superfluous html tags, because they actually are xml tags
  //  buf = buf.replace( /&lt;<span class="htmltagname">(\w+)<\/span>:/g, "&lt;$1:");
  //  buf = buf.replace( /<span class="htmltag">(&lt;\w+\:\w+.*?&gt;)<\/span>/g, "$1");

  bench( "  html", null, 6);

  if(0) {
//    bench( null, 6 );
//    buf = buf.replace( rule_pfxTag,     '<span class="pfxtag">&lt;$1&gt;</span>' );
//    buf = buf.replace( rule_pfxTagName, '&lt;<span class="pfxtagName">$1</span>$2&gt;' );
//    buf = buf.replace( rule_pfxEndTag,  '<span class="pfxendtag">&lt;$1&gt;</span>' );
//    
//    buf = buf.replace( rule_xslTag,     '<span class="xsltag">&lt;$1&gt;</span>' );
//    buf = buf.replace( rule_xslTagName, '&lt;<span class="xsltagName">$1</span>$2&gt;' );
//    buf = buf.replace( rule_xslEndTag,  '<span class="xslendtag">&lt;$1&gt;</span>' );
//    
//    buf = buf.replace( rule_ixslTag,     '<span class="ixsltag">&lt;$1&gt;</span>' );
//    buf = buf.replace( rule_ixslTagName, '&lt;<span class="ixsltagName">$1</span>$2&gt;' );
//    buf = buf.replace( rule_ixslEndTag,  '<span class="ixslendtag">&lt;$1&gt;</span>' );
//    bench( "  pfx|xsl|ixsl", null, 6);
  } else {
  }

  bench( null, 6 );
  buf = buf.replace( rule_entity,     '<span class="entity">$1</span>' );
  bench( "  entities", null, 6 );

  //--------------------------
  // comments: postprocessing
  //--------------------------

  bench( null, 6);
  if( comments instanceof Array ) {
    for( var i=0; i<comments.length; i++ ) {
      buf = buf.replace( new RegExp('&lt;!-- '+i+' --&gt;'), '<span class="comment">'+comments[i]+'</span>' );
    }
  }
  bench( "  comments2", null, 6);

  bench( "  src2col(intern)", null, 10);

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
    // insert &lrm; to ensure correct line numbering for empty lines
    content = content.replace( /<br \/>/g, '<span style="font-weight:bold">&lrm;</span>\r\n' );
  } else {
    // insert &lrm; to ensure correct line height for some monospace fonts
    //    content = content.replace( /<br \/>/g, '<span style="font-weight:bold">&lrm;</span><br />' );
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
	default: try { 
	    //		  alert("execCommand: " + cmdID + ", " + UI + ", " + param);
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
  //alert("_editorEvent()..." + ev.type);

  var editor = this;

  //  var keyEvent = (wfx.is_ie && ev.type == "keydown") || (ev.type == "keypress");
  var keyEvent = (ev.type == "keydown") || (ev.type == "keypress");
  //  this._dbg.value += ev.type + ", " + ev.keyCode;
  //---------------------------------------------------------------------------

  if( ev.type == "mousedown" || ev.type == "scroll" ) {
    // no other actions if mousedown (e.g. allow mouse selection in IE)
    //    return;
  }
  //---------------------------------------------------------------------------
 
  if (keyEvent && !(ev.type == "mousedown" || ev.type == "scroll") ) {
    // other keys here
    switch (ev.keyCode) {
  
    case 0:
      this.skipRehighlighting = true;
      return;
      break;
    case 65: // A
      if( ev.altKey ) {
	this.skipRehighlighting = true;
	wfxEditor._stopEvent(ev);
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

      this.skipRehighlighting = true;

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
	content2 = this.col2tag(buf);
      } else {
	content = this.col2tag(this.getHTML());
      }
      //	alert("1:\n" + wfxEditor.str2chr(content));
      //-------------------------------------------------------------------------

      //	content = this.indentCurrentLine( content, optiTab );
      content = this.indentCurrentRange( content, optiTab );
      //-------------------------------------------------------------------------
      //-------------------------------------------------------------------------

      if( optiTab ) {
	buf     = content;
	content = content2;
      }
      //-------------------------------------------------------------------------

      if( optiTab ) {
	if( wfx.is_ie ) {
	  newline = '\r\n';
	} else {
	  newline = '\n';
	}
  
	count =  0;
	pos   = -1;
	while( (count<linenumber-1) && ((pos = content.indexOf(newline, pos+1)) != -1) ) {
	  count++;
	}
	//  alert("count:" + count);

	charOffset = 1;
	if( wfx.is_ie && linenumber>1 ) {
	  charOffset = 2;
	}
	if( linenumber==1 ) {
	  charOffset = 1;
	}

	content = content.substr( 0, pos+charOffset) + charOffset + 
	  content.substr( pos+charOffset);
  
	count =  0;
	while( content.charAt(++pos) == " " ) {
	  count++;
	}
	//  alert("spaces:" + count);
      }
      //-------------------------------------------------------------------------

      //------------
      // getOffsets
      //------------
    
      //	alert("getOffsets:\n" + wfxEditor.str2chr(content));

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
	  content = content.replace( /<span id="sfx.+?"><\/span>/g, "");
	} else {
	  content = this.removeChangedRangeMarker(content);
	}

	//	  alert( wfxEditor.str2chr(content) );

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
	  this._doc.body.innerHTML = content;
	  //	    alert("innerHTML:\n" + wfxEditor.str2chr(content));
	} else {
	  this._doc.body.innerHTML = '<pre>' + content + '</pre>';
	  //	    alert("innerHTML:\n" + wfxEditor.str2chr('<pre>' + content + '</pre>'));
	}

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
      break;


    case 33: // PageUp
    case 34: // PageDown
    case 35: // End
    case 36: // Pos1
    case 37: // CursorLeft
    case 38: // CursorUp
    case 39: // CursorRight
    case 40: // CursorDown
      this.skipRehighlighting = true;
      break;
    default:
      this.skipRehighlighting = false;
      if( insideRehighlighting ) {
	cancelRehighlighting = true;
	this._dbg.value += "cancel\n";
      } else {
	cancelRehighlighting = false;
      }
      this._timestamp = (new Date()).getTime();
    }
  }
  //---------------------------------------------------------------------------
  
  //-----------
  // scrolling
  //-----------
  
  setTimeout( function() {
    var scrollTop;
    if(wfx.is_ie) {
      scrollTop = editor._doc.documentElement.scrollTop;
    } else {
      scrollTop = editor._editor.pageYOffset;
    }
    editor._showColumn.value = scrollTop;
    
    if( scrollTop != editor._scrollTop ) {
      // scroll offset has changed
      editor._scrollTop = scrollTop;

      // update linebar
      if(wfx.is_ie) {
	editor._linebar.document.documentElement.scrollTop = scrollTop;
      } else {
	editor._linebar.scroll( 0, scrollTop );
      }
    }
  }, 50);   // Moz needs some delay to detect current scrolling
  //---------------------------------------------------------------------------

  // update the toolbar state after some time
  if (editor._timerToolbar) {
    clearTimeout(editor._timerToolbar);
  }
  editor._timerToolbar = setTimeout
  (function() {
    if(editor._showMsg.value != "TAB" + editor._showLine.value) {
      //      editor._showMsg.value = "";
    }

    var sel = editor._getSelection();
    var rng = editor._createRange(sel);

    if( 0 ) {
      if(wfx.is_ie) {

	//----
	// IE
	//----

	var parNode = rng.parentElement().nodeName;
	var className = rng.parentElement().className;
	var content;
      
	if( parNode == "BODY" || parNode == "PRE" ) {

	  // expand to the left till ">"
	  while( rng.moveStart( "character", -1 ) &&
		 rng.text.charAt(0)!=">" ) {
	  }
	  rng.moveStart( "character", 1 );

	  // expand to the right till "<"
	  while( rng.moveEnd( "character", 1 ) &&
		 rng.text.charAt(rng.text.length-1)!="<" ) {
	  }
	  rng.moveEnd( "character", -1 );

	  content = rng.text + "[" + parNode + "]";
	
	} else if( className == "htmltag" ||
		   className == "pfxtag"  ||
		   className == "xsltag"  ||
		   className == "ixsltag" ) {

	  // look to the right
	  rng.moveEnd( "character", 1);
	  content = rng.text;

	  if( content.charAt(0)== "=" ) {
	    // before "=" <==> End Of AttrVar
	    while( rng.moveStart( "character", -1 ) &&
		   rng.text.charAt(0)!=" " ) {
	    }
	    rng.moveStart( "character", 1 );

	    content = rng.text + "[EndOfAttrVar]";
	    //	  rng.select();
	  } else if( content.charAt(0)== ">" ) {
	    // Before Closing Bracket
	    content = "??? [BeforeClosingBracket]";
	  } else if( content.charAt(0)== "<" ) {
	    // Before Opening Bracket
	    content = "??? [BeforeOpeningBracket]";
	  } else {

	    // look to the left
	    rng.moveStart( "character", -1);
	    rng.moveEnd( "character", -1);
	    content = rng.text;
	  
	    if( content.charAt(0)== ">" ) {
	      // After Closing Bracket
	      content = "??? [AfterClosingBracket]";
	    } else {

	      // look again to the right
	      rng.moveStart( "character", 1);
	      rng.moveEnd( "character", 1);
	      //	    content = rng.text;
	  
	      rng.expand("word");

	      if( rng.text.charAt(0) == "=" ) {
		// before AttrVal
		content = "??? [BeforeAttrVal]";
	      } else {
		rng.moveEnd( "character", 1 );
		content = rng.text + "[InsideAttrVar]";
	      }
	    }
	  }
	} else if( 1 ) {
	  content = rng.parentElement().innerHTML + "[???]";
	}

	editor._showMsg.value =
	  "par.nodeName:" + rng.parentElement().nodeName + ", " +
	  "(" + className + "), " + 
	  "content:" + content;
      } else {

	//-----
	// Moz
	//-----

	editor._showMsg.value = rng.startContainer.nodeValue;

	var parEl = rng.commonAncestorContainer.parentNode;
	editor._showMsg.value += " (" + parEl.className + ")";
      }
    }

    var linenumber = editor._linenumberFromRange( rng );
    if( linenumber && (linenumber != editor._linenumber)) {
      editor._showLine.value = linenumber;

      editor._linenumber = linenumber;
    }

    if( !editor.skipRehighlighting ) {

      //      var t1 = (new Date()).getTime();
      //      if( editor._timestamp == null || t1-editor._timestamp > 3000 ) {

      //	editor.rehighlight();
	//      }
    }
    editor._timestamp = (new Date()).getTime();

    //    editor.updateToolbar();
    editor._timerToolbar = null;
  }, 50);
};

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype.indentCurrentRange = function( content, optiTab ) {

  var newline;
  var pos, posprev, count;
  var posEnd, posEndprev;

  //  alert( "content:\n" + wfxEditor.str2chr(content));

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

  if( (posprev = content.substring(0, pos).lastIndexOf(newline)) == -1 ) {
    // no previous newline
    posprev = 0;   // set to start of content
  } else {
    posprev += newline.length;
  }
  
  if( pos>-1 ) {
    pos += newline.length;
  }

  //  alert("posprev:"+ posprev + "\npos:" + pos);
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
    posEnd += newline.length;
  }

  //  alert("posprev:"+ posprev + "\npos:" + pos);
  //---------------------------------------------------------------------------

  var contentLeading, contentTrailing;
  contentLeading  = content.substring( 0,       posprev );
  contentTrailing = content.substr(    posEnd );
  content         = content.substring( posprev, posEnd );

//  alert( wfxEditor.str2chr(contentLeading) + "\n**********************************************************************\n" +
//	 wfxEditor.str2chr(content) + "\n**********************************************************************\n" +
//	 wfxEditor.str2chr(contentTrailing) + "\n**********************************************************************\n" );

  pos     -= contentLeading.length;
  posprev -= contentLeading.length;
  //  alert("posprev:"+ posprev + "\npos:" + pos);
  //---------------------------------------------------------------------------

  //  content = this.indentCurrentLineInternal( content, posprev, pos, optiTab );
  //  return contentLeading + content + contentTrailing;

  do {
    content = this.indentCurrentLineInternal( content, posprev, pos, optiTab );

    posprev = pos;
    pos = posprev+1 + content.substring(posprev+1).indexOf(newline);
    if( pos>-1 ) {
      pos += newline.length;
    }

  } while( pos>-1 && pos<content.length );

  return contentLeading + content + contentTrailing;
};

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype.indentCurrentLineInternal = function( content, posprev, pos, optiTab ) {

//  alert("indentCurrentLineInternal():\n\n" + wfxEditor.str2chr(content) + 
//	"\n\nposprev:"+ posprev + "\npos:" + pos + "\n\n" + 
//	wfxEditor.str2chr(content.substring(posprev, pos)));
  //-------------------------------------------------------------------------

  var prevline = content.substring( posprev, pos );

  if( optiTab ) {
    prevline = this.col2tag(prevline);
  }

  prevline = this.removeChangedRangeMarker(prevline);
  //  alert("prevline:\n" + wfxEditor.str2chr(prevline));
  //-------------------------------------------------------------------------

  // determine indentation of previous line

  var indentprev =  -1;
  while( prevline.charAt(++indentprev) == " " ) {
  }
  //  alert("spaces(prev):" + indentprev);
  //-------------------------------------------------------------------------

  var rule_htmlTag        = /<([\w\:\-]+\b.*?)>/g;
  var rule_htmlEndTag     = /<(\/[\w\:\-]+)>/g;
  var rule_htmlSingleTag  = /<([\w\:\-]+\b[^>]*?)\/>/g;

  var prevline = prevline.replace( rule_htmlSingleTag, "");

  var count_tags    = prevline.match(rule_htmlTag);
  count_tags = (count_tags instanceof Array ? count_tags.length : 0);
  var count_endtags = prevline.match(rule_htmlEndTag);
  count_endtags = (count_endtags instanceof Array ? count_endtags.length : 0);
  var indentdiff = count_tags - count_endtags;
  //	alert("count_tags:" + count_tags + "\ncount_endtags:" + count_endtags);
  //-------------------------------------------------------------------------

  var line = content.substr( pos );
  //  alert( "line:\n" + wfxEditor.str2chr(line));

  // determine indentation of current line

  posEnd = -1;
  posStart = line.indexOf("[[[sfxStart]]]");
  if( posStart != -1 ) {
    content = content.replace( /\[\[\[sfxStart\]\]\]/, "");
    posEnd   = line.indexOf("[[[sfxEnd]]]");
    if( posEnd != -1 ) {
      content = content.replace( /\[\[\[sfxEnd\]\]\]/, "");
    }
  }
  //  alert("(current) posStart:" + posStart + "\nposEnd:" + posEnd);
	
  var postmp = pos;

  var indent =  0;
  while( content.charAt(postmp++) == " " ) {
    indent++;
  }
  //	alert("spaces(current):" + indent);
  //-------------------------------------------------------------------------


  if( prevline.search( /^\s*<\// ) != -1 ) {
    // previous line starts with endtag
    indentdiff++;
  }
  if( line.replace(/\[\[\[sfx.*?\]\]\]/, "").search( /^\s*<\// ) != -1 ) {
    // current line starts with endtag
    indentdiff--;
  }
  //-------------------------------------------------------------------------

  var indentnew = indentprev + indentdiff * wfxEditor.tabWidth;
  if(indentnew < 0) {
    indentnew = 0;
  }
  //-------------------------------------------------------------------------
	
  //  alert("indent:" + indent + "\nindentnew:" + indentnew);

  var indentstr = "";
  for( var i=0; i<indentnew; i++) {
    indentstr += " ";
  }

  if( posStart != -1 ) {
    if( posStart <= indent ) {
      posStart = indentnew;
      posEnd   = -1;
    } else {
      posStart = indentnew + (posStart - indent);
      if( posEnd != -1 ) {
	posEnd = indentnew + (posEnd - indent);
      }
    }
  }
  //  alert("new posStart:" + posStart);
  //-------------------------------------------------------------------------

  content = content.substr( 0, pos) + indentstr + 
    content.substr( pos + indent);

  if( posStart != -1 ) {
    content = content.substr( 0, pos + posStart) + 
      "[[[sfxStart]]]" + content.substr( pos + posStart );
    if( posEnd != -1 ) {
      content = content.substr( 0, pos + posEnd) + 
	"[[[sfxEnd]]]" + content.substr( pos + posEnd);
    }
  }
  //  alert("return from Internal():\n" + wfxEditor.str2chr(content));

  return content;
};

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype.indentCurrentLine = function( content, optiTab ) {

  var newline;
  var pos, posprev, count;

  //  alert( "content:\n" + wfxEditor.str2chr(content));

  if( wfx.is_ie ) {
    newline = '\r\n';
  } else {
    newline = '\n';
  }
  if( optiTab ) {
    newline = '<br />';
  }
  
  if( (pos = content.indexOf("[[[sfxStart]]]")) == -1 ) {
    // error
    return content;
  }

  if( (pos = content.substring(0, pos).lastIndexOf(newline)) == -1 ) {
    // no previous newline <==> in first line
    pos = 0; 
  }

  if( (posprev = content.substring(0, pos).lastIndexOf(newline)) == -1 ) {
    // no previous newline
    posprev = 0;   // set to start of content
  } else {
    posprev += newline.length;
  }
  
  if( pos>0 ) {
    pos += newline.length;
  }

  //  alert("posprev:"+ posprev + "\npos:" + pos);
  //-------------------------------------------------------------------------

  var prevline = content.substring( posprev, pos );

  if( optiTab ) {
    prevline = this.col2tag(prevline);
  }

  //  alert("prevline:\n" + wfxEditor.str2chr(prevline));

  var rule_htmlTag        = /<([\w\:\-]+\b.*?)>/g;
  var rule_htmlEndTag     = /<(\/[\w\:\-]+)>/g;
  var rule_htmlSingleTag  = /<([\w\:\-]+\b[^>]*?)\/>/g;

  prevline = prevline.replace( rule_htmlSingleTag, "");
	
  var count_tags    = prevline.match(rule_htmlTag);
  count_tags = (count_tags instanceof Array ? count_tags.length : 0);
  var count_endtags = prevline.match(rule_htmlEndTag);
  count_endtags = (count_endtags instanceof Array ? count_endtags.length : 0);
  var indentdiff = count_tags - count_endtags;
  //	alert("count_tags:" + count_tags + "\ncount_endtags:" + count_endtags);
  //-------------------------------------------------------------------------

  // determine indentation of previous line

  var posStart, posEnd = -1;
  posStart = prevline.indexOf("[[[sfxStart]]]");
  if( posStart != -1 ) {
    content = content.replace( /\[\[\[sfxStart\]\]\]/, "");
    posEnd   = prevline.indexOf("[[[sfxEnd]]]");
    if( posEnd != -1 ) {
      content = content.replace( /\[\[\[sfxEnd\]\]\]/, "");
    }
  }
  //  alert( "posStart:" + posStart + "\nposEnd:" + posEnd );

  var postmp = posprev;

  var indentprev =  0;
  while( content.charAt(postmp++) == " " ) {
    indentprev++;
  }
  //	alert("spaces(prev):" + indentprev);
  //-------------------------------------------------------------------------

  var line = content.substr( pos );
  //  alert( "line:\n" + wfxEditor.str2chr(line));

  // determine indentation of current line

  posEnd = -1;
  posStart = line.indexOf("[[[sfxStart]]]");
  if( posStart != -1 ) {
    content = content.replace( /\[\[\[sfxStart\]\]\]/, "");
    posEnd   = line.indexOf("[[[sfxEnd]]]");
    if( posEnd != -1 ) {
      content = content.replace( /\[\[\[sfxEnd\]\]\]/, "");
    }
  }
  //	alert("(current) posStart:" + posStart + "\nposEnd:" + posEnd);
	
  postmp = pos;

  var indent =  0;
  while( content.charAt(postmp++) == " " ) {
    indent++;
  }
  //	alert("spaces(current):" + indent);
  //-------------------------------------------------------------------------


  if( prevline.search( /^\s*<\// ) != -1 ) {
    // previous line starts with endtag
    indentdiff++;
  }
  if( line.replace(/\[\[\[sfx.*?\]\]\]/, "").search( /^\s*<\// ) != -1 ) {
    // current line starts with endtag
    indentdiff--;
  }
  //-------------------------------------------------------------------------

  var indentnew = indentprev + indentdiff * wfxEditor.tabWidth;
  if(indentnew < 0) {
    indentnew = 0;
  }
  //-------------------------------------------------------------------------
	
  //  alert("indent:" + indent + "\nindentnew:" + indentnew);

  var indentstr = "";
  for( var i=0; i<indentnew; i++) {
    indentstr += " ";
  }

  if( posStart != -1 ) {
    if( posStart <= indent ) {
      posStart = indentnew;
      posEnd   = -1;
    } else {
      posStart = indentnew + (posStart - indent);
      if( posEnd != -1 ) {
	posEnd = indentnew + (posEnd - indent);
      }
    }
  }
  //	alert("new posStart:" + posStart);
  //-------------------------------------------------------------------------

  content = content.substr( 0, pos) + indentstr + 
    content.substr( pos + indent);

  if( posStart != -1 ) {
    content = content.substr( 0, pos + posStart) + 
      "[[[sfxStart]]]" + content.substr( pos + posStart );
    if( posEnd != -1 ) {
      content = content.substr( 0, pos + posEnd) + 
	"[[[sfxEnd]]]" + content.substr( pos + posEnd);
    }
  }
  //  alert(wfxEditor.str2chr(content));

  return content;
};

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype._linenumberFromRange = function( rng2 ) {

  var rng;
  
  // extend start of range to the top 
  if (wfx.is_ie) {
    rng = rng2.duplicate();
    rng.moveStart( "textedit", -1 );
//    var str = "";
//    while( rng.moveStart( "character", -1) == -1 ) {
//      str += wfxEditor.str2chr(rng.htmlText) + "\n";
//    }
//    //    rng.moveStart( "character", -1 );
//
//    alert(str);
    // needed to include the previous newline if at the very beginning of 
    // a line
    rng.moveEnd( "character", 1);
  } else {
    rng = rng2.cloneRange();
    var startNode = this._doc.getElementById("bodynode");
    //    alert("startNode:" + (typeof startNode));
    if( typeof startNode != "undefined" ) {
      try {
        rng.setStart( startNode, 0);
      } catch(e) {
      }
    }
  }
  
  var html = null;
  if (wfx.is_ie) {
    html = rng.htmlText;

//    html = html.replace( /<(\/?)PRE>/g, '<$1pre>' );
//    html = html.replace( /&nbsp;<\/pre>/g, '</pre>' );
//    html = html.replace( /<\/pre><pre>/g , "\r\n" );

  } else {
    html = wfxEditor.getHTML(rng.cloneContents(), false);
  }
  
  //  alert(html);

  rng.collapse(false);
  this._ta_src.value = html;

  return this._linenumberFromHTML( html );
}

//#----------------------------------------------------------------------------
//#
//#----------------------------------------------------------------------------
wfxEditor.prototype._linenumberFromHTML = function( html ) {

  //  alert("_linenumberFromHTML:" + wfxEditor.str2chr(html));
  
  if(html == null) {

    return null;
  } else {
  
    var newline;
    var count;
    if (wfx.is_ie) {
      newline = "\r\n";
      count = 1;
    } else {
      newline = "<br />";
      count = 1;
    }

    var pos = 0;    
    while( (pos = html.indexOf( newline, pos)+1) > 0 ) {
      count++;
    }

    return count;
  }
};

// focuses the iframe window.  returns a reference to the editor document.
wfxEditor.prototype.focusEditor = function() {
  try { 
    this._editor.focus() 
  } catch(e) {}
}

// retrieve the HTML
wfxEditor.prototype.getHTML = function(dbg) {

  bench( null, 3 );

  var html;
  if(wfx.is_ie) {

    //    html = wfxEditor.getHTML(this._doc.body, false, dbg);

    html = this._doc.body.innerHTML;

    html = html.replace( /\"/g, '&quot;');

    html = html.replace( /<SPAN (class|id)=(.*?)>/g, '<span $1="$2">');
    html = html.replace( /<\/SPAN>/g, '</span>' );
    html = html.replace( /<BR>/g, '<br />' );
    html = html.replace( /<(\/?)PRE>/g, '<$1pre>' );
    html = html.replace( /&nbsp;<\/pre>/g, '</pre>' );

    //    alert( html + "\n**********************************************************************\n" + html3 + "\n**********************************************************************\n" + html2);

    // remove bogus spans (introduced by Return at AfterClosingBracket

    var rule_emptyspans = /<span class="[^\"]+?"><\/span>/g;
    
    while( html.match(rule_emptyspans) ) {
      html = html.replace( rule_emptyspans, "" );
    }

    html = html.replace( /<\/pre><pre>/g , "<br />" );
    
//    html = html.replace( /<p \/>/g, "<br />" );
//    html = html.replace( /<\/p>((<br \/>)*)<p>/g, "<br />$1" );
//    html = html.replace( /<p>/g, "" );
//    html = html.replace( /<\/p>/g, "" );

//    alert("getHTML() postprocessed:\n" + wfxEditor.str2chr(html));

    html = html.replace( /\r\n/g , "<br />" );

  } else {
    //    totalGetHTML = 1;
    //    html = wfxEditor.getHTML(this._doc.body, false, dbg);
    //    alert("total:" + totalGetHTML );
    html = this._doc.body.innerHTML;

    html = html.replace( /<br>/g, '<br />' );
  }
  
  html = html.replace( /\u200E/g, '' );   // &#8206; == &lrm;

  //  alert( "getHTML():\n" + wfxEditor.str2chr(html));
  bench( "getHTML", null, 3 );

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

wfxEditor._removeClass = function(el, className) {
	if (!(el && el.className)) {
		return;
	}
	var cls = el.className.split(" ");
	var ar = new Array();
	for (var i = cls.length; i > 0;) {
		if (cls[--i] != className) {
			ar[ar.length] = cls[i];
		}
	}
	el.className = ar.join(" ");
};

wfxEditor._addClass = function(el, className) {
	// remove the class first, if already there
	wfxEditor._removeClass(el, className);
	el.className += " " + className;
};

wfxEditor._hasClass = function(el, className) {
	if (!(el && el.className)) {
		return false;
	}
	var cls = el.className.split(" ");
	for (var i = cls.length; i > 0;) {
		if (cls[--i] == className) {
			return true;
		}
	}
	return false;
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

// Retrieves the HTML code from the given node.	 This is a replacement for
// getting innerHTML, using standard DOM calls.
wfxEditor.getHTML = function(root, outputRoot) {
  //  var thisdbg = 0;

  var html = "";   //[" + (totalGetHTML++) + "]";
  switch (root.nodeType) {
  case 1: // Node.ELEMENT_NODE
    //    thisdbg && alert("case 1: Node.ELEMENT_NODE");
  case 11: // Node.DOCUMENT_FRAGMENT_NODE
    //    thisdbg && alert("case 11: Node.DOCUMENT_FRAGMENT_NODE");
    var closed;
    var i;
    if (outputRoot) {
      closed = (!(root.hasChildNodes() || wfxEditor.needsClosingTag(root)));
      html += "<" + root.tagName.toLowerCase();
      var attrs = root.attributes;
      for (i = 0; i < attrs.length; ++i) {
	var a = attrs.item(i);
	if (!a.specified) {
	  continue;
	}
	var name = a.nodeName.toLowerCase();
	if (/_moz/.test(name)) {
	  // Mozilla reports some special tags
	  // here; we don't need them.
	  continue;
	}
	var value;
//	if (name != "style") {
//	  // IE5.5 reports 25 when cellSpacing is
//	  // 1; other values might be doomed too.
//	  // For this reason we extract the
//	  // values directly from the root node.
//	  // I'm starting to HATE JavaScript
//	  // development.  Browser differences
//	  // suck.
//
//	  // XXX: IE: String, Number, Boolean !!!
//	  // XXX: Moz: String only
//
//	  if( wfx.is_ie && typeof root[a.nodeName] != "undefined") {
//	    value = root[a.nodeName];
//	  } else {
	    value = a.nodeValue;
//	  }
//	} else { // IE fails to put style in attributes list
//	  // FIXME: cssText reported by IE is UPPERCASE
//	  value = root.style.cssText;
//	}
//	if (/_moz/.test(value)) {
//	  // Mozilla reports some special tags
//	  // here; we don't need them.
//	  continue;
//	}
	html += " " + name + '="' + value + '"';
      }
      //      if( wfx.is_ie && root.tagName.toLowerCase() == "br" ) {
      //	html += " />\r\n";
      //      } else {
	html += closed ? " />" : ">";
	//      }
    }

    //    thisdbg && alert("html (before for):>>>" + html + "<<<");
    for (i = root.firstChild; i; i = i.nextSibling) {
      html += wfxEditor.getHTML(i, true);
    }
    if (outputRoot && !closed) {
      html += "</" + root.tagName.toLowerCase() + ">";
    }
    break;
  case 3: // Node.TEXT_NODE
    //    thisdbg && alert("case 3: Node.TEXT_NODE:" + root.data + "<<");
    html += wfxEditor.htmlEncode(root.data);
    //    thisdbg && alert("html>>>" + html + "<<<");
    break;
  }

  return html;
};

wfxEditor.str2chr = function( str ) {

//*****************************************************************************
//
//*****************************************************************************
  var res = "";
  
  for( var i=0; i<str.length; i++) {
    res += str.charAt(i)+"("+str.charCodeAt(i)+")";
  }

  return str + "\n---------------------------------------\n" + res;
}

//*****************************************************************************
//
//*****************************************************************************
wfxEditor.prototype.restoreRange = function( offsetStart, offsetEnd, dbg ) {

  wfxEditor.currentOffset = 0;
  wfxEditor.reachedOffset = false;

  //alert("this.restoreRange( ~, ~, " + offsetStart +", " + offsetEnd + " )");
  wfxEditor.restoreRange(this._doc.body, false, offsetStart, offsetEnd, dbg);

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
    if (outputRoot && !closed) {
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
	sel.removeAllRanges();
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
  
  //-------------------
  // XML wellformdness
  //-------------------

  var err = new Array();
  if( !this.isWellFormedXML( content, err) ) {
    
    if( err instanceof Array ) {
      alert("errReason:" + err["Reason"] + "\n" + 
	    "errLine:"   + err["Line"]   + "(???)\n" +
	    "errSource:" + err["Source"] + "\n");
    }
    return;
  }
  //---------------------------------------------------------------------------

  //  content = this.tag2src(content).replace(/<br \/>/g, newline);

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
  
  //  alert("content:" + wfxEditor.str2chr(content));    
  this._content_tag = content;

  content = this.tag2src( content );
  //alert("content( after tag2src):" + wfxEditor.str2chr(content));    
  this._content_src  = content;
  this._ta_src.value = content;

  content = this.src2col( content );
  //alert("content( after src2col):" + wfxEditor.str2chr(content));    
  this._content_col  = content;
  this._ta_col.value = content;

  var doctype =  '<!' + 'DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">\n';
  var html_header1 = '<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">\n<head>\n' + 
  '<meta http-equiv="Content-type" content="text/html; charset=UTF-8" />\n<title></title>\n';
  var html_header2 = '</head>\n<body id="bodynode">\n';
  // Moz: \n after body tag is required to prevent exception in rng.setStart
  
  var html_footer  = '</body>\n</html>\n';
//  var style_source = '<style>\n' + 
//  '@import url("wfxSource.css")\n' +
//  '</style>\n';
//  var style_source = '<link rel="stylesheet" type="text/css" href="wfxSource.css" />\n';
  var style_source = '<style>\n' + 
  'body,pre { font-family: monospace; font-size: 13px; margin:0px }\n' + 
  'p    { margin-top: 0px !important; margin-bottom: 0px !important; }\n' + 
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
  '</style>\n';

//  '.string      { color:#0000ff; font-weight:normal }\n' + 
//  '.htmltag     { color:#990099; font-weight:normal }\n' + 
//  '.htmlendtag  { color:#990099; font-weight:normal }\n' + 
//  '.pfxtag      { color:#0000aa; font-weight:normal }\n' + 
//  '.pfxendtag   { color:#0000aa; font-weight:normal }\n' + 
//  '.xsltag      { color:#dd0000; font-weight:normal }\n' + 
//  '.xslendtag   { color:#dd0000; font-weight:normal }\n' + 
//  '.ixsltag     { color:#cc44aa; font-weight:normal }\n' + 
//  '.ixslendtag  { color:#cc44aa; font-weight:normal }\n' + 
//  '.entity      { color:#ff0000; font-weight:normal }\n' + 
//  '.comment     { color:#00aa00; font-weight:normal; font-style:italic }\n' + 

    
  content = this.prepareContent( content );
  content = '<pre>' + content + '</pre>';    
  
  content = doctype + html_header1 + style_source + html_header2 + content + html_footer;

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
  this._linebarheight = Math.ceil( parseInt(document.getElementById("wfxline01").style.height) / this._linepx );

  this.updateLineNumbers();

  //  bench( "...generate()" );
};

//#****************************************************************************
//#
//#****************************************************************************
wfxEditor.prototype.startIntervalRehighlighting = function() {

  var editor = this;

  this._timerRehighlight = setInterval( function() {

    //    editor._dbg.value += "insideRehighlighting...\n";
  
    if( insideRehighlighting ) {
      editor._dbg.value += "insideRehighlighting ==> cancel\n";
      cancelHighlighting = true;
      return;
    }

    if( editor.skipRehighlighting ) {
      //      editor._dbg.value += "S";
      return;
    }

    if( (new Date()).getTime() - editor._timestamp < wfxEditor.timeEvent ) {
      //      editor._dbg.value += "K";
      return;
    }

    insideRehighlighting = true;

    var content;

    bench( null, 1);
    //---------------------------------------------------------------------------

    //----------------
    // setRangeMarker
    //----------------

    bench( null, 2);
    editor.setRangeMarker();
    bench( "setRangeMarker", null, 2);
    //---------------------------------------------------------------------------

    if( cancelRehighlighting ) {
      editor._dbg.value += "c1\n";
      insideRehighlighting = false;
      return;
    }
    //---------------------------------------------------------------------------

    //-----------------------
    // get content (col2tag)
    //-----------------------

    bench( null, 2);
    content = editor.col2tag(editor.getHTML());
    bench( "col2tag(getHTML())", null, 2);
    //    alert(wfxEditor.str2chr(content));

//    if( content == "[[[sfxStart]]]" ) {
//      // empty content
//      editor._doc.body.innerHTML = '<pre></pre>';
//      insideRehighlighting = false;
//      return;
//    }
    //---------------------------------------------------------------------------

    if( cancelRehighlighting ) {
      editor._dbg.value += "c2\n";
      insideRehighlighting = false;
      return;
    }
    //---------------------------------------------------------------------------

    //------------
    // getOffsets
    //------------
    
    var ret = new Array();
    editor.getOffsets( content, ret );

    //-------------------------------------------------------------------------
    if( cancelRehighlighting ) {
      editor._dbg.value += "c3\n";
      insideRehighlighting = false;
      return;
    }
    //-------------------------------------------------------------------------

    var offsetStart         = ret[0];
    var offsetNewlinesStart = ret[1];
    var offsetEnd           = ret[2];
    var offsetNewlinesEnd   = ret[3];
    var posEnd              = ret[4];

    //    alert("offsetStart:" + offsetStart + ", offsetNewlinesStart:" + offsetNewlinesStart + "\noffsetEnd:" + offsetEnd + ", offsetNewlinesEnd:" +  offsetNewlinesEnd + "\nposEnd:" + posEnd);
    //---------------------------------------------------------------------------

    //-------------------
    // XML wellformdness
    //-------------------

    //  var err = new Array();
    //  if( !this.isWellFormedXML( this.removeChangedRangeMarker(content), err) ) {
    //    //    alert("XML parse error!");
    //
    //    if( err instanceof Array ) {
    //      alert("errReason:" + err["Reason"] + "\n" + 
    //            "errLine:"   + err["Line"]   + "\n" +
    //            "errColumn:" + err["Column"] + "\n" +    
    //            "errSource:" + err["Source"] + "\n");
    //    }
    //    return;
    //  }
    //---------------------------------------------------------------------------
  
    try {

      //      content = editor.src2col(editor.tag2src(editor.removeChangedRangeMarker(content)));

      if( cancelRehighlighting ) {
	editor._dbg.value += "c4\n";
	insideRehighlighting = false;
	return;
      }
      //-------------------------------------------------------------------------

      //--------------------------
      // removeChangedRangeMarker
      //--------------------------

      bench( null, 4);
      content = editor.removeChangedRangeMarker(content);
      bench( "removeChangedRangeMarker", null, 4);
      //-------------------------------------------------------------------------

      if( cancelRehighlighting ) {
	editor._dbg.value += "c5\n";
	insideRehighlighting = false;
	return;
      }
      //-------------------------------------------------------------------------

      //--------------------------------------------
      // transform content in step 1 of 2 (tag2src)
      //--------------------------------------------

      bench( null, 4);
      content = editor.tag2src(content);
      bench( "tag2src", null, 4);
      //-------------------------------------------------------------------------

      if( cancelRehighlighting ) {
	editor._dbg.value += "c6\n";
	insideRehighlighting = false;
	return;
      }
      //-------------------------------------------------------------------------

      //--------------------------------------------
      // transform content in step 2 of 2 (src2col)
      //--------------------------------------------

      bench( null, 4);
      content = editor.src2col(content);
      bench( "src2col", null, 4);
      //-------------------------------------------------------------------------

      if( cancelRehighlighting ) {
	editor._dbg.value += "c7\n";
	insideRehighlighting = false;
	return;
      }
      //-------------------------------------------------------------------------

      //-------------------------
      // set content (innerHTML)
      //-------------------------
  
      content = editor.prepareContent( content );

      if( cancelRehighlighting ) {
	editor._dbg.value += "c8\n";
	insideRehighlighting = false;
	return;
      }

      //      alert("innerHTML=content:\n" + wfxEditor.str2chr(content));
      bench( null, 4);
      editor._doc.body.innerHTML = '<pre>' + content + '</pre>';
      bench( "innerHTML", null, 4);

      bench( "...content set", null, 1);
    

    } catch(e) {
      alert("Exception:\n" + e);
    }

    bench( null, 2);
    //---------------------------------------------------------------------------

    //------------
    // setOffsets
    //------------

    editor.setOffsets( [ offsetStart, offsetNewlinesStart, 
			 offsetEnd,   offsetNewlinesEnd,
			 posEnd ] );
    //---------------------------------------------------------------------------

    editor.skipRehighlighting = true;

    bench( "...cursor set", null, 2);

    bench("rehighlight()", null, 1);

    //    document.forms[0].dbg.value += benchMsg;

    //    editor._dbg.value = parseInt(editor._dbg.value)+1;

    insideRehighlighting = false;

  }, wfxEditor.timeInterval );

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
    //    alert(wfxEditor.str2chr(buf));

    var returns, newlines;

    // determine number of newlines
    newlines = buf.match( /\n/g );
    if(newlines instanceof Array) {
      newlines  = newlines.length;
    } else {
      newlines  = 0;
    }
    //    alert("newlines:" + newlines );

    if( posStart && buf.substr(buf.length-1) == "\n" ) {
      // marker positioned immediately after newline
      
      offsetNewlinesStart = true;
      offsetStart = newlines;
    } else {
      // marker not positioned immediately after newline

      offsetStart = posStart - newlines;
    }
    bench( "...posStart", null, 2);
    //    alert("offsetStart:" + offsetStart + ", " + offsetNewlinesStart);
    
    //-----
    // end
    //-----

    var posEnd = content.indexOf("[[[sfxEnd]]]");
    if( posEnd != -1 ) {

      // truncate buffer after marker position
      buf = content.substr( 0, posEnd );
      
      var returns, newlines;
      
      newlines = buf.match( /\n/g );
      if(newlines instanceof Array) {
	newlines  = newlines.length;
      } else {
	newlines  = 0;
      }
      //      alert("returns:" + returns + ", newlines:" + newlines );

      if( posEnd && buf.substr(buf.length-1) == "\n" ) {
	// marker positioned immediately after newline
	
	offsetNewlinesEnd = true;
	offsetEnd = newlines;
      } else {
	// marker not positioned immediately after newline
	
	offsetEnd = posEnd - newlines - 14; // 14 == [[[sfxStart]]]".length
      }
      bench( "...posEnd", null, 2);
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

      //      this._dbg.value += "\n\noffsetNewlinesStart:" + offsetNewlinesStart + ", offsetStart:" + offsetStart + "\noffsetNewlinesEnd:" + offsetNewlinesEnd + ", offsetEnd:" + offsetEnd + "\n\n";

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

      if( typeof wfxEditor.nodeStart != "undefined" ) {
	
	this.setRange( wfxEditor.nodeStart, wfxEditor.offsetStart, 
		       wfxEditor.nodeEnd,   wfxEditor.offsetEnd  );
      }
    }
  }

  wfxEditor.nodeStart   = null;
  wfxEditor.offsetStart = -1;
  wfxEditor.nodeEnd     = null;
  wfxEditor.offsetEnd   = -1;
};

function sleepMSec( msec ) {

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

    //    str = wfxEditor.getHTML(rng.cloneContents(), false);

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
  

    //      html = wfxEditor.getHTML(rng.cloneContents(), false);

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
var totalGetHTML = 1;

var doBench = true;
var benchMsg = "";
var tt = new Array();

var insideRehighlighting = false;

wfxEditor._doc;
wfxEditor._bodynode;

wfxEditor.currentOffset = 0;
wfxEditor.reachedOffset = false;

wfxEditor.nodeStart   = null;
wfxEditor.offsetStart = -1;
wfxEditor.nodeEnd     = null;
wfxEditor.offsetEnd   = -1;

wfxEditor.tabWidth    = 2;

wfxEditor.timeInterval   = 300;   // msec of rehighlighting frequency
wfxEditor.timeEvent      = 50 ;   // minimum msec between (key)events
//---------------------------------------------------------------------------

//---------------------------------------------------------------------------

// EOF
// Local variables: //
// c-basic-offset:2 //
// indent-tabs-mode:t //
// End: //
