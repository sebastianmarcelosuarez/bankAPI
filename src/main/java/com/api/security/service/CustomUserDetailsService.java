package com.api.security.service;

import com.api.security.entity.User;
import com.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String userName)  {
        User user = userRepository.findByUserName(userName) ;
        return new org.springframework.security.core.userdetails.User(user.getUserName(),user.getPassword(),new ArrayList<>());
    }
}
