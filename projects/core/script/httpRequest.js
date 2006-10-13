//*****************************************************************************
// httpRequest.js
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
function HTTP_Request() {

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

HTTP_Request._xml = [];
HTTP_Request._xmlThis = [];
HTTP_Request._xmlTimer = [];
HTTP_Request._xmlTimerCount = [];
HTTP_Request._xmlTimerCountMax = 1000;
HTTP_Request._xmlTimerInterval = 5;

HTTP_Request.builtin = window.XMLHttpRequest ? true : false;

// iframe
HTTP_Request.IFRAMES_NEVER    = -1;
HTTP_Request.IFRAMES_FALLBACK =  0;
HTTP_Request.IFRAMES_ONLY     =  1;

// set iframe behaviour
HTTP_Request.prototype.iframes  = HTTP_Request.IFRAMES_FALLBACK;

// set headers
HTTP_Request.prototype.headers = [];

//-------
// Opera
//-------

if( _isOpera ) {
  // opera 7.6pr1's support of XMLHttpRequest still buggy
  // - no setRequestHeader()
  // - Content-Length: 0

  // deactivate XMLHttpRequest
  HTTP_Request.builtin = false;

  // use iframes instead, if allowed by configuration
  HTTP_Request.prototype.iframes = HTTP_Request.prototype.iframes || 1;
}

//--------
// Mshtml
//--------

HTTP_Request.msXmlHttp = null;
if( !HTTP_Request.builtin && !_isOpera && window.ActiveXObject ) {
  // determine working ActiveX XMLHTTP component
  // both security settings needed (secure(1.) and plugins(3.))
  // if successful, HTTP_Request.msXmlHttp is of type "string", 
  // otherwise "object"

  var msXmlHttpList = ["MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1", "MICROSOFT.XMLHTTP"];
      
  var obj;
  for (var j=0; j<msXmlHttpList.length; j++) {
    try {
      obj = new ActiveXObject(msXmlHttpList[j]);
      
      // success ==> store, quit loop
      HTTP_Request.msXmlHttp = msXmlHttpList[j];
      break;
    } catch(e) {
      // ignore failures
    } 
  }
}

HTTP_Request.activeX = typeof HTTP_Request.msXmlHttp == "string";

//*****************************************************************************
//
//*****************************************************************************
HTTP_Request.prototype.start = function( content, headers, reqId ) {

  // unique timestamp to prevent caching
  //  var uniq = ""+ new Date().getTime() + Math.floor(1000 * Math.random());
  //  this.url += ( ( this.url.indexOf('?')+1 ) ? '&' : '?' ) + uniq;

  var i = HTTP_Request._xml.length;

  if( this.iframes!=HTTP_Request.IFRAMES_ONLY ) {

    if( HTTP_Request.builtin ) {

      //----------------
      // XMLHttpRequest
      //----------------

      try {
        HTTP_Request._xml[i] = new XMLHttpRequest();
        
      } catch(e) {
        HTTP_Request._xml[i] = null;
        if( this.iframes == HTTP_Request.IFRAMES_NEVER ) {
          throw new Error("HTTP_Request: Could not create XMLHttpRequest");
        } 
      }
    } else if( HTTP_Request.activeX ) {

      //---------
      // ActiveX
      //---------

      try {
        HTTP_Request._xml[i] = new ActiveXObject(HTTP_Request.msXmlHttp);
      } catch(e) {
        HTTP_Request._xml[i] = null;
        if( this.iframes == HTTP_Request.IFRAMES_NEVER ) {
          throw new Error("HTTP_Request: Could not create ActiveXObject " + HTTP_Request.msXmlHttp);
        } 
      }
    }

    if( typeof HTTP_Request._xml[i] != "undefined" ) {

      if( this.callback ) {
        //-------
        // async
        //-------

        try {
          var self = this;

          if( HTTP_Request.activeX ) {
            HTTP_Request._xml[i].onreadystatechange = function() {
              if( HTTP_Request._xml[i].readyState == 4 ) {
                var reqId;
                try {
                  reqId = HTTP_Request._xml[i].getResponseHeader("Request-Id");
                } catch(e) {
                }
                self.callback.call( self.context, self._getResponse(HTTP_Request._xml[i]), reqId);
                HTTP_Request._xml[i] = null;
              }
            };
          } else {
            HTTP_Request._xml[i].onreadystatechange = function() {
              if( HTTP_Request._xml[i].readyState == 4 ) {
                try {
                  self.status     = HTTP_Request._xml[i].status;
                  self.statusText = HTTP_Request._xml[i].statusText;
                } catch(e) {
                }
                if( self.status && self.status >= 300 ) {
                  //throw new Error("HTTP_Request: Asynchronous call failed" + " (status " + self.status + ", " + self.statusText + ")");
                }
                var reqId;
                try {
                  reqId = HTTP_Request._xml[i].getResponseHeader("Request-Id");
                } catch(e) {
                }
                self.callback.call( self.context, self._getResponse(HTTP_Request._xml[i]), reqId );
                HTTP_Request._xml[i] = null;
              }
            };
          }

        } catch(e) {
          HTTP_Request._xml[i] = null;
          throw new Error("HTTP_Request: Onreadystatechange failed");
        }
      }


      try {
        HTTP_Request._xml[i].open( this.method, this.url, this.callback ? true : false); 

        for( var j=0; j<this.headers.length; j++ ) {
          try {
            // not implemented in Opera 7.6pr1
            HTTP_Request._xml[i].setRequestHeader( this.headers[j][0], this.headers[j][1] );
          } catch(e) {
          }
        }

        if( this.callback && typeof reqId != "undefined" ) {
          try {
            // not implemented in Opera 7.6pr1
            HTTP_Request._xml[i].setRequestHeader( "Request-Id", reqId.toString() );
          } catch(e) {
          }
        }

        HTTP_Request._xml[i].send(content);
          
        if( !this.callback ) {
          this.status=HTTP_Request._xml[i].status;
          this.statusText=HTTP_Request._xml[i].statusText;
          return this._getResponse(HTTP_Request._xml[i]);
        } else {
          return true;
        }
      } catch(e) {
        HTTP_Request._xml[i] = null;
        throw new Error("HTTP_Request: Call failed");
      }  
    }
  }

  if( this.iframes!=HTTP_Request.IFRAMES_NEVER && !HTTP_Request._xml[i] && document.createElement ) {

    //--------
    // iframe
    //--------

    if( !this.callback ) {
      throw new Error("HTTP_Request: Synchronous call by iframe not supported"); 
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
        HTTP_Request._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, HTTP_Request._xmlTimerInterval);

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

        HTTP_Request._xml[i] = this.callback;
        HTTP_Request._xmlThis[i] = this;
        HTTP_Request._xmlTimer[i] = true;
        HTTP_Request._xmlTimerCount[i] = 0;

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
            document.forms[document.forms.length-1].submit();
          }, 1 );
        }, 1 );        

        HTTP_Request._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, HTTP_Request._xmlTimerInterval);
      } else {
        // method other than GET or POST are not supported
        throw new Error("HTTP_Request: Iframes do not support method " + this.method);
      }

      return "iframe";
    } catch(e) {
      throw new Error("HTTP_Request: Iframes failed" + e);
    }
  }

  throw new Error("HTTP_Request: Failure");
};

//*****************************************************************************
//
//*****************************************************************************
HTTP_Request.prototype.setRequestHeader = function( field, value ) {
  
  this.headers.push( [field, value] );
};

//*****************************************************************************
//
//*****************************************************************************
HTTP_Request.prototype.setQueryParameter = function( url, field, value ) {
  
  url += ( ( url.indexOf('?')+1 ) ? '&' : '?' ) + field + "=" + encodeURI(value);

  return url;
};

//*****************************************************************************
//
//*****************************************************************************
HTTP_Request.prototype.getQueryParameter = function( url, field ) {

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
HTTP_Request.prototype._getResponse = function(request) {
  var ctype=request.getResponseHeader("Content-Type");
  if(ctype==null) {
    throw new Error("Missing response content type");
  } else if(ctype.indexOf("text/plain")==0) {
    return request.responseText;
  } else if(ctype.indexOf("text/xml")==0) {
    return request.responseXML;
  } else {
    throw new Error("Illegal response content type: "+ctype);
  }
};

//*****************************************************************************
//
//*****************************************************************************
HTTP_Request.prototype._customOnReadyStateChange = function() {

  var win = null;

  for( var i=0; i<HTTP_Request._xml.length; i++ ) {
    if( HTTP_Request._xmlTimer[i] && HTTP_Request._xml[i] ) {

      try {
        if( HTTP_Request._xmlTimerCount[i]<HTTP_Request._xmlTimerCountMax ) {

          win = window.frames['pfxxmliframe'+i];
          if( win && 
              win.document && 
              win.location.href != "about:blank" && 
              (_isMshtml ? win.document.readyState=="complete" : true)) {

            HTTP_Request._xml[i].call( HTTP_Request._xmlThis[i].context, 
                                      _isMshtml ? win.document.body : win.document,
                                      HTTP_Request._xmlThis[i].getQueryParameter( win.location.href, "PFX_Request_ID") );
            this.cancelOnReadyStateChange(i);
          } else {
            HTTP_Request._xmlTimerCount[i]++;            
          }
        } else {
          this.cancelOnReadyStateChange(i, "too many intervals " + i + ", " + HTTP_Request._xmlTimerCount[i]);
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
HTTP_Request.prototype._cancelOnReadyStateChange = function( i, msg ) {

  try {
    window.clearInterval(HTTP_Request._xmlTimer[i]);
    HTTP_Request._xmlTimer[i] = null;
    HTTP_Request._xml[i] = null;
    HTTP_Request._xmlThis[i] = null;
    HTTP_Request._xmlTimerCount[i] = 0;
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
    throw new Error("HTTP_Request: " + msg);
  }
};
//*****************************************************************************
//*****************************************************************************
