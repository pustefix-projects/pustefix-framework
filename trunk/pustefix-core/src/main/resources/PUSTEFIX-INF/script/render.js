if(!window.pfx) pfx={};

// Remotely renders an include part and passes the output to a callback function.
// Arguments can be passed as list (deprecated) or as single JSON object argument:
//   href          - path to include file
//   part          - include part name
//   module        - module name (optional)
//   search        - search type (optional)
//   callback      - callback function (optional)
//   context       - callback object scope (optional)
//   requestId     - request/response assignment id (optional)
//   params        - additional request parameters (optional)
//   requestPath   - alternative request URL path (optional)
//   errorCallback - callback function for HTTP errors (optional)
pfx.render = function(href, part, module, search, callback, context, requestId, params, requestPath, errorCallback) {
   if(arguments.length == 1) {
      var arg = arguments[0];
      href = arg.href;
      part = arg.part;
      module = arg.module;
      search = arg.search;
      callback = arg.callback;
      context = arg.context;
      requestId = arg.requestId;
      params = arg.params;
      requestPath = arg.requestPath;
      errorCallback = arg.errorCallback;
   }
   if(!href) throw new Error("Missing parameter: href");
   if(!part) throw new Error("Missing parameter: part");
   if(!callback) throw new Error("Missing parameter: callback");
   var url = window.location.href;
   var ind = url.indexOf('?');
   if(ind > -1) url = url.substring(0, ind);
   ind = url.indexOf('#');
   if(ind > -1) url = url.substring(0, ind);
   if(requestPath) {
	   if(requestPath.charAt(0) == '/') {
		   requestPath = requestPath.substring(1);
	   }
	   var start = url;
	   var end = "";
	   ind = url.indexOf(';');
	   if(ind > -1) {
	       start = url.substring(0, ind);
	       end = url.substring(ind);
	   }
       url = start + ( start.charAt(start.length-1) == '/' ? '' : '/' ) + requestPath + end;
   }
   url += "?__render_href=" + href + "&__render_part=" + encodeURIComponent(part);
   if(module) url += "&__render_module=" + encodeURIComponent(module);
   if(search) url += "&__render_search=" + encodeURIComponent(search);
   if(params) {
     for(var param in params) {
       if(params[param] instanceof Array) {
         for(var i=0;i<params[param].length;i++) {
           url += "&" + encodeURIComponent(param) + "=" + encodeURIComponent(params[param][i]);
         }
       } else {
         url += "&" + encodeURIComponent(param) + "=" + encodeURIComponent(params[param]);
       }
     }
   }
   var httpReq = new pfx.net.HTTPRequest("GET", url, callback, context, errorCallback);
   httpReq.start("", null, requestId);
};

// Remotely submits a form and renders an include part
// Arguments can be passed as list (deprecated) or as single JSON object argument:
//   callback      - callback function
//   context       - callback object scope (optional)
//   requestId     - request/response assignment id (optional)
//   form          - reference to the form element
//   errorCallback - callback function for HTTP errors (optional)
pfx.renderSubmit = function(callback, context, requestId, form, errorCallback) {
  if(arguments.length == 1) {
    var arg = arguments[0];
    callback = arg.callback;
    context = arg.context;
    requestId = arg.requestId;
    form = arg.form;
    errorCallback = arg.errorCallback;
  }
  var content = "";
  for(var i = 0; i < form.elements.length; i++) {
    content += (i>0?"&":"") + form.elements[i].name + "=" + encodeURIComponent(form.elements[i].value);
  }
  var headers = new Array();
  headers[0] = new Array();
  headers[0][0] = "Content-Type";
  headers[0][1] = "application/x-www-form-urlencoded";
  var httpReq = new pfx.net.HTTPRequest("POST", form.action, callback, context, errorCallback);
  httpReq.start(content, headers, requestId);
};