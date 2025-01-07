package com.example.iste.controller;

import com.example.iste.entity.Appointment;
import com.example.iste.entity.Comment;
import com.example.iste.entity.User;
import com.example.iste.repository.AppointmentRepository;
import com.example.iste.repository.CommentRepository;
import com.example.iste.repository.UserRepository;
import com.example.iste.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@SessionAttributes("username") // Oturumda saklanacak birden fazla attribute
public class MainController {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    // Public page (no login required)
    @GetMapping("/public")
    public String publicPage() {
        return "public";
    }

    // Admin login page
    @GetMapping("/login-admin")
    public String adminPage() {
        return "login-admin";
    }

    // User login page
    @GetMapping("/login-user")
    public String loginUserPage() {
        return "login-user";
    }

    // Admin login process
    @PostMapping("/login-admin")
    public String loginAdmin(@RequestParam("email") String email,
                             @RequestParam("password") String password,
                             Model model) {
        if (email.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "Kullanıcı adı veya şifre boş olamaz.");
            return "login-admin"; // Error handling for empty inputs
        }

        // Use Spring Security for authentication instead of manual checking
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getName().equals(email)) {
            model.addAttribute("error", "Yanlış kullanıcı adı veya şifre.");
            return "login-admin"; // Incorrect login
        }

        User admin = (User) userService.loadUserByUsername(email);
        if (admin == null || !passwordEncoder.matches(password, admin.getPassword())) {
            model.addAttribute("error", "Yanlış kullanıcı adı veya şifre.");
            return "login-admin"; // Incorrect login
        }

        if (!admin.getRole().contains("ADMIN")) {
            model.addAttribute("error", "Yönetici yetkiniz yok.");
            return "login-admin"; // No permission to access
        }
        // Successful login
        return "redirect:/admin-dashboard"; // Redirect to admin dashboard
    }

    // User login process
    @PostMapping("/login-user")
    public String loginUser(@RequestParam("email") String email,
                            @RequestParam("password") String password,
                            Model model, RedirectAttributes redirectAttributes) {
        if (email.isEmpty() || password.isEmpty()) {
            model.addAttribute("error", "E-posta veya şifre boş olamaz.");
            return "login-user"; // Error handling for empty inputs
        }

        // Use Spring Security for authentication instead of manual checking
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getName().equals(email)) {
            model.addAttribute("error", "Yanlış e-posta veya şifre.");
            return "login-user"; // Incorrect login
        }

        User user = (User) userService.loadUserByUsername(email);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            model.addAttribute("error", "Yanlış e-posta veya şifre.");
            return "login-user"; // Incorrect login
        }

        if (!user.getRole().contains("USER")) {
            model.addAttribute("error", "Yönetici yetkiniz yok.");
            return "login-user"; // No permission to access
        }

        redirectAttributes.addAttribute("username", user.getUsername());

        // Successful login
        return "forward:/user-dashboard"; // Redirect to user dashboard
    }

    @Secured("ADMIN")
    @GetMapping("/admin-dashboard")
    public String adminDashboard(Model model) {
        List<Appointment> appointments = appointmentRepository.findAll();
        model.addAttribute("appointments", appointments);

        List<Comment> comments = commentRepository.findAll(); // Farz edelim ki Comment entity'niz var
        model.addAttribute("comments", comments);

        return "admin-dashboard";
    }

    @Secured("USER")
    @GetMapping("/user-dashboard")
    public String userDashboard(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Kullanıcı adını alır

        // Improved way to fetch user from database
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı!"));

        model.addAttribute("username", user.getUsername()); // Add username to model
        return "user-dashboard"; // user-dashboard.html template
    }

}

