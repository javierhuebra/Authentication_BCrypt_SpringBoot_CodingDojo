package com.codingdojo.authentication.users;

import com.codingdojo.authentication.models.User;
import com.codingdojo.authentication.services.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller //Esto se llama Users igual que el modelo pero es el Controller je
public class Users {
    private final UserService userService;

    public  Users(UserService userService){
        this.userService = userService;
    }

    // GET Y POST PARA REGISTRO ----------------------
    @GetMapping("/registration")
    public String renderRegisterForm(@ModelAttribute("user") User user){
        return "registrationPage";
    }

    @PostMapping("/registration")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result, HttpSession session, RedirectAttributes redirectAttributes){
        if(result.hasErrors()){
            return "redirect:/registration";
        }else{

            if(!user.getPassword().equals(user.getPasswordConfirmation())){
                redirectAttributes.addFlashAttribute("errorPasswords", "Las contraseñas no coinciden!");
                return "redirect:/registration";
            }

            userService.registerUser(user);
            session.setAttribute("idLogueado", user.getId());//Guardo el id del usuario en la sesion

            return "redirect:/home";
        }
    }

    // GET Y POST PARA LOGIN -------------------------
    @GetMapping("/login")
    public String renderLogin(){ //No le pongo ningun parametro modelatribute porque va a recibir un requestparam

        return "loginPage";
    }

    @PostMapping("/login")
    public String loginUser(@RequestParam("email") String email,
                            @RequestParam("password") String password,
                            Model model, HttpSession session){
        System.out.println(email + password);
        if(userService.authenticateUser(email,password)){
            session.setAttribute("idLogueado",userService.findByEmail(email).getId());//Busco el usuario por email y saco el id con el getter

            return "redirect:/home";
        }else{
            System.out.println("hay errores");
            model.addAttribute("error","Credenciales inválidas!");
            System.out.println(model.getAttribute("error"));
            return "loginPage"; //Esto podria ser un flash atribute y andaria mejor
        }
    }

    // GET PARA HOME Y LOGOUT
    @GetMapping("/home")
    public String renderHome(HttpSession session, Model model){
        Long idLogueado = (Long) session.getAttribute("idLogueado"); //Hay que acordarse de castear a Long
        if( idLogueado != null){
            User userLogueado = userService.findUserById(idLogueado);
            model.addAttribute("usuario", userLogueado);
            return "home";
        }else{
            return "redirect:/login";
        }
    }

    @GetMapping("/logout")
    public String logOut(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }
}
