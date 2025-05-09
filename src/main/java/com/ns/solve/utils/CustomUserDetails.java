package com.ns.solve.utils;

import com.ns.solve.domain.entity.Role;
import com.ns.solve.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User userEntity;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();
        GrantedAuthority authority = new SimpleGrantedAuthority(userEntity.getRole().name());
        collection.add(authority);
        return collection;
    }

    public Long getUserId() {return userEntity.getId();}

    public Role getUserRole() {return userEntity.getRole();}

    @Override public String getPassword() {return userEntity.getPassword();}
    @Override public String getUsername() {return userEntity.getAccount();}
    @Override public boolean isAccountNonExpired() {return true;}
    @Override public boolean isAccountNonLocked() {return true;}
    @Override public boolean isCredentialsNonExpired() {return true;}
    @Override public boolean isEnabled() {return true;}
}

