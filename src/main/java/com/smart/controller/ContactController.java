package com.smart.controller;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.ClassPathResource;
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
@RequestMapping("/user/contact")
public class ContactController {

    private final UserRepository userRepository;
    private final ContactRepository contactRepository;

    private User user;

    public ContactController(UserRepository userRepository,
                             ContactRepository contactRepository) {
        this.userRepository = userRepository;
        this.contactRepository = contactRepository;

    }

    @ModelAttribute
    public void addCommonData(Model model,
                              Principal principal) {
        String username = principal.getName();
        user = userRepository.getUserByUserName(username);
        model.addAttribute("user" , user);
    }


    @GetMapping("/delete/{contactId}")
    public String deleteContact(@PathVariable("contactId") Integer contactId, Model model, HttpSession session) {

        System.out.println("Deleting contact with id :" + contactId);

        Optional<Contact> optionalContact = this.contactRepository.findById(contactId);
        Contact contact = optionalContact.get();

        if(contact.getUser().getId() == user.getId()) {
            this.contactRepository.delete(contact);

            //delete contact image too here

            session.setAttribute("message", new Message("Contact has been deleted" , "alert-success"));
        }

        return "redirect:/user/view-contacts/0";
    }

    //update contact form handler
    @PostMapping(path="/update-contact/{contactId}")
    public String updateContact(@PathVariable("contactId") Integer contactId,
                                Model model) throws NoSuchFieldException {
        model.addAttribute("title", "Update Contact");

        Contact contact = contactRepository.findById(contactId).orElseThrow(NoSuchFieldException::new);

        model.addAttribute("contact", contact);
        return "regular/update_contact";
    }

    @PostMapping(path = "/process-update-contact")
    public String processUpdateContactForm(@ModelAttribute Contact contact,
                                           @RequestParam("profileImage")MultipartFile profileImage,
                                           Model model,
                                           HttpSession session,
                                           Principal principal) {

        System.out.println("Contact: " + contact.toString());

        Contact existingContact = contactRepository.findById(contact.getContactId()).orElseThrow(() -> new RuntimeException("Existing contact not found with id: " + contact.getContactId()));
        int contactId = existingContact.getContactId();
        String oldFilename = contactId+existingContact.getImageUrl();
        try{
            if(!profileImage.isEmpty()) {

                File fileTobeDeleted = new File(new ClassPathResource("static/image").getFile() + File.separator + oldFilename);
                fileTobeDeleted.delete();

                String newFilename = contactId+profileImage.getOriginalFilename();
                contact.setImageUrl(newFilename);

                Files.copy(profileImage.getInputStream(),
                        Paths.get(new ClassPathResource("static/image").getFile() + File.separator + newFilename),
                        StandardCopyOption.REPLACE_EXISTING);

            } else {
                contact.setImageUrl(oldFilename);
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }

        contact.setUser(user);
        contactRepository.save(contact);

        session.setAttribute("message", new Message("Contact has been updated", "alert-success"));
        return "redirect:/user/view-contacts/0";
    }
}