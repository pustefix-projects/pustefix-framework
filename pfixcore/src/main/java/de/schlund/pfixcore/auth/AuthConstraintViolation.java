/*
 * This file is part of PFIXCORE.
 *
 * PFIXCORE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * PFIXCORE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with PFIXCORE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package de.schlund.pfixcore.auth;

/**
 * 
 * @author mleidig@schlund.de
 * 
 */
public class AuthConstraintViolation extends AuthorizationException {

    private static final long   serialVersionUID = 4452140461087940426L;
    private final static String TYPESTR          = "AUTHCONSTRAINT_VIOLATION";
    private AuthConstraint      constraint;

    public AuthConstraintViolation(String message, String subtype, String target, AuthConstraint constraint) {
        super(message, TYPESTR, subtype, target);
        this.constraint = constraint;
    }

    public AuthConstraint getViolatedConstraint() {
        return constraint;
    }

}
