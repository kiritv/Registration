package com.ishtec.server.service;

import com.ishtec.server.types.ROLE_TYPE;
import com.ishtec.server.entities.LookupRole;
import com.ishtec.server.entities.UserSecret;
import com.ishtec.server.repository.UserSecretRepository;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {
    private final UserSecretRepository userSecretRepository;
    
    public MyUserDetailsService(UserSecretRepository userSecretRepository) {
        this.userSecretRepository = userSecretRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserDetails userDetails = null;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if(userSecret.isPresent())
        {
            List<GrantedAuthority> grantList = new ArrayList<>();
            for(LookupRole role : userSecret.get().getUserRoles()) {
                GrantedAuthority authority = new SimpleGrantedAuthority(role.getRoleName());
                grantList.add(authority);
            }
            userDetails = (UserDetails) new User(userSecret.get().getEmail(),
                    userSecret.get().getEncryptedPassword(), grantList);
        } else {
            throw new UsernameNotFoundException("User " + email + " was not found.");
        }
        return userDetails;
    }
    
    public boolean hasRole(ROLE_TYPE roleType) {
    	boolean hasRole = false;
    	Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>) 
    			SecurityContextHolder.getContext().getAuthentication().getAuthorities();
    	for (GrantedAuthority authority : authorities) {
	    	hasRole = authority.getAuthority().equals(roleType.toString());
	    	if (hasRole) {
	    		break;
	    	}
    	}
    	return hasRole;
    }
}
