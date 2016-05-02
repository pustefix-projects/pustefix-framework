function errorCallback(res) {
  if(res.status == 403) {
    window.location.reload();  
  } else {
	//do something else
  }
}

//Optional global error callback function for HTTP request errors,
//will be overridden by function directly passed to render calls
pfx.net.HTTPRequest.errorCallback = errorCallback;

function rexCallback(result, requestId, responseInfo) {
	document.getElementById("rexresult").innerHTML=result;
	document.getElementById("rexjsresult").innerHTML=getSerial();
}
function rexTest() {
	//Deprecated style of calling pfx.render() with long argument list
	//pfx.render("txt/common.xml", "rextest", "", "", rexCallback, null, "1", null, null, errorCallback);
	var args = { "href": "txt/common.xml", 
			     "part": "rextest", 
			     "callback": rexCallback, 
			     "requestId": "1" };
	pfx.render(args);
}

function searchCallback(result, requestId, responseInfo) {
	document.getElementById("searchresult").innerHTML=result;
}
function doSearch() {
	//Deprecated style of call pfx.renderSubmit() with long argument list
	//pfx.renderSubmit(searchCallback, null, "1", document.getElementById("search"), errorCallback);
	var args = { "callback": searchCallback, 
			     "requestId": "1", 
			     "form": document.getElementById("search"),
			     "errorCallback": errorCallback };
	pfx.renderSubmit(args);
}