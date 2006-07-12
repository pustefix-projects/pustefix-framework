/*
 * de.schlund.pfixcore.webservice.WebServiceServlet
 */
package de.schlund.pfixcore.webservice;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.ConfigurationException;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import de.schlund.pfixcore.webservice.config.ConfigProperties;
import de.schlund.pfixcore.webservice.config.Configuration;
import de.schlund.pfixcore.webservice.config.GlobalServiceConfig;
import de.schlund.pfixcore.webservice.config.ServiceConfig;
import de.schlund.pfixcore.webservice.fault.Fault;
import de.schlund.pfixcore.webservice.fault.FaultHandler;
import de.schlund.pfixcore.webservice.monitor.Monitor;
import de.schlund.pfixcore.webservice.monitor.MonitorHistory;
import de.schlund.pfixcore.webservice.monitor.MonitorRecord;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixxml.PathFactory;
import de.schlund.pfixxml.config.BuildTimeProperties;
import de.schlund.pfixxml.config.CustomizationHandler;
import de.schlund.pfixxml.config.XMLPropertiesUtil;
import de.schlund.pfixxml.loader.AppLoader;
import de.schlund.pfixxml.util.TransformerHandlerAdapter;

/**
 * WebServiceServlet.java 
 * 
 * Created: 22.07.2004
 * 
 * @author mleidig
 */
public class WebServiceServlet extends AxisServlet {

    private Category LOG=Logger.getLogger(getClass().getName());
    private boolean DEBUG=LOG.isDebugEnabled();
    
    private final static String WS_CONF_NS = "http://pustefix.sourceforge.net/wsconfig200401";
    private final static String CUS_NS = "http://www.schlund.de/pustefix/customize";
    
    private WebServiceContext wsc;
    private ClassLoader currentLoader;
    
    private static ThreadLocal currentFault=new ThreadLocal();
    private static ThreadLocal currentRequest=new ThreadLocal();
    
    public static void setCurrentFault(Fault fault) {
        currentFault.set(fault);
    }
    
    public static Fault getCurrentFault() {
        return (Fault)currentFault.get();
    }
    
    public static void setCurrentRequest(HttpServletRequest request) {
        currentRequest.set(request);
    }
    
    public static HttpServletRequest getCurrentRequest() {
        return (HttpServletRequest)currentRequest.get();
    }

    public void init(ServletConfig config) throws ServletException {
    	AppLoader loader=AppLoader.getInstance();
    	if(loader.isEnabled()) {
    		ClassLoader newLoader=loader.getAppClassLoader();
    		//ClassUtils.setDefaultClassLoader(newLoader);
    		Thread.currentThread().setContextClassLoader(newLoader);
            currentLoader=newLoader;
    	}
        super.init(config);
        ArrayList al = new ArrayList();

        // Create temporary files with global properties
        String common = config.getInitParameter(Constants.PROP_COMMON_FILE);
        File tempFile = null;
        if (common != null) {
            File commonFile = PathFactory.getInstance().createPath(common).resolve();
            Properties globalProps;
            try {
                globalProps = XMLPropertiesUtil.loadPropertiesFromXMLFile(commonFile);
                tempFile = File.createTempFile("pfixglobal", ".prop");
                globalProps.store(new FileOutputStream(tempFile), "Automatically generated for webservices");
            } catch (SAXException e) {
                // Log and ignore
                tempFile = null;
                LOG.warn("Ignoring common properties file " + commonFile.getAbsolutePath());
            } catch (IOException e) {
                // Log and ignore
                tempFile = null;
                LOG.warn("Ignoring common properties file " + commonFile.getAbsolutePath());
            }
        }
        if (tempFile != null) {
            al.add(tempFile);
        }
        
        File tempFile2 = null;
        String servlet = config.getInitParameter(Constants.PROP_SERVLET_FILE);
        if (servlet != null) {
            File servletFile = PathFactory.getInstance().createPath(servlet).resolve();

            // Transform XML, creating temporary property file
            XMLReader xreader;
            try {
                xreader = XMLReaderFactory.createXMLReader();
            } catch (SAXException e) {
                throw new ServletException("Could not create XMLReader", e);
            }
            TransformerFactory tf = SAXTransformerFactory.newInstance();
            if (tf.getFeature(SAXTransformerFactory.FEATURE)) {
                SAXTransformerFactory stf = (SAXTransformerFactory) tf;
                TransformerHandler th;
                try {
                    th = stf.newTransformerHandler();
                } catch (TransformerConfigurationException e) {
                    throw new RuntimeException(
                            "Failed to configure TransformerFactory!", e);
                }
                DOMResult dr = new DOMResult();
                try {
                    tempFile2 = File.createTempFile("webservice", ".prop");
                } catch (IOException e) {
                    throw new ServletException("Could not create temporary file", e);
                }
                //StreamResult sr = new StreamResult(tempFile2);
                th.setResult(dr);
                DefaultHandler dh = new TransformerHandlerAdapter(th);
                DefaultHandler cushandler = new CustomizationHandler(dh, WS_CONF_NS, CUS_NS);
                xreader.setContentHandler(cushandler);
                xreader.setDTDHandler(cushandler);
                xreader.setErrorHandler(cushandler);
                xreader.setEntityResolver(cushandler);
                try {
                    xreader.parse(new InputSource(new FileInputStream(servletFile)));
                    Transformer trans = tf.newTransformer(new StreamSource(
                            PathFactory.getInstance().createPath(
                                    "core/build/create_webservice.xsl").resolve()));
                    trans.setParameter("docroot", PathFactory.getInstance().createPath("").resolve().getAbsolutePath());
                    trans.transform(new DOMSource(dr.getNode()), new StreamResult(new FileOutputStream(tempFile2)));
                } catch (Exception e) {
                    throw new ServletException("Error on reading config file " + servletFile.getAbsolutePath(), e);
                }
            } else {
                throw new ServletException(
                        "Could not get instance of SAXTransformerFactory!");
            }
            
            al.add(tempFile2);
        }

        try {
            File[] propFiles=new File[al.size()];
            for(int i=0;i<al.size();i++) propFiles[i]=(File)al.get(i);
            ConfigProperties cfgProps=new ConfigProperties(propFiles);
            Configuration srvConf=new Configuration(cfgProps);
            wsc=new WebServiceContext(srvConf);
            getServletContext().setAttribute(wsc.getClass().getName(),wsc);
            GlobalServiceConfig globConf=wsc.getConfiguration().getGlobalServiceConfig();
            if(globConf.getMonitoringEnabled()) {
                Monitor monitor=new Monitor(globConf.getMonitoringHistorySize(),globConf.getMonitoringScope());
                wsc.setAttribute(Monitor.class.getName(),monitor);
            }
        } catch(Exception x) {
            LOG.error("Can't get web service configuration",x);
            throw new ServletException("Can't get web service configuration",x);
        }
    }
    
    protected void sendForbidden(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
        res.setContentType("text/html");
        writer.println("<h2>Forbidden!</h2>");
        writer.close();
    }
    
    protected void sendBadRequest(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_BAD_REQUEST);
        res.setContentType("text/html");
        writer.println("<h2>Bad request!</h2>");
        writer.close();
    }
    
    protected void sendError(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        res.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        res.setContentType("text/html");
        writer.println("<h2>Internal server error!</h2>");
        writer.close();
    }
 
    public void doPost(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
            ClassLoader newLoader=loader.getAppClassLoader();
           	//ClassLoader currentLoader=org.apache.axis.utils.ClassUtils.getDefaultClassLoader();
           	Thread.currentThread().setContextClassLoader(newLoader);
            synchronized(this) {
            	if(newLoader!=currentLoader) {
            		if(DEBUG) LOG.debug("Reload Axis Engine.");
            		//ClassUtils.setDefaultClassLoader(newLoader);
            		Thread.currentThread().setContextClassLoader(newLoader);
            		currentLoader=newLoader;
            		axisServer=null;
            		getServletContext().removeAttribute(ATTR_AXIS_ENGINE);
            		getServletContext().removeAttribute(getServletName() + ATTR_AXIS_ENGINE);
            		init();
            	}
            }
        }
        
        try {
        	setCurrentFault(null);
        	setCurrentRequest(req);
        
        	String serviceName=getServiceName(req);
        	Configuration config=wsc.getConfiguration();
        	ServiceConfig srvConf=config.getServiceConfig(serviceName);
        	HttpSession session=req.getSession(false);
        	Context pfxContext=null;
        	if(session!=null) pfxContext=(Context)session.getAttribute(srvConf.getContextName()+"__CONTEXT__");
        
        	if(DEBUG) LOG.debug("Process webservice request: "+req.getPathInfo());
        	if(req.getHeader(Constants.HEADER_SOAP_ACTION)==null && req.getParameter(Constants.PARAM_SOAP_MESSAGE)!=null) {
        		if(DEBUG) LOG.debug("no SOAPAction header, but soapmessage parameter -> iframe method");
        		HttpServletResponse response=res;
        		String reqID=req.getParameter(Constants.PARAM_REQUEST_ID);
        		if(DEBUG) if(reqID!=null) LOG.debug("contains requestID parameter: "+reqID);
        		String insPI=req.getParameter("insertpi");
        		if(insPI!=null) response=new InsertPIResponseWrapper(res);
        		if(DEBUG) if(insPI!=null) LOG.debug("contains insertpi parameter");
        		if(pfxContext!=null&&srvConf.doSynchronizeOnContext()) {
        			synchronized(pfxContext) {
        				super.doPost(new SOAPActionRequestWrapper(req),response);
        			}
        		} else {
        			super.doPost(new SOAPActionRequestWrapper(req),response);
        		}
        	} else if(req.getHeader(Constants.HEADER_SOAP_ACTION)!=null) {
        		if(DEBUG) LOG.debug("found SOAPAction header, but no soapmessage parameter -> xmlhttprequest version");
        		String reqID=req.getHeader(Constants.HEADER_REQUEST_ID);
        		if(DEBUG) if(reqID!=null) LOG.debug("contains requestID header: "+reqID);
        		if(reqID!=null) res.setHeader(Constants.HEADER_REQUEST_ID,reqID);
        		if(pfxContext!=null&&srvConf.doSynchronizeOnContext()) {
        			synchronized(pfxContext) {
        				super.doPost(req,res);
        			}
        		} else {
        			super.doPost(req,res);
        		}
        	} else {
        		if(DEBUG) LOG.debug("no SOAPAction header, no soapmessage parameter -> bad request");
        		sendBadRequest(req,res,res.getWriter());
        	}
        } catch(Throwable t) {
        	LOG.warn("Error while processing webservice request.",t);
        	throw new ServletException("Error while processing webservice request.",t);
        } finally {
        	setCurrentFault(null);
        	setCurrentRequest(null);
        }
    }
    
    private static String getServiceName(HttpServletRequest req) {
        String service=req.getPathInfo();
        if(service==null) throw new IllegalArgumentException("No service name found.");
        int ind=service.lastIndexOf('/');
        if(ind<0) throw new IllegalArgumentException("No service name found.");
        else {
            if(!((service.length()-ind)>1)) throw new IllegalArgumentException("No service name found.");
            return service.substring(ind+1);
        }
    }
    
    public void doGet(HttpServletRequest req,HttpServletResponse res) throws ServletException,IOException {
        HttpSession session=req.getSession(false);
        PrintWriter writer = res.getWriter();
        String qs=req.getQueryString();
        if(qs==null) {
            sendBadRequest(req,res,writer);
        } else if(qs.equalsIgnoreCase("WSDL")) {
            if(wsc.getConfiguration().getGlobalServiceConfig().getWSDLSupportEnabled()) {
                String pathInfo=req.getPathInfo();
                String serviceName;
                if (pathInfo.startsWith("/")) {
                    serviceName=pathInfo.substring(1);
                } else {
                    serviceName=pathInfo;
                }
                ServiceConfig conf=wsc.getConfiguration().getServiceConfig(serviceName);
                String type=conf.getSessionType();
                if(type.equals(Constants.SESSION_TYPE_SERVLET) && session==null) {
                    sendForbidden(req,res,writer);
                    return;
                }
                res.setContentType("text/xml");
                String repoPath=wsc.getConfiguration().getGlobalServiceConfig().getWSDLRepository();
                InputStream in=getServletContext().getResourceAsStream(repoPath+"/"+serviceName+".wsdl");
                if(in!=null) {
                    if(type.equals(Constants.SESSION_TYPE_SERVLET)) {
                        StringBuffer sb=new StringBuffer();
                        BufferedInputStream bis=new BufferedInputStream(in);
                        int ch=0;
                        while((ch=bis.read())!=-1) {
                            sb.append((char)ch);
                        }
                        String str=sb.toString();
                        String sid=Constants.SESSION_PREFIX+session.getId();
                        Pattern pat=Pattern.compile("(wsdlsoap:address location=\"[^\"]*)");
                        Matcher mat=pat.matcher(str);
                        sb=new StringBuffer();
                        while(mat.find()) {
                            mat.appendReplacement(sb,mat.group(1)+sid);
                        }
                        mat.appendTail(sb);
                        str=sb.toString();
                        writer.println(str);
                    } else {
                        BufferedInputStream bis=new BufferedInputStream(in);
                        int ch=0;
                        while((ch=bis.read())!=-1) {
                            writer.write(ch);
                        }
                    }
                    writer.close();
                } else sendError(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else if(qs.equalsIgnoreCase("monitor")) {
            if(session!=null && wsc.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            	sendMonitor(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else if(qs.equalsIgnoreCase("admin")) {
            if(session!=null && wsc.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
                sendAdmin(req,res,writer);
            } else sendForbidden(req,res,writer);
        } else sendBadRequest(req,res,writer);
    }
    
    protected void processAxisFault(AxisFault axisFault) {
        Fault fault=getCurrentFault();
        if(fault==null) {
        	HttpServletRequest req=getCurrentRequest();
        	LOG.warn(dumpRequest(req,true));
        	Throwable t=axisFault.getCause();
            if(t!=null) LOG.warn(t,t);
        } else {
        	Throwable t=axisFault.getCause();
        	if(t!=null) LOG.error("Exception while processing request",t);
        	String serviceName=fault.getServiceName();
        	Configuration config=wsc.getConfiguration();
        	ServiceConfig serviceConfig=config.getServiceConfig(serviceName);
        	FaultHandler faultHandler=serviceConfig.getFaultHandler();
        	if(faultHandler==null) {
        		GlobalServiceConfig globalConfig=config.getGlobalServiceConfig();
        		faultHandler=globalConfig.getFaultHandler();
        	}
        	if(faultHandler!=null) {
        		fault.setThrowable(t);
        		faultHandler.handleFault(fault);
        		axisFault.setFaultString(fault.getFaultString());
        	}
        }
        axisFault.removeFaultDetail(org.apache.axis.Constants.QNAME_FAULTDETAIL_STACKTRACE);
    }

    private String dumpRequest(HttpServletRequest srvReq,boolean showHeaders) {
		StringBuffer sb=new StringBuffer();
		sb.append(srvReq.getScheme());
		sb.append("://");
		sb.append(srvReq.getServerName());
		sb.append(":");
		sb.append(srvReq.getServerPort());
		sb.append(srvReq.getRequestURI());
		HttpSession session=srvReq.getSession(false);
		if(session!=null) {
			sb.append(Constants.SESSION_PREFIX);
			sb.append(session.getId());
		}
		String s=srvReq.getQueryString();
		if(s!=null&&!s.equals("")) {
			sb.append("?");
			sb.append(s);
		}
		sb.append("\n");
		if(showHeaders) {
			Enumeration headers=srvReq.getHeaderNames();
			while(headers.hasMoreElements()) {
				String header=(String)headers.nextElement();
				String value=srvReq.getHeader(header);
				sb.append(header+": "+value+"\n");
			}
		}
		return sb.toString();
	}
    
    public void sendAdmin(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
      
        AppLoader loader=AppLoader.getInstance();
        if(loader.isEnabled()) {
          
            ClassLoader newLoader=loader.getAppClassLoader();
            if(newLoader!=null) {
                //ClassLoader currentLoader=Thread.currentThread().getContextClassLoader();
               
                Thread.currentThread().setContextClassLoader(newLoader);
             
            }
           
        }
        
        //TODO: source out html
        HttpSession session=req.getSession(false);
        if(session!=null && wsc.getConfiguration().getGlobalServiceConfig().getAdminEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            writer.println("<html><head><title>Web service admin</title>"+getJS()+getCSS()+"</head><body>");
            writer.println("<div class=\"title\">Web service admin</div><div class=\"content\">");
            try {
                AxisEngine engine=getEngine();
                writer.println("<table>");
                writer.println("<tr><td>");
                Iterator it=engine.getConfig().getDeployedServices();
                while(it.hasNext()) {
                    ServiceDesc desc=(ServiceDesc)it.next();
                    String name=desc.getName();
                    writer.println("<p>");
                    writer.println("<b>"+name+"</b>");
                    String wsdlUri=req.getRequestURI()+"/"+name+";jsessionid="+session.getId()+"?WSDL";
                    writer.println("&nbsp;&nbsp;<a style=\"color:#666666\" target=\"_blank\" href=\""+wsdlUri+"\">WSDL</a>");
                    writer.println("<br/>");
                    ArrayList operations=desc.getOperations();
                    if(!operations.isEmpty()) {
                        writer.println("<ul>");
                        for (Iterator oit = operations.iterator(); oit.hasNext();) {
                            OperationDesc opDesc = (OperationDesc) oit.next();
                            writer.println("<li>" + opDesc.getName());
                        }
                        writer.println("</ul>");
                    }
                     writer.println("</p>");
                    
                }
                writer.println("</td></tr>");
                writer.println("</table");
            } catch(AxisFault fault) {
            
            } catch(ConfigurationException x) {
            
            }
            writer.println("</div></body></html>");
            writer.close();
        } else sendForbidden(req,res,writer);
    }
    
    public void sendMonitor(HttpServletRequest req,HttpServletResponse res,PrintWriter writer) {
        //TODO: source out html
        Monitor monitor=(Monitor)wsc.getAttribute(Monitor.class.getName());
        if(monitor!=null && wsc.getConfiguration().getGlobalServiceConfig().getMonitoringEnabled()) {
            res.setStatus(HttpURLConnection.HTTP_OK);
            res.setContentType("text/html");
            String ip=req.getRemoteAddr();
            MonitorHistory history=monitor.getMonitorHistory(ip);
            SimpleDateFormat format=new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            writer.println("<html><head><title>Web service monitor</title>"+getJS()+getCSS()+"</head><body>");
            writer.println("<div class=\"title\">Web service monitor</div><div class=\"content\">");
            writer.println("<table class=\"overview\">");
            writer.println("<tr>");
            writer.println("<th align=\"left\">Start</th>");
            writer.println("<th align=\"left\">Time (in ms)</th>");
            writer.println("<th align=\"left\">Service</th>");
            writer.println("</tr>");
            MonitorRecord[] records=history.getRecords();
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String styleClass="nosel";
                if(i==records.length-1) styleClass="sel";
                writer.println("<tr name=\"row_entry\" class=\""+styleClass+"\" onclick=\"toggleDetails(this,'"+id+"')\">");
                writer.println("<td align=\"left\">"+format.format(new Date(record.getStartTime()))+"</td>");
                writer.println("<td align=\"right\">"+record.getTime()+"</td>");
                writer.println("<td align=\"left\">"+record.getTarget()+"</td>");
                writer.println("</tr>");
            }
            writer.println("</table");
            for(int i=0;i<records.length;i++) {
                MonitorRecord record=records[i];
                String id="entry"+i;
                String display="none";
                if(i==records.length-1) display="block";
                writer.println("<div name=\"detail_entry\" id=\""+id+"\" style=\"display:"+display+"\">");
                writer.println("<table width=\"100%\">");
                writer.println("<tr>");
                writer.println("<td><b>Request:</b><br/><textarea class=\"xml\">");
                writer.println(record.getRequest());
                writer.println("</textarea></td>");
                writer.println("<td><b>Response:</b><br/><textarea class=\"xml\">");
                writer.println(record.getResponse());
                writer.println("</textarea></td>");
                writer.println("</tr>");
                writer.println("</table>");
                writer.println("</div");
            }
            writer.println("</div></body></html>");
            writer.close();
        } else sendForbidden(req,res,writer);
    }
    
    private String getJS() {
        //TODO: source out js
        String js=
            "<script type=\"text/javascript\">" +
            "function toggleDetails(src,id) {" +
            "   var elems=document.getElementsByName('row_entry');"+
            "   for(var i=0;i<elems.length;i++) {" +
            "       elems[i].className='nosel';" +
            "   }" +
            "   src.className='sel';" +
            "   elems=document.getElementsByName('detail_entry');"+
            "   for(var i=0;i<elems.length;i++) {" +
            "       elems[i].style.display='none';" +
            "   }" +
            "   var elem=document.getElementById(id);" +
            "   if(elem.style.display=='none') {" +
            "       elem.style.display='block';" +
            "   } else {" +
            "       elem.style.display='none';" +
            "   }" +
            "}" +
            "</script>";
        return js;
    }
    
    private String getCSS() {
        //TODO: source out css
        String css=
            "<style type=\"text/css\">" +
            "   body {margin:0pt;border:0pt;background-color:#b6cfe4}" +
            "   div.content {padding:5pt;}" +
            "   div.title {padding:5pt;font-size:18pt;width:100%;background-color:black;color:white}" +
            "   table.overview td,th {padding-bottom:5pt;padding-right:15pt}" +
            "   table.overview tr.nosel {cursor:pointer;color:#000000;}" +
            "   table.overview tr.sel {cursor:pointer;color:#666666;}" +
            "   textarea.xml {width:100%;height:400px}" +
            "</style>";
        return css;
    }

}
