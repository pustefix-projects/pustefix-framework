package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.icl.saxon.Context;
import com.icl.saxon.expr.SingletonNodeSet;
import com.icl.saxon.expr.Value;
import com.icl.saxon.expr.XPathException;
import com.icl.saxon.functions.Extensions;
import com.icl.saxon.om.NodeInfo;

import de.schlund.pfixxml.util.ExtensionFunctionUtils;

public class IncludeContextController {
	
	private NodeInfo contextNode;
	
	private Stack<IncludeContext> includeContextStack = new Stack<IncludeContext>();
	
	public IncludeContextController(NodeInfo contextNode) {
		this.contextNode = contextNode;
	}
	
	public void setContextNode(Object contextNode) throws Exception {
		try {
			this.contextNode = getNodeInfo(contextNode);
		} catch(Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	public Object getContextNode() throws Exception {
		try {
			return contextNode;
		} catch(Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	public void pushInclude(Context context, Node includeElementNode, Node includePartNode) throws Exception {
		try {
			IncludeContext newContext = new IncludeContext(includeElementNode, includePartNode, includePartNode, includePartNode);
			Map<String, Value> newTunnelParams = new HashMap<String, Value>();
			if(!includeContextStack.isEmpty()) {
				IncludeContext parentContext = includeContextStack.peek();
				Map<String, Value> tunnelParams = parentContext.getIncludeElementTunnelParams();
				for(String tunnelParamName: tunnelParams.keySet()) {
					newTunnelParams.put(tunnelParamName, tunnelParams.get(tunnelParamName));
				}
			}
			NodeList nodes = includeElementNode.getChildNodes();	
			if(nodes != null) {
				for(int i=0; i<nodes.getLength(); i++) {
					Node child = nodes.item(i);
					if(child.getNodeType() == Node.ELEMENT_NODE) {
						if(child.getNodeName().equals("pfx:includeparam")) {
							Element elem = (Element)child;
							String paramName = elem.getAttribute("name");
							if(paramName.length() == 0) {
								throw new IllegalArgumentException("Include parameter with missing name");
							}
							Value value = null;
							String select = elem.getAttribute("select");
							if(select.length() > 0) {
								value = evaluate(context, select);
							} else {
								value = new SingletonNodeSet((NodeInfo)elem);
							}
							boolean tunnel = "yes".equals(elem.getAttribute("tunnel"));
							if(tunnel) {
								newTunnelParams.put(paramName, value);
							} else {
								newContext.addIncludeElementParam(paramName, value);
							}
						}
					}
				}
			}
			newContext.setIncludeElementTunnelParams(newTunnelParams);
			includeContextStack.push(newContext);
			nodes = includePartNode.getChildNodes();	
			if(nodes != null) {
				for(int i=0; i<nodes.getLength(); i++) {
					Node child = nodes.item(i);
					if(child.getNodeType() == Node.ELEMENT_NODE) {
						if(child.getNodeName().equals("pfx:includeparam")) {
							Element elem = (Element)child;
							String paramName = elem.getAttribute("name");
							if(paramName.length() == 0) {
								throw new IllegalArgumentException("Include parameter with missing name");
							}
							
							//check if param value was directly passed
							Value value = newContext.getIncludeElementParam(paramName);
							//check if param value was passed via tunneling
							if(value == null) {
								if("yes".equals(elem.getAttribute("tunnel"))) {
									value = newContext.getIncludeElementTunnelParam(paramName);
								}
							}
							//use default value if available
							if(value == null) {
								String select = elem.getAttribute("select");
								if(select.length() > 0) {
									value = evaluate(context, select);
								} else {
									value = new SingletonNodeSet((NodeInfo)elem);
								}
							}
							newContext.addIncludePartParam(paramName, value);
						}
					}
				}
			}
			
		} catch(Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	public void popInclude() throws Exception {
		try {
			includeContextStack.pop();
		} catch(Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	public Value getIncludeParam(Context parentContext, String name) throws Exception {
		try {
			IncludeContext includeContext = includeContextStack.peek();
			Value value = includeContext.getIncludePartParam(name);
			if(value == null) throw new XPathException("Include parameter '" + name + "' not found.");
			return value;
		} catch (Exception x) {
			x.printStackTrace();
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	public void setAppliedParameter(String name, Value value) {
		IncludeContext includeContext = includeContextStack.peek();
		includeContext.addIncludePartParam(name, value);
	}
	
	
	public Value evaluate (Context parentContext, String expr) throws Exception {
		try {
			expr = expr.replaceAll("\\$(\\w+)", "pfx:__incparam('$1')");
			Context context = parentContext.newContext();
			context.setContextNode((NodeInfo)contextNode);
			context.setPosition(1);
	        context.setLast(1);
	        return Extensions.evaluate(context, expr);
		} catch (Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	public static NodeInfo getNodeInfo(Object contextNode) {
		return ((SingletonNodeSet)contextNode).getFirst();
	}
	
	public static IncludeContextController create(Object contextNode) throws Exception {
		try {
		return new IncludeContextController(getNodeInfo(contextNode));
		} catch(Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
}
