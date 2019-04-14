package com.ttn.reapProject.controller;

import com.ttn.reapProject.component.LoggedInUser;
import com.ttn.reapProject.entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class IndexController {
    @GetMapping("/")
    public ModelAndView index(RedirectAttributes redirectAttributes) {
        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("newUser", new User());
        modelAndView.addObject("loggedInUser", new LoggedInUser());
        redirectAttributes.addAttribute("error");
        redirectAttributes.addAttribute("success");
        return modelAndView;
    }
}
