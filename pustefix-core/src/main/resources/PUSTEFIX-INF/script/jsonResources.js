if (!pfx.json) {
	pfx.json={};
}

/*
 * Requests one or more resources from the server.
 * The first parameter is a comma-separated list of the 
 * resource alias names.
 * The second (optional) parameter is a callback function.
 * If the callback function is supplied, this function 
 * will return immediately and the callback function 
 * will be called with the data returned from the 
 * server as the only parameter.
 * The returned data is a map using the alias names as 
 * keys and the serialized resource data as values.
 */
pfx.json.requestResources = function() {
	var resourceList = arguments[0];
	var callback = null;
	var jsonCallback = null;
	if (arguments.length > 1) {
		callback = arguments[1];
		jsonCallback = function(response) {
			var json = eval("(" + response + ")");
			callback(json);
		};
	}
	
	var index;
	var url = window.location.href;
	if ((index = url.indexOf("?")) != -1) {
		url = url.substring(0, index);
	}
	var jsessionId = "";
	if ((index = url.lastIndexOf(";jsessionid=")) != -1) {
		
		jsessionId = url.substring(index);
		url = url.substring(0, index);
	}
	url = url.substring(0, url.lastIndexOf("/"));
	url = url + "/__json/" + resourceList + jsessionId;
	
	var httpRequest = new pfx.net.HTTPRequest("GET", url, jsonCallback, null);
	httpRequest._getResponse = function(request) {
		var ctype=request.getResponseHeader("Content-Type");
		if (ctype == null) {
			throw new Error("Missing response content type");
		} else if (ctype.indexOf("application/json") == 0) {
			return request.responseText;
		} else if (ctype.indexOf("text/plain") == 0) {
			return request.responseText;
		} else {
			throw new Error("Illegal response content type: "+ctype);
		}
	};
	var result = httpRequest.start("");
	if (jsonCallback) {
		return result;
	} else {
		return eval("(" + result + ")");
	}
};

/*
 * Requests one resource from the server.
 * Works similar to pfx.json.requestResources, 
 * however only a single resource is requested and 
 * the data of this resource is returned directly.
 */
pfx.json.requestResource = function() {
	var resourceName = arguments[0];
	var callback = null;
	var extractCallback = null;
	if (arguments.length > 1) {
		callback = arguments[1];
		extractCallback = function(json) {
			callback(json[resourceName]);
		};
	}
	if (extractCallback) {
		return pfx.json.requestResources(resourceName, extractCallback);
	} else {
		json = pfx.json.requestResources(resourceName);
		return json[resourceName];
	}
};
