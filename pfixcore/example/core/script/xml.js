//*****************************************************************************
// xml.js
//*****************************************************************************

//-------------------
// browser detection
//-------------------

var _browser;
if (window.opera)
  _browser = "opera";
else if (typeof navigator.vendor=="string" && navigator.vendor=="KDE")
  _browser = "khtml";
else if (typeof navigator.product=="string" && navigator.product=="Gecko")
  _browser = "gecko";
else if (/msie/i.test(navigator.userAgent))
  _browser = "mshtml";

//*****************************************************************************
//
//*****************************************************************************
function xmlRequest() {

  this.method   = arguments[0];
  this.url      = arguments[1];
  this.callback = arguments[2];
  this.context  = arguments[3];

  this.errors = [];
  this.status = 0;
  this.statusText = "";

  var self = this;
  this.customOnReadyStateChange = function() { self._customOnReadyStateChange(); };
  this.cancelOnReadyStateChange = function() { self._cancelOnReadyStateChange(); };
}

xmlRequest._xml = [];
xmlRequest._xmlThis = [];
xmlRequest._xmlTimer = [];
xmlRequest._xmlTimerCount = [];
xmlRequest._xmlTimerCountMax = 1000;
xmlRequest._xmlTimerInterval = 5;

xmlRequest.builtin = window.XMLHttpRequest ? true : false;
xmlRequest.activeX = window.ActiveXObject  ? true : false;

// iframe
xmlRequest.IFRAMES_NEVER    = -1;
xmlRequest.IFRAMES_FALLBACK =  0;
xmlRequest.IFRAMES_ONLY     =  1;

// set iframe behaviour
xmlRequest.prototype.iframes  = xmlRequest.IFRAMES_FALLBACK;
if( /^opera$/.test(_browser) ) {
  // opera 7.6 pr1 supports XMLHttpRequest but not setRequestHeader(), yet
  xmlRequest.prototype.iframes = 1;
}

xmlRequest.prototype.headers = [ [ 'SOAPAction', '""'] ];

xmlRequest.msXmlHttp = null;
if( xmlRequest.activeX ) {
  // determine working XMLHTTP component
  var msXmlHttpList = ["MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1", "MICROSOFT.XMLHTTP"];
      
  var obj;
  for (var j=0; j<msXmlHttpList.length; j++) {
    try {
      obj = new ActiveXObject(msXmlHttpList[j]);
      
      // success ==> store, quit loop
      xmlRequest.msXmlHttp = msXmlHttpList[j];
      break;
    } catch(e) {
      // ignore failures
    } 
  }
}

//*****************************************************************************
//
//*****************************************************************************
xmlRequest.prototype.start = function( content ) {

  // unique timestamp to prevent caching
  var uniq = ""+ new Date().getTime() + Math.floor(1000 * Math.random());
  //  this.url += ( ( this.url.indexOf('?')+1 ) ? '&' : '?' ) + uniq;

  var i = xmlRequest._xml.length;

  if( this.iframes!=xmlRequest.IFRAMES_ONLY ) {

    if( xmlRequest.builtin ) {

      //----------------
      // XMLHttpRequest
      //----------------

      try {
        xmlRequest._xml[i] = new XMLHttpRequest();
        
      } catch(e) {
        xmlRequest._xml[i] = null;
        if( this.iframes == xmlRequest.IFRAMES_NEVER ) {
          throw new Error("xmlRequest: Could not create XMLHttpRequest");
        } 
      }
    } else if( xmlRequest.activeX ) {

      //---------
      // ActiveX
      //---------

      try {
        xmlRequest._xml[i] = new ActiveXObject(xmlRequest.msXmlHttp);
      } catch(e) {
        xmlRequest._xml[i] = null;
        if( this.iframes == xmlRequest.IFRAMES_NEVER ) {
          throw new Error("xmlRequest: Could not create ActiveXObject " + xmlRequest.msXmlHttp);
        } 
      }
    }
    
    if( xmlRequest._xml[i] ) {

      if( this.callback ) {
        //-------
        // async
        //-------

        try {
          var self = this;

          if( xmlRequest.activeX ) {
            xmlRequest._xml[i].onreadystatechange = function() {
              if( xmlRequest._xml[i].readyState == 4 ) {
                self.callback.call( self.context, xmlRequest._xml[i].responseXML);
                xmlRequest._xml[i] = null;
              }
            };
          } else {
            xmlRequest._xml[i].onreadystatechange = function() {
              if( xmlRequest._xml[i].readyState == 4 ) {
                try {
                  self.status     = xmlRequest._xml[i].status;
                  self.statusText = xmlRequest._xml[i].statusText;
                } catch(e) {
                }
                if( self.status && self.status >= 300 ) {
                  throw new Error("xmlRequest: Asynchronous call failed" + " (status " + self.status + ", " + self.statusText + ")");
                }
                self.callback.call( self.context, xmlRequest._xml[i].responseXML );
                xmlRequest._xml[i] = null;
              }
            };
          }

        } catch(e) {
          xmlRequest._xml[i] = null;
          throw new Error("xmlRequest: Onreadystatechange failed");
        }
      }


      try {
        xmlRequest._xml[i].open( this.method, this.url, this.callback ? true : false); 

        for( var j=0; j<this.headers.length; j++ ) {
          try {
            // not implemented in Opera 7.6 preview1
            xmlRequest._xml[i].setRequestHeader( this.headers[j][0], this.headers[j][1] );
          } catch(e) {
          }
        }

        xmlRequest._xml[i].send(content);
          
        if( !this.callback ) {
          this.status=xmlRequest._xml[i].status;
          this.statusText=xmlRequest._xml[i].statusText;
          return xmlRequest._xml[i].responseXML;
        } else {
          return true;
        }
      } catch(e) {
        xmlRequest._xml[i] = null;
        throw new Error("xmlRequest: Call failed");
      }  
    } else {
      return false;
    }
  }

  if( this.iframes!=xmlRequest.IFRAMES_NEVER && !xmlRequest._xml[i] && document.createElement ) {

    //--------
    // iframe
    //--------

	if( !this.callback ) {
		throw new Error("xmlRequest: Synchronous call by iframe not supported"); 
	}

    try {

      //--------------
      // result frame
      //--------------

      var el;

      if( !/^(mshtml|opera)$/i.test(_browser) ) {
        el = document.createElement("iframe");
        el.style.display = "none";
        el.name          = "pfxxmliframe"+i;
        el.id            = "pfxxmliframe"+i;
      } else {
        el = document.createElement("div");
        el.style.display = "none";
        el.id            = "pfxxmldiv"+i;
        document.body.appendChild(el);
      }

      if( this.method.toLowerCase() == "get" ) {

        //-----
        // GET
        //-----

        el.src = this.url;
        document.body.appendChild(el);
        var self = this;
        xmlRequest._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, xmlRequest._xmlTimerInterval);

      } else if( this.method.toLowerCase() == "post" ) {

        //------
        // POST
        //------

        if( /^(mshtml|opera)$/i.test(_browser) ) {

          document.getElementById("pfxxmldiv"+i).innerHTML = '<' + 'iframe id="pfxxmliframe' + i + '" name="pfxxmliframe' + i + '" style="display:block"><' + '/iframe>';
        } else {
          document.body.appendChild(el);
        }

        //------------
        // form frame
        //------------

        el = document.createElement("div");
        el.style.display = "none";
        el.id               = "pfxxmlformdiv"+i;

        xmlRequest._xml[i] = this.callback;
        xmlRequest._xmlThis[i] = this;
        xmlRequest._xmlTimer[i] = true;
        xmlRequest._xmlTimerCount[i] = 0;

        var self = this;
        window.setTimeout( function() {

          var elForm = document.createElement("form");
          elForm.action = self.url;
          elForm.target = "pfxxmliframe"+i;
          elForm.method = self.method;
          elForm.id     = "pfxxmlform"+i;
          
          var elField = document.createElement("textarea");
          elField.name = "soapmessage";
          elField.value = content;
          elForm.appendChild(elField);
          el.appendChild(elForm);
          document.body.appendChild(el);
          
          document.getElementById("pfxxmlform"+i).target = "pfxxmliframe"+i;

          window.setTimeout( function() {
            document.forms[document.forms.length-1].submit();
          }, 1 );
        }, 1 );        

        xmlRequest._xmlTimer[i] = window.setInterval( self.customOnReadyStateChange, xmlRequest._xmlTimerInterval);
      } else {
        // method other than GET or POST are not supported
        throw new Error("xmlRequest: Iframes do not support method " + this.method);
      }

      return "iframe";
    } catch(e) {
      throw new Error("xmlRequest: Iframes failed" + e);
    }
  }

  throw new Error("xmlRequest: Failure");
};

//*****************************************************************************
//
//*****************************************************************************
xmlRequest.prototype.setRequestHeader = function( field, value ) {
  
  this.headers.push( [field, value] );
};

//*****************************************************************************
//
//*****************************************************************************
xmlRequest.prototype._customOnReadyStateChange = function() {

  for( var i=0; i<xmlRequest._xml.length; i++ ) {
    if( xmlRequest._xmlTimer[i] && xmlRequest._xml[i] ) {

      try {
        if( xmlRequest._xmlTimerCount[i]<xmlRequest._xmlTimerCountMax ) {

          if( window.frames['pfxxmliframe'+i] && window.frames['pfxxmliframe'+i].document && window.frames['pfxxmliframe'+i].location == "about:blank" ) {
            xmlRequest._xmlTimerCount[i]++;
          } else {
           
            xmlRequest._xml[i].call( xmlRequest._xmlThis[i].context, window.frames['pfxxmliframe'+i].document );
            xmlRequest._xml[i] = null;
            this.cancelOnReadyStateChange(i); //, "regular finalization");
          }
        } else {
          this.cancelOnReadyStateChange(i); //, "too many intervals");
        }
      } catch(e) {
        this.cancelOnReadyStateChange(i); //, "Exception:" + e);
      }
    }
  }
};

//*****************************************************************************
//
//*****************************************************************************
xmlRequest.prototype._cancelOnReadyStateChange = function( i, msg ) {

  try {
    if( msg ) {
      alert("cancelOnReadyStateChange:" + i + ", " + msg);
    }
    window.clearInterval(xmlRequest._xmlTimer[i]);
    xmlRequest._xmlTimer[i] = null;
    xmlRequest._xml[i] = null;
    xmlRequest._xmlThis[i] = null;
    xmlRequest._xmlTimerCount[i] = 0;
  } catch(e) {
    throw new Error("xmlRequest: Could not cancel");
  }

  try {
    var el;
    if( el = document.getElementById("pfxxmliframe"+i) ) {
      document.body.removeChild(el);
    }
    if( el = document.getElementById("pfxxmldiv"+i) ) {
      document.body.removeChild(el);
    }
    if( el = document.getElementById("pfxxmlform"+i) ) {
      document.body.removeChild(el);
    }
    if( el = document.getElementById("pfxxmlformdiv"+i) ) {
      document.body.removeChild(el);
    }
  } catch(e) {
  }
};
//*****************************************************************************
//*****************************************************************************
