package org.filespace.security;

import org.filespace.model.entities.simplerelations.User;
import org.filespace.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findUserByUsername(username);
        if (user == null)
            throw new UsernameNotFoundException("No such user");
        return UserDetailsServiceImpl.fromUser(user);
    }

    public static UserDetails fromUser(User user) {
        return new UserDetailsImpl(user.getUsername(), user.getPassword(),
                user.isEnabled(), new LinkedList<GrantedAuthority>());
    }
}
