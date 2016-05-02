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
package org.pustefixframework.container.spring.beans;

import org.springframework.beans.BeansException;

/**
 * Exception which can be thrown when a given BeanFactory implementation
 * isn't supported, e.g. when the given implementation doesn't implement
 * a required interface.
 * 
 * @author mleidig
 *
 */
public class BeanFactoryImplNotSupportedException extends BeansException {
    
    private static final long serialVersionUID = 9085851219118895102L;

    public BeanFactoryImplNotSupportedException(String msg) {
        super(msg);
    }

}
