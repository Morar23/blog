package blog.service.impl;

import blog.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import blog.entity.User;
import blog.repository.UserRepository;

import java.text.MessageFormat;

import static blog.util.StringUtils.INVALID_USERNAME;

@Service("blogUserDetailsService")
@AllArgsConstructor
public class BlogUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository
            .findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(MessageFormat.format(INVALID_USERNAME, email)));
        return new UserPrincipal(user);
    }
}