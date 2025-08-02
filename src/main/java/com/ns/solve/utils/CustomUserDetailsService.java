package com.ns.solve.utils;


import com.ns.solve.domain.entity.user.User;
import com.ns.solve.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        User user = userRepository.findByAccount(account);

        if (user == null) {
            throw new UsernameNotFoundException("Not found account: " + account);
        }

        return new CustomUserDetails(user);
    }
}
