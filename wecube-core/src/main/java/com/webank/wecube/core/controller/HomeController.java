package com.webank.wecube.core.controller;

import static com.webank.wecube.core.domain.JsonResponse.okay;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.webank.wecube.core.domain.JsonResponse;
import com.webank.wecube.core.service.UserManagerService;

@Controller
public class HomeController {

    @Autowired
    private UserManagerService userManagerService;

    @GetMapping(value = {"/", "index.html"})
    public String index() {
        return "index.html";
    }
    
    @GetMapping(value = { "login.html" })
    public String loginPage() {
        return "login.html";
    }

    @GetMapping(value = {"/home"})
    public String home(Model model, Principal principal) {
        model.addAttribute("system_name", "Wecube Core");
        model.addAttribute("login_user", principal.getName());
        return "home.html";
    }

    @GetMapping("/my-menus")
    @ResponseBody
    public JsonResponse getMenuItems(Principal principal) {
        if (principal != null) {
            return okay().withData(userManagerService.getMenuItemsByUsername(principal.getName(), true));
        }
        return okay();
    }
}



