package de.schlund.pfixxml.perflogging;

import java.lang.reflect.Field;
import java.util.Properties;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.cglib.proxy.Enhancer;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.IWrapperContainerImpl;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;
import de.schlund.pfixxml.SPDocument;

@Aspect
public class CorePerfLoggingAspect {

	@Pointcut("execution(* de.schlund.pfixcore.workflow.State.needsData(..)) && args(context,preq)")
	public void perflogNeedsData(Context context, PfixServletRequest preq) {}

	@Pointcut("execution(* de.schlund.pfixcore.workflow.State.isAccessible(..)) && args(context,preq)")
	public void perflogIsAccessible(Context context, PfixServletRequest preq) {}
	
	@Pointcut("execution(* de.schlund.pfixcore.workflow.app.IWrapperContainer.retrieveCurrentStatus(..))")
	public void perflogRetrieve() {}
	
	@Pointcut("execution(* de.schlund.pfixcore.workflow.app.IHandlerContainer.createIWrapperContainerInstance(..)) && args(context,preq,resdoc)")
	public void perflogInitIWrappers(Context context, PfixServletRequest preq, ResultDocument resdoc) {}
	
	@Pointcut("execution(* de.schlund.pfixcore.workflow.app.IWrapperContainer.handleSubmittedData(..))")
	public void perflogPageHandleSubmittedData() {}
	
	@Pointcut("execution(* de.schlund.pfixcore.generator.IHandler.handleSubmittedData(..))")
	public void perflogHandleSubmittedData() {}
	
	@Pointcut("execution(* de.schlund.pfixcore.generator.IHandler.needsData(..))")
	public void perflogIHandlerNeedsData() {}
	
	@Pointcut("execution(* de.schlund.pfixcore.generator.IHandler.prerequisitesMet(..))")
	public void perflogIHandlerPrerequisitesMet() {}
	
	@Pointcut("execution(* de.schlund.pfixcore.generator.IHandler.isActive(..))")
	public void perflogIHandlerIsActive() {}

	@Pointcut("execution(* de.schlund.pfixcore.util.StateUtil.renderContextResource(..)) && args(cr,resdoc,nodename)")
	public void perflogInsertStatus(Object cr, ResultDocument resdoc, String nodename) {}
	
	@Pointcut("execution(de.schlund.pfixxml.PfixServletRequestImpl.new(..)) && args(req,properties)")
	public void perflogInitRequest(HttpServletRequest req, Properties properties) {}
	
	@Pointcut("execution(* org.pustefixframework.http.AbstractPustefixRequestHandler.callProcess(..)) && args(preq,req,res)")
	public void perflogCallProcess(PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res) {}
	
	@Pointcut("call(* org.pustefixframework.http.AbstractPustefixXMLRequestHandler.getDom(..)) && within(org.pustefixframework.http.AbstractPustefixXMLRequestHandler)")
    public void perflogGetDom() {}
	
	@Pointcut("call(* org.pustefixframework.http.AbstractPustefixXMLRequestHandler.createETag(..)) && within(org.pustefixframework.http.AbstractPustefixXMLRequestHandler) && args(output,spdoc)")
    public void perflogCreateETag(String output, SPDocument spdoc) {}
	
	@Pointcut("execution(* org.pustefixframework.http.AbstractPustefixXMLRequestHandler.doHandleDocument(..)) && args(spdoc,stylesheet,paramhash,preq,res,session)")
    public void perflogHandleDocument(SPDocument spdoc, String stylesheet, TreeMap<String, Object> paramhash, 
            PfixServletRequest preq, HttpServletResponse res, HttpSession session) {}
	
	
	@Around("perflogNeedsData(context,preq)")
	public Object callNeedsData(ProceedingJoinPoint joinPoint, Context context, PfixServletRequest preq) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.PAGE_NEEDSDATA, context.getCurrentPageRequest().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogIsAccessible(context,preq)")
	public Object callIsAccessible(ProceedingJoinPoint joinPoint, Context context, PfixServletRequest preq) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.PAGE_ISACCESSIBLE, context.getCurrentPageRequest().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogRetrieve()")
	public Object callRetrieve(ProceedingJoinPoint joinPoint) throws Throwable {
		IWrapperContainerImpl ic = (IWrapperContainerImpl)joinPoint.getTarget();
		Field field = ic.getClass().getDeclaredField("context");
		Context context = (Context)field.get(ic);
		PerfEvent pe = new PerfEvent(PerfEventType.PAGE_RETRIEVECURRENTSTATUS, context.getCurrentPageRequest().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogInitIWrappers(context,preq,resdoc)")
	public Object callInitIWrappers(ProceedingJoinPoint joinPoint, Context context, PfixServletRequest preq, ResultDocument resdoc) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.PAGE_INITIWRAPPERS, context.getCurrentPageRequest().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogPageHandleSubmittedData()")
	public Object callPageHandleSubmittedData(ProceedingJoinPoint joinPoint) throws Throwable {
		IWrapperContainerImpl ic = (IWrapperContainerImpl)joinPoint.getTarget();
		Field field = ic.getClass().getDeclaredField("context");
		Context context = (Context)field.get(ic);
		PerfEvent pe = new PerfEvent(PerfEventType.PAGE_HANDLESUBMITTEDDATA, context.getCurrentPageRequest().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogHandleSubmittedData()")
	public Object callHandleSubmittedData(ProceedingJoinPoint joinPoint) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_HANDLESUBMITTEDDATA, joinPoint.getTarget().getClass().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogIHandlerNeedsData()")
	public Object callIHandlerNeedsData(ProceedingJoinPoint joinPoint) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_NEEDSDATA, joinPoint.getTarget().getClass().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogIHandlerPrerequisitesMet()")
	public Object callIHandlerPrerequisitesMet(ProceedingJoinPoint joinPoint) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_PREREQUISITESMET, joinPoint.getTarget().getClass().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogIHandlerIsActive()")
	public Object callIHandlerIsActive(ProceedingJoinPoint joinPoint) throws Throwable {
		PerfEvent pe = new PerfEvent(PerfEventType.IHANDLER_ISACTIVE, joinPoint.getTarget().getClass().getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogInsertStatus(cr,resdoc,nodename)")
	public Object callInsertStatus(ProceedingJoinPoint joinPoint, Object cr, ResultDocument resdoc, String nodename) throws Throwable {
	    Class<?> clazz = cr.getClass();
	    if(Enhancer.isEnhanced(clazz)) clazz = clazz.getSuperclass();
	    PerfEvent pe = new PerfEvent(PerfEventType.CONTEXTRESOURCE_INSERTSTATUS, clazz.getName());
        pe.start();
		try {
			return joinPoint.proceed();
		} finally {
			pe.save();
		}
	}
	
	@Around("perflogInitRequest(req,properties)")
    public Object callInitRequest(ProceedingJoinPoint joinPoint, HttpServletRequest req, Properties properties) throws Throwable {
        PerfEvent pe = new PerfEvent(PerfEventType.PFIXSERVLETREQUEST_INIT, req.getRequestURI());
        pe.start();
        try {
            return joinPoint.proceed();
        } finally {
            pe.save();
        }
    }
	
	@Around("perflogCallProcess(preq,req,res)")
    public Object callCallProcess(ProceedingJoinPoint joinPoint, PfixServletRequest preq, HttpServletRequest req, HttpServletResponse res ) throws Throwable {
        PerfEvent pe = new PerfEvent(PerfEventType.XMLSERVER_CALLPROCESS, req.getRequestURI());
        pe.start();
        try {
            return joinPoint.proceed();
        } finally {
            pe.save();
        }
    }
	
	@Around("perflogGetDom()")
    public Object callGetDom(ProceedingJoinPoint joinPoint) throws Throwable {
        PerfEvent pe = new PerfEvent(PerfEventType.XMLSERVER_GETDOM);
        pe.start();
        try {
            SPDocument doc = (SPDocument)joinPoint.proceed();
            pe.setIdentfier(doc.getPagename());
            return doc;
        } finally {
            pe.save();
        }
    }
	
	@Around("perflogCreateETag(output,spdoc)")
    public Object callCreateETag(ProceedingJoinPoint joinPoint, String output, SPDocument spdoc) throws Throwable {
        PerfEvent pe = new PerfEvent(PerfEventType.XMLSERVER_CREATEETAG, spdoc.getPagename());
        pe.start();
        try {
            return joinPoint.proceed();
        } finally {
            pe.save();
        }
    }
	
	@Around("perflogHandleDocument(spdoc,stylesheet,paramhash,preq,res,session)")
	public Object callCreateETag(ProceedingJoinPoint joinPoint, SPDocument spdoc, String stylesheet, TreeMap<String, Object> paramhash, 
	        PfixServletRequest preq, HttpServletResponse res, HttpSession session) throws Throwable {
	    PerfEvent pe = new PerfEvent(PerfEventType.XMLSERVER_HANDLEDOCUMENT, spdoc.getPagename());
	    pe.start();
	    try {
	        return joinPoint.proceed();
	    } finally {
	        pe.save();
	    }
	}
	
}
