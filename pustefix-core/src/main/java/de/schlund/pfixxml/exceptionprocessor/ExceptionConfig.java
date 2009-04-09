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

package de.schlund.pfixxml.exceptionprocessor;

/**
 *
 * @author <a href="mailto:benjamin@schlund.de">Benjamin Reitzammer</a>
 * @version $Id$
 */
public class ExceptionConfig {

    //~ Instance/static variables ..............................................

    private String type = null;
    private String page = null;
    private boolean forward  = false;
    private ExceptionProcessor processor = null;

    //~ Constructors ...........................................................

    /**
     */
    public ExceptionConfig() {
    }

    //~ Methods ................................................................

    /**
     * @return true if the state of this object is a valid exception configuration, that is:
     * <ul>
     *   <li>{@link #processor processor} is not allowed to be <code>null</code></li>
     *   <li>{@link #type type} is not allowed to be null or an empty String</li>
     *   <li>{@link #page page} is not allowed to be null or an empty String if,
     *       {@link #forward forward} is true</li>
     * </ul>
     */
     public boolean validate() {
         boolean valid = true;

         if ( processor == null || type == null )
             valid = false;
         if ( type == null || "".equals(type) )
             valid = false;
         if ( forward && (page == null || "".equals(page)) )
             valid = false;

         return valid;
     }

    /**
	 * Returns the value of type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the value of type.
	 * @param type The value to assign type.
	 */
	public void setType(String type) {
		this.type = type;
	}

    /**
	 * Returns the value of processor.
	 */
	public ExceptionProcessor getProcessor() {
		return processor;
	}

	/**
	 * Sets the value of processor.
	 * @param processor The value to assign processor.
	 */
	public void setProcessor(ExceptionProcessor processor) {
		this.processor = processor;
	}

  /**
	 * Returns the value of page.
	 */
	public String getPage() {
		return page;
	}

	/**
	 * Sets the value of page.
	 * @param page The value to assign page.
	 */
	public void setPage(String page) {
		this.page = page;
	}


  /**
	 * Returns the value of forward.
	 */
	public boolean getForward() {
		return forward;
	}

	/**
	 * Sets the value of forward.
	 * @param forward The value to assign forward.
	 */
	public void setForward(boolean forward) {
		this.forward = forward;
	}

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getName());
        sb.append("\n type: [").append(type);
        sb.append("]\n processor: [").append(processor);
        sb.append("]\n page: [").append(page).append("], forward: ");
        sb.append(forward ? "[true]" : "[false]");
        return sb.toString();
    }

}