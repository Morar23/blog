package blog.controller;

import blog.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import blog.model.UserModel;
import blog.model.UserEditModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/register")
    public String register(Model model){
        return this.userService.loadRegisterView(model);
    }

    @PostMapping("/register")
    public String registerProcess(UserModel userModel) throws IOException {
        return this.userService.registerUser(userModel);
    }

    @GetMapping("/user/forgot-password/{id}")
    public String forgotPassword(@PathVariable Integer id, Model model){
        return this.userService.loadForgotPasswordView(id, model);
    }

    @PostMapping("/user/forgot-password/{id}")
    public String forgotPassProcess(@PathVariable Integer id, HttpServletRequest request, UserEditModel userEditModel){
        return this.userService.changeForgotPassword(id, request, userEditModel);
    }

    @GetMapping("/login")
    public String login(Model model){
        return this.userService.loadLoginView(model);
    }

    @GetMapping("/forgot-password-input-email")
    public String inputEmail(Model model){
        return this.userService.loadInputEmailView(model);
    }

    @PostMapping("/forgot-password-input-email")
    public String inputEmailProcess(UserModel userModel){
        return this.userService.sendMail(userModel);
    }

    @GetMapping("/logout")
    public String logoutPage(HttpServletRequest request, HttpServletResponse response){
        return this.userService.logoutFromPage(request, response);
    }

    @GetMapping("/profile")
    public String profilePage(Model model) throws IOException {
        return this.userService.loadProfilePageView(model);
    }

    @GetMapping("/send-mail")
    public String sendPasswordForgotMailPage(Model model){
        return this.userService.loadSendPasswordForgotMailPageView(model);
    }

    @GetMapping("/user/edit/{id}")
    public String edit(@PathVariable Integer id, Model model){
        return this.userService.loadEditView(id, model);
    }

    @PostMapping("/user/edit/{id}")
    public String editProcess(@PathVariable Integer id, UserEditModel userEditModel) throws IOException {
        return this.userService.editUser(id, userEditModel);
    }
}