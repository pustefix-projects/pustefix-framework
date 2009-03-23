var wsChat=new WS_Chat();
var jwsChat=new JWS_Chat();

function ChatApp() {
	this.name=null;
	this.msgIv=null;
	this.nameIv=null;
	this.chat=null;
}

ChatApp.prototype.restartService=function() {
	
}
	
ChatApp.prototype.getChat=function() {
   if(this.chat==null) {
      if(soapEnabled()) this.chat=wsChat;
      else this.chat=jwsChat;
   } else {
      if(soapEnabled()&&this.chat==jwsChat) this.chat=wsChat;
      else if(!soapEnabled()&&this.chat==wsChat) this.chat=jwsChat;
   }
   return this.chat;
}
   
ChatApp.prototype.init=function(loggedin,nickname) {
	if(loggedin=="true") {
		this.name=nickname;
		this.loginDisplay();
		this.getMessages();
		this.startUpdates();
	} else {
		this.logoutDisplay();
	}
}
	
ChatApp.prototype.login=function(name) {
	this.restartService();
	
	var self=this;
	var f=function(res,id,ex) {
		if(ex!=undefined) {alert(ex.message);return;}
		self.name=name;
		self.loginDisplay();
		self.initText();
		self.startUpdates();
	}
	this.getChat().login(name,f); 
}

ChatApp.prototype.startUpdates=function() {
	refreshMessages();
	refreshNames();
	this.msgIv=window.setInterval("refreshMessages()",1000);
	this.nameIv=window.setInterval("refreshNames()",5000);
}

ChatApp.prototype.stopUpdates=function() {
	window.clearInterval(this.msgIv);
	window.clearInterval(this.nameIv);
}

ChatApp.prototype.loginDisplay=function() {
	document.getElementById("loginInput").disabled=true;
	document.getElementById("logoutInput").disabled=false;
	document.getElementById("nicknameInput").disabled=true;
	document.getElementById("sendInput").disabled=false;
	document.getElementById("messageInput").disabled=false;
}

ChatApp.prototype.logout=function() {
	var self=this;
	var f=function() {
		self.stopUpdates();
		self.logoutDisplay();
		self.clearText();
	}
	this.getChat().logout(f);
}

ChatApp.prototype.logoutDisplay=function() {
	document.getElementById("loginInput").disabled=false;
	document.getElementById("logoutInput").disabled=true;
	document.getElementById("nicknameInput").disabled=false;
	document.getElementById("sendInput").disabled=true;
	document.getElementById("messageInput").disabled=true;
}

ChatApp.prototype.send=function(text) {
	var self=this;
	var f=function() {
		document.getElementById("messageInput").value="";
	}
	this.getChat().sendMessage(text,new Array(),f);
}

ChatApp.prototype.initText=function() {
	var div=document.getElementById("messages");
	var elem=document.createElement("p");
	elem.style.fontWeight="bold";
	elem.appendChild(document.createTextNode("Hello "+this.name+"! Welcome to the Pustefix SOAP chat!"));
	div.appendChild(elem);
}

ChatApp.prototype.clearText=function() {
	document.getElementById("nicknameInput").value="";
	document.getElementById("messageInput").value="";
	this.clearMessages();
	this.clearNickNames();
}

ChatApp.prototype.clearMessages=function() {
	var div=document.getElementById("messages");
	while(div.firstChild) div.removeChild(div.firstChild);
}

ChatApp.prototype.getMessages=function() {
	var self=this;
	var f=function(msgs) {
		for(var i=0;i<msgs.length;i++) {
			self.addMessage(msgs[i]);
		}
	}
	this.getChat().getMessages(f);
}

ChatApp.prototype.getLastMessages=function() {
	var self=this;
	var f=function(msgs) {
		for(var i=0;i<msgs.length;i++) {
			self.addMessage(msgs[i]);
		}
	}
	this.getChat().getLastMessages(f);
}

ChatApp.prototype.addMessage=function(msg) {	
	var div=document.getElementById("messages");
	var dateElem=document.createElement("span");
	dateElem.style.color="#666666";
	dateElem.appendChild(document.createTextNode("["+msg.date.toLocaleString()+"] "));
	div.appendChild(dateElem);
	var nameElem=document.createElement("span");
	nameElem.style.fontWeight="bold";
	nameElem.appendChild(document.createTextNode(msg.from+": "));
	div.appendChild(nameElem);
	var msgElem=document.createElement("span");
	msgElem.appendChild(document.createTextNode(msg.text));
	div.appendChild(msgElem);
	div.appendChild(document.createElement("br"));
	div.scrollTop=div.scrollHeight-parseInt(div.style.height);
}

ChatApp.prototype.getNickNames=function() {
	var self=this;
	var f=function(names) {
		self.clearNickNames();
		for(var i=0;i<names.length;i++) {
			self.addNickName(names[i]);
		}
	}
	this.getChat().getNickNames(f);
}

ChatApp.prototype.clearNickNames=function() {
	var div=document.getElementById("nicknames");
	while(div.firstChild) div.removeChild(div.firstChild);
}

ChatApp.prototype.addNickName=function(name) {
	var div=document.getElementById("nicknames");
	var elem=document.createElement("span");
	elem.appendChild(document.createTextNode(name));
	div.appendChild(elem);
	div.appendChild(document.createElement("br"));
}


var chatApp=new ChatApp();

function refreshMessages() {
	chatApp.getLastMessages();
}

function refreshNames() {
	chatApp.getNickNames();
}

