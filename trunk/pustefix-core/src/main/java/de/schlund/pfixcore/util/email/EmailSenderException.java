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

package de.schlund.pfixcore.util.email;


/**
 * Class for handling errors for EmailSender.
 *
 * @author <a href="mailto: haecker@schlund.de">Joerg Haecker</a>
 */

public class EmailSenderException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = -4067731464316109717L;
    private String message_;
    
    public EmailSenderException(String message) {
        super(message);
        this.message_=message;
    }
    
    public EmailSenderException(String message, Throwable e) {
        super(message, e);
        this.message_=message;
    }
    
    @Override
    public String getMessage() {
        return message_;
    }

}
