function setupDisplay() {
	var disp="no_disp";
	if(parent.__Browser.gk) disp="gecko_disp";
	else if(parent.__Browser.ie) disp="ie_disp";
	var elems=document.getElementsByName(disp);
	for(var i=0;i<elems.length;i++) {
		elems[i].style.display='block';
	}
}
