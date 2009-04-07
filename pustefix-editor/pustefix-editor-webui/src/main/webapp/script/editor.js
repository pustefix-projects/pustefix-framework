function pfx_editor_TreeRoot(element)  {
  this._element = element;
  this._dirs = new Array();
}

pfx_editor_TreeRoot.prototype.appendDir = function(dir) {
  this._element.appendChild(dir.getElement());
  this._dirs.push(dir);
}

pfx_editor_TreeRoot.prototype.getDir = function(dirname) {
  for (i = 0; i < this._dirs.length; i++) {
    if (this._dirs[i].getName() == dirname) {
      return this._dirs[i];
    }
  }
  return false;
}

pfx_editor_TreeRoot.prototype.getFile = function(filename) {
  var dirname;
  if (filename.lastIndexOf("/") == -1) {
    dirname = "/";
  } else {
    var dirname = filename.substring(0, filename.lastIndexOf("/"));
  }
  var dir = this.getDir(dirname);
  if (dir) {
    return dir.getFile(filename);
  } else {
    return false;
  }
}

pfx_editor_TreeRoot.prototype.getAllDirs = function() {
  return this._dirs;
}


function pfx_editor_Directory(dirname) {
  this._name = dirname;
  this._files = new Array();
  
  this._expanded = false;
  
  // Create HTML
  this._element = document.createElement("div");
  this._element.className = "editor_includes_sidebar_directory";
  var title = document.createElement("div");
  title.className = "editor_includes_sidebar_directory_title";
  this._elementExpandButton = document.createElement("span");
  this._elementExpandButton.className = "editor_includes_sidebar_directory_showbutton";
  this._elementExpandButton.onclick = function() {
    pfx_editor_expandDirectory(dirname);
  }
  this._elementExpandButton.appendChild(document.createTextNode("+"));
  title.appendChild(this._elementExpandButton);
  this._elementCollapseButton = document.createElement("span");
  this._elementCollapseButton.className = "editor_includes_sidebar_directory_hidebutton";
  this._elementCollapseButton.onclick = function() {
    pfx_editor_collapseDirectory(dirname);
  }
  this._elementCollapseButton.style.display = "none";
  this._elementCollapseButton.appendChild(document.createTextNode("-"));
  title.appendChild(this._elementCollapseButton);
  title.appendChild(document.createTextNode(" "));
  title.appendChild(document.createTextNode("Dir: "));
  var title2 = document.createElement("b");
  title2.appendChild(document.createTextNode(dirname));
  title.appendChild(title2);
  this._element.appendChild(title);
  this._elementContainer = document.createElement("div");
  this._elementContainer.style.display = "none";
  this._element.appendChild(this._elementContainer);
  
}

pfx_editor_Directory.prototype.getName = function() {
  return this._name;
}

pfx_editor_Directory.prototype.appendFile = function(file) {
  this._elementContainer.appendChild(file.getElement());
  this._files.push(file);
}

pfx_editor_Directory.prototype.clearFiles = function() {
  for (i = 0; i < this._files.length; i++) {
    this._elementContainer.removeChild(this._files[i].getElement());
  }
  this._files = new Array();
}

pfx_editor_Directory.prototype.getFile = function(filename) {
  for (i = 0; i < this._files.length; i++) {
    if (this._files[i].getName() == filename) {
      return this._files[i];
    }
  }
  return false;   
}

pfx_editor_Directory.prototype.getElement = function() {
  return this._element;
}

pfx_editor_Directory.prototype.isExpanded = function() {
  return this._expanded;
}

pfx_editor_Directory.prototype.expand = function() {
  this._expanded = true;
  this._elementExpandButton.style.display = "none";
  this._elementCollapseButton.style.display = "inline";
  this._elementContainer.style.display = "block";
}

pfx_editor_Directory.prototype.collapse = function() {
  this._expanded = false;
  this._elementExpandButton.style.display = "inline";
  this._elementCollapseButton.style.display = "none";
  this._elementContainer.style.display = "none";
  for (i = 0; i < this._files.length; i++) {
    this._files[i].collapse();
  }    
}


function pfx_editor_File(filename) {
  this._expanded = false;
  this._name = filename;
  this._parts = new Array();
  
  // Create HTML
  this._element = document.createElement("div");
  this._element.className = "editor_includes_sidebar_file";
  this._elementExpandButton = document.createElement("span");
  this._elementExpandButton.className = "editor_includes_sidebar_file_showbutton";
  this._elementExpandButton.onclick = function() {
    pfx_editor_expandFile(filename);
  }
  this._elementExpandButton.appendChild(document.createTextNode("+"));
  this._element.appendChild(this._elementExpandButton);
  this._elementCollapseButton = document.createElement("span");
  this._elementCollapseButton.className = "editor_includes_sidebar_file_hidebutton";
  this._elementCollapseButton.onclick = function() {
    pfx_editor_collapseFile(filename);
  }
  this._elementCollapseButton.style.display = "none";
  this._elementCollapseButton.appendChild(document.createTextNode("-"));
  this._element.appendChild(this._elementCollapseButton);
  this._element.appendChild(document.createTextNode(" "));
  var htm = document.createElement("b");
  htm.appendChild(document.createTextNode(filename.substring(filename.lastIndexOf("/") + 1, filename.length)));
  this._element.appendChild(htm);
  this._elementContainer = document.createElement("div");
  this._elementContainer.style.display = "none";
  this._element.appendChild(this._elementContainer);
}

pfx_editor_File.prototype.getName = function() {
  return this._name;
}

pfx_editor_File.prototype.expand = function() {
  this.expanded = true;
  this._elementExpandButton.style.display = "none";
  this._elementCollapseButton.style.display = "inline";
  this._elementContainer.style.display = "block";   
}

pfx_editor_File.prototype.collapse = function() {
  this.expanded = false;
  this._elementExpandButton.style.display = "inline";
  this._elementCollapseButton.style.display = "none";
  this._elementContainer.style.display = "none";
}

pfx_editor_File.prototype.appendPart = function(part) {
  this._elementContainer.appendChild(part.getElement());
  this._parts.push(part);
}

pfx_editor_File.prototype.clearParts = function() {
  for (i = 0; i < this._parts.length; i++) {
    this._elementContainer.removeChild(this._parts[i].getElement());
  }
  this._parts = new Array();
}

pfx_editor_File.prototype.getElement = function() {
  return this._element;
}


function pfx_editor_Part(pFile, pPart, pTheme, isSelected) {
  this._file = pFile;
  this._part = pPart;
  this._theme = pTheme;
  
  // Create HTML
  this._element = document.createElement("div");
  if (isSelected) {
    this._element.className = "editor_includes_sidebar_part_selected";
  } else {
    this._element.className = "editor_includes_sidebar_part";
  }
  this._element.appendChild(document.createTextNode(String.fromCharCode(160, 160, 160, 160)));
  var link = document.createElement("a");
  var url = pfx_editor_contextpath + "/xml/main/" + pfx_editor_pagename + ";"
    + pfx_editor_sessid 
    + "?__frame=_top&__sendingdata=1&selectinclude.Path=" + this._file
    + "&selectinclude.Part=" + this._part
    + "&selectinclude.Theme=" + this._theme
    + "&__CMD[" + pfx_editor_pagename 
    + "]:SELWRP=selectinclude&__CMD[" + pfx_editor_pagename + "]:SELWRP=upload";
  link.setAttribute("href", url);
  link.setAttribute("target", "_top");
  link.appendChild(document.createTextNode(this._part));
  this._element.appendChild(link);
  this._element.appendChild(document.createTextNode(" (" + this._theme + ")"));
}

pfx_editor_Part.prototype.getFile = function() {
  return this._file;
}

pfx_editor_Part.prototype.getPart = function() {
  return this._part;
}

pfx_editor_Part.prototype.getTheme = function() {
  return this._theme;
}

pfx_editor_Part.prototype.getElement = function() {
  return this._element;
}


function pfx_editor_expandDirectory(dirname) {
  var req = new pfx_editor_XMLHttpRequest();
  req.openDirectoryTree(dirname);
}

function pfx_editor_collapseDirectory(dirname) {
  var req = new pfx_editor_XMLHttpRequest();
  req.closeDirectoryTree(dirname);
}

function pfx_editor_expandFile(filename) {
  var req = new pfx_editor_XMLHttpRequest();
  req.openFileTree(filename);
}

function pfx_editor_collapseFile(filename) {
  var req = new pfx_editor_XMLHttpRequest();
  req.closeFileTree(filename);
}

function pfx_editor_expandAllDirectories() {
  var dirs = pfx_editor_inc_root.getAllDirs();
  for (i = 0; i < dirs.length; i++) {
    if (!dirs[i].isExpanded()) {
      pfx_editor_expandDirectory(dirs[i].getName());
    }
  }
}

function pfx_editor_collapseAllDirectories() {
  var dirs = pfx_editor_inc_root.getAllDirs();
  for (i = 0; i < dirs.length; i++) {
    if (dirs[i].isExpanded()) {
      pfx_editor_collapseDirectory(dirs[i].getName());
    }
  }
}

function pfx_editor_XMLHttpRequest() {
  if (window.XMLHttpRequest) {
    try {
      this._req = new XMLHttpRequest();
    } catch (e) {
      this._req = false;
    }
  } else if (window.ActiveXObject) {
    try {
      this._req = new ActiveXObject("Msxml2.XMLHTTP");
    } catch (e) {
      try {
        this._req = new ActiveXObject("Microsoft.XMLHTTP");
      } catch (e) {
        this._req = false;
      }
    }
  }
  
  var self = this;
  
  if (this._req) {
    this._req.onreadystatechange = function() {
      if (self._req.readyState == 4) {
        if (self._req.status == 200 && self._req.responseXML) {
          self._callback();
        } else {
          window.alert("XMLHttpRequest failed!");
        }
        pfx_editor_actionCounter.decrease();
      }
    }
  }
}

pfx_editor_XMLHttpRequest.prototype._sendRequest = function(action, params) {
  this._req.open("GET", pfx_editor_contextpath + "/xml/main/ws_" + pfx_editor_pagename + ";" + pfx_editor_sessid + "?action=" + action + "&" + params, true);
  this._req.send("");
  pfx_editor_actionCounter.increase();
}

pfx_editor_XMLHttpRequest.prototype.openDirectoryTree = function(dirname) {
  this._callback = function() {
    var files = this._req.responseXML.documentElement.childNodes;
    var dir = pfx_editor_inc_root.getDir(dirname);
    dir.clearFiles();
    for (i = 0; i < files.length; i++) {
      var file = new pfx_editor_File(files[i].getAttribute("path"));
      dir.appendFile(file);
    }
    dir.expand();
  }
  this._sendRequest("openDirectoryTree", "dir=" + dirname);
}

pfx_editor_XMLHttpRequest.prototype.closeDirectoryTree = function(dirname) {
  this._callback = function() {
    var dir = pfx_editor_inc_root.getDir(dirname);
    dir.collapse();
  }
  this._sendRequest("closeDirectoryTree", "dir=" + dirname);
}

pfx_editor_XMLHttpRequest.prototype.openFileTree = function(filename) {
  this._callback = function() {
    var parts = this._req.responseXML.documentElement.childNodes;
    var file = pfx_editor_inc_root.getFile(filename);
    file.clearParts();
    for (i = 0; i < parts.length; i++) {
      var part = new pfx_editor_Part(
        parts[i].getAttribute("file"),
        parts[i].getAttribute("part"),
        parts[i].getAttribute("theme"));
      file.appendPart(part);
    }
    file.expand();
  }
  this._sendRequest("openFileTree", "file=" + filename);
}

pfx_editor_XMLHttpRequest.prototype.closeFileTree = function(filename) {
  this._callback = function() {
    var file = pfx_editor_inc_root.getFile(filename);
    file.collapse();
  }
  this._sendRequest("closeFileTree", "file=" + filename);
}


var pfx_editor_actionCounter = new Object();

pfx_editor_actionCounter.count = 0;

pfx_editor_actionCounter.increase = function() {
  this.count++;
  document.getElementsByTagName("body")[0].style.cursor = "wait";
}

pfx_editor_actionCounter.decrease = function() {
  this.count--;
  if (this.count == 0) {
    document.getElementsByTagName("body")[0].style.cursor = "auto";
  }
}
