//*****************************************************************************
//
//*****************************************************************************

var _xml = [];
var _xmlThis = [];
var _xmlTimer = [];
var _xmlTimerCount = [];
var _xmlTimerCountMax = 1000;
var _xmlTimerInterval = 5;

//*****************************************************************************
//
//*****************************************************************************
function xmlRequest() {

  this.method   = arguments[0];
  this.url      = arguments[1];
  this.callback = arguments[2];
  this.context  = arguments[3];

  this.iframes  = 1;

  this.msXmlHttp = "";

  this.headers = [ [ 'SOAPAction', '""'] ];
  this.errors = [];
  this.status = 0;
}

xmlRequest.prototype.IFRAMES_NEVER    = -1;
xmlRequest.prototype.IFRAMES_FALLBACK =  0;
xmlRequest.prototype.IFRAMES_ONLY     =  1;

//*****************************************************************************
//
//*****************************************************************************
xmlRequest.prototype.start = function( content ) {

  // unique timestamp to prevent caching
  var uniq = ""+ new Date().getTime() + Math.floor(1000 * Math.random());
  //  this.url += ( ( this.url.indexOf('?')+1 ) ? '&' : '?' ) + uniq;

  var i = _xml.length;

  if( this.iframes!=this.IFRAMES_ONLY ) {

    if( window.XMLHttpRequest ) {

      //----------------
      // XMLHttpRequest
      //----------------

      try {
        _xml[i] = new XMLHttpRequest();
        
      } catch(e) {
        _xml[i] = null;
        if( this.iframes == this.IFRAMES_NEVER ) {
          throw "xmlRequest: Could not create XMLHttpRequest";
        } 
      }
    } else if( window.ActiveXObject ) {

      //---------
      // ActiveX
      //---------

      if( !this.msXmlHttp ) {
        // determine working XMLHTTP component
        var msXmlHttpList = ["MSXML2.XMLHTTP.5.0", "MSXML2.XMLHTTP.4.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "MICROSOFT.XMLHTTP.1.0", "MICROSOFT.XMLHTTP.1", "MICROSOFT.XMLHTTP"];
        
        for (var j=0; j<msXmlHttpList.length; j++) {
          try {
            _xml[i] = new ActiveXObject(msXmlHttpList[j]);
            
            // success ==> store, quit loop
            this.msXmlHttp = msXmlHttpList[j];
            break;
          } catch(e) {
            // ignore failures
          } 
        }
      }

      try {
        _xml[i] = new ActiveXObject(this.msXmlHttp);
      } catch(e) {
        _xml[i] = null;
        if( this.iframes == this.IFRAMES_NEVER ) {
          throw "xmlRequest: Could not create ActiveXObject " + this.msXmlHttp;
        } 
      }
    }
    
    if( _xml[i] ) {

      if( this.callback ) {
        //-------
        // async
        //-------

        try {
          var self = this;

          if( this.msXmlHttp ) {
            _xml[i].onreadystatechange = function() {
              if( _xml[i].readyState == 4 ) {
                self.callback.call( self.context, _xml[i].responseXML); 
              }
            };
          } else {
            _xml[i].onreadystatechange = function() {
              if( _xml[i].readyState == 4 ) {
                self.status = _xml[i].status;
                if( _xml[i].status < 300 ) { 
                  self.callback.call( self.context, _xml[i].responseXML); 
                } else {
                  throw "xmlRequest: Asynchronous call failed" + " (status " + _xml[i].status + ")";
                }
              }
            };
          }

        } catch(e) {
          _xml[i] = null;
          throw "xmlRequest: onreadystatechange failed";
        }
      }

      try {
        _xml[i].open( this.method, this.url, this.callback ? true : false); 
          
        for( var j=0; j<this.headers.length; j++ ) {
          _xml[i].setRequestHeader( this.headers[j][0], this.headers[j][1] );
        }
          
        _xml[i].send(content);
          
        if( !this.callback ) {
          return _xml[i].responseXML;
        } else {
          return true;
        }
      } catch(e) {
        _xml[i] = null;
        throw "xmlRequest: call failed";
      }  
    }
  }

  if( this.iframes!=this.IFRAMES_NEVER && !_xml[i] && document.createElement && document.childNodes ) {

    //--------
    // iframe
    //--------

    try {

      var iframe;

      //--------------
      // result frame
      //--------------

      iframe = document.createElement("iframe");
      iframe.style.visibility = "hidden";
      //     iframe.style.position   = "absolute";
      //     iframe.style.left       = "0px";
      //     iframe.style.top        = "0px";
      iframe.style.width      = "0px";
      iframe.style.height     = "0px";    
      iframe.id               = "pfxxmliframe"+i;
      iframe.name             = "pfxxmliframe"+i;

      if( this.method.toUpperCase() == "GET" ) {

        //-----
        // GET
        //-----

        iframe.src            = this.url;
        document.body.appendChild(iframe);
        _xmlTimer[i] = window.setInterval('customOnReadyStateChange()', _xmlTimerInterval);

      } else {

        //-------------------------------------------
        // POST (or others, which are not supported)
        //-------------------------------------------

        document.body.appendChild(iframe);

        //------------
        // form frame
        //------------

        iframe = document.createElement("iframe");
        iframe.style.visibility = "hidden";
        //     iframe.style.position   = "absolute";
        //     iframe.style.left       = "0px";
        //     iframe.style.top        = "0px";
        iframe.style.width      = "0px";
        iframe.style.height     = "0px";    
        iframe.id               = "pfxxmliframeform"+i;
        iframe.name             = "pfxxmliframeform"+i;

        document.body.appendChild(iframe);

        _xml[i] = this.callback;
        _xmlThis[i] = this;
        _xmlTimer[i] = true;
        _xmlTimerCount[i] = 0;

        var self = this;
        setTimeout( function() {

          var iDoc;
          try {
            iDoc = iframe.contentWindow.document;
          } catch(e) {
            try {
              iDoc =  iframe.contentDocument;
            } catch(e) {
              throw "could not use iframe";
            }
          }

          var form = iDoc.createElement("form");
          form.action = self.url;
          form.target = "pfxxmliframe"+i;
          form.method = self.method;

          var field = iDoc.createElement("textarea");
          field.name = "soapmessage";
          field.value = content;
          form.appendChild(field);

          iDoc.body.appendChild(form);
        
          iDoc.forms[iDoc.forms.length-1].submit();

          _xmlTimer[i] = window.setInterval('customOnReadyStateChange()', _xmlTimerInterval);
        }, 5 );
      }

      return "iframe";
    } catch(e) {
      throw "xmlRequest: Iframes failed";
    }
  }

  throw "xmlRequest: failed";
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
function customOnReadyStateChange() {

  for( var i=0; i<_xml.length; i++ ) {
    if( _xmlTimer[i] && _xml[i] ) {

      try {
        if( _xmlTimerCount[i]<_xmlTimerCountMax ) {

          if( window.frames['pfxxmliframe'+i] && window.frames['pfxxmliframe'+i].document && window.frames['pfxxmliframe'+i].document.body && !window.frames['pfxxmliframe'+i].document.body.innerHTML && 
              (/blank\.hmtl$/.test(window.frames['pfxxmliframe'+i].location) || /about\:blank$/.test(window.frames['pfxxmliframe'+i].location)) ) {
            _xmlTimerCount[i]++;
          } else {

            _xml[i].call( _xmlThis[i].context, window.frames['pfxxmliframe'+i].document );
            _xml[i] = null;
            cancelOnReadyStateChange(i);
          }
        } else {
          cancelOnReadyStateChange(i, "too many intervals");
        }
      } catch(e) {
        cancelOnReadyStateChange(i, "Exception:" + e);
      }
    }
  }
}

//*****************************************************************************
//
//*****************************************************************************
function cancelOnReadyStateChange( i, msg ) {

  try {
    if( msg ) {
      //      alert("cancelOnReadyStateChange:" + i + ", " + msg);
    }
    window.clearInterval(_xmlTimer[i]);
    _xmlTimer[i] = null;
  } catch(e) {
    alert("Exception2:" + e);
  }

  try {
    var iframe;
    if( iframe = document.getElementById("pfxxmliframe"+i) ) {
      document.body.removeChild( iframe );
    }
    if( iframe = document.getElementById("pfxxmliframeform"+i) ) {
      document.body.removeChild( iframe );
    }
  } catch(e) {
    alert("Exception3:" + e);
  }
}
//*****************************************************************************
//*****************************************************************************
