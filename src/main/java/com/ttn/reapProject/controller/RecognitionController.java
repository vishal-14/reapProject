package com.ttn.reapProject.controller;

import com.ttn.reapProject.entity.Recognition;
import com.ttn.reapProject.entity.User;
import com.ttn.reapProject.service.EmailService;
import com.ttn.reapProject.service.RecognitionService;
import com.ttn.reapProject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
public class RecognitionController {
    @Autowired
    RecognitionService recognitionService;

    @Autowired
    UserService userService;

    @Autowired
    EmailService emailService;

    // Create a new recognition
    @PostMapping("/recognize")
    public ResponseEntity<String> recognizeNewer(@Valid @ModelAttribute("recognition") Recognition recognition,
                                                 RedirectAttributes redirectAttributes) {
        String receiverName = recognition.getReceiverName();
        User receivingUser = userService.getUserByFullName(receiverName);
        if (receivingUser == null) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("myResponseHeader", "doesNotExist");
            return new ResponseEntity<String>("User does not exist", httpHeaders, HttpStatus.OK);
        }
        if (receivingUser.getId().equals(recognition.getSenderId())) {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.set("myResponseHeader", "selfRecognition");
            return new ResponseEntity<String>("Users cannot recognize themselves", httpHeaders, HttpStatus.OK);
        }
        recognition.setReceiverId(receivingUser.getId());
        recognitionService.createRecognition(recognition);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("myResponseHeader", "successfulRecognition");
        return new ResponseEntity<String>("User recognized!", httpHeaders, HttpStatus.OK);
    }

    // Revoke recognition
    @PutMapping("/recognitions/{id}")
    @ResponseBody
    public void revokeRecognition(@PathVariable("id") String id) {
        Integer recognitionId = Integer.parseInt(id);
        Recognition recognition = recognitionService.getRecognitionById(recognitionId).get();
        recognition.setRevoked(true);
        recognitionService.updateRecognition(recognition);
        // Update receiving user's badges
        User receivingUser = userService.findUserById(recognition.getReceiverId());
        userService.revokeReceivingUserBadge(receivingUser, recognition.getBadge());
        // Update sending user's badges
        User sendingUser = userService.findUserById(recognition.getSenderId());
        userService.updateSendingUserBadge(sendingUser, recognition.getBadge());
        // Send recognition receiver an email on revocation of badge
        SimpleMailMessage badgeRevokedEmail = new SimpleMailMessage();
        badgeRevokedEmail.setFrom("reap-support@ttn.com");
        badgeRevokedEmail.setTo(receivingUser.getEmail());
        badgeRevokedEmail.setSubject("REAP - Recognition Revoked");
        badgeRevokedEmail.setText("Hi, " + recognition.getReceiverName() +
                "!\nYour recognition with id " + recognition.getId() +
                " for a " + recognition.getBadge() +
                " badge shared by " + recognition.getSenderName() +
                " for " + recognition.getReason() +
                ", with comments '" + recognition.getComment() +
                "', made on " + recognition.getDate() +
                ", has been revoked." +
                "\nYou now have " + receivingUser.getPoints() + " redeemable points.");
        emailService.sendEmail(badgeRevokedEmail);
    }
}