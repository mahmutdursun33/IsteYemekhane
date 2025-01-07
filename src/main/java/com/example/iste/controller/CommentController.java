package com.example.iste.controller;

import com.example.iste.entity.Appointment;
import com.example.iste.entity.Comment;
import com.example.iste.service.AppointmentService;
import com.example.iste.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@SessionAttributes("username")  // Oturumda username'i tutmak
public class CommentController {
    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private CommentService commentService;

    @PostMapping("/api/comments")
    public String submitComment(@ModelAttribute Comment comment, Model model) {
        // Kullanıcının oturum bilgisini almak
        String username = (String) model.getAttribute("username");

        if (username == null) {
            model.addAttribute("error", "Kullanıcı oturum bilgisi bulunamadı.");
            return "login-user";  // Kullanıcı oturumu yoksa login sayfasına yönlendir
        }

        // Kullanıcının mevcut randevusunu kontrol et
        Comment existingAppointment = commentService.findByUsername(username);

        if (existingAppointment != null) {
            model.addAttribute("error", "Zaten daha önceden yorum yaptınız.");
            return "/user-dashboard"; // Eğer randevu varsa, kullanıcıyı yönlendirin
        }
        comment.setUsername(username);
        commentService.saveComment(comment.getContent(), comment.getStars(),comment.getUsername());

        // Başarılı işlemi takiben kullanıcıyı dashboard'a yönlendir
        return "user-dashboard";
    }
}
