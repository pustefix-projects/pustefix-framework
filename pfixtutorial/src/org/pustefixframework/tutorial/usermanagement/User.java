package org.pustefixframework.tutorial.usermanagement;

import java.net.URL;
import java.util.Date;

import org.pustefixframework.tutorial.caster.ToURL;

import de.schlund.pfixcore.generator.annotation.Caster;
import de.schlund.pfixcore.generator.annotation.IWrapper;
import de.schlund.pfixcore.generator.annotation.Param;
import de.schlund.pfixcore.generator.annotation.Transient;
import de.schlund.pfixcore.oxm.impl.annotation.DateSerializer;

@IWrapper(name="UserWrapper", ihandler=UserHandler.class)
public class User {
    private Integer id;
    private String name;
    private String email;
    private Date birthday;
    private Boolean admin;
    private URL homepage;
    private String gender;

    public User() {
    }
    
    public User(String name, String email, Date birthday, Boolean admin, URL homepage, String gender) {
        this.name = name;
        this.email = email;
        this.birthday = birthday;
        this.admin = admin;
        this.homepage = homepage;
        this.gender = gender;
    }
    
    @Transient
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Param(name="name", mandatory=true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Param(name="email", mandatory=true)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Param(name="birthday", mandatory=true)
    @DateSerializer("yyyy/MM/dd")
    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    @Param(name="admin", mandatory=false)
    public Boolean isAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    @Param(name="homepage", mandatory=false)
    @Caster(type=ToURL.class)
    public URL getHomepage() {
        return homepage;
    }

    public void setHomepage(URL homepage) {
        this.homepage = homepage;
    }

    @Param(name="gender", mandatory=true)
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
