package org.pustefixframework.container.spring.beans;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.relation.RoleList;

import junit.framework.TestCase;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import de.schlund.pfixxml.Tenant;

public class TenantTargetSourceTest extends TestCase {

    public void testBaseClassDetection() {

        Class<?>[] clazzes = new Class[] {RoleList.class, AbstractCollection.class, ArrayList.class};
        assertTrue(AbstractCollection.class == TenantTargetSource.getBaseClass(clazzes));

        assertTrue(Object.class == TenantTargetSource.getBaseClass(new Class[] {HashMap.class, ArrayList.class}));

        assertTrue(null == TenantTargetSource.getBaseClass(new Class[0]));

        assertTrue(null == TenantTargetSource.getBaseClass(null));

        assertTrue(HashMap.class == TenantTargetSource.getBaseClass(new Class[] {HashMap.class}));

        assertTrue(Object.class == TenantTargetSource.getBaseClass(new Class[] {Object.class, Set.class}));

        assertTrue(AbstractCollection.class == TenantTargetSource.getBaseClass(new Class[] {HashSet.class, ArrayList.class}));

        assertTrue(AbstractSet.class == TenantTargetSource.getBaseClass(new Class[] {TreeSet.class, HashSet.class}));

        assertTrue(AbstractSet.class == TenantTargetSource.getBaseClass(new Class[] {HashSet.class, TreeSet.class}));

        assertTrue(AbstractSet.class == TenantTargetSource.getBaseClass(new Class[] {TreeSet.class, HashSet.class}));
    }

    public void testTargetRetrieval() throws Exception {

        Map<String, Object> targets = new HashMap<String, Object>();
        Set<String> fooObj = new HashSet<String>();
        targets.put("foo", fooObj);
        Set<String> barObj = new TreeSet<String>();
        targets.put("*", barObj);

        TenantTargetSource src = new TenantTargetSource();
        src.setTargets(targets);

        assertTrue(AbstractSet.class == src.getTargetClass());

        MockHttpServletRequest req = new MockHttpServletRequest();
        RequestAttributes attrs = new ServletRequestAttributes(req);
        RequestContextHolder.setRequestAttributes(attrs);

        attrs.setAttribute("__PFX_TENANT__", new Tenant("foo"), RequestAttributes.SCOPE_REQUEST);
        assertTrue(fooObj == src.getTarget());

        attrs.setAttribute("__PFX_TENANT__", new Tenant("baz"), RequestAttributes.SCOPE_REQUEST);
        assertTrue(barObj == src.getTarget());

    }

    public void testIntegration() {

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:/org/pustefixframework/container/spring/beans/spring.xml");

        MockHttpServletRequest req = new MockHttpServletRequest();
        RequestAttributes attrs = new ServletRequestAttributes(req);
        RequestContextHolder.setRequestAttributes(attrs);

        attrs.setAttribute("__PFX_TENANT__", new Tenant("foo"), RequestAttributes.SCOPE_REQUEST);
        assertEquals(1, ((Counter)context.getBean("counterByInterface")).count());
        assertEquals(2, ((Counter)context.getBean("counterByInterface")).count());

        attrs.setAttribute("__PFX_TENANT__", new Tenant("baz"), RequestAttributes.SCOPE_REQUEST);
        assertEquals(1, ((Counter)context.getBean("counterByInterface")).count());
        assertEquals(2, ((Counter)context.getBean("counterByInterface")).count());

        attrs.setAttribute("__PFX_TENANT__", new Tenant("foo"), RequestAttributes.SCOPE_REQUEST);
        assertEquals(1, ((CounterImpl)context.getBean("counterByClass")).count());
        assertEquals(2, ((CounterImpl)context.getBean("counterByClass")).count());

        attrs.setAttribute("__PFX_TENANT__", new Tenant("baz"), RequestAttributes.SCOPE_REQUEST);
        assertEquals(1, ((CounterImpl)context.getBean("counterByClass")).count());
        assertEquals(2, ((CounterImpl)context.getBean("counterByClass")).count());

    }

    public void testCommonBaseClassDetection() {

        assertTrue(AbstractSet.class == TenantTargetSource.getCommonBaseClass(new Class[] {HashSet.class, TreeSet.class}));
        assertTrue(AbstractSet.class == TenantTargetSource.getCommonBaseClass(new Class[] {TreeSet.class, HashSet.class}));
        assertTrue(Object.class == TenantTargetSource.getCommonBaseClass(new Class[] {HashMap.class, ArrayList.class}));
        assertTrue(CounterImpl.class == TenantTargetSource.getCommonBaseClass(new Class[] {DerivedCounter.class, CounterImpl.class}));
        assertTrue(Object.class == TenantTargetSource.getCommonBaseClass(new Class[] {DerivedCounter.class, OtherCounter.class}));
    }

    public void testInterfaceDetection() {

        Set<Class<?>> itfs = TenantTargetSource.getNonMarkerInterfaces(TreeSet.class);
        assertEquals(5, itfs.size());
        assertTrue(itfs.contains(Iterable.class));
        assertTrue(itfs.contains(Collection.class));
        assertTrue(itfs.contains(SortedSet.class));
        assertTrue(itfs.contains(NavigableSet.class));
        assertTrue(itfs.contains(Set.class));

        itfs = TenantTargetSource.getNonMarkerInterfaces(DerivedCounter.class);
        assertEquals(1, itfs.size());
        assertTrue(itfs.contains(Counter.class));
    }

}
