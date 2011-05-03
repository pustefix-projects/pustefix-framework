function rexCallback(result, foo, bar) {
	document.getElementById("rexresult").innerHTML=result;
}
function rexTest() {
	pfx.render("txt/common.xml", "rextest", "", "", rexCallback, null, "1");
}