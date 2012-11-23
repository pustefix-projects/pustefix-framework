package de.schlund.pfixxml;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	
	private static Pattern PARAM_PATTERN = Pattern.compile("\\$([a-zA-Z_][a-zA-Z_0-9\\-\\.]*)");
	
	private NodeInfo contextNode;
	private int contextNodePosition;
	
	private Stack<IncludeContext> includeContextStack = new Stack<IncludeContext>();
	
	public IncludeContextController(NodeInfo contextNode) {
		this.contextNode = contextNode;
	}
	
	public void setContextNode(Object contextNode, int contextNodePosition) throws Exception {
		try {
			this.contextNode = getNodeInfo(contextNode);
			this.contextNodePosition = contextNodePosition;
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
	
	public int getContextNodePosition() throws Exception {
		try {
			return contextNodePosition;
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
			if(!includeContextStack.isEmpty()) { 
				IncludeContext includeContext = includeContextStack.peek();
				Value value = includeContext.getIncludePartParam(name);
				if(value != null) {
					return value;
				}
			}
			throw new XPathException("Include parameter '" + name + "' not found.");
		} catch (Exception x) {
			ExtensionFunctionUtils.setExtensionFunctionError(x);
			throw x;
		}
	}
	
	private boolean isIncludeParam(String name) {
		if(!includeContextStack.isEmpty()) {
			IncludeContext includeContext = includeContextStack.peek();
			return includeContext.hasIncludePartParam(name);
		}
		return false;
	}
	
	public void setAppliedParameter(String name, Value value) {
		IncludeContext includeContext = includeContextStack.peek();
		includeContext.addIncludePartParam(name, value);
	}
	
	
	public Value evaluate (Context parentContext, String expr) throws Exception {
		try {
			StringBuffer sb = new StringBuffer();
			Matcher matcher = PARAM_PATTERN.matcher(expr);
			while(matcher.find()) {
				String match = matcher.group(1);
				if(isIncludeParam(match)) {
					matcher.appendReplacement(sb, "pfx:__incparam('" + match + "')");
				} else {
					matcher.appendReplacement(sb, "\\$" + match);
				}
			}
			matcher.appendTail(sb);
			
			expr = sb.toString();
			
			Context context = parentContext.newContext();
			context.setContextNode((NodeInfo)contextNode);
			context.setPosition(contextNodePosition);
			context.setCurrentNode((NodeInfo)contextNode);
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
