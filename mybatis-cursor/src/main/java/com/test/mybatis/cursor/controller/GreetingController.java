package com.test.mybatis.cursor.controller;

import com.test.mybatis.cursor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetingController {

    @Autowired
    private UserService userService;

    @RequestMapping("/greeting")
    public String greeting(String name, Model m) {
        m.addAttribute("name", name);
        userService.testCursor();
        return "greeting";
    }
}
