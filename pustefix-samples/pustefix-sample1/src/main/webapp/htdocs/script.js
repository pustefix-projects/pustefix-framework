function rexCallback(result) {
	document.getElementById("rexresult").innerHTML=result;
	document.getElementById("rexjsresult").innerHTML=getSerial();
}
function rexTest() {
	pfx.render("txt/common.xml", "rextest", "", "", rexCallback, null, "1");
}

function searchCallback(result) {
	document.getElementById("searchresult").innerHTML=result;
}
function doSearch() {
	var params = {};
	params["__sendingdata"] = 1;
	params["__forcestop"] = "true";
	params["__CMD[]:SUBWRP"] = "search";
	params["search.term"] = document.getElementById("search").elements["search.term"].value;
	pfx.render("txt/common.xml", "searchresult", "", "", searchCallback, null, "1", params);
}