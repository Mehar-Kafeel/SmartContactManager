package com.smart.controller;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class HomeController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login - Smart contact manager");
        return "login";
    }

    @RequestMapping("/login-fail")
    public String loginFail() {

        return "login-fail";
    }

    @GetMapping("/home")
    public String home(Model model) {

        model.addAttribute("title" , "Home - Smart contact manager");
        return "home";
    }

    @GetMapping("/about")
    public String about(Model model) {

        model.addAttribute("title" , "About - Smart contact manager");
        return "about";
    }

    @GetMapping("/signup")
    public String signUp(Model model) {
        model.addAttribute("title", "Register - Smart contact manager");
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/do-register")
    public String register(@Valid @ModelAttribute User user,
                           @RequestParam("profileImage") MultipartFile profileImage,
                           BindingResult result,
                           @RequestParam(value = "agreement", defaultValue  = "false") Boolean agreement,
                           Model model) {

        try {
            if (!agreement) {
                System.out.println("You have not agreed to terms and condition");
                throw new Exception("You have not agreed to terms and condition");
            }

            if(result.hasErrors()) {
                return "/signup";
            }

            //setting role and enabling user
            user.setRole("ROLE_USER");
            user.setEnabled(true);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            String imagePrefix = "SCM"+user.getId();
            String filename;
            if(!profileImage.isEmpty()) {
                filename = imagePrefix+profileImage.getOriginalFilename();
                user.setImage(filename);

                Files.copy(profileImage.getInputStream(),
                        Paths.get(new ClassPathResource("static/image").getFile() + File.separator + filename),
                        StandardCopyOption.REPLACE_EXISTING);
            } else {
                System.out.println("Image is empty");
                user.setImage(imagePrefix+"profilePhoto.jpg");
            }


            //saving user
            User savedUser = userRepository.save(user);

            System.out.println("User has been saved: " + savedUser);
            System.out.println("Agreement: " + agreement);

            //showing blank fields
            model.addAttribute("User", new User());

            //setting success msg to session
            model.addAttribute("message",
                    new Message("User has ben successfully registered",
                            "alert-success"));

        }catch (Exception ex) {
            ex.printStackTrace();
            model.addAttribute("user", user);

            //setting error msg
            model.addAttribute("message",
                    new Message("Something went wrong. " + ex.getMessage(),
                            "alert-danger"));
        }
        return "/signup";
    }
}
