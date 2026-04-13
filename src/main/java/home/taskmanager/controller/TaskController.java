package home.taskmanager.controller;
import home.taskmanager.model.Task;
import home.taskmanager.model.User;
import home.taskmanager.service.TaskService;
import home.taskmanager.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("tasks", taskService.getTasksForCurrentUser());
        return "dashboard";
    }

    @GetMapping("/tasks/create")
    public String createTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("users", userService.findAll());
        return "create_task";
    }
    @PostMapping("/tasks/create")
    public String createTaskSubmit(@Valid @ModelAttribute Task task,
                                   BindingResult result,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.findAll());
            return "create_task";
        }

        // отримуємо поточного користувача через Spring Security
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User currentUser;
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            currentUser = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            throw new RuntimeException("No authenticated user");
        }

        // прив'язуємо таск до поточного користувача
        task.setAssignedTo(currentUser);

        taskService.save(task);
        return "redirect:/dashboard";
    }

    @GetMapping("/tasks/{id}")
    public String taskDetails(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.findById(id));
        return "task_details";
    }

    @GetMapping("/tasks/edit/{id}")
    public String editTaskForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", taskService.findById(id));
        model.addAttribute("users", userService.findAll());
        return "create_task";
    }

    @PostMapping("/tasks/edit/{id}")
    public String editTaskSubmit(@PathVariable Long id,
                                 @Valid @ModelAttribute Task task,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("users", userService.findAll());
            return "create_task";
        }
        taskService.update(id, task);
        return "redirect:/dashboard";
    }

    @GetMapping("/tasks/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.delete(id);
        return "redirect:/dashboard";
    }
}

