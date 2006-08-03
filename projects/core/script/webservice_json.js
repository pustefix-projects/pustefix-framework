function WS_Webservice(service,uri,protocol) {
	this._service=service;
	if(uri) {
		this._uri=uri;
	} else {
  		var session=window.location.href.match(/;jsessionid=[A-Z0-9]+\.[a-zA-Z0-9]+/);
  		this._uri=window.location.protocol+"//"+window.location.host+"/xml/webservice/"+service+session;
	}
   this._calls={};
   this._ids=0;
   if(protocol) {
   	this._protocol=protocol;
   } else {
   	this._protocol="jsonws";
   }
   this._proxySetup();
}

WS_Webservice.prototype._proxySetup=function() {
	var req=new HTTP_Request('POST',this._uri+'?json',this._proxySetupCB,this);
   req.setRequestHeader('wstype',this._protocol);
   req.start('',0);
}

WS_Webservice.prototype._proxySetupCB=function(response) {
	eval("response="+response);
	var methods=response.result;
	for(var i=0;i<methods.length;i++) {
		this._createMethod(methods[i]);
	}
}

WS_Webservice.prototype._createMethod=function(name) {
	var f=function() {
		return f.ownerObject._callMethod.call(f.ownerObject,f.methodName,arguments);
	};
	f.ownerObject=this;
	f.methodName=name;
	this[name]=f;
}

WS_Webservice.prototype._callMethod=function(method,args) {
	var wsCall={};
	var jsonReq={};
	jsonReq.id=this._ids++;
	if(this._protocol=="jsonrpc") {
		jsonReq.method=this._service+"."+method;
	} else {
		jsonReq.method=method;
	}
	var arglen=args.length;
	if(arglen>0 && typeof args[arglen-1]=='function') {
		wsCall.callback=args[arglen-1];
		arglen=arglen-1;
	} else if(arglen>1 && typeof args[arglen-2]=='function') {
		wsCall.requestID=args[arglen-1];
		wsCall.callback=args[arglen-2];
		arglen=arglen-2;
	}
	var jsonParams=[];
	for(var i=0;i<arglen;i++) jsonParams[i]=args[i];
	jsonReq.params=jsonParams;
	var jsonStr=this._serialize(jsonReq);
   if(wsCall.callback) {
 		var httpReq=new HTTP_Request('POST',this._uri,this._returnMethod,this);
		httpReq.setRequestHeader("Content-Type","text/plain");
    	httpReq.start(jsonStr,null,wsCall.requestID);
    	this._calls[jsonReq.id]=wsCall;
   } else {	
   	var httpReq=new HTTP_Request('POST',this._uri);
     	httpReq.setRequestHeader("Content-Type","text/plain");
      var response=httpReq.start(jsonStr,null,wsCall.requestID);
     	response=this._deserialize(response);
     	return this._createResult(response);
   }
}

WS_Webservice.prototype._returnMethod=function(response) {
	var response=this._deserialize(response);
	var result=this._createResult(response);
	var cb=this._calls[response.id].callback;
	cb(result);
}

WS_Webservice.prototype._deserialize=function(response) {
	eval("response="+response);
	return response;
}

WS_Webservice.prototype._createResult=function(response) {
	if(response.error) {
		var error=new Error();
		error.name=response.error.name;
		error.message=response.error.msg;
		return error;
	} else {
		return response.result;
	}
}

WS_Webservice.prototype._serialize=function(obj) {
	var json=null;
	if(obj!=null) {
		if(obj.constructor==String) {
			json=this._escapeJSONString(obj);
    	} else if(obj.constructor==Number) {
			json=obj.toString();
    	} else if(obj.constructor==Boolean) {
			json=obj.toString();
   	} else if(obj.constructor==Date) {
			json='new Date('+obj.valueOf()+')';
    	} else if(obj.constructor==Array) {
			var arr=[];
			for(var i=0;i<obj.length;i++) arr.push(this._serialize(obj[i]));
			json="["+arr.join(",")+"]";
    	} else {
			var arr=[];
			for(attr in obj) {
	    		var attrObj=obj[attr];
	    		if(attrObj==null) arr.push("\""+attr+"\": null");
	    		else if(typeof attrObj!="function") 
	    			arr.push(this._escapeJSONString(attr)+":"+this._serialize(attrObj));
			}
			json="{"+arr.join(",")+"}";
		}
	}
	return json;
};

WS_Webservice.prototype._escapeJSONChar=function(ch) {
   if(ch=="\""||ch=="\\") return "\\"+c;
   else if(ch=="\b") return "\\b";
   else if(ch=="\f") return "\\f";
   else if(ch=="\n") return "\\n";
   else if(ch=="\r") return "\\r";
   else if(ch=="\t") return "\\t";
   var hex=ch.charCodeAt(0).toString(16);
   if(hex.length==1) return "\\u000"+hex;
   else if(hex.length==2) return "\\u00"+hex;
   else if(hex.length==3) return "\\u0"+hex;
   else return "\\u"+hex;
};

WS_Webservice.prototype._escapeJSONString=function(str) {
	return "\""+str.replace(/([^\u0020-\u007f]|[\\\"])/g,this._escapeJSONChar)+"\"";
};

