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
package org.pustefixframework.web.mvc;

import org.springframework.web.bind.support.WebArgumentResolver;
import org.springframework.web.bind.support.WebBindingInitializer;

/**
 * Configures the AnnotationMethodHandlerAdapter used by Pustefix States.
 * The Pustefix default configuration can be overridden by adding an own
 * bean definition of this type to the Spring configuration.
 */
public class AnnotationMethodHandlerAdapterConfig {

    private WebArgumentResolver[] argumentResolvers;
    private WebBindingInitializer webBindingInitializer;

    public void setCustomArgumentResolver(WebArgumentResolver argumentResolver) {
        this.argumentResolvers = new WebArgumentResolver[] { argumentResolver };
    }

    public void setCustomArgumentResolvers(WebArgumentResolver[] argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    public WebArgumentResolver[] getCustomArgumentResolvers() {
        return argumentResolvers;
    }

    public void setWebBindingInitializer(WebBindingInitializer webBindingInitializer) {
        this.webBindingInitializer = webBindingInitializer;
    }

    public WebBindingInitializer getWebBindingInitializer() {
        return webBindingInitializer;
    }

}
