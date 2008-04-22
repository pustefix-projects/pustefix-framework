package de.schlund.pfixcore.example.bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schlund.pfixcore.auth.Role;
import de.schlund.pfixcore.auth.RoleImpl;
import de.schlund.pfixcore.auth.RoleNotFoundException;
import de.schlund.pfixcore.auth.RoleProvider;

public class BankRoleProvider implements RoleProvider {

    private Map<String, Role> roles;

    public BankRoleProvider() {
        roles = new HashMap<String, Role>();
        Role role = new RoleImpl("TEST", false);
        roles.put(role.getName(), role);
        role = new RoleImpl("ADMIN", false);
        roles.put(role.getName(), role);
    }

    public Role getRole(String roleName) throws RoleNotFoundException {
        return roles.get(roleName);
    }

    public List<Role> getRoles() {
        return new ArrayList<Role>(roles.values());
    }

}
