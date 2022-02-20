package com.ishtec.server.service;

import com.ishtec.server.repository.LookupRoleRepository;
import com.ishtec.server.entities.LookupRole;
import com.ishtec.server.TestData;
import com.ishtec.server.entities.LookupStatus;
import com.ishtec.server.entities.UserProfile;
import com.ishtec.server.entities.UserSecret;
import com.ishtec.server.entities.UserStatusLog;
import com.ishtec.server.repository.LookupStatusRepository;
import com.ishtec.server.repository.UserSecretRepository;
import com.ishtec.server.repository.UserStatusLogRepository;
import com.ishtec.server.repository.UserTokenRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.util.Optional;

@DisplayName("User Service Tests")
@SpringBootTest
public class UserServiceTest {
    @InjectMocks
    @Resource
    private UserService userService;
        
    @MockBean
    private UserSecretRepository userSecretRepository;
    @MockBean
    private LookupRoleRepository roleRepository;
    @MockBean
    private LookupStatusRepository statusRepository;
    @MockBean
    private UserStatusLogRepository userStatusLogRepository;
    @MockBean
    private UserTokenRepository userTokenRepository;
    @MockBean
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private MyUserDetailsService myUserDetailsService;
    //@MockBean
    //private JavaMailSender javaMailSender;
    
    private Optional<UserSecret> userSecret;
    private Optional<LookupRole> lookupRole;
    private Optional<LookupStatus> lookupStatus;
    private Optional<UserStatusLog> userStatusLog;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        userSecret = Optional.of(new UserSecret(TestData.EMAIL, TestData.PASSWORD, true));
        lookupRole = Optional.of(new LookupRole(TestData.ROLE_NAME.toString()));
        lookupStatus = Optional.of(new LookupStatus(TestData.STATUS_NAME.toString()));
        userStatusLog = Optional.of(new UserStatusLog(userSecret.get(), lookupStatus.get()));
    }
    @AfterEach
    public void finalize() {
        // Nothing to do here
    }

    @Test
    @DisplayName("createUser for new user")
    void createUserTest() {
        //Optional<UserSecret> userSecretOriginal = Optional.of(userSecret.get());
        //Optional<UserStatusLog> userStatusLogOriginal = Optional.of(userStatusLog.get());
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode(TestData.PASSWORD)).thenReturn(TestData.PASSWORD);
        Mockito.when(roleRepository.findByRoleName(TestData.ROLE_NAME.toString())).thenReturn(lookupRole);
        Mockito.when(statusRepository.findByStatusName(TestData.STATUS_NAME.toString())).thenReturn(lookupStatus);
        Mockito.when(userSecretRepository.save(userSecret.get())).thenReturn(null);
        Mockito.when(userStatusLogRepository.save(userStatusLog.get())).thenReturn(null);
        Assertions.assertTrue(userService.createUser(TestData.EMAIL, TestData.PASSWORD), "createUser call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(passwordEncoder, Mockito.times(1)).encode(TestData.PASSWORD);
        Mockito.verify(roleRepository, Mockito.atLeast(1)).findByRoleName(TestData.ROLE_NAME.toString());
        Mockito.verify(statusRepository, Mockito.atLeast(1)).findByStatusName(TestData.STATUS_NAME.toString());
        //TODO: Need to check this calls are done only once, issue is the object has been changed at verification time
        //Mockito.verify(userSecretRepository, Mockito.times(1)).save(userSecretOriginal.get());
        //Mockito.verify(userStatusLogRepository, Mockito.times(1)).save(userStatusLogOriginal.get());
    }
    @Test
    @DisplayName("createUser for existing user")
    void createUserExistingTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertFalse(userService.createUser(TestData.EMAIL, TestData.PASSWORD), "creteUser call for existing user is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("validateEmailAndPassword for existing user")
    void validateEmailAndPasswordTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(passwordEncoder.matches(TestData.PASSWORD, TestData.PASSWORD)).thenReturn(true);
        Assertions.assertTrue(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD).isPresent(), "validateEmailAndPassword call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(passwordEncoder, Mockito.times(1)).matches(TestData.PASSWORD, TestData.PASSWORD);
    }
    @Test
    @DisplayName("validateEmailAndPassword for password mismatch")
    void validateEmailAndPasswordInvalidTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(passwordEncoder.matches(TestData.PASSWORD, TestData.PASSWORD)).thenReturn(false);
        Assertions.assertFalse(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD).isPresent(), "validateEmailAndPassword call for invalid password match is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(passwordEncoder, Mockito.times(1)).matches(TestData.PASSWORD, TestData.PASSWORD);
    }
    @Test
    @DisplayName("validateEmailAndPassword for non-existing user")
    void validateEmailAndPasswordUserDoesNotExistsTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertFalse(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD).isPresent(), "validateEmailAndPassword call for user which does not exists failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("findUserByEmail for existing user")
    void findUserByEmailTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertTrue(userService.findUserByEmail(TestData.EMAIL).isPresent(), "findUserByEmail call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("findUserByEmail for non-existing user")
    void findUserByEmailInvalidTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertFalse(userService.findUserByEmail(TestData.EMAIL).isPresent(), "findUserByEmail call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("findUserByEmail for existing inactive user")
    void findUserByEmailInactiveTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        userSecret.get().setEnabled(false);
        Assertions.assertFalse(userService.findUserByEmail(TestData.EMAIL).isPresent(), "findUserByEmail call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("existsRoleMembership with valid role")
    void existsRoleMembershipTest() {
        userSecret.get().getUserRoles().add(lookupRole.get()); // add default role to check that membership
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertTrue(userService.existsRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "existsRoleMembership with valid default role call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("existsRoleMembership without valid role")
    void existsRoleMembershipWithoutRoleTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertFalse(userService.existsRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "existsRoleMembership without valid default role call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("addRoleMembership with valid role")
    void addRoleMembershipTest() {
        Optional<UserSecret> userSecretOriginal = Optional.of(userSecret.get());
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(roleRepository.findByRoleName(TestData.ROLE_NAME.toString())).thenReturn(lookupRole);
        Mockito.when(userSecretRepository.save(userSecret.get())).thenReturn(null);
        Assertions.assertTrue(userService.addRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "addRoleMembership with valid role call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(roleRepository, Mockito.atLeast(1)).findByRoleName(TestData.ROLE_NAME.toString());
        Mockito.verify(userSecretRepository, Mockito.times(1)).save(userSecretOriginal.get());
    }
    @Test
    @DisplayName("addRoleMembership with valid existing role")
    void addRoleMembershipWithExistingRoleTest() {
        userSecret.get().getUserRoles().add(lookupRole.get()); // add default role to check that membership
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertTrue(userService.addRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "addRoleMembership with valid existing role call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("addRoleMembership with invalid user")
    void addRoleMembershipWithInvalidUserTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertFalse(userService.addRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "addRoleMembership with invalid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }

    @Test
    @DisplayName("deleteRoleMembership with valid role")
    void deleteRoleMembershipTest() {
        Optional<UserSecret> userSecretOriginal = Optional.of(userSecret.get());
        userSecret.get().getUserRoles().add(lookupRole.get());
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(roleRepository.findByRoleName(TestData.ROLE_NAME.toString())).thenReturn(lookupRole);
        Mockito.when(userSecretRepository.save(userSecret.get())).thenReturn(null);
        Assertions.assertTrue(userService.deleteRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "deleteRoleMembership with valid role call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(roleRepository, Mockito.atLeast(1)).findByRoleName(TestData.ROLE_NAME.toString());
        Mockito.verify(userSecretRepository, Mockito.times(1)).save(userSecretOriginal.get());
    }
    @Test
    @DisplayName("deleteRoleMembership with valid existing role")
    void deleteRoleMembershipWithExistingRoleTest() {
        userSecret.get().getUserRoles().remove(lookupRole.get()); // delete default role
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertTrue(userService.deleteRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "deleteRoleMembership with valid existing role call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("deleteRoleMembership with invalid user")
    void deleteRoleMembershipWithInvalidUserTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertFalse(userService.deleteRoleMembership(TestData.EMAIL, TestData.ROLE_NAME), "deleteRoleMembership with invalid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }

    @Test
    @DisplayName("updateUserPassword with valid user")
    void updateUserPasswordTest() {
        String newPassword = TestData.PASSWORD.toUpperCase();
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(userSecretRepository.save(userSecret.get())).thenReturn(null);
        Assertions.assertTrue(userService.updateUserPassword(TestData.EMAIL, newPassword), "updateUserPassword with valid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(userSecretRepository, Mockito.times(1)).save(userSecret.get());
    }
    @Test
    @DisplayName("updateUserPassword with invalid user")
    void updateUserPasswordInvalidUserTest() {
        String newPassword = TestData.PASSWORD.toUpperCase();
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertFalse(userService.updateUserPassword(TestData.EMAIL, newPassword), "updateUserPassword with invalid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("updateUserProfile with valid user")
    void updateUserProfileTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(userSecretRepository.save(userSecret.get())).thenReturn(null);
        Assertions.assertTrue(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME), "updateUserProfile with valid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(userSecretRepository, Mockito.times(1)).save(userSecret.get());
    }
    @Test
    @DisplayName("updateUserProfile with invalid user")
    void updateUserProfileInvalidUserTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertFalse(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME), "updateUserProfile with invalid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("updateUserProfile with valid user and existing profile")
    void updateUserProfileWithExistingProfileTest() {
        userSecret.get().setUserProfile(new UserProfile(userSecret.get(), TestData.FIRST_NAME, TestData.LAST_NAME));
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Mockito.when(userSecretRepository.save(userSecret.get())).thenReturn(null);
        Assertions.assertTrue(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME), "updateUserProfile with valid user and existing profile call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
        Mockito.verify(userSecretRepository, Mockito.times(1)).save(userSecret.get());
    }

    @Test
    @DisplayName("clearUserTokens with valid user")
    void clearUserTokensTest() {
        Mockito.when(userTokenRepository.deleteByEmail(TestData.EMAIL)).thenReturn(1L);
        Assertions.assertEquals(1L, userService.clearUserTokens(TestData.EMAIL), "clearUserTokens with valid user call is failing");
        Mockito.verify(userTokenRepository, Mockito.times(1)).deleteByEmail(TestData.EMAIL);
    }

    @Test
    @DisplayName("MyUserDetailsService.loadUserByUsername with valid user")
    void loadUserByUsernameTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(userSecret);
        Assertions.assertNotNull(myUserDetailsService.loadUserByUsername(TestData.EMAIL), "MyUserDetailsService.loadUserByUsername with valid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }
    @Test
    @DisplayName("MyUserDetailsService.loadUserByUsername with invalid user")
    void loadUserByUsernameFailureTest() {
        Mockito.when(userSecretRepository.findByEmailIgnoreCase(TestData.EMAIL)).thenReturn(Optional.empty());
        Assertions.assertThrows(UsernameNotFoundException.class, () -> myUserDetailsService.loadUserByUsername(TestData.EMAIL), "MyUserDetailsService.loadUserByUsername with invalid user call is failing");
        Mockito.verify(userSecretRepository, Mockito.times(1)).findByEmailIgnoreCase(TestData.EMAIL);
    }

}
