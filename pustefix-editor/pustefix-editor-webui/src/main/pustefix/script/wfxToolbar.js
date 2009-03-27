//#****************************************************************************
//#
//#****************************************************************************
function wfxToolbar( config ) {

  if (typeof config == "undefined") {
    // new (default) configuration
    this.config = new wfxToolbar.Config();
  } else {
    // existing configuration
    this.config = config;
  }

  this._toolbar = null;   // HTMLElement
  this._editor  = null;   // wfxEditor object
  
  this._timerToolbar = null; //XXX
}

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar.prototype.registerEditor = function ( wfxEditor ) {

  this._editor       = wfxEditor;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar.prototype.generate = function( elid, className ) {

  var thistb = this;	// to access this in nested functions

  if (typeof elid == "string") {
    // it's not element but ID
    elid = document.getElementById(elid);
  }

  this._toolbar = elid;
  //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

  var toolbar = this._toolbar;
  toolbar.className = className || "wfxtoolbar";
  toolbar.unselectable = "1";
  var tb_row = null;
  var tb_objects = new Object();
  this._toolbarObjects = tb_objects;

  // creates a new line in the toolbar
  function newLine() {
    var table = document.createElement("table");
    table.border = "0px";
    table.cellSpacing = "0px";
    table.cellPadding = "0px";
    toolbar.appendChild(table);
    // TBODY is required for IE, otherwise you don't see anything
    // in the TABLE.
    var tb_body = document.createElement("tbody");
    table.appendChild(tb_body);
    tb_row = document.createElement("tr");
    tb_body.appendChild(tb_row);
  }; // END of function: newLine
  // init first line
  newLine();

  // updates the state of a toolbar element.  This function is member of
  // a toolbar element object (unnamed objects created by createButton
  // functions below).
  //#*******************************************************************
  //#
  //#*******************************************************************
  function setButtonStatus(id, newval) {
    //    alert("setButtonStatus( " + id + ", " + newval + " )");
    var oldval = this[id];
    var el = this.element;
    if (oldval != newval) {
      switch (id) {
      case "enabled":
	if (newval) {
	  wfxToolbar._removeClass(el, "buttonDisabled");
	  el.disabled = false;
	} else {
	  wfxToolbar._addClass(el, "buttonDisabled");
	  el.disabled = true;
	}
	break;
      case "active":
	if (newval) {
	  wfxToolbar._addClass(el, "buttonPressed");
	} else {
	  wfxToolbar._removeClass(el, "buttonPressed");
	}
	break;
      }
      this[id] = newval;
    }
  }; // END of function: setButtonStatus

  // appends a new button to toolbar
  //#*******************************************************************
  //#
  //#*******************************************************************
  function createButton(txt) {
    // the element that will be created
    var el = null;
    var btn = null;
    switch (txt) {
    case "separator":
      el = document.createElement("div");
      el.className = "separator";
      break;
    case "space":
      el = document.createElement("div");
      el.className = "space";
      break;
    case "linebreak":
      newLine();
      return false;
    case "textindicator":
      el = document.createElement("div");
      el.appendChild(document.createTextNode("A"));
      el.className = "indicator";
      el.title = "XXX"; //HTMLArea.I18N.tooltips.textindicator;
      var obj = {
	name	: txt,             // the button name (i.e. 'bold')
	element : el,              // the UI element (DIV)
	enabled : true,            // is it enabled?
	active	: false,           // is it pressed?
	text	: false,           // enabled in text mode?
	cmd	: "textindicator", // the command ID
	state	: setButtonStatus  // for changing state
      };
      tb_objects[txt] = obj;
      break;
    default:
      btn = thistb.config.btnList[txt];
    }
    if (!el && btn) {
      el = document.createElement("div");
      el.title = btn[0];
      el.className = "button";
      // let's just pretend we have a button object, and
      // assign all the needed information to it.
      var obj = {
	name	: txt,             // the button name (i.e. 'bold')
	element : el,              // the UI element (DIV)
	enabled : true,            // is it enabled?
	active	: false,           // is it pressed?
	text	: btn[2],          // enabled in text mode?
	cmd	: btn[3],          // the command ID
	state	: setButtonStatus, // for changing state
	context : btn[4] || null   // enabled in a certain context?
      };
      tb_objects[txt] = obj;
      // handlers to emulate nice flat toolbar buttons
      wfxToolbar._addEvent(el, "mouseover", function () {
			     if (obj.enabled) {
			       wfxToolbar._addClass(el, "buttonHover");
			     }
			   });
      wfxToolbar._addEvent(el, "mouseout", function () {
			     if (obj.enabled) with (wfxToolbar) {
			       _removeClass(el, "buttonHover");
			       _removeClass(el, "buttonActive");
			       (obj.active) && _addClass(el, "buttonPressed");
			     }
			   });
      wfxToolbar._addEvent(el, "mousedown", function (ev) {
			     if (obj.enabled) with (wfxToolbar) {
			       _addClass(el, "buttonActive");
			       _removeClass(el, "buttonPressed");
			       _stopEvent(wfx.is_ie ? window.event : ev);
			     }
			   });
      // when clicked, do the following:
      wfxToolbar._addEvent(el, "click", function (ev) {
			     if (obj.enabled) with (wfxToolbar) {
			       _removeClass(el, "buttonActive");
			       _removeClass(el, "buttonHover");
//			       alert("obj.cmd: " + obj.cmd);
//			       alert(thistb._editor + ", " + 
//				     obj.name + ", " + obj);
//			       alert("editor: " + thistb._editor);
			       obj.cmd(thistb._editor, obj.name, obj); //XXX
			       _stopEvent(wfx.is_ie ? window.event : ev);
			     }
			   });
      if( btn[1] == "checkbox" ) {
	var checkbox = document.createElement("input");
	checkbox.type = "checkbox";
	checkbox.id = "wfxtoolbar__el__" + btn[0];
	el.appendChild(checkbox);
      } else {
	var img = document.createElement("img");
	img.src = thistb.imgURL(btn[1]);
	img.style.width = "18px";
	img.style.height = "18px";
	el.appendChild(img);
      }
    }

    if (el) {
      var tb_cell = document.createElement("td");
      tb_row.appendChild(tb_cell);
      tb_cell.appendChild(el);
    } else {
      alert("FIXME: Unknown toolbar item: " + txt);
    }
    return el;
  };
  //#*******************************************************************

  var first = true;
  for (var i in this.config.toolbar) {
    if (!first) {
      createButton("linebreak");
    } else {
      first = false;
    }
    var group = this.config.toolbar[i];
    for (var j in group) {
      var code = group[j];
      if (/^([IT])\[(.*?)\]/.test(code)) {
	// special case, create text label
	var l7ed = RegExp.$1 == "I"; // localized?
	var label = RegExp.$2;
	if (l7ed) {
	  label = "XXX"; //HTMLArea.I18N.custom[label];
	}
	var tb_cell = document.createElement("td");
	tb_row.appendChild(tb_cell);
	tb_cell.className = "label";
	tb_cell.innerHTML = label;
      } else {
	if(code=="syntaxhighlighting") {
	  
	} else {
	  createButton(code);
	}
      }
    }
  }
};

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar.prototype.imgURL = function(file) {
  return this.config.toolbarURL + file;
};

// updates enabled/disable/active state of the toolbar elements
//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar.prototype.updateToolbar = function( ancestors ) {
  //  alert("updateToolbar() ...");
  var doc = this._editor._doc;
  var text = (this._editor.editMode == "textmode");

  for (var i in this._toolbarObjects) {
    //    alert("i: " + i);
    var btn = this._toolbarObjects[i];
    var cmd = i;
    var inContext = true;

    if (btn.context && !text) {
      inContext = false;
      var context = btn.context;
      //      alert("btn.context: " + btn.context);
      var attrs = [];
      if (/(.*)\[(.*?)\]/.test(context)) {
	context = RegExp.$1;
	attrs = RegExp.$2.split(",");
      }
      context = context.toLowerCase();
      var match = (context == "*");
      for (var k in ancestors) {
	if (!ancestors[k]) {
	  // the impossible really happens.
	  continue;
	}
	if (match || (ancestors[k].tagName.toLowerCase() == context)) {
	  inContext = true;
	  for (var ka in attrs) {
	    if (!eval("ancestors[k]." + attrs[ka])) {
	      inContext = false;
	      break;
	    }
	  }
	  if (inContext) {
	    break;
	  }
	}
      }
    }

    btn.state("enabled", (!text || btn.text) && inContext);

    if (typeof cmd == "function") {
      continue;
    }

    switch (cmd) {
    default:
      try {
	//	alert("queryCommandState("+cmd+"): "+ doc.queryCommandState(cmd));
	// XXX: tag A ==> "underline" ???
	btn.state("active", (!text && doc.queryCommandState(cmd)));
      } catch (e) {}
    }
  }
}

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar.Config = function () {

	if (typeof _toolbar_url != "undefined") {
		this.toolbarURL = _toolbar_url;
	} else {
		this.toolbarURL = "";
	}

	this.imgURL = "/core/img/editor/";

	/** CUSTOMIZING THE TOOLBAR
	 */

	this.toolbar = [
			[ "undo", "redo" ],
			];

	this.btnList = {
	  undo: [ "Undo", "../../img/wfx/ed_undo.gif", false, function(e) {e.execCommand("undo");} ],
	  redo: [ "Redo", "../../img/wfx/ed_redo.gif", false, function(e) {e.execCommand("redo");} ]
	};
	//	  syntaxrehighlighting: [ "syntaxrehighlighting", "checkbox", false, function(e) {alert("syntaxrehighlighting");} ],
};

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar.Config.prototype.registerButton = function(id, tooltip, image, textMode, action, context) {
	var the_id;
	if (typeof id == "string") {
		the_id = id;
	} else if (typeof id == "object") {
		the_id = id.id;
	} else {
		alert("ERROR [wfxToolbar.Config::registerButton]:\ninvalid arguments");
		return false;
	}
	// check for existing id
	if (typeof this.btnList[the_id] != "undefined") {
		alert("WARNING [wfxToolbar.Config::registerButton]:\nA button with the same ID (" + the_id + ") already exists.");
	}
	switch (typeof id) {
	    case "string": this.btnList[id] = [ tooltip, image, textMode, action, context ]; break;
	    case "object": this.btnList[id.id] = [ id.tooltip, id.image, id.textMode, id.action, id.context ]; break;
	}
};

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar._removeClass = function(el, className) {
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

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar._addClass = function(el, className) {
	// remove the class first, if already there
	wfxToolbar._removeClass(el, className);
	el.className += " " + className;
};

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar._addEvent = function(el, evname, func) {
	if(wfx.is_ie) {
		el.attachEvent("on" + evname, func);
	} else {
		el.addEventListener(evname, func, true);
	}
};

//#****************************************************************************
//#
//#****************************************************************************
wfxToolbar._stopEvent = function(ev) {
	if (wfx.is_ie) {
		ev.cancelBubble = true;
		ev.returnValue = false;
	} else {
		ev.preventDefault();
		ev.stopPropagation();
	}
};
//#****************************************************************************
//#****************************************************************************

// EOF
// Local variables: //
// c-basic-offset:2 //
// indent-tabs-mode:t //
// End: //
