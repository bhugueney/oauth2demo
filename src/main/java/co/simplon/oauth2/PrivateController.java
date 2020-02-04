package co.simplon.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
@RestController()
public class PrivateController {
    @Autowired
    MyService myService;

    @RequestMapping("/np/private")
    public String privateInfo() {
        return myService.privateInfo();
    }

    @RequestMapping("/np/admin")
    public String adminInfo() {
        return myService.adminInfo();
    }
    @RequestMapping("/np")
    public String np() {
        return myService.publicInfo();
    }

}
