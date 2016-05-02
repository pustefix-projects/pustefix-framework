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
package de.schlund.pfixcore.auth;

/**
 * @author mleidig@schlund.de
 */
public class AuthorizationException extends RuntimeException {

    private static final long serialVersionUID = -1180372711031615963L;
    private String            type;
    private String            authorization;
    private String            target;

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, String type, String authorization, String target) {
        super(message);
        this.type = type;
        this.authorization = authorization;
        this.target = target;
    }

    public String getType() {
        return type;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getTarget() {
        return target;
    }

}
