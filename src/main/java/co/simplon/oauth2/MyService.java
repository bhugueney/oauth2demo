package co.simplon.oauth2;


import org.springframework.stereotype.Service;

import javax.annotation.security.RolesAllowed;

@Service
public class MyService {
    public String publicInfo() {
        return "for anybody";
    }
    @RolesAllowed({ "ROLE_ADMIN" })
    public String adminInfo() {
        return "for admin only";
    }
    @RolesAllowed({ "ROLE_USER", "ROLE_ADMIN" })
    public String privateInfo() {
        return "for user";
    }

}
