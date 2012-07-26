package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import com.icl.saxon.expr.Value;

public class IncludeContext {
	
	private Node includeElementNode;
	private Node includeElementContext;
	private Map<String, Value> includeElementParams;
	private Map<String, Value> includeElementTunnelParams;
	
	private Node includePartNode;
	private Node includePartContext;
	private Map<String, Value> includePartParams;
	
	
	public IncludeContext(Node includeElementNode, Node includeElementContext, Node includePartNode, Node includePartContext) {
		this.includeElementNode = includeElementNode;
		this.includeElementContext = includeElementContext;
		this.includePartNode = includePartNode;
		this.includePartContext = includePartContext;
		includeElementParams = new HashMap<String, Value>();
		includeElementTunnelParams = new HashMap<String, Value>();
		includePartParams = new HashMap<String, Value>();
	}
	
	public Node getIncludeElementNode() {
		return includeElementNode;
	}
	public void setIncludeElementNode(Node includeElementNode) {
		this.includeElementNode = includeElementNode;
	}
	
	public Node getIncludeElementContext() {
		return includeElementContext;
	}
	public void setIncludeElementContext(Node includeElementContext) {
		this.includeElementContext = includeElementContext;
	}
	
	public Value getIncludeElementParam(String name) {
		return includeElementParams.get(name);
	}
	public void addIncludeElementParam(String name, Value value) {
		includeElementParams.put(name, value);
	}
	
	public Map<String,Value> getIncludeElementTunnelParams() {
		return includeElementTunnelParams;
	}
	public void setIncludeElementTunnelParams(Map<String,Value> includeElementTunnelParams) {
		this.includeElementTunnelParams = includeElementTunnelParams;
	}
	public Value getIncludeElementTunnelParam(String name) {
		return includeElementTunnelParams.get(name);
	}
	
	public Value getIncludePartParam(String name) {
		return includePartParams.get(name);
	}
	public void addIncludePartParam(String name, Value value) {
		includePartParams.put(name, value);
	}

	public Node getIncludePartNode() {
		return includePartNode;
	}
	public void setIncludePartNode(Node includePartNode) {
		this.includePartNode = includePartNode;
	}
	
	public Node getIncludePartContext() {
		return includePartContext;
	}
	public void setIncludePartContext(Node includePartContext) {
		this.includePartContext = includePartContext;
	}
	
}
