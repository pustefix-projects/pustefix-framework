function soapEnabled() {
	return document.getElementById('protocol_soap').checked;
}

function jsonEnabled() {
	return document.getElementById('protocol_json').checked;
}

function printTime(time) {
	var elem=document.getElementById('service_time_value');
	elem.innerHTML=time;
}

function printError(error) {
	var elem=document.getElementById('service_error');
	elem.style.visibility="visible";
	elem.innerHTML=error;
}

function resetError() {
	var elem=document.getElementById('service_error');
	elem.style.visibility="hidden";
	elem.innerHTML="";
}

function printResult(result) {
	resetError();
	var elem=document.getElementById('service_result');
	elem.innerHTML=result;
}

function setFormResult(result) {
	resetError();
	var elem=document.getElementById('service_formresult');
	elem.value=result;
}

function Timer() {
	this.reset();
}

Timer.prototype.reset=function() {
	this.t1=0;
	this.t2=0;
	this.total=0;
	this.running=false;
}

Timer.prototype.start=function() {
	if(this.running==true) throw new Error("Timer is already running.");
	this.running=true;
	this.t1=(new Date()).getTime();
}

Timer.prototype.stop=function() {
	if(this.running==false) throw new Error("Timer is not running.");
	this.running=false;
	this.t2=(new Date()).getTime();
	this.total+=(this.t2-this.t1);
}

Timer.prototype.getTime=function() {
	if(this.running==true) return ((new Date()).getTime())-this.t1;
	return this.t2-this.t1;
}

Timer.prototype.getTotalTime=function() {
	return this.total;
}