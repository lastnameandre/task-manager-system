package home.taskmanager.controller;

import home.taskmanager.model.User;
import home.taskmanager.security.CustomAuthSuccessHandler;
import home.taskmanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private CustomAuthSuccessHandler customAuthSuccessHandler;

    // Форма реєстрації
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public void registerSubmit(@Valid @ModelAttribute User user,
                               BindingResult result,
                               HttpServletRequest request,
                               HttpServletResponse response) throws Exception {

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            result.rejectValue("username", "error.user", "Username is already taken");
        }

        if (result.hasErrors()) {
            request.setAttribute("org.springframework.validation.BindingResult.user", result);
            request.setAttribute("user", user);
            request.getRequestDispatcher("/register").forward(request, response);
            return;
        }

        User savedUser = userService.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        // 🔥 ВАЖЛИВО — додаємо в контекст
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 🔥 ВАЖЛИВО — створюємо сесію
        request.getSession(true).setAttribute(
                "SPRING_SECURITY_CONTEXT",
                SecurityContextHolder.getContext()
        );

        // викликаємо handler
        customAuthSuccessHandler.onAuthenticationSuccess(request, response, auth);
    }

    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "create_user";
    }

    @PostMapping("/create")
    public String createUserSubmit(@Valid @ModelAttribute User user,
                                   BindingResult result) {
        if (result.hasErrors()) {
            return "create_user";
        }
        userService.save(user);
        return "redirect:/dashboard";
    }
}