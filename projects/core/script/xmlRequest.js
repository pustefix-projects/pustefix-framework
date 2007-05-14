//*****************************************************************************
// xmlRequest.js
//*****************************************************************************

//-------------------
// browser detection
//-------------------

// (to be exported into separate module)

var _browser;
if (window.opera)
  _browser = "opera";
else if (typeof navigator.vendor=="string" && navigator.vendor=="KDE")
  _browser = "khtml";
else if (typeof navigator.product=="string" && navigator.product=="Gecko")
  _browser = "gecko";
else if (/msie/i.test(navigator.userAgent))
  _browser = "mshtml";

_isMshtml = _browser == "mshtml";
_isGecko  = _browser == "gecko";
_isOpera  = _browser == "opera";
_isKhtml  = _browser == "khtml";

//*****************************************************************************
//
//*****************************************************************************
function XML_Request() {

  this.method   = arguments[0];
  this.url      = arguments[1];
  this.callback = arguments[2];
  this.context  = arguments[3];

  this.errors = [];
  this.status = 0;
  this.statusText = "";

  var self = this;
  this.customOnReadyStateChange = function() { self._customOnReadyStateChange(); };
  this.cancelOnReadyStateChange = function(i, msg) { self._cancelOnReadyStateChange(i, msg); };
}

XML_Request._xml = [];
XML_Request._xmlThis = [];
XML_Request._xmlTimer = [];
XML_Request._xmlTimerCount = [];
XML_Request._xmlTimerCountMax = 1000;
XML_Request._xmlTimerInterval = 5;

XML_Request.builtin = window.XMLHttpRequest ? true : false;

// iframe
XML_Request.IFRAMES_NEVER    = -1;
XML_Request.IFRAMES_FALLBACK =  0;
XML_Request.IFRAMES_ONLY     =  1;

// set iframe behaviour
XML_Request.prototype.iframes  = XML_Request.IFRAMES_FALLBACK;

// set headers required for SOAP (Axis)
XML_Request.prototype.headers = [ [ 'SOAPAction', '""'] ];

//--------
// Mshtml
//--------

XML_Request.msXmlHttp = null;
if( !XML_Request.builtin && !_isOpera && window.ActiveXObject ) {
  // determine working ActiveX XMLHTTP component
  // both security settings needed (secure(1.) and plugins(3.))
  // if successful, XML_Request.msXmlHttp is of type "string", 
  // otherwise "object"

  var msXmlHttpList = ["MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1", "MICROSOFT.XMLHTTP"];
      
  var obj;
  for (var j=0; j<msXmlHttpList.length; j++) {
    try {
      obj = new ActiveXObject(msXmlHttpList[j]);
      
      // success ==> store, quit loop
      XML_Request.msXmlHttp = msXmlHttpList[j];
      break;
    } catch(e) {
      // ignore failures
    } 
  }
}

XML_Request.activeX = typeof XML_Request.msXmlHttp == "string";

//*****************************************************************************
//
//*****************************************************************************
XML_Request.prototype.start = function( content, reqId ) {

  // unique timestamp to prevent caching
  //  var uniq = ""+ new Date().getTime() + Math.floor(1000 * Math.random());
  //  this.url += ( ( this.url.indexOf('?')+1 ) ? '&' : '?' ) + uniq;

  var i = XML_Request._xml.length;

  if( this.iframes!=XML_Request.IFRAMES_ONLY ) {

    if( XML_Request.builtin ) {

      //----------------
      // XMLHttpRequest
      //----------------

      try {
        XML_Request._xml[i] = new XMLHttpRequest();
        
      } catch(e) {
        XML_Request._xml[i] = null;
        if( this.iframes == XML_Request.IFRAMES_NEVER ) {
          throw new Error("XML_Request: Could not create XMLHttpRequest");
        } 
      }
    } else if( XML_Request.activeX ) {

      //---------
      // ActiveX
      //---------

      try {
        XML_Request._xml[i] = new ActiveXObject(XML_Request.msXmlHttp);
      } catch(e) {
        XML_Request._xml[i] = null;
        if( this.iframes == XML_Request.IFRAMES_NEVER ) {
          throw new Error("XML_Request: Could not create ActiveXObject " + XML_Request.msXmlHttp);
        } 
      }
    }

    if( typeof XML_Request._xml[i] != "undefined" ) {

      if( this.callback ) {
        //-------
        // async
        //-------

        try {
          var self = this;

          if( XML_Request.activeX ) {
            XML_Request._xml[i].onreadystatechange = function() {
              if( XML_Request._xml[i].readyState == 4 ) {
                var reqId;
                try {
                  reqId = XML_Request._xml[i].getResponseHeader("Request-Id");
                } catch(e) {
                }
                self.callback.call( self.context, XML_Request._xml[i].responseXML, reqId);
                XML_Request._xml[i] = null;
              }
            };
          } else {
            XML_Request._xml[i].onreadystatechange = function() {
              if( XML_Request._xml[i].readyState == 4 ) {
                try {
                  self.status     = XML_Request._xml[i].status;
                  self.statusText = XML_Request._xml[i].statusText;
                } catch(e) {
                }
                if( self.status && self.status >= 300 ) {
                  //throw new Error("XML_Request: Asynchronous call failed" + " (status " + self.status + ", " + self.statusText + ")");
                }
                var reqId;
                try {
                  reqId = XML_Request._xml[i].getResponseHeader("Request-Id");
                } catch(e) {
                }
                self.callback.call( self.context, XML_Request._xml[i].responseXML, reqId );
                XML_Request._xml[i] = null;
              }
            };
          }

        } catch(e) {
          XML_Request._xml[i] = null;
          throw new Error("XML_Request: Onreadystatechange failed");
        }
      }


      try {
        XML_Request._xml[i].open( this.method, this.url, this.callback ? true : false); 

        for( var j=0; j<this.headers.length; j++ ) {
          try {
            // not implemented in Opera 7.6pr1
            XML_Request._xml[i].setRequestHeader( this.headers[j][0], this.headers[j][1] );
          } catch(e) {
          }
        }

        if( this.callback && typeof reqId != "undefined" ) {
          try {
            // not implemented in Opera 7.6pr1
            XML_Request._xml[i].setRequestHeader( "Request-Id", reqId.toString() );
          } catch(e) {
          }
        }

        XML_Request._xml[i].send(content);
          
        if( !this.callback ) {
          this.status=XML_Request._xml[i].status;
          this.statusText=XML_Request._xml[i].statusText;
          return XML_Request._xml[i].responseXML;
        } else {
          return true;
        }
      } catch(e) {
        XML_Request._xml[i] = null;
        throw new Error("XML_Request: Call failed");
      }  
    }
  }

  if( this.iframes!=XML_Request.IFRAMES_NEVER && !XML_Request._xml[i] && document.createElement ) {

    //--------
    // iframe
    //--------

    if( !this.callback ) {
      throw new Error("XML_Request: Synchronous call by iframe not supported"); 
    }

    try {

      //--------------
      // result frame
      //--------------

      var el;

      if( !( _isMshtml || _isOpera ) ) {
        el = document.createElement("iframe");
        el.style.display = "none";
        el.name          = "pfxxmliframe"+i;
        el.id            = "pfxxmliframe"+i;

        document.body.appendChild(el);

      } else {
        el = document.createElement("div");
        el.style.display = "none";
        el.id            = "pfxxmldiv"+i;
        document.body.appendChild(el);

        document.getElementById("pfxxmldiv"+i).innerHTML = '<' + 'iframe id="pfxxmliframe' + i + '" name="pfxxmliframe' + i + '" style="display:block"><' + '/iframe>';
      }

      var url = this.url;
      if( reqId ) {
        url = this.setQueryParameter( url, "PFX_Request_ID", reqId );
      }

      if( this.method.toLowerCase() == "get" ) {

        //-----
        // GET
        //-----
		
        // better use location.replace(url) to prevent entry into browser history
        el.src = url;
        
        document.body.appendChild(el);
        var self = this;
        XML_Request._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, XML_Request._xmlTimerInterval);

      } else if( this.method.toLowerCase() == "post" ) {

        //------
        // POST
        //------

        //------------
        // form frame
        //------------

        el = document.createElement("div");
        el.style.display = "none";
        el.id               = "pfxxmlformdiv"+i;

        XML_Request._xml[i] = this.callback;
        XML_Request._xmlThis[i] = this;
        XML_Request._xmlTimer[i] = true;
        XML_Request._xmlTimerCount[i] = 0;

        var self = this;
        window.setTimeout( function() {

          var elForm = document.createElement("form");
          elForm.action = url;
          elForm.target = "pfxxmliframe"+i;
          elForm.method = self.method;
          elForm.id     = "pfxxmlform"+i;
          
          var elField = document.createElement("textarea");
          elField.name  = "soapmessage";
          elField.value = content;
          elForm.appendChild(elField);

          if( _isMshtml ) {
            elField = document.createElement("input");
            elField.type  = "hidden";
            elField.name  = "insertpi";
            elField.value = "1";
            elForm.appendChild(elField);
          }

          el.appendChild(elForm);
          document.body.appendChild(el);
          
          document.getElementById("pfxxmlform"+i).target = "pfxxmliframe"+i;

          window.setTimeout( function() {
            elForm.submit();
          }, 1 );
        }, 1 );        

        XML_Request._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, XML_Request._xmlTimerInterval);
      } else {
        // method other than GET or POST are not supported
        throw new Error("XML_Request: Iframes do not support method " + this.method);
      }

      return "iframe";
    } catch(e) {
      throw new Error("XML_Request: Iframes failed" + e);
    }
  }

  throw new Error("XML_Request: Failure");
};

//*****************************************************************************
//
//*****************************************************************************
XML_Request.prototype.setRequestHeader = function( field, value ) {
  
  this.headers.push( [field, value] );
};

//*****************************************************************************
//
//*****************************************************************************
XML_Request.prototype.setQueryParameter = function( url, field, value ) {
  
  url += ( ( url.indexOf('?')+1 ) ? '&' : '?' ) + field + "=" + encodeURI(value);

  return url;
};

//*****************************************************************************
//
//*****************************************************************************
XML_Request.prototype.getQueryParameter = function( url, field ) {

  var pairs = url.substr( url.indexOf('?')+1 ).split('&');
  
  var param;
  for( var i=0; i<pairs.length; i++ ) {
    param = pairs[i].split('=');
    if( param[0] == field ) {
      return param[1];
    }
  }

  return null;
};

//*****************************************************************************
//
//*****************************************************************************
XML_Request.prototype._customOnReadyStateChange = function() {

  var win = null;

  for( var i=0; i<XML_Request._xml.length; i++ ) {
    if( XML_Request._xmlTimer[i] && XML_Request._xml[i] ) {

      try {
        if( XML_Request._xmlTimerCount[i]<XML_Request._xmlTimerCountMax ) {

          win = window.frames['pfxxmliframe'+i];
          if( win && 
              win.document && 
              win.location.href != "about:blank" && 
              (_isMshtml ? win.document.readyState=="complete" : true)) {

            XML_Request._xml[i].call( XML_Request._xmlThis[i].context, 
                                      _isMshtml ? win.document.body : win.document,
                                      XML_Request._xmlThis[i].getQueryParameter( win.location.href, "PFX_Request_ID") );
            this.cancelOnReadyStateChange(i);
          } else {
            XML_Request._xmlTimerCount[i]++;            
          }
        } else {
          this.cancelOnReadyStateChange(i, "too many intervals " + i + ", " + XML_Request._xmlTimerCount[i]);
        }
      } catch(e) {
        this.cancelOnReadyStateChange(i, "Exception:" + e);
      }
    }
  }
};

//*****************************************************************************
//
//*****************************************************************************
XML_Request.prototype._cancelOnReadyStateChange = function( i, msg ) {

  try {
    window.clearInterval(XML_Request._xmlTimer[i]);
    XML_Request._xmlTimer[i] = null;
    XML_Request._xml[i] = null;
    XML_Request._xmlThis[i] = null;
    XML_Request._xmlTimerCount[i] = 0;
  } catch(e) {
    msg = "Could not cancel";
  }

  // remove div, iframe, etc.; speedup by reusing iframes not build in
  var el;
  try {
    if( el = document.getElementById("pfxxmliframe"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}
  try {
    if( el = document.getElementById("pfxxmldiv"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}
  try {
    if( el = document.getElementById("pfxxmlform"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}
  try {
    if( el = document.getElementById("pfxxmlformdiv"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}

  if( msg ) {
    throw new Error("XML_Request: " + msg);
  }
};
//*****************************************************************************
//*****************************************************************************
