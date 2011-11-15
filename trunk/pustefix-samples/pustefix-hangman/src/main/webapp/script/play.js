var ws = new WS_Play();

function guessCallback(result,reqID,exception) {
	var oldurl = document.getElementById('playimg').src;
	var newurl = oldurl.replace(/Hangman-\d.png/,"Hangman-" + result.misses + ".png");
	document.getElementById('playimg').src = newurl;
	document.getElementById('playword').innerHTML = result.displayWord;
	if(result.completed) {
		window.location.reload();
	}
}

function guess(button) {
	ws.guess(button.value, guessCallback);
	return false;
}
