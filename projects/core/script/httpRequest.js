if(!window.pfx) pfx = {};
if(!pfx.net) pfx.net = {};

pfx.net.HTTPRequest = function(method, url, callback, context) { 
  this.method   = method;
  this.url      = url;
  this.callback = callback;
  this.context  = context;
  this.headers  = [];
}

//User-definable constants:
pfx.net.HTTPRequest.MAXIMUM_ACTIVE_REQUESTS = 5;
pfx.net.HTTPRequest.REQUEST_ID_START = 100000;

pfx.net.HTTPRequest.prototype.setRequestHeader=function(name,value) {
  this.headers.push( [name, value] );
}

pfx.net.HTTPRequest.prototype.start=function( content, headers, reqId ) {
  var req = {};
  req.client = this;
  req.reqId=reqId;
  if(reqId) req.reqId = reqId;
  else req.reqId = pfx.net.HTTPRequest._generateRequestId();
  req.data = content;
  if(this.callback == null) {
    return this._sendRequest.call(this,req);
  } else {
    req.callback=this.callback;
    req.context=this.context;
    pfx.net.HTTPRequest._requests.push(req);
    pfx.net.HTTPRequest._triggerSchedule();
    return true;
  }
};

pfx.net.HTTPRequest._requestId = 1;

pfx.net.HTTPRequest._generateRequestId=function() {
  if(pfx.net.HTTPRequest._requestId<pfx.net.HTTPRequest.REQUEST_ID_START) 
    pfx.net.HTTPRequest._requestId=pfx.net.HTTPRequest.REQUEST_ID_START;
  else pfx.net.HTTPRequest._requestId++;
  return pfx.net.HTTPRequest._requestId;
}

pfx.net.HTTPRequest._requests = [];
pfx.net.HTTPRequest._scheduled = {};
pfx.net.HTTPRequest._responses = [];
pfx.net.HTTPRequest._timeout = null;
pfx.net.HTTPRequest._active = 0;

pfx.net.HTTPRequest._schedule = function() {
  pfx.net.HTTPRequest._timeout = null;
  while(pfx.net.HTTPRequest._responses.length > 0) {
	var res = pfx.net.HTTPRequest._responses.shift();
	if(res.canceled) continue;
	res.callback.call( res.context, res.data, res.reqId );
  }
  while(pfx.net.HTTPRequest._requests.length > 0 &&
        pfx.net.HTTPRequest._active < pfx.net.HTTPRequest.MAXIMUM_ACTIVE_REQUESTS) {
	var req = pfx.net.HTTPRequest._requests.shift();
	if(req.canceled) continue;
	req.client._sendRequest.call(req.client, req);
  }
};

pfx.net.HTTPRequest._triggerSchedule = function() {
  if(pfx.net.HTTPRequest._timeout == null)
	pfx.net.HTTPRequest._timeout = setTimeout(pfx.net.HTTPRequest._schedule, 0);
	
};

pfx.net.HTTPRequest.cancelRequest = function(reqId) {
  if(pfx.net.HTTPRequest._scheduled[reqId]) {
	pfx.net.HTTPRequest._scheduled[reqId].canceled = true;
	return true;
  }
  for(var i in pfx.net.HTTPRequest._requests) {
	if(pfx.net.HTTPRequest._requests[i].reqId == reqId) {
	  pfx.net.HTTPRequest._requests[i].canceled = true;
	  return true;
	}
  }
  for(var i in pfx.net.HTTPRequest._responses) {
	if(pfx.net.HTTPRequest._responses[i].reqId == reqId) {
	  pfx.net.HTTPRequest._responses[i].canceled = true;
	  return true;
	}
  }
  return false;
};

pfx.net.HTTPRequest.prototype._sendRequest = function(req) {
  var http = pfx.net.HTTPRequest.poolGetHTTPRequest();
  pfx.net.HTTPRequest._active++;
  http.open("POST", this.url, (req.callback != null));
  try { 
    http.setRequestHeader("Content-type", "text/plain"); 
    for(var j=0; j<this.headers.length; j++ ) {
      http.setRequestHeader( this.headers[j][0], this.headers[j][1] );
    }
  } catch(e) {}
  if(req.callback) {
	 var self = this;
	 http.onreadystatechange = function() {
	   if(http.readyState == 4) {
		  http.onreadystatechange = function () {};
		  var res={"callback":req.callback, "context":req.context, "data":null, "reqId":req.reqId};
		  try { 
		    res.data = self._handleResponse(http);
		  } catch(e) { res.ex = e; }
		  if(!pfx.net.HTTPRequest._scheduled[req.reqId].canceled) {
		    pfx.net.HTTPRequest._responses.push(res);
		  }
		  delete pfx.net.HTTPRequest._scheduled[req.reqId];
		  pfx.net.HTTPRequest._triggerSchedule();
	   }
	 };
  } else {
	http.onreadystatechange = function() {};
  }
  pfx.net.HTTPRequest._scheduled[req.reqId] = req;
  try {
 
	http.send(req.data);
  } catch(e) {
	pfx.net.HTTPRequest.poolReturnHTTPRequest(http);
	pfx.net.HTTPRequest._active--;
	throw new Error("Connection failed");
  }
  if(!req.callback) return this._handleResponse(http);
};

pfx.net.HTTPRequest.prototype._handleResponse = function(http) {
  var status, statusText, data, ctype;
  try {
	status = http.status;
	statusText = http.statusText;
	ctype = http.getResponseHeader("Content-Type");
    if(ctype!=null) {
      if(ctype.indexOf("text/plain")>-1) {
        data = http.responseText;
      } else if(ctype.indexOf("text/xml")>-1) {
        data = http.responseXML;
      }
    }
  } catch(e) {
	pfx.net.HTTPRequest.poolReturnHTTPRequest(http);
	pfx.net.HTTPRequest._active--;
	pfx.net.HTTPRequest._triggerSchedule();
	throw new Error("Request failed");
  }
  pfx.net.HTTPRequest.poolReturnHTTPRequest(http);
  pfx.net.HTTPRequest._active--;
  if(!(status==200 || (status==500 && ctype.indexOf("text/xml")>-1))) {
    throw new Error("Response error: "+statusText);
  }
  if(data == null) {
    throw new Error("No response content");
  }
  return data;
};

pfx.net.HTTPRequest.http_spare = [];
pfx.net.HTTPRequest.http_max_spare = 8;

pfx.net.HTTPRequest.poolGetHTTPRequest = function() {
  if(pfx.net.HTTPRequest.http_spare.length > 0) {
	return pfx.net.HTTPRequest.http_spare.pop();
  }
  return pfx.net.HTTPRequest.getHTTPRequest();
};

pfx.net.HTTPRequest.poolReturnHTTPRequest = function(http) {
  if(pfx.net.HTTPRequest.http_spare.length >= pfx.net.HTTPRequest.http_max_spare)
	delete http;
  else
	pfx.net.HTTPRequest.http_spare.push(http);
};

pfx.net.HTTPRequest.msxmlNames = [ 
  "MSXML2.XMLHTTP.5.0",
  "MSXML2.XMLHTTP.4.0",
  "MSXML2.XMLHTTP.3.0",
  "MSXML2.XMLHTTP",
  "Microsoft.XMLHTTP" 
];

pfx.net.HTTPRequest.getHTTPRequest = function() {
  try {
	return new XMLHttpRequest();
  } catch(e) {}
  for (var i=0;i < pfx.net.HTTPRequest.msxmlNames.length; i++) {
	try {
      return new ActiveXObject(pfx.net.HTTPRequest.msxmlNames[i]);
	} catch (e) {}
  }
  throw new Error("XMLHttpRequest not available");
};

pfx.net.HTTPRequest.isAvailable = function() {
  try {
    var http = pfx.net.HTTPRequest.poolGetHTTPRequest();
    pfx.net.HTTPRequest.poolReturnHTTPRequest(http);
    return true;
  } catch(e) {
    return false;
  }
}