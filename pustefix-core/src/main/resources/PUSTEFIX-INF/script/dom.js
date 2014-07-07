/* DOM */

var domElement = document.getElementsByTagName("li")[0],
    domElements = document.getElementsByTagName("li"),
    currentLocation = document.URL.replace('xmlonly=1','xmlonly=3'),
    nodeElements = document.getElementsByTagName("a"),
    xpath = document.getElementById("xpath"),
    collapse = document.getElementById("collapse"),
    expand = document.getElementById("expand"),
    xml = loadXMLDoc(currentLocation),
    autocompletion = document.getElementById("autocompletion"),
    body = document.getElementsByTagName("body")[0];
    
    
 // ie check
 
 if (!xml.evaluate) {
  xpath.className = '';
} else {
  body.onload = function() {
    xPathChecker();
    autocompletion.style.display = "none";
  }
}
    
for (var x = 0; x < nodeElements.length; x+=1) {
  if (nodeElements[x].addEventListener) {
    nodeElements[x].addEventListener("click", function(e) { getXpathExpression(e.target); }, false);
  } else {
    nodeElements[x].attachEvent("onclick", function(e) { getXpathExpression(e.srcElement); }, false);
  }
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
    xPathChecker();
    autocompletion.style.display = "none";
  }
}

function collapseAll() {
  for (var x = 0; x < domElements.length; x+=1) {
    if (domElements[x].className == "expanded") {
      domElements[x].className = "collapsed";
    }
  }
}

function hideAutoCompletion() {
  autocompletion.style.display = "none";
}
function showAutoCompletion() {
  autocompletion.style.display = "block";
}

function expandAll() {
  for (var x = 0; x < domElements.length; x+=1) {
    if (domElements[x].className == "collapsed") {
      domElements[x].className = "expanded";
    }
  }
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
