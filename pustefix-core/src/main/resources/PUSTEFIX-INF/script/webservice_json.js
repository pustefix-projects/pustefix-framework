if(!window.pfx) pfx={};
if(!pfx.ws) pfx.ws={};
if(!pfx.ws.json) pfx.ws.json={}; 

pfx.ws.json.deserialize=function(response) {
   if(this._debug==true) alert("Response: "+response);
	eval("res="+response);
	return res;
};

pfx.ws.json.serialize=function(obj) {
	var json=null;
	if(obj!=null) {
		if(obj.constructor==String) {
			json=pfx.ws.json.escapeJSONString(obj);
    	} else if(obj.constructor==Number) {
			json=obj.toString();
    	} else if(obj.constructor==Boolean) {
			json=obj.toString();
   	} else if(obj.constructor==Date) {
			json='new Date('+obj.valueOf()+')';
    	} else if(obj.constructor==Array) {
			var arr=[];
			for(var i=0;i<obj.length;i++) arr.push(pfx.ws.json.serialize(obj[i]));
			json="["+arr.join(",")+"]";
    	} else {
			var arr=[];
			for(attr in obj) {
	    		var attrObj=obj[attr];
	    		if(attrObj==null) arr.push("\""+attr+"\": null");
	    		else if(typeof attrObj!="function") 
	    			arr.push(pfx.ws.json.escapeJSONString(attr)+":"+pfx.ws.json.serialize(attrObj));
			}
			json="{"+arr.join(",")+"}";
		}
	} else json="null";
	return json;
};

pfx.ws.json.escapeJSONChar=function(ch) {
   if(ch=="\""||ch=="\\") return "\\"+ch;
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

pfx.ws.json.replaceCallbackSupport=null;

pfx.ws.json.escapeJSONString=function(str) {
   if(pfx.ws.json.replaceCallbackSupport==null) {
      if(navigator.userAgent.indexOf("Safari")!=-1) pfx.ws.json.replaceCallbackSupport=false;
      else pfx.ws.json.replaceCallbackSupport=true;
   }
   if(pfx.ws.json.replaceCallbackSupport) {
      return "\""+str.replace(/([^\u0020-\u007f]|[\\\"])/g,pfx.ws.json.escapeJSONChar)+"\"";
   } else {
      var escStr=[];
      for(var i=0;i<str.length;i++) {
        if(str.charAt(i)=='"'||str.charAt(i)=='\\'||str.charCodeAt(i)<32||str.charCodeAt(i)>=128) {
           escStr[i]=pfx.ws.json.escapeJSONChar(str.charAt(i));
        } else {
           escStr[i]=str.charAt(i);
        }
      }
      return "\""+escStr.join("")+"\"";
   }
};

//
// BaseStub
//

pfx.ws.json.BaseStub=function(service,context,scope) {
   this._service=service;
   this._context=context;
   this._scope=scope;
   this._requestPath="/webservice";
   this._protocol="jsonws";
   this._uri=null;
};

pfx.ws.json.BaseStub.prototype.setService=function(service) {
   this._service=service;
   this._uri=null;
};

pfx.ws.json.BaseStub.prototype.setContext=function(context) {
   this._context=context;
};

pfx.ws.json.BaseStub.prototype.setRequestPath=function(requestPath) {
   this._requestPath=requestPath;
   this._uri=null;
};

pfx.ws.json.BaseStub.prototype.setDebug=function(debug) {
   this._debug=debug;
};

pfx.ws.json.BaseStub.prototype.getURI=function() {
   if(this._uri==null) {
      var session=window.location.href.match(/;jsessionid=[A-Z0-9]+(\.[a-zA-Z0-9]+)?/)[0];
      var reqpath=window.location.pathname;
      var pcs=reqpath.split('/');
      pcs=pcs.slice(1,pcs.length-3);
      reqpath="";
      for (var i=0;i<pcs.length;i++) reqpath+="/"+pcs[i];
      reqpath=reqpath+this._requestPath+"/"+this._service;
      this._uri=window.location.protocol+"//"+window.location.host+reqpath+session;
   }
   return this._uri;
};

pfx.ws.json.BaseStub.prototype.callMethod=function(method,args,expLen) {
   var wsCall=new pfx.ws.json.Call(this.getURI(),this._context,this._scope,this._debug);
   return wsCall.invoke(method,args,expLen);
};


//
// DynamicProxy
//

pfx.ws.json.DynamicProxy=function(service,context) {
   pfx.ws.json.BaseStub.call(this,service,context);
   this._proxySetup();
};

pfx.ws.json.DynamicProxy.prototype=new pfx.ws.json.BaseStub;

pfx.ws.json.DynamicProxy.prototype._proxySetup=function() {
   var req=new pfx.net.HTTPRequest('POST',this.getURI()+'?json',this._proxySetupCB,this);
   req.setRequestHeader('wstype',this._protocol);
   req.start('',0);
};

pfx.ws.json.DynamicProxy.prototype._proxySetupCB=function(response) {
   eval("response="+response);
   var methods=response.result;
   for(var i=0;i<methods.length;i++) {
      this._createMethod(methods[i]);
   }
};

pfx.ws.json.DynamicProxy.prototype._createMethod=function(name) {
   var f=function() {
      return f.ownerObject._callMethod.call(f.ownerObject,f.methodName,arguments);
   };
   f.ownerObject=this;
   f.methodName=name;
   this[name]=f;
};

pfx.ws.json.DynamicProxy.prototype._callMethod=function(method,args) {
   var wsCall=new pfx.ws.json.Call(this.getURI(),this._context,this._debug);
   return wsCall.invoke(method,args);
};



//
// Call
//

pfx.ws.json.Call=function(uri,context,scope,debug) {
   this._uri=uri;
   this._context=context;
   this._scope=scope;
   this._debug=debug;
   this._opName=null;
   this._userCallback=null;
};

pfx.ws.json.Call.prototype.invoke=function(method,args,expLen) {
   this._opName=method;
   var jsonReq={};
   jsonReq.method=method;
   
   var argLen=args.length;
 
   if(expLen) {
      if(argLen==expLen+1) {
         if(typeof args[argLen-1]=="function" || typeof args[argLen-1]=="object" ) this._userCallback=args[argLen-1];
         else this._requestId=args[argLen-1];
         argLen=argLen-1;
      } else if(argLen==expLen+2 && (typeof args[argLen-2]=="function" || typeof args[argLen-2]=="object") && typeof args[argLen-1]=="string") {
         this._userCallback=args[argLen-2];
         this._requestId=args[argLen-1];
         argLen=argLen-2;
      } else if(argLen!=expLen) throw new Error("Wrong number of arguments: "+argLen+" - "+expLen);
   } else {
      if(argLen>0 && typeof args[argLen-1]=='function') {
         this._userCallback=args[argLen-1];
         argLen=argLen-1;
      } else if(argLen>1 && typeof args[argLen-2]=='function') {
         this._requestId=args[argLen-1];
         this._userCallback=args[argLen-2];
         argLen=argLen-2;
      }
   }
   if(this._requestId) jsonReq.id=this._requestId;
      
   var jsonParams=[];
   for(var i=0;i<argLen;i++) jsonParams[i]=args[i];
   jsonReq.params=jsonParams;
   var jsonStr=pfx.ws.json.serialize(jsonReq);
  
   if(this._debug==true) alert("Request: "+jsonStr);
   if(this._userCallback || this._context) {
      var httpReq=new pfx.net.HTTPRequest('POST',this._uri,this.callback,this);
      httpReq.setRequestHeader("Content-Type","text/plain");
      httpReq.setRequestHeader("wstype","jsonws");
      httpReq.start(jsonStr,null,jsonReq.id);
   } else {
      var httpReq=new pfx.net.HTTPRequest('POST',this._uri);
      httpReq.setRequestHeader("Content-Type","text/plain");
      httpReq.setRequestHeader("wstype","jsonws");
      var response=httpReq.start(jsonStr,null,jsonReq.id);
      return this.callback(response);
   }
};

pfx.ws.json.Call.prototype.callback=function(text) {
   var res=pfx.ws.json.deserialize(text);
   if(res.error) {
     var error=new Error();
     error.name=res.error.name;
     error.message=res.error.message;
     if(this._userCallback) this._userCallback(null,res.id,error);
     else if(this._context) this._context[this._opName].call(this._scope?this._scope:this._context,null,res.id,error);
     else throw error;
   } else {
     if(this._userCallback) this._userCallback(res.result,res.id,null);
     else if(this._context) this._context[this._opName].call(this._scope?this._scope:this._context,res.result,res.id,null);
     else return res.result;
   }
};
