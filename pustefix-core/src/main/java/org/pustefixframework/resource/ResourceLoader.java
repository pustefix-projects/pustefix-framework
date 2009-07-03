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

package org.pustefixframework.resource;

import java.net.URI;
import java.util.Map;


/**
 * Provides access to resources.  
 * 
 * @author Sebastian Marsching <sebastian.marsching@1und1.de>
 */
public interface ResourceLoader {
    /**
     * Returns the "best-matching" resource for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * 
     * @param uri URI describing the resource
     * @return resource for uri
     */
    Resource getResource(URI uri);

    /**
     * Returns all resources for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * 
     * @param uri URI describing the resource
     * @return all resource for uri
     */
    Resource[] getResources(URI uri);

    /**
     * Returns the "best-matching" resource for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * {@link ResourceSelector}s may use the parameters to decide
     * which resource matches best.
     * 
     * @param uri URI describing the resource
     * @param parameters paramters for selectors or <code>null</code>
     *  if no parameters are given
     * @return resource for uri
     */
    Resource getResource(URI uri, Map<String, ?> parameters);

    /**
     * Returns all resources for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * {@link ResourceSelector}s may use the parameters to decide
     * which resources match best.
     * 
     * @param uri URI describing the resource
     * @param parameters paramters for selectors or <code>null</code>
     *  if no parameters are given
     * @return all resource for uri
     */
    Resource[] getResources(URI uri, Map<String, ?> parameters);

    /**
     * Returns the "best-matching" resource for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * The list of found resources is filtered for only those resources,
     * that implement the given interface.
     * 
     * @param uri URI describing the resource
     * @param resourceClass class or interface the returned resource(s)
     *  has to implement.
     * @return resource for uri
     */
    <T extends Resource> T getResource(URI uri, Class<? extends T> resourceClass);

    /**
     * Returns all resources for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * The list of found resources is filtered for only those resources,
     * that implement the given interface.
     * 
     * @param uri URI describing the resource
     * @param resourceClass class or interface the returned resource(s)
     *  has to implement.
     * @return all resource for uri
     */
    <T extends Resource> T[] getResources(URI uri, Class<? extends T> resourceClass);

    /**
     * Returns the "best-matching" resource for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * {@link ResourceSelector}s may use the parameters to decide
     * which resource matches best.
     * The list of found resources is filtered for only those resources,
     * that implement the given interface.
     * 
     * @param uri URI describing the resource
     * @param parameters paramters for selectors or <code>null</code>
     *  if no parameters are given
     * @param resourceClass class or interface the returned resource(s)
     *  has to implement.
     * @return resource for uri
     */
    <T extends Resource> T getResource(URI uri, Map<String, ?> parameters, Class<? extends T> resourceClass);

    /**
     * Returns all resources for a given URI or
     * <code>null</code> if no resource can be found for the URI.
     * {@link ResourceSelector}s may use the parameters to decide
     * which resources match best.
     * The list of found resources is filtered for only those resources,
     * that implement the given interface.
     * 
     * @param uri URI describing the resource
     * @param parameters paramters for selectors or <code>null</code>
     *  if no parameters are given
     * @param resourceClass class or interface the returned resource(s)
     *  has to implement.
     * @return all resource for uri
     */
    <T extends Resource> T[] getResources(URI uri, Map<String, ?> parameters, Class<? extends T> resourceClass);

}
