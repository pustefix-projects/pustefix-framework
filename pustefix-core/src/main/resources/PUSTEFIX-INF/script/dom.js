/* DOM */

var currentLocation = document.URL.replace('xmlonly=1','xmlonly=3'),
    xpath = document.getElementById("xpath"),
    xml = loadXMLDoc(currentLocation),
    autocompletion = document.getElementById("autocompletion");
    
    
 // ie check
 
 if (!xml.evaluate) {
  xpath.className = '';
}

if (xpath.addEventListener) {
  xpath.addEventListener("keydown", function(e) { xPathChecker(); }, false);
  xpath.addEventListener("keyup", function(e) { xPathChecker(); }, false);
  xpath.addEventListener("mouseout", function(e) { hideAutoCompletion(); }, false);
  xpath.addEventListener("change", function(e) { hideAutoCompletion(); xPathChecker }, false);
} else {
  xpath.attachEvent("onkeydown", function(e) { xPathChecker(); }, false);
  xpath.attachEvent("onkeyup", function(e) { xPathChecker(); }, false);
  xpath.attachEvent("onmouseout", function(e) { hideAutoCompletion(); }, false);
  xpath.attachEvent("onchange", function(e) { hideAutoCompletion(); xPathChecker }, false);
}


if (autocompletion.addEventListener) {
  autocompletion.addEventListener("mouseover", function(e) { showAutoCompletion(); }, false);
  autocompletion.addEventListener("mouseout", function(e) { hideAutoCompletion(); }, false);
} else {
  autocompletion.attachEvent("onmouseover", function(e) { showAutoCompletion(); }, false);
  autocompletion.attachEvent("onmouseout", function(e) { hideAutoCompletion(); }, false);
}

function getXpathExpression(element) {
  if (element.title != "") {
    xpath.value = element.title;
    xPathChecker();
    autocompletion.style.display = "none";
  }
}


function hideAutoCompletion() {
  autocompletion.style.display = "none";
}
function showAutoCompletion() {
  autocompletion.style.display = "block";
}

function loadXMLDoc(dname) {

  if (window.XMLHttpRequest) {
    xhttp = new XMLHttpRequest();
  } else {
    xhttp=new ActiveXObject("Microsoft.XMLHTTP");
  }
  
  xhttp.open("GET",dname,false);
  xhttp.send("");
  
  return xhttp.responseXML;
  
}

function contains(a, obj) {
  for (var i = 0; i < a.length; i++) {
      if (a[i] === obj) {
          return true;
      }
  }
  return false;
}

function xPathChecker() {

  if (checkXPathValidation()) {
    xpath.className = "valid";
  } else {
    xpath.className = "invalid";
  }
  
  getAttributes();
  getChildNodes();
  
}

function getAttributes() {

  fullExpression = xpath.value,
  lastChar = xpath.value[xpath.value.length - 1];
  
  if (fullExpression.lastIndexOf("/") > 0 && fullExpression.lastIndexOf("]") < 0 && fullExpression.lastIndexOf("[") < 0) {
    fullExpression = fullExpression.substr(0, fullExpression.lastIndexOf("/"));
  } 
 
  if (fullExpression.lastIndexOf("@") > 0) {
    fullExpression = fullExpression.substr(0, fullExpression.lastIndexOf("@"));
  } 
  
  if (fullExpression.lastIndexOf("[") > 0) {
    fullExpression = fullExpression.substr(0, fullExpression.lastIndexOf("["));
  }
  
  if (lastChar == '@') {
  
    try {
    
      var iterator = xml.evaluate(fullExpression, xml, null, XPathResult.ANY_TYPE, null),
          thisNode = iterator.iterateNext(),
          allAttributes = [];
          
      while (thisNode) {
      
      attributes = thisNode.attributes;
        
        if (attributes.length > 0) {
        
          autocompletion.innerHTML = "";
          autocompletion.style.display = "block";
        
          for (var x = 0; x < attributes.length; x+=1) {
            if (!contains(allAttributes, attributes[x])) {
              autocompletion.innerHTML += attributes[x].name + '<br />';
              allAttributes.push(attributes[x]);
            }
          }
        
        } else {
          autocompletion.style.display = "none";
        }
        
        thisNode = thisNode.iterateNext();
        
      }
      
    } catch (e) {
      //console.log('damn error!');
    }
    
  }

}

function getChildNodes() {

  xpath.className = "invalid";

  try {
  
    var iterator = xml.evaluate(xpath.value, xml, null, XPathResult.ANY_TYPE, null),
        lastNode = xpath.value.split('/'),
        lastNode = lastNode[lastNode.length-1],
        thisNode = iterator.iterateNext(),
        allChildNodes = [];
        
    while (thisNode) {
            
      xpath.className = "valid";

      childNodes = thisNode.childNodes;

      if (childNodes.length > 1) {
      
        autocompletion.innerHTML = "";
        autocompletion.style.display = "block";
      
        for (var x = 0; x < childNodes.length; x+=1) {
        
          childNode = childNodes[x];
        
          if (childNode.nodeName != '#text' && childNode.nodeName != '#comment') {
            if (!contains(allChildNodes, childNode.nodeName)) {
              autocompletion.innerHTML += childNode.nodeName + '<br />';
              allChildNodes.push(childNode.nodeName);
            }
          }
          
        }
      
      } else {
        autocompletion.style.display = "none";
      }
    
      thisNode = thisNode.iterateNext();
    
    }
    
  } catch (e) {
    //console.log('damn error!');
  }

}


function checkXPathValidation() {

    try {
    
      xml.evaluate(xpath.value, xml, null, XPathResult.ANY_TYPE, null);
      
      return true;
      
    } catch (e) {
    
      return false;
      
    }
    
}

// functions
function registerEvent(element, event, callback) {
  if (window.addEventListener) {
    element.addEventListener(event, callback, false);
  } else {
    element.attachEvent(event, 'on' + callback, false);
  }
}

function getOffset(object) {
  var offset = { x: 0, y: 0 };
  var scrolled = { x: 0, y: 0 };

  _getOffset(object, offset);
  _getScrolled(object, scrolled);

  var posX = offset.x - scrolled.x;
  var posY = offset.y - scrolled.y;

  return { left: posX, top : posY };
}

function _getOffset(object, offset) {
  if (!object) {
    return;
  }

  offset.x += object.offsetLeft;
  offset.y += object.offsetTop;

  _getOffset(object.offsetParent, offset);
}

function _getScrolled(object, scrolled) {
  if (!object) {
    return;
  }

  scrolled.x += object.scrollLeft;
  scrolled.y += object.scrollTop;

  if (object.tagName.toLowerCase () != "html") {
    _getScrolled(object.parentNode, scrolled);
  }
}

function mouseOverAndOut(event) {
  'preventDefault' in event && event.preventDefault();

  toggleClass(getXmlLine(event.target || event.srcElement), 'active');
}

function getXmlLine(target) {
  while (target && target.parentNode && !hasClass(target, 'xml-line')) {
    target = target.parentNode;
  }

  return target;
}

function getXmlTag(target) {
  while (target && target.parentNode && !hasClass(target, 'xml-tag')) {
    target = target.parentNode;
  }

  return target;
}

function domTreeDblClick(event) {
  clearSelection();

  'preventDefault' in event && event.preventDefault();

  var target = getXmlLine(event.target || event.srcElement);

  if (target && hasClass(target, 'collapsible')) {
    toggleClass(target, 'expanded');
  }
}

function domTreeSingleClick(event) {
  'preventDefault' in event && event.preventDefault();

  var target = event.target || event.srcElement;

  var expression = [], xpathExpression = null;

  var xmlLine = getXmlLine(target);

  if (xmlLine != target) {
    if (xpathExpression = target.getAttribute("data-xpath")) {
      expression.push(xpathExpression);
    }
  }

  while (xpathExpression = xmlLine.getAttribute("data-xpath")) {
    expression.push(xpathExpression);      

    xmlLine = xmlLine.parentNode;
  }

  if (!!expression.length) {
    xpath.value = expression.reverse().join('');

    xPathChecker();

    autocompletion.style.display = "none";
  }
}

function clearSelection() {
  if (window.getSelection) {
    var sel = window.getSelection();
    sel.removeAllRanges();
  } else if (document.selection && document.selection.empty) {
    document.selection.empty();
  }
}

function hasClass(target, className) {
  return new RegExp('(\\s|^)' + className + '(\\s|$)').test(target.className);
}

function toggleClass(element, className){
  if (!element || !className) {
    return;
  }

  var classString = element.className || '';
  var nameIndex = classString.indexOf(className);

  if (nameIndex == -1) {
    classString += ' ' + className;
  } else {
    classString = classString.substr(0, nameIndex) + classString.substr(nameIndex + className.length);
  }

  element.className = classString;
}

function getElementsByClassName(className, context) {
  var elements = [];

  var domElements = (context || document).getElementsByTagName('*');

  for (var i = 0; i < domElements.length; i++) {
    var element = domElements[i];

    if (hasClass(element, className)) {
      elements.push(element);
    }
  }

  return elements;
}

function expandAll() {
  getElementsByClassName('collapsible').forEach(function(element) {
    if (!hasClass(element, 'expanded')) {
      toggleClass(element, 'expanded');
    }
  });
}

function collapseAll() {
  getElementsByClassName('collapsible').forEach(function(element) {
    if (hasClass(element, 'expanded')) {
      toggleClass(element, 'expanded');
    }
  });
}

function getScrollTop() {
  return document.documentElement.scrollTop || document.body.scrollTop;
}

function hasTextSelection() {
  var selection = undefined;

  if (window.getSelection) {
    selection = window.getSelection().toString();
  } else if (document.selection) {
    selection = document.selection.createRange().text;
  }

  return !!(selection || '').length;
}

function registerCurrentDOMElement(event) {
  event = event || window.event;

  latestDOMElementHovered = (event.target || event.srcElement);

  if (ancestorHighlightNode) {
    showAncestors(latestDOMElementHovered);
  }
}

function showAncestors(domElement) {
  domElement = domElement || latestDOMElementHovered;

  if (domElement && !hasTextSelection() && ancestorHighlightNode) {
    var xmlLine = getXmlLine(domElement);

    ancestorShowTimer = window.setTimeout(function() {
      ancestor = xmlLine.parentNode;

      var ancestors = null;
      var ancestorsEndTagsArray = [];

      while (hasClass(ancestor, 'xml-line')) {
        var targetCloneBottom = ancestor.cloneNode(true);
        var targetCloneTop = ancestor.cloneNode(true);
        var targetCloneTopChildNodes = targetCloneTop.childNodes;
        var targetCloneBottomChildNodes = targetCloneBottom.childNodes;

        for (var i = targetCloneTopChildNodes.length - 1; i > 0; --i) {
          targetCloneTop.removeChild(targetCloneTopChildNodes[i]);
        }

        for (var i = targetCloneBottomChildNodes.length - 2; i >= 0; --i) {
          targetCloneBottom.removeChild(targetCloneBottomChildNodes[i]);
        }

        if (ancestors) {
          targetCloneTop.appendChild(ancestors);
        }

        ancestorsEndTagsArray.push(targetCloneBottom);

        ancestors = targetCloneTop;

        ancestor = ancestor.parentNode;
      }

      if (ancestors) {
        ancestorsEndTagsArray.reverse();

        var ancestorsEndTags = ancestorsEndTagsArray[0];
        var laterstChildNode = ancestorsEndTags;

        if (hasClass(ancestorsEndTags, 'collapsible')) {
          toggleClass(ancestorsEndTags, 'collapsible');
        }

        for (var i = 1; i <= ancestorsEndTagsArray.length - 1; i++) {
          ancestorEndTag = ancestorsEndTagsArray[i];

          if (hasClass(ancestorEndTag, 'collapsible')) {
            toggleClass(ancestorEndTag, 'collapsible');
          }
    
          laterstChildNode.insertBefore(ancestorEndTag, laterstChildNode.firstChild);

          laterstChildNode = ancestorEndTag;
        };

        domTreeCloneTop.appendChild(ancestors);
        domTreeCloneBottom.appendChild(ancestorsEndTags);

        var offsetTop = (getScrollTop() + getOffset(xmlLine).top - domTreeCloneWrapperTop.offsetHeight);
        var offsetBottom = (getScrollTop() + getOffset(xmlLine).top + xmlLine.offsetHeight);

        if (offsetTop >= getScrollTop()) {
          domTreeCloneWrapperTop.style.top = offsetTop;
          domTreeCloneWrapperBottom.style.top = offsetBottom;

          domTreeCloneBlockerTop.style.height = offsetTop + 'px';

          domTreeCloneBlockerBottom.style.top =  offsetBottom + 'px';
          domTreeCloneBlockerBottom.style.height = document.documentElement.offsetHeight - offsetBottom + 'px';
        } else {
          hideAncestors();
        }
      }
    }, 50);
  }
}

function hideAncestors() {
  latestDOMElementHovered = null;

  window.clearTimeout(ancestorShowTimer);

  if (domTreeCloneTop.firstChild) {
    domTreeCloneTop.removeChild(domTreeCloneTop.firstChild);
    domTreeCloneTop.style.top = 0;
  }

  if (domTreeCloneBottom.firstChild) {
    domTreeCloneBottom.removeChild(domTreeCloneBottom.firstChild);
    domTreeCloneBottom.style.top = 0;
  }

  domTreeCloneBlockerTop.style.height = 0;
  domTreeCloneBlockerBottom.style.top = 0;
  domTreeCloneBlockerBottom.style.height = 0;
}

function enableAncestorHighlightMode(event) {
  event = event || window.event;
  var keyCode = event.keyCode || event.which;

  if (!ancestorHighlightNode) {
    ancestorHighlightNode = (keyCode == 17);

    if (ancestorHighlightNode) {
      showAncestors(latestDOMElementHovered);
    }
  }
}

function disableAncestorHighlightMode(event) {
  event = event || window.event;
  var keyCode = event.keyCode || event.which;

  if (ancestorHighlightNode) {
    ancestorHighlightNode = !(keyCode == 17);

    if (!ancestorHighlightNode) {
      hideAncestors();
    }
  }
}

// variables
var domTree = document.getElementById('dom-tree');
var domTreeCloneWrapperTop = document.getElementById('dom-tree-clone-wrapper-top');
var domTreeCloneWrapperBottom = document.getElementById('dom-tree-clone-wrapper-bottom');
var domTreeCloneTop = document.getElementById('dom-tree-clone-top');
var domTreeCloneBottom = document.getElementById('dom-tree-clone-bottom');
var domTreeCloneBlockerTop = document.getElementById('dom-tree-clone-blocker-top');
var domTreeCloneBlockerBottom = document.getElementById('dom-tree-clone-blocker-bottom');
var collapse = document.getElementById("collapse");
var expand = document.getElementById("expand");
var ancestorShowTimer = null;
var ancestorHighlightNode = false;
var latestDOMElementHovered = null;

// initialize events
registerEvent(domTree, 'click', domTreeSingleClick);
registerEvent(domTree, 'dblclick', domTreeDblClick);
registerEvent(domTree, 'mouseover', mouseOverAndOut);
registerEvent(domTree, 'mouseout', mouseOverAndOut);
registerEvent(domTree, 'mouseover', registerCurrentDOMElement);
registerEvent(domTree, 'mouseout', hideAncestors);
registerEvent(collapse, 'click', collapseAll);
registerEvent(expand, 'click', expandAll);

registerEvent(window, 'keyup', disableAncestorHighlightMode);
registerEvent(window, 'keydown', enableAncestorHighlightMode);

registerEvent(document, document.addEventListener ? 'DOMContentLoaded' : 'readystatechange', function() {
  xPathChecker();

  autocompletion.style.display = "none";
});
