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
pfx.render = function(href, part, module, search, callback, context, requestId) {
   if(!href) throw new Error("Missing parameter: href");
   if(!part) throw new Error("Missing parameter: part");
   if(!callback) throw new Error("Missing parameter: callback");
   var url = window.location.href;
   var ind = url.indexOf('?');
   if(ind > -1) url = url.substring(0, ind);
   url += "?__render_href=" + href + "&__render_part=" + part;
   if(module) url += "&__render_module=" + module;
   if(search) url += "&__render_search=" + search;
   var httpReq = new pfx.net.HTTPRequest("GET", url, callback, context);
   httpReq.start("", null, requestId);
}