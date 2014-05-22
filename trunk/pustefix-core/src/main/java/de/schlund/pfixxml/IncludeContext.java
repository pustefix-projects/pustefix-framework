package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

import com.icl.saxon.expr.Value;

public class IncludeContext {
	
	private Node includeElementNode;
	private Map<String, Value> includeElementParams;
	private Map<String, Value> includeElementTunnelParams;
	
	private Node includePartNode;
	private String includePartSystemId;
	private String includePartName;
	private Map<String, Value> includePartParams;
	
	public IncludeContext(Node includeElementNode, Node includePartNode, String includePartSystemId, String includePartName) {
		this.includeElementNode = includeElementNode;
		this.includePartNode = includePartNode;
		this.includePartSystemId = includePartSystemId;
		this.includePartName = includePartName;
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
	public boolean hasIncludePartParam(String name) {
		return includePartParams.containsKey(name);
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
	
	public String getIncludePartSystemId() {
		return includePartSystemId;
	}
	
	public String getIncludePartName() {
		return includePartName;
	}
	
}
