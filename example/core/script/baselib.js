//  Copyright (C) 2001-2003  Schlund + Partner AG
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Array.prototype.mapKeys = function() {
    var c = new Array();
    for (var i = 0; i < this.length; i++) {
        var p = this[i].split(":");
        if (typeof p[0] != "undefined") c[p[0]] = p[1];
    };
    return c;
};

Array.prototype.unmapKeys = function() {
    var c = new Array();
    var i = 0;
    for (attr in this) {
        var t = typeof this[attr];
        if (t != "string" && t != "number") continue;
        c[i] = attr + ":" + this[attr];
        i++;
    };
    return c;
};

var __js_Cookie = {
  // Data for the highlevel functions
    base : "data",
    days : 365,
    splitter : "|",
    path : "/",
    
    // Official specs allows 4000 Bytes/Cookie and 20 Cookies/Page
    maxstr : 4000,
    maxarr : 10,
    
    set : function(name, value) {
        var c   = this.prep_sub();
        c[name] = value;
        var d = c.unmapKeys();
        var s = "";
        var j = 0;
        for (var i=0; i<d.length; i++) {
            if ((s.length + d[i].length) > this.maxstr) {
                this.create(this.base + this.norm_sub(j), s, this.days);
                
                s = d[i] + this.splitter;
                j++;
            } else {
                s += d[i] + this.splitter;
            };
        };
        
        if (s != "") {
            this.create(this.base + this.norm_sub(j), s, this.days);
            j++;
        };

        if (j < this.maxarr)
            for (var i = j; i < this.maxarr; i++) this.erase(this.base + this.norm_sub(i));
    },

    get : function(name) {
        var c = this.prep_sub()[name];
        if (c && typeof c != "undefied") return c;
        return null;
    },

    norm_sub : function(i) {
        return String(i < 10 ? "0" + i : i);
    },

    get_sub : function() {
        var s = "";
        for (var i = 0; i < 20; i++)
        {
            var r = this.read(this.base + this.norm_sub(i))
                if(r) s+=r;
        };
        return s;
    },

    prep_sub : function() {
        return this.get_sub().split(this.splitter).mapKeys();
    },

    // Low Level Internal Functions
    create : function(name, value, days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days*24*60*60*1000));
            var expires = "; expires=" + date.toGMTString();
        } else {
            var expires = "";
        };
        document.cookie = name + "=" + value + expires + "; path=" + this.path;
    },

    read : function(name) {
        var ne = name + "=";
        var ca = document.cookie.split(";");
        
        for (var i=0; i < ca.length; i++) {
            var c = ca[i];
            while (c.charAt(0) == " ")
                c = c.substring(1,c.length);
            if (c.indexOf(ne) == 0)
                return c.substring(ne.length,c.length);
        };
        return null;
    },

    erase : function(name) {
        this.create(name,"",-1);
    }
}

function __js_Browser() {
  // features
  this.all        = document.all ? true : false;
  this.layers     = document.layers ? true : false;
  this.dom        = document.getElementById ? true : false;
  this.dom2events = (document.addEventListener && document.removeEventListener) ? true : false;
  this.dom2core   = (document.createTextNode && document.firstChild && document.getElementsByTagName &&
                     document.createElement && document.createComment) ? true : false;
  this.dom2       = (this.dom2events && this.dom2core) ? true : false;

  // identification
  this.vendor     = navigator.vendor ? navigator.vendor.toLowerCase() : false;
  this.product    = navigator.product ? navigator.product.toLowerCase() : false;

  // display mode
  this.stdmode    = document.compatMode == "CSS1Compat" ? true : false;

  // browser
  this.ic         = (window.ScriptEngine && ScriptEngine().indexOf( 'InScript' ) + 1) ? true : false;

  this.op         = window.opera ? true : false;
  this.op7        = this.op && this.dom2;
  this.op6        = this.op && !this.op7

  this.kq         = (this.vendor == "kde") ? true : false;
  this.sf         = (this.mac && this.vendor == "kde") ? true : false;
  this.gk         = (this.dom2 && document.defaultView && this.product == "gecko") ? true : false;

  this.ie         = (document.all && !this.kq && !this.op) ? true : false;
  this.ie6        = (this.ie && this.dom2 && document.fireEvent && document.createComment) ? true : false;
  this.ie55       = (this.ie && document.fireEvent && !this.dom2) ? true : false;
  this.ie5        = (this.ie && !document.fireEvent) ? true : false
  // We don't detect this so we assume no :-(
  this.ie5mac     = false; 
  this.ie4        = (this.ie && !this.ie6 && !this.ie55 && !this.ie5 && !this.ie5mac) ? true : false;
}

__Browser = new __js_Browser();

//
// BUTTONS
//

__js_allButtons = new Array();

function __js_button(pic, picomo) {
    this.pic = new Image();
    this.pic.src = pic;
    this.pic_active = new Image();
    this.pic_active.src = picomo;
}

function __js_moveover(doc, id) {
    doc[id].src = __js_allButtons[id].pic_active.src; 
}

function __js_moveout(doc, id) {
    doc[id].src = __js_allButtons[id].pic.src;
}

//
// LAYERS
//

var __js_allLayers  = new Array();

function __js_getLayer(id) {
    if (__js_allLayers[id] != null) {
        return __js_allLayers[id];
    } else {
        var temp = new __js_Layer(id);
        __js_allLayers[id] = temp;
        return temp;
    }
}

function __js_toggleLayers() {
    for (var i = 0; i < __js_toggleLayers.arguments.length; i++) {
        var layer = __js_getLayer(__js_toggleLayers.arguments[i]);
        if (layer.visible) {
            layer.hide();
            layer.hideChildren();
        } else {
            layer.show();
        }
    }
}

function __js_showLayers() {
    for (var i = 0; i < __js_showLayers.arguments.length; i++) {
        var layer = __js_getLayer(__js_showLayers.arguments[i]);
        if (!layer.visible) {
            layer.show();
        }
    }
}

function __js_hideLayers() {
    for (var i = 0; i < __js_hideLayers.arguments.length; i++) {
        var layer = __js_getLayer(__js_hideLayers.arguments[i]);
        if (layer.visible) {
            layer.hide();
            layer.hideChildren();
        }
    }
}

function __js_Layer(layer_id) {
    this.initialized = false;
    this.parents     = new Array();
    this.children    = new Array(); 
    this.switch_on   = new Array();
    this.switch_off  = new Array();
    this.input       = new Array();
    this.visible     = true;
    this.frame       = null;
    this.store       = null;
    this.id          = layer_id;
    this.element     = null;

    this.init = function (visible, frame, store) {
        this.store   = store;
        this.frame   = frame;
        this.element = frame.document.getElementById(this.getId());
        
        var fromcookie =  "nocookie";
        if (navigator.cookieEnabled == true && store != "false") {
            fromcookie = __js_Cookie.get(this.getId());;
        }
        if (fromcookie == "true") {
            this.show();
        } else if (fromcookie == "false") {
            this.hide();
        } else {
            if (visible == 'false') {
                this.hide();
            } else {
                this.show();
            }
        }
        this.initialized = true;
    };
    
    this.getId       = function () { return this.id; };
    this.getFrame    = function () { return this.frame; };
    this.getChildren = function () { return this.children; };
    this.getParents  = function () { return this.parents; };

    this.addChild    = function (child) { this.children[this.children.length] = child; };
    this.addParent   = function (parent) { this.parents[this.parents.length] = parent; };

    this.show     = function () { this.__change(true, '',      'none'); };
    this.hide     = function () { this.__change(false, 'none', ''    ); };

    this.__change = function (cookievis, elemvis, switchoffvis) {
        if (navigator.cookieEnabled == true && this.store != "false") {
            __js_Cookie.set(this.getId(), String(cookievis));
        }
        this.element.style.display = elemvis;
        this.visible = cookievis;
        if (this.switch_off != null && this.switch_off.length > 0) {
            for (var i = 0; i < this.switch_off.length; i++) {
                var temp = this.switch_off[i];
                temp.style.display = switchoffvis;
            }
        }
        if (this.switch_on != null && this.switch_on.length > 0) {
            for (var i = 0; i < this.switch_on.length; i++) {
                var temp = this.switch_on[i];
                temp.style.display = elemvis;
            }
        }
        if (this.input != null && this.input.length > 0) {
            for (var i = 0; i < this.input.length; i++) {
                var temp = this.input[i];
                temp.checked = cookievis;
            }
        }
    };

    this.hideChildren = function () {
        if (this.children != null && this.children.length > 0) {
            for (var i = 0; i < this.children.length; i++) {
                var temp = this.children[i];
                temp.hide();
                temp.hideChildren();
            }
        }
    };
    
    this.addSwitchOn  = function (switchon) {
        this.switch_on[this.switch_on.length] = switchon;
        if (this.initialized && !this.visible) {
            switchon.style.display = 'none';
        }
    };

    this.addSwitchOff = function (switchoff) { 
        this.switch_off[this.switch_off.length] = switchoff;
        if (this.initialized && this.visible) {
            switchoff.style.display = 'none';
        }
    };

    this.addInputElem  = function (inputelem) {
        this.input[this.input.length] = inputelem;
        if (this.initialized && !this.visible) {
            inputelem.checked = 'false';
        }
    };

    this.moveRight  = function (val) { this.element.style.right = val; };
    this.moveBottom = function (val) { this.element.style.bottom = val; };
    this.moveLeft   = function (val) { this.element.style.left = val; };
    this.moveTop    = function (val) { this.element.style.top = val; };

    this.getRight   = function () { return this.element.style.right; };
    this.getBottom  = function () { return this.element.style.bottom; };
    this.getLeft    = function () { return this.element.style.left; };
    this.getTop     = function () { return this.element.style.top; };

}


// PROGRESS BAR

var __js_progress_begin     = 1;
var __js_progress_handler   = 0;
var __js_progress_frame_idx = 0;

function __js_progress_start_show_work(index) {
    __js_progress_frame_idx = index;
    top.frames[__js_progress_frame_idx].captureEvents(Event.KEYPRESS);
    top.frames[__js_progress_frame_idx].onkeypress = __js_progress_user_abort;
    __js_progress_handler = window.setInterval('__js_progress_show_work()',100);
    obj  = top.frames[__js_progress_frame_idx].document.getElementById("core_progress_wait");
    if (obj != null) {
        obj.style.visibility = "visible";
    }
}

function __js_progress_show_work() {
    obj = top.frames[__js_progress_frame_idx].document.getElementById("core_progress_bar");
    if (obj != null) {
        __js_progress_begin += 2;
        if (__js_progress_begin > 100) { 
            __js_progress_begin = 100;
            window.clearInterval(__js_progress_handler);
        }
    
        obj.style.width = __js_progress_begin + "%";
    }
    return true;
}

function __js_progress_user_abort(Event) {
    obj  = top.frames[__js_progress_frame_idx].document.getElementById("core_progress_wait");
    obj2 = top.frames[__js_progress_frame_idx].document.getElementById("core_progress_bar");
    obj.style.visibility = "hidden";
    obj2.style.width = "0%";
     __js_progress_begin = 1;
    window.clearInterval(__js_progress_handler);
}

//
// Misc 
//

function __js_getFormElement(obj, formindex, elemname) {
    var form = obj.forms[formindex];
    for (var i = 0; i < form.length; i++) {
        if (form[i].name == elemname) { 
            return form[i];
        }
    }
}

function __js_getFormElementWithValue(obj, formindex, elemname, elemvalue) {
    var form = obj.forms[formindex];
    for (var i = 0; i < form.length; i++) {
        if (form[i].name == elemname && form[i].value == elemvalue) { 
            return form[i];
        }
    }
}

function __js_popup(href, title, width, height) {
    child=window.open(href, title,'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width='+width+',height='+height+',screenX=100,screenY=100');
}

// This method automates a reload into the _top frame
function __js_switchFrame() {
    if(self.location!=top.location) {
        top.location.href=self.location.href;
    }
}
