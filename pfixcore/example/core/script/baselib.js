var __js_ok = false;
__js_but = new Array();
if ( navigator.appName.substring(0,9) == "Microsoft" &&
        parseInt(navigator.appVersion) >= 4 ) __js_ok = true;
if ( navigator.appName.substring(0,8) == "Netscape" &&
        parseInt(navigator.appVersion) >= 3 ) __js_ok = true;

function __js_button(pic, picomo) {
        if (__js_ok) {
                this.pic = new Image();
                this.pic.src = pic;
                this.pic_active = new Image();
                this.pic_active.src = picomo;
        }
}

function __js_moveover(doc, id) {
        if (__js_ok) {
                doc[id].src = __js_but[id].pic_active.src; 
        }
}

function __js_moveout(doc, id) {
        if (__js_ok) {
                doc[id].src = __js_but[id].pic.src;
        }
}


function __js_popup(href, title, width, height) {
    if (__js_ok) {
        child=window.open(href, title,'toolbar=no,location=no,status=no,menubar=no,scrollbars=yes,resizable=yes,width='+width+',height='+height+',screenX=100,screenY=100');
    }
}


// This method automates a reload into the _top frame
function __js_switchFrame() {
    if(self.location!=top.location) {
        top.location.href=self.location.href;
    }
}




var __js_progress_begin   = 1;
var __js_progress_handler = 0;
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
// COOKIES
//


function __js_CookieMgr(cookies) {
    

}


//
// LAYERS
//

var __js_allLayers  = new Array();

function __js_registerLayerChild(parent_id, child_id) {
    var parent = __js_getLayer(parent_id);
    var child  = __js_getLayer(child_id);
    parent.addChild(child);
    child.addParent(parent);
}

function __js_registerSwitchOn(layer_id, switch_elem) {
    var layer = __js_getLayer(layer_id);
    layer.addSwitchOn(switch_elem);
}

function __js_registerSwitchOff(layer_id, switch_elem) {
    var layer = __js_getLayer(layer_id);
    layer.addSwitchOff(switch_elem);
}

function __js_toggleLayer() {
    for (var i = 0; i < __js_toggleLayer.arguments.length; i++) {
        var layer = __js_getLayer(__js_toggleLayer.arguments[i]);
        if (layer.visible) {
            layer.hide();
            __js_hideChildren(layer);
        } else {
            layer.show();
        }
    }
    // alert(document.cookie);
}

function __js_getLayer(id) {
    if (__js_allLayers[id] != null) {
        return __js_allLayers[id];
    } else {
        var temp = new __js_Layer(id);
        __js_allLayers[id] = temp;
        return temp;
    }
}

//
// These all  are meant to be private
// 

function __js_Layer(layer_id) {
    this.parents     = new Array();
    this.children    = new Array(); 
    this.switch_on   = new Array();
    this.switch_off  = new Array();
    this.visible     = true;
    this.frame       = null;
    this.store       = null;
    this.id          = layer_id;

    this.init         = __js_Layer_init;
    this.getId        = __js_Layer_getId;
    this.getFrame     = __js_Layer_getFrame;
    this.show         = __js_Layer_show;
    this.hide         = __js_Layer_hide;
    this.addChild     = __js_Layer_addChild;
    this.addParent    = __js_Layer_addParent;
    this.addSwitchOn  = __js_Layer_addSwitchOn;
    this.addSwitchOff = __js_Layer_addSwitchOff;
    this.getChildren  = __js_Layer_getChildren;
    this.getParents   = __js_Layer_getParents;
    this.moveTop      = __js_Layer_moveTop; 
    this.moveBottom   = __js_Layer_moveBottom; 
    this.moveLeft     = __js_Layer_moveLeft; 
    this.moveRight    = __js_Layer_moveRight; 
    this.getLeft      = __js_Layer_getLeft; 
    this.getRight     = __js_Layer_getRight; 
    this.getTop       = __js_Layer_getTop; 
    this.getBottom    = __js_Layer_getBottom; 

}

function __js_Layer_checkCookies(id) {
    if (navigator.cookieEnabled == true && this.store != "false") {
        var cookie = document.cookie;
        if (cookie.indexOf("LR_" + id + "=true") >= 0) {
            return "true";
        } else if (cookie.indexOf("LR_" + id + "=false") > 0) {
            return "false";
        }
    }
    return "nocookie";
}

function __js_Layer_init(visible, frame, store) {
    this.store = store;
    this.frame = frame;
    var fromcookie = __js_Layer_checkCookies(this.getId());
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
}

function __js_Layer_getId() {
    return this.id;
}

function __js_Layer_getFrame() {
    return this.frame;
}

function __js_Layer_moveRight(val) {
    this.getFrame().document.getElementById(this.getId()).style.right = val;
}

function __js_Layer_moveBottom(val) {
    this.getFrame().document.getElementById(this.getId()).style.bottom = val;
}

function __js_Layer_moveLeft(val) {
    this.getFrame().document.getElementById(this.getId()).style.left = val;
}

function __js_Layer_moveTop(val) {
    this.getFrame().document.getElementById(this.getId()).style.top = val;
}

function __js_Layer_getRight() {
    return this.getFrame().document.getElementById(this.getId()).style.right;
}

function __js_Layer_getBottom() {
    return this.getFrame().document.getElementById(this.getId()).style.bottom;
}

function __js_Layer_getLeft() {
    return this.getFrame().document.getElementById(this.getId()).style.left;
}

function __js_Layer_getTop() {
    return this.getFrame().document.getElementById(this.getId()).style.top;
}

function __js_Layer_show() {
    if (navigator.cookieEnabled == true && this.store != "false") {
        document.cookie = "LR_" + this.getId() + "=" + "true; path=/";
    }
    this.getFrame().document.getElementById(this.getId()).style.display = 'block';
    this.visible = true;
    if (this.switch_off != null && this.switch_off.length > 0) {
        for (var i = 0; i < this.switch_off.length; i++) {
            var temp = this.switch_off[i];
            temp.style.display = 'none';
        }
    }
    if (this.switch_on != null && this.switch_on.length > 0) {
        for (var i = 0; i < this.switch_on.length; i++) {
            var temp = this.switch_on[i];
            temp.style.display = 'inline';
        }
    }
}

function __js_Layer_hide() {
    if (navigator.cookieEnabled == true && this.store != "false") {
        document.cookie = "LR_" + this.getId() + "=" + "false; path=/";
    }
    this.getFrame().document.getElementById(this.getId()).style.display = 'none';
    this.visible = false;
    if (this.switch_off != null && this.switch_off.length > 0) {
        for (var i = 0; i < this.switch_off.length; i++) {
            var temp = this.switch_off[i];
            temp.style.display = 'inline';
        }
    }
    if (this.switch_on != null && this.switch_on.length > 0) {
        for (var i = 0; i < this.switch_on.length; i++) {
            var temp = this.switch_on[i];
            temp.style.display = 'none';
        }
    }
}

function __js_Layer_addChild(child) {
    this.children[this.children.length] = child;
}

function __js_Layer_addParent(parent) {
    this.parents[this.parents.length] = parent;
}

function __js_Layer_addSwitchOn(switchon) {
    this.switch_on[this.switch_on.length] = switchon;
}

function __js_Layer_addSwitchOff(switchoff) {
    this.switch_off[this.switch_off.length] = switchoff;
}

function __js_Layer_getChildren() {
    return this.children;
}

function __js_Layer_getParents() {
    return this.parents;
}

function __js_hideChildren(layer) {
    var children = layer.getChildren();
    if (children != null && children.length > 0) {
        for (var i = 0; i < children.length; i++) {
            var temp = children[i];
            temp.hide();
            __js_hideChildren(temp);
        }
    }
}

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




// End of private section
