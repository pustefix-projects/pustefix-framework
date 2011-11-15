if(!window.pfx) pfx={};
if(!pfx.net) pfx.net={};

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
pfx.net.HTTPRequest=function() {

  this.method   = arguments[0];
  this.url      = arguments[1];
  this.callback = arguments[2];
  this.context  = arguments[3];

  this.headers = [];
  this.errors = [];
  this.status = 0;
  this.statusText = "";

  var self = this;
  this.customOnReadyStateChange = function() { self._customOnReadyStateChange(); };
  this.cancelOnReadyStateChange = function(i, msg) { self._cancelOnReadyStateChange(i, msg); };
}

pfx.net.HTTPRequest._xml = [];
pfx.net.HTTPRequest._xmlThis = [];
pfx.net.HTTPRequest._xmlTimer = [];
pfx.net.HTTPRequest._xmlTimerCount = [];
pfx.net.HTTPRequest._xmlTimerCountMax = 1000;
pfx.net.HTTPRequest._xmlTimerInterval = 5;

pfx.net.HTTPRequest.builtin = window.XMLHttpRequest ? true : false;

// iframe
pfx.net.HTTPRequest.IFRAMES_NEVER    = -1;
pfx.net.HTTPRequest.IFRAMES_FALLBACK =  0;
pfx.net.HTTPRequest.IFRAMES_ONLY     =  1;

// set iframe behaviour
pfx.net.HTTPRequest.prototype.iframes  = pfx.net.HTTPRequest.IFRAMES_FALLBACK;

//--------
// Mshtml
//--------

pfx.net.HTTPRequest.msXmlHttp = null;
if( !pfx.net.HTTPRequest.builtin && !_isOpera && window.ActiveXObject ) {
  // determine working ActiveX XMLHTTP component
  // both security settings needed (secure(1.) and plugins(3.))
  // if successful, pfx.net.HTTPRequest.msXmlHttp is of type "string", 
  // otherwise "object"

  var msXmlHttpList = ["MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1", "MICROSOFT.XMLHTTP"];
      
  var obj;
  for (var j=0; j<msXmlHttpList.length; j++) {
    try {
      obj = new ActiveXObject(msXmlHttpList[j]);
      
      // success ==> store, quit loop
      pfx.net.HTTPRequest.msXmlHttp = msXmlHttpList[j];
      break;
    } catch(e) {
      // ignore failures
    } 
  }
}

pfx.net.HTTPRequest.activeX = typeof pfx.net.HTTPRequest.msXmlHttp == "string";

//*****************************************************************************
//
//*****************************************************************************
pfx.net.HTTPRequest.prototype.start = function( content, headers, reqId ) {

  // unique timestamp to prevent caching
  //  var uniq = ""+ new Date().getTime() + Math.floor(1000 * Math.random());
  //  this.url += ( ( this.url.indexOf('?')+1 ) ? '&' : '?' ) + uniq;

  var i = pfx.net.HTTPRequest._xml.length;

  if( this.iframes!=pfx.net.HTTPRequest.IFRAMES_ONLY ) {

    if( pfx.net.HTTPRequest.builtin ) {

      //----------------
      // XMLHttpRequest
      //----------------

      try {
        pfx.net.HTTPRequest._xml[i] = new XMLHttpRequest();
        
      } catch(e) {
        pfx.net.HTTPRequest._xml[i] = null;
        if( this.iframes == pfx.net.HTTPRequest.IFRAMES_NEVER ) {
          throw new Error("HTTP_Request: Could not create XMLHttpRequest");
        } 
      }
    } else if( pfx.net.HTTPRequest.activeX ) {

      //---------
      // ActiveX
      //---------

      try {
        pfx.net.HTTPRequest._xml[i] = new ActiveXObject(pfx.net.HTTPRequest.msXmlHttp);
      } catch(e) {
        pfx.net.HTTPRequest._xml[i] = null;
        if( this.iframes == pfx.net.HTTPRequest.IFRAMES_NEVER ) {
          throw new Error("HTTP_Request: Could not create ActiveXObject " + pfx.net.HTTPRequest.msXmlHttp);
        } 
      }
    }

    if( typeof pfx.net.HTTPRequest._xml[i] != "undefined" ) {

      if( this.callback ) {
        //-------
        // async
        //-------

        try {
          var self = this;

          if( pfx.net.HTTPRequest.activeX ) {
            pfx.net.HTTPRequest._xml[i].onreadystatechange = function() {
              if( pfx.net.HTTPRequest._xml[i].readyState == 4 ) {
                var reqId;
                try {
                  reqId = pfx.net.HTTPRequest._xml[i].getResponseHeader("Request-Id");
                } catch(e) {
                }
                var content = self._getResponse(pfx.net.HTTPRequest._xml[i]);
                if(content!=null) self.callback.call( self.context, content, reqId);
                else if(!pfx.net.HTTPRequest._xml[i].aborted) throw new Error("Empty response");
                pfx.net.HTTPRequest._xml[i] = null;
              }
            };
          } else {
            pfx.net.HTTPRequest._xml[i].onreadystatechange = function() {
              if( pfx.net.HTTPRequest._xml[i].readyState == 4 ) {
                try {
                  self.status     = pfx.net.HTTPRequest._xml[i].status;
                  self.statusText = pfx.net.HTTPRequest._xml[i].statusText;
                } catch(e) {
                }
                if( self.status && self.status >= 400 ) {
                  throw new Error("HTTP_Request: Asynchronous call failed" + " (status " + self.status + ", " + self.statusText + ")");
                }
                var reqId;
                try {
                  reqId = pfx.net.HTTPRequest._xml[i].getResponseHeader("Request-Id");
                } catch(e) {
                }
                var content = self._getResponse(pfx.net.HTTPRequest._xml[i]);
                if(content!=null) self.callback.call( self.context, content, reqId);
                else if(!pfx.net.HTTPRequest._xml[i].aborted) throw new Error("Empty response");
                pfx.net.HTTPRequest._xml[i] = null;
              }
            };
          }

        } catch(e) {
          pfx.net.HTTPRequest._xml[i] = null;
          throw new Error("HTTP_Request: Onreadystatechange failed");
        }
      }


      try {
     
        pfx.net.HTTPRequest._xml[i].open( this.method, this.url, this.callback ? true : false); 
 
        for( var j=0; j<this.headers.length; j++ ) {
          try {
            // not implemented in Opera 7.6pr1
            pfx.net.HTTPRequest._xml[i].setRequestHeader( this.headers[j][0], this.headers[j][1] );
          } catch(e) {
          }
        }

        if( this.callback && typeof reqId != "undefined" ) {
          try {
            // not implemented in Opera 7.6pr1
            pfx.net.HTTPRequest._xml[i].setRequestHeader( "Request-Id", reqId.toString() );
          } catch(e) {
          }
        }
        
        pfx.net.HTTPRequest._xml[i].send(content);
          
        if( !this.callback ) {
    
          this.status=pfx.net.HTTPRequest._xml[i].status;
          this.statusText=pfx.net.HTTPRequest._xml[i].statusText;
          return this._getResponse(pfx.net.HTTPRequest._xml[i]);
        } else {
      
          return true;
        }
      } catch(e) {
        pfx.net.HTTPRequest._xml[i] = null;
        throw new Error("HTTP_Request: Call failed [Cause: "+e+"]");
      }  
    }
  }

  if( this.iframes!=pfx.net.HTTPRequest.IFRAMES_NEVER && !pfx.net.HTTPRequest._xml[i] && document.createElement ) {

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
        el.name          = "pfxreqiframe"+i;
        el.id            = "pfxreqiframe"+i;

        document.body.appendChild(el);

      } else {
        el = document.createElement("div");
        el.style.display = "none";
        el.id            = "pfxreqdiv"+i;
        document.body.appendChild(el);

        document.getElementById("pfxreqdiv"+i).innerHTML = '<' + 'iframe id="pfxreqiframe' + i + '" name="pfxreqiframe' + i + '" style="display:block"><' + '/iframe>';
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
        pfx.net.HTTPRequest._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, pfx.net.HTTPRequest._xmlTimerInterval);

      } else if( this.method.toLowerCase() == "post" ) {

        //------
        // POST
        //------

        //------------
        // form frame
        //------------

        el = document.createElement("div");
        el.style.display = "none";
        el.id               = "pfxreqformdiv"+i;

        pfx.net.HTTPRequest._xml[i] = this.callback;
        pfx.net.HTTPRequest._xmlThis[i] = this;
        pfx.net.HTTPRequest._xmlTimer[i] = true;
        pfx.net.HTTPRequest._xmlTimerCount[i] = 0;

        var self = this;
        window.setTimeout( function() {

          var elForm = document.createElement("form");
          elForm.action = url;
          elForm.target = "pfxreqiframe"+i;
          elForm.method = self.method;
          elForm.id     = "pfxreqform"+i;
          
          var elField = document.createElement("textarea");
          elField.name  = "message";
          elField.value = content;
          elForm.appendChild(elField);
          
          for( var h=0; h<self.headers.length; h++ ) {
            elField = document.createElement("input");
            elField.type = "hidden";
            elField.name = self.headers[h][0];
            elField.value = self.headers[h][1];
            elForm.appendChild(elField);
          }

          if( _isMshtml ) {
            elField = document.createElement("input");
            elField.type  = "hidden";
            elField.name  = "insertpi";
            elField.value = "1";
            elForm.appendChild(elField);
          }

          el.appendChild(elForm);
          document.body.appendChild(el);
          
          document.getElementById("pfxreqform"+i).target = "pfxreqiframe"+i;

          window.setTimeout( function() {
            var form=document.getElementById("pfxreqform"+i);
            form.submit();
          }, 1 );
        }, 1 );        

        pfx.net.HTTPRequest._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, pfx.net.HTTPRequest._xmlTimerInterval);
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
pfx.net.HTTPRequest.prototype.setRequestHeader = function( field, value ) {
  
  this.headers.push( [field, value] );
};

//*****************************************************************************
//
//*****************************************************************************
pfx.net.HTTPRequest.prototype.setQueryParameter = function( url, field, value ) {
  
  url += ( ( url.indexOf('?')+1 ) ? '&' : '?' ) + field + "=" + encodeURI(value);

  return url;
};

//*****************************************************************************
//
//*****************************************************************************
pfx.net.HTTPRequest.prototype.getQueryParameter = function( url, field ) {

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
pfx.net.HTTPRequest.prototype._getResponse = function(request) {
  var ctype=request.getResponseHeader("Content-Type");
  if(ctype==null) {
    if(request.status==0) {
       //Handle aborted requests in Firefox
       request.aborted = true;
       return null;
    }
    throw new Error("Missing response content type");
  } else if(ctype.indexOf("text/plain")==0 || ctype.indexOf("text/html")==0) {
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
pfx.net.HTTPRequest.prototype._customOnReadyStateChange = function() {

  var win = null;

  for( var i=0; i<pfx.net.HTTPRequest._xml.length; i++ ) {
    if( pfx.net.HTTPRequest._xmlTimer[i] && pfx.net.HTTPRequest._xml[i] ) {

      try {
        if( pfx.net.HTTPRequest._xmlTimerCount[i]<pfx.net.HTTPRequest._xmlTimerCountMax ) {

          win = window.frames['pfxreqiframe'+i];
          if( win && 
              win.document && 
              win.location.href != "about:blank" && 
              (_isMshtml ? win.document.readyState=="complete" : true)) {
            
            var resdoc=_isMshtml ? win.document.body : win.document;
            var text=resdoc.getElementsByTagName("pre")[0].firstChild.nodeValue;
            pfx.net.HTTPRequest._xml[i].call( pfx.net.HTTPRequest._xmlThis[i].context, text,
                                      pfx.net.HTTPRequest._xmlThis[i].getQueryParameter( win.location.href, "PFX_Request_ID") );
            this.cancelOnReadyStateChange(i);
          } else {
            pfx.net.HTTPRequest._xmlTimerCount[i]++;            
          }
        } else {
          this.cancelOnReadyStateChange(i, "too many intervals " + i + ", " + pfx.net.HTTPRequest._xmlTimerCount[i]);
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
pfx.net.HTTPRequest.prototype._cancelOnReadyStateChange = function( i, msg ) {
  try {
    window.clearInterval(pfx.net.HTTPRequest._xmlTimer[i]);
    pfx.net.HTTPRequest._xmlTimer[i] = null;
    pfx.net.HTTPRequest._xml[i] = null;
    pfx.net.HTTPRequest._xmlThis[i] = null;
    pfx.net.HTTPRequest._xmlTimerCount[i] = 0;
  } catch(e) {
    msg = "Could not cancel";
  }

  // remove div, iframe, etc.; speedup by reusing iframes not build in
  var el;
  try {
    if( el = document.getElementById("pfxreqiframe"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}
  try {
    if( el = document.getElementById("pfxreqdiv"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}
  try {
    if( el = document.getElementById("pfxreqform"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}
  try {
    if( el = document.getElementById("pfxreqformdiv"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {}

  if( msg ) {
    throw new Error("HTTP_Request: " + msg);
  }
};
//*****************************************************************************
//*****************************************************************************
