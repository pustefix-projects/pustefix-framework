/*
 * This file is part of Pustefix.
 *
 * Pustefix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Pustefix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Pustefix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.example;

import org.junit.Assert;
import org.pustefixframework.test.PustefixWebApplicationContextLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit38.AbstractJUnit38SpringContextTests;

@ContextConfiguration(loader=PustefixWebApplicationContextLoader.class,locations={
    "docroot:/WEB-INF/project.xml", "docroot:/WEB-INF/spring.xml", "classpath:/de/schlund/pfixcore/example/spring-test.xml"})
public class ResourceTest extends AbstractJUnit38SpringContextTests {
    
    @Autowired
    private ResourceTestBean resTest;
    
    public void testResource() throws Exception {
        Assert.assertNotNull(resTest.getResource().getInputStream());
    }

}
