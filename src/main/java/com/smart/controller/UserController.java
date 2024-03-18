package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    private final ContactRepository contactRepository;

    public User user;

    public UserController(UserRepository userRepository, ContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
    }

    //this annotation will make it run in the start
    //method to add common data to response
    @ModelAttribute
    public void addCommonData(Model model,
                              Principal principal) {
        String username = principal.getName();
        System.out.println("Username: " + username);

        user = userRepository.getUserByUserName(username);
        System.out.println("User: " + user);

        model.addAttribute("user" , user);
    }

    //dashboard home
    @RequestMapping("/index")
    public String dashboard(Model model) {
        model.addAttribute("title", "User Dashboard");
        return "regular/user-dashboard";
    }

    //open add contact form
    @GetMapping("/add-contact")
    public String openAddContactForm(Model model){

        model.addAttribute("title", "Add contact");
        model.addAttribute("contact", new Contact());

        return "regular/add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContactForm(@ModelAttribute Contact contact,
                                     @RequestParam("profileImage")MultipartFile profileImage,
                                     Model model,
                                     HttpSession session) {

        try {
            System.out.println("Contact data:" + contact);

            user.getContacts().add(contact);
            String fileName;

            contact.setUser(user);

            Contact savedContact = contactRepository.save(contact);

            if(!profileImage.isEmpty()) {
                fileName = savedContact.getContactId()+profileImage.getOriginalFilename();

                Files.copy(profileImage.getInputStream(),
                        Paths.get(new ClassPathResource("static/image").getFile() + File.separator + fileName),
                        StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image is uploaded!");
            } else {
                System.out.println("Image is empty");
                fileName = savedContact.getContactId()+"profilePhoto.jpg";
            }
            savedContact.setImageUrl(fileName);
            contactRepository.save(savedContact);
            System.out.println("Contact has been added successfully");

            session.setAttribute("message",
                    new Message("Contact has been successfully added", "alert-success"));

            System.out.println("User has been added to database: " + user);
        }catch(Exception ex) {
            System.out.println("Error: " + ex.getMessage());

            //setting error msg
            session.setAttribute("message",
                    new Message("Something went wrong. " + ex.getMessage(),
                            "alert-danger"));
        }
        return "regular/add_contact_form";
    }

    //show contacts
    // per page 5 contacts
    // current page = 0
    @GetMapping("/view-contacts/{pageNo}")
    public String showContacts(@PathVariable("pageNo") Integer pageNo,
                               Model model){
        System.out.println("In view contacts handler");
        model.addAttribute("title", "View User Contacts");

        Pageable pageable = PageRequest.of(pageNo, 10);
        Page<Contact> contacts =
                this.contactRepository.findContactsByUserId(user.getId(), pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "regular/view_contacts";
    }

    //show contact details
    @GetMapping("/{contactId}/contact")
    public String showContactDetail(@PathVariable("contactId") Integer contactId,
                                    Model model) {

        System.out.println("Contact Id: " + contactId);

        Optional<Contact> optionalContact = this.contactRepository.findById(contactId);
        Contact contact = optionalContact.get();

        if(user.getId() == contact.getUser().getId())
            model.addAttribute("contact", contact);
        model.addAttribute("title", contact.getName());
        return "regular/contact_detail";
    }

    @GetMapping("/profile")
    public String profileHandler(Model model) {
        model.addAttribute("title", "Profile page");
        return "regular/profile";
    }
}
