var domElement = document.getElementsByTagName("li")[0],
    domElements = document.getElementsByTagName("li"),
    nodeElements = document.getElementsByTagName("a"),
    xpath = document.getElementById("xpath"),
    collapse = document.getElementById("collapse"),
    expand = document.getElementById("expand");

for (var x = 0; x < nodeElements.length; x+=1) {
  if (nodeElements[x].addEventListener) {
    nodeElements[x].addEventListener("click", function(e) { getXpathExpression(e.target); }, false);
  } else {
    nodeElements[x].attachEvent("onclick", function(e) { getXpathExpression(e.srcElement); }, false);
  }
}

if (collapse.addEventListener) {
  collapse.addEventListener("click", function(e) { collapseAll(); }, false);
} else {
  collapse.attachEvent("onclick", function(e) { collapseAll(); }, false);
}

if (expand.addEventListener) {
  expand.addEventListener("click", function(e) { expandAll(); }, false);
} else {
  expand.attachEvent("onclick", function(e) { expandAll(); }, false);
}

if (domElement.addEventListener) {
  domElement.addEventListener("dblclick", function(e) { toggleDomElement(e.target); }, false);
} else {
  domElement.attachEvent("ondblclick", function(e) { toggleDomElement(e.srcElement); }, false);
}

function toggleDomElement(element) {

  while (element != null && element.parentNode != null && element.tagName != "LI") {
    element = element.parentNode;
  }
  
  if (element.getElementsByTagName("ul").length && element.className.indexOf('formesult') == -1) {
    element.className = (element.className == "expanded" ? "collapsed" : "expanded");
  }
  
}

function getXpathExpression(element) {
  if (element.title != "") {
    xpath.value = element.title;
  }
}

function collapseAll() {
  for (var x = 0; x < domElements.length; x+=1) {
    if (domElements[x].className == "expanded") {
      domElements[x].className = "collapsed";
    }
  }
}

function expandAll() {
  for (var x = 0; x < domElements.length; x+=1) {
    if (domElements[x].className == "collapsed") {
      domElements[x].className = "expanded";
    }
  }
}
