package blog.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import blog.model.UserEditModel;
import blog.entity.Role;
import blog.entity.User;
import blog.repository.ArticleRepository;
import blog.repository.RoleRepository;
import blog.repository.UserRepository;
import blog.service.AdminUserService;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import static blog.util.StringUtils.*;

@Service
@AllArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;

    private final ArticleRepository articleRepository;

    private final RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public String loadListUsersView(Model model){
        List<User> users = this.userRepository.findAll();

        model.addAttribute(USERS, users);
        model.addAttribute(VIEW, ADMIN_USERS_LIST);

        return BASE_LAYOUT;
    }

    @Override
    public String loadUserEditView(Integer id, Model model){
        if(!this.userRepository.existsById(id)){
            return REDIRECT_ADMIN_USERS;
        }

        User user = this.userRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_USER_ID, id))
        );
        List<Role>roles = this.roleRepository.findAll();

        model.addAttribute(USER, user);
        model.addAttribute(ROLES, roles);
        model.addAttribute(VIEW, ADMIN_USERS_EDIT);

        return BASE_LAYOUT;
    }

    @Override
    public String editUser(Integer id, UserEditModel userEditModel){
        if(!this.userRepository.existsById(id)){
            return REDIRECT_ADMIN_USERS;
        }

        User user = this.userRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_USER_ID, id))
        );

        String password = userEditModel.getPassword();
        String confirmPassword = userEditModel.getConfirmPassword();

        if(!(password.isEmpty() || confirmPassword.isEmpty()) && password.equals(confirmPassword)){
            String encodedPassword = passwordEncoder.encode(userEditModel.getPassword());
            user.setPassword(encodedPassword);
        }

        user.setFullName(userEditModel.getFullName());
        user.setEmail(userEditModel.getEmail());

        List<Role> roles = new LinkedList<>();

        for (Integer roleId : userEditModel.getRoles()){
            Role role = this.roleRepository.findById(roleId).orElseThrow(
                () -> new IllegalArgumentException(MessageFormat.format(INVALID_ROLE_ID, id))
            );
            roles.add(role);
        }

        user.setRoles(roles);

        this.userRepository.saveAndFlush(user);

        return REDIRECT_ADMIN_USERS;
    }

    @Override
    public String loadUserDeleteView(Integer id, Model model){
        if(!this.userRepository.existsById(id)){
            return REDIRECT_ADMIN_USERS;
        }

        User user = this.userRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_USER_ID, id))
        );

        model.addAttribute(USER, user);
        model.addAttribute(VIEW, ADMIN_USERS_DELETE);

        return BASE_LAYOUT;
    }

    @Override
    public String deleteUser(Integer id){
        if(!this.userRepository.existsById(id)){
            return REDIRECT_ADMIN_USERS;
        }

        User user = this.userRepository.findById(id).orElseThrow(
            () -> new IllegalArgumentException(MessageFormat.format(INVALID_USER_ID, id))
        );

        this.articleRepository.deleteAll(user.getArticles());

        this.userRepository.delete(user);

        return REDIRECT_ADMIN_USERS;
    }
}
