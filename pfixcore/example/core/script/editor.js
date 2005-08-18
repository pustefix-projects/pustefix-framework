var editor_includes_sidebar_state = new Array();

function editor_includes_sidebar_showNoSave(id) {
  var elem = document.getElementById(id);
  for (var i = 0; i < elem.childNodes.length; i++) {
    var child = elem.childNodes[i];
    if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_file") {
      child.style.display = "block";
    } else if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_directory_title") {
      for (var j = 0; j < child.childNodes.length; j++) {
        var button = child.childNodes[j];
        if (button.nodeType == 1 && button.getAttribute("class") == "editor_includes_sidebar_directory_showbutton") {
          button.style.display = "none";
        } else if (button.nodeType == 1 && button.getAttribute("class") == "editor_includes_sidebar_directory_hidebutton") {
          button.style.display = "inline";
        }
      }
    }
  }
}

function editor_includes_sidebar_hideNoSave(id) {
  var elem = document.getElementById(id);
  for (var i = 0; i < elem.childNodes.length; i++) {
    var child = elem.childNodes[i];
    if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_file") {
      child.style.display = "none";
    } else if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_directory_title") {
      for (var j = 0; j < child.childNodes.length; j++) {
        var button = child.childNodes[j];
        if (button.nodeType == 1 && button.getAttribute("class") == "editor_includes_sidebar_directory_showbutton") {
          button.style.display = "inline";
        } else if (button.nodeType == 1 && button.getAttribute("class") == "editor_includes_sidebar_directory_hidebutton") {
          button.style.display = "none";
        }
      }
    }
  }
}

function editor_includes_sidebar_show(id) {
  editor_includes_sidebar_showNoSave(id);
  editor_includes_sidebar_state[id] = 1;
  editor_includes_sidebar_saveState();
}

function editor_includes_sidebar_hide(id) {
  editor_includes_sidebar_hideNoSave(id);
  editor_includes_sidebar_state[id] = 2;
  editor_includes_sidebar_saveState();
}

function editor_includes_sidebar_saveState() {
  var serialized = "";
  for (var id in editor_includes_sidebar_state) {
    serialized = serialized.concat(id + "|" + editor_includes_sidebar_state[id] + "#");
  }
  document.cookie = "pustefix_editor=" + escape(serialized);
  // __js_Cookie.set("editor_includes_sidebar_state", serialized);
}

function editor_includes_sidebar_loadState() {
  // var serialized = __js_Cookie.get("editor_includes_sidebar_state");
  var dc = document.cookie;
  var prefix = "pustefix_editor" + "=";
  var begin = dc.indexOf("; " + prefix);
  if (begin == -1) {
    begin = dc.indexOf(prefix);
    if (begin != 0) return null;
  } else {
    begin += 2;
  }
  var end = document.cookie.indexOf(";", begin);
  if (end == -1) {
    end = dc.length;
  }
  var serialized = unescape(dc.substring(begin + prefix.length, end));
  if (serialized) {
    while (serialized.length != 0) {
      var id = serialized.substring(0, serialized.indexOf("|"));
      serialized = serialized.substring(id.length + 1, serialized.length);
      var value = serialized.substring(0, serialized.indexOf("#"));
      serialized = serialized.substring(value.length + 1, serialized.length);
      editor_includes_sidebar_state[id] = value;
    }
  }
}

function editor_includes_sidebar_init() {
  editor_includes_sidebar_loadState();
  var elem = document.getElementById("editor_includes_sidebar_content");
  for (var i = 0; i < elem.childNodes.length; i++) {
    var child = elem.childNodes[i];
    if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_directory") {
      var id = child.getAttribute("id");
      if (id) {
        if (editor_includes_sidebar_state[id] && editor_includes_sidebar_state[id] == 1) {
          editor_includes_sidebar_showNoSave(id);
        } else {
          editor_includes_sidebar_hideNoSave(id);
        }
      }
    }
  }
}

function editor_includes_sidebar_showAll() {
  var elem = document.getElementById("editor_includes_sidebar_content");
  for (var i = 0; i < elem.childNodes.length; i++) {
    var child = elem.childNodes[i];
    if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_directory") {
      var id = child.getAttribute("id");
      if (id) {
        editor_includes_sidebar_show(id);
      }
    }
  }
}

function editor_includes_sidebar_hideAll() {
  var elem = document.getElementById("editor_includes_sidebar_content");
  for (var i = 0; i < elem.childNodes.length; i++) {
    var child = elem.childNodes[i];
    if (child.nodeType == 1 && child.getAttribute("class") == "editor_includes_sidebar_directory") {
      var id = child.getAttribute("id");
      if (id) {
        editor_includes_sidebar_hide(id);
      }
    }
  }
}
