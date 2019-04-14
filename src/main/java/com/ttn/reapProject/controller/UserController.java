package com.ttn.reapProject.controller;

import com.ttn.reapProject.component.LoggedInUser;
import com.ttn.reapProject.component.RecognitionSearch;
import com.ttn.reapProject.entity.Item;
import com.ttn.reapProject.entity.Recognition;
import com.ttn.reapProject.entity.Role;
import com.ttn.reapProject.entity.User;
import com.ttn.reapProject.exception.UserNotFoundException;
import com.ttn.reapProject.service.RecognitionService;
import com.ttn.reapProject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Controller
public class UserController {
    @Autowired
    UserService userService;

    @Autowired
    RecognitionService recognitionService;

    //Save the uploaded file to this folder
    private static String IMG_LOCATION = "/home/ttn/IdeaProjects/user-images/";

    /*@GetMapping("/users")
    @ResponseBody
    List<User> getUserList() {
        return userService.getUserList();
    }*/

    // Show user dashboard
    @GetMapping("/users/{id}")
    public ModelAndView getUser(@PathVariable Integer id,
                                HttpServletRequest httpServletRequest,
                                RedirectAttributes redirectAttributes) {
        HttpSession httpSession = httpServletRequest.getSession();
        User activeUser = (User) httpSession.getAttribute("activeUser");

        try {
            if (id != activeUser.getId()) {
                ModelAndView modelAndView = new ModelAndView("redirect:/");
                redirectAttributes.addFlashAttribute("error", "Please log in to continue");
                return modelAndView;
            }
        } catch (NullPointerException ne) {
            ModelAndView modelAndView = new ModelAndView("redirect:/");
            redirectAttributes.addFlashAttribute("error", "Please log in to continue");
            return modelAndView;
        }

        Optional<User> optionalUser = userService.getUser(id);
        if (!optionalUser.isPresent())
            throw new UserNotFoundException("No user with id " + id);
        ModelAndView modelAndView = new ModelAndView("dashboard");
        modelAndView.addObject("user", optionalUser.get());
        modelAndView.addObject("recognition", new Recognition());
        modelAndView.addObject("recognitionSearch", new RecognitionSearch());
        List<Recognition> recognitionList = recognitionService.getListOfRecognitions();
        Collections.reverse(recognitionList);
        modelAndView.addObject("recognitionList", recognitionList);
        Map<String, List<Integer>> recognizedUserRedeemableBadges = new LinkedHashMap<>();
        Integer recognizedUserGold, recognizedUserSilver, recognizedUserBronze;
        for (Recognition recognition : recognitionList) {
            User recognizedUser = userService.getUserByFullName(recognition.getReceiverName());
            recognizedUserGold = recognizedUser.getGoldRedeemable();
            recognizedUserSilver = recognizedUser.getSilverRedeemable();
            recognizedUserBronze = recognizedUser.getBronzeRedeemable();
            recognizedUserRedeemableBadges.put(recognizedUser.getFullName(), Arrays.asList(recognizedUserGold, recognizedUserSilver, recognizedUserBronze));
        }
        // System.out.println(recognizedUserRedeemableBadges);
        modelAndView.addObject("recognizedUserRedeemableBadges", recognizedUserRedeemableBadges);
        redirectAttributes.addAttribute("error");
        boolean isAdmin = optionalUser.get().getRoleSet().contains(Role.ADMIN);
        if (isAdmin) {
            modelAndView.addObject("isAdmin", isAdmin);
            List<User> userList = userService.getUserList();
            modelAndView.addObject("users", userList);
        }
        return modelAndView;
    }

    // Show user recognitions
    @GetMapping("/users/{id}/recognitions")
    public ModelAndView getUserRecognitions(@PathVariable("id") Integer id,
                                            HttpServletRequest httpServletRequest,
                                            RedirectAttributes redirectAttributes) {
        HttpSession httpSession = httpServletRequest.getSession();
        User activeUser = (User) httpSession.getAttribute("activeUser");
        try {
            if (id != activeUser.getId()) {
                ModelAndView modelAndView = new ModelAndView("redirect:/");
                redirectAttributes.addFlashAttribute("error", "Please log in to view your recognitions");
                return modelAndView;
            }
        } catch (NullPointerException ne) {
            ModelAndView modelAndView = new ModelAndView("redirect:/");
            redirectAttributes.addFlashAttribute("error", "Please log in to view your recognitions");
            return modelAndView;
        }
        Optional<User> optionalUser = userService.getUser(id);
        if (!optionalUser.isPresent()) {
            throw new UserNotFoundException("No user with id " + id);
        }
        ModelAndView modelAndView = new ModelAndView("recognitions");
        modelAndView.addObject("user", optionalUser.get());
        List<Recognition> receivedRecognitionsList = recognitionService.getRecognitionsByReceiverId(optionalUser.get().getId());
        modelAndView.addObject("receivedRecognitionsList", receivedRecognitionsList);
        List<Recognition> sentRecognitionsList = recognitionService.getRecognitionsBySenderId(optionalUser.get().getId());
        modelAndView.addObject("sentRecognitionsList", sentRecognitionsList);
        return modelAndView;
    }



    // Create new user
    @PostMapping("/register")
    public ModelAndView createNewUser(@Valid @ModelAttribute("newUser") User user,
                                      BindingResult bindingResult,
                                      @ModelAttribute("loggedInUser") LoggedInUser loggedInUser,
                                      @RequestParam("image") MultipartFile file,
                                      HttpServletRequest httpServletRequest,
                                      RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            ModelAndView modelAndView = new ModelAndView("redirect:/");
            redirectAttributes.addFlashAttribute("error","User not registered!Try again with valid values!");
            return modelAndView;
        } else {
            List<String> emails = userService.findAllEmails();
            if (emails.contains(user.getEmail())) {
                ModelAndView modelAndView = new ModelAndView("redirect:/");
                redirectAttributes.addFlashAttribute("error", "Email already in use");
                return modelAndView;
            }
            HttpSession httpSession = httpServletRequest.getSession();
            httpSession.setAttribute("activeUser", user);
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get(IMG_LOCATION + file.getOriginalFilename());
                Files.write(path, bytes);
                String photoPath = "/home/ttn/IdeaProjects/user-images/" + file.getOriginalFilename();
                user.setPhoto(photoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            userService.save(user);
            ModelAndView modelAndView = new ModelAndView("redirect:/users/" + user.getId());
            List<Item> itemList = new ArrayList<>();
            httpSession.setAttribute("itemList", itemList);
            return modelAndView;
        }
    }

    // Modify user with id {id}
    @PutMapping("/users/{id}")
    public ModelAndView editUser(@PathVariable Integer id,
                                 @RequestParam Map<String, String> requestParams,
                                 HttpServletRequest httpServletRequest) {
        HttpSession httpSession = httpServletRequest.getSession();
        User activeUser = (User) httpSession.getAttribute("activeUser");
        if (httpSession == null) {
            throw new RuntimeException("Unauthorized modification of users");
        }
        Optional<User> userOptional = userService.getUser(id);
        if (!userOptional.isPresent())
            throw new UserNotFoundException("No user with id " + id);
        User user = userOptional.get();
        Set<Role> userRoleSet = user.getRoleSet();

        if (requestParams.get("active") == null) {
            user.setActive(false);
        } else if (requestParams.get("active").equals("on")) {
            user.setActive(true);
        }

        userRoleSet = userService.roleModifier(userRoleSet, requestParams.get("adminCheck"), Role.ADMIN);
        userRoleSet = userService.roleModifier(userRoleSet, requestParams.get("practiceHeadCheck"), Role.PRACTICE_HEAD);
        userRoleSet = userService.roleModifier(userRoleSet, requestParams.get("supervisorCheck"), Role.SUPERVISOR);
        userRoleSet = userService.roleModifier(userRoleSet, requestParams.get("userCheck"), Role.USER);

        user.setRoleSet(userRoleSet);

        user.setGoldRedeemable(Integer.parseInt(requestParams.get("goldRedeemable")));
        user.setSilverRedeemable(Integer.parseInt(requestParams.get("silverRedeemable")));
        user.setBronzeRedeemable(Integer.parseInt(requestParams.get("bronzeRedeemable")));

        userService.adminEditUser(user);
        // Update admin's points in the current session
        User activeUserRefreshed = userService.findUserById(activeUser.getId());
        httpSession.setAttribute("activeUser", activeUserRefreshed);
        ModelAndView modelAndView = new ModelAndView("redirect:/users/" + activeUserRefreshed.getId());
        return modelAndView;
    }

    // Log user in
    @PostMapping("/login")
    public ModelAndView logUserIn(@ModelAttribute("loggedInUser") LoggedInUser loggedInUser,
                                  HttpServletRequest httpServletRequest,
                                  RedirectAttributes redirectAttributes) {
        Optional<User> optionalUser = userService.findUserByEmailAndPassword(loggedInUser.getEmail(), loggedInUser.getPassword());
        if (!optionalUser.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("redirect:/");
            redirectAttributes.addFlashAttribute("error", "Invalid credentials!Login Again");
            return modelAndView;
        } else {
            HttpSession httpSession = httpServletRequest.getSession();
            httpSession.setAttribute("activeUser", optionalUser.get());
            List<Item> itemList = new ArrayList<>();
            httpSession.setAttribute("itemList", itemList);
            return new ModelAndView("redirect:/users/" + optionalUser.get().getId());
        }
    }

    // Log user out
    @PostMapping("/logout")
    public ModelAndView logUserOut(HttpServletRequest httpServletRequest,
                                   RedirectAttributes redirectAttributes) {
        HttpSession httpSession = httpServletRequest.getSession();
        httpSession.invalidate();
        ModelAndView modelAndView = new ModelAndView("redirect:/");
        redirectAttributes.addFlashAttribute("success", "Logged out");
        return modelAndView;
    }

    // Search recognitions by receiver name
    @PostMapping("/searchRecognitionByName")
    @ResponseBody
    public List<Recognition> getRecognitionsByName(@ModelAttribute("recognitionSearch") RecognitionSearch recognitionSearch) {
        List<Recognition> recognitionList = recognitionService.getRecognitionsByName(recognitionSearch.getFullName());
        return recognitionList;
    }

    // Search recognitions by date
    @GetMapping("/searchRecognitionsByDate/{date}")
    @ResponseBody
    public List<Recognition> getRecognitionsByDate(@PathVariable("date") String dateString) {
        List<Recognition> recognitionList = recognitionService.getRecognitionsBetweenDates(dateString);
        return recognitionList;
    }

    // Autocomplete user name
    @GetMapping("/autocomplete")
    @ResponseBody
    public List<User> getUsersByNamePattern(@RequestParam("pattern") String pattern) {
        List<User> userList = userService.findUserByFullNamePattern(pattern + "%");
        return userList;
    }
}