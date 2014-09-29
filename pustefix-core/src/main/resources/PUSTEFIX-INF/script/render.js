if(!window.pfx) pfx={};

// Remotely renders an include part and passes the output to a callback function.
// Parameters are:
//  href      - path to include file
//  part      - include part name
//  module    - module name (can be empty)
//  search    - search type (can be empty)
//  callback  - callback function
//  context   - callback object scope (can be empty)
//  requestId - request/response assignment id (can be empty)
pfx.render = function(href, part, module, search, callback, context, requestId, params) {
   if(!href) throw new Error("Missing parameter: href");
   if(!part) throw new Error("Missing parameter: part");
   if(!callback) throw new Error("Missing parameter: callback");
   var url = window.location.href;
   var ind = url.indexOf('?');
   if(ind > -1) url = url.substring(0, ind);
   url += "?__render_href=" + href + "&__render_part=" + encodeURIComponent(part);
   if(module) url += "&__render_module=" + encodeURIComponent(module);
   if(search) url += "&__render_search=" + encodeURIComponent(search);
   if(params) {
     for(var param in params) {
	   url += "&" + encodeURIComponent(param) + "=" + encodeURIComponent(params[param]);
     }
   }
   var httpReq = new pfx.net.HTTPRequest("GET", url, callback, context);
   httpReq.start("", null, requestId);
};
pfx.renderSubmit = function(callback, context, requestId, form) {
  var content = "";
  for(var i = 0; i < form.elements.length; i++) {
	  content += (i>0?"&":"") + form.elements[i].name + "=" + encodeURIComponent(form.elements[i].value);
  }
  var headers = new Array();
  headers[0] = new Array();
  headers[0][0] = "Content-Type";
  headers[0][1] = "application/x-www-form-urlencoded";
  var httpReq = new pfx.net.HTTPRequest("POST", form.action, callback, context);
  httpReq.start(content, headers, requestId);
}