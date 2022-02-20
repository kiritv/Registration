package com.ishtec.server.controller;

import com.ishtec.server.types.ROLE_TYPE;
import com.ishtec.server.TestData;
import com.ishtec.server.entities.UserProfile;
import com.ishtec.server.entities.UserSecret;
import com.ishtec.server.repository.UserSecretRepository;
import com.ishtec.server.service.MyUserDetailsService;
import com.ishtec.server.service.UserService;
import com.ishtec.server.util.JwtUtil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;


@DisplayName("User Controller Tests")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private MyUserDetailsService userDetailsService;
    @MockBean
    private UserSecretRepository userSecretRepository;
    @MockBean
    private JwtUtil jwtTokenUtil;


    private Optional<UserSecret> userSecret;
    private UserDetails userDetails;
    private UserDetails userDetailsAdmin;

    private final String authorization = "authorization";
    private final String authorizationHeader = "Bearer " + TestData.TOKEN;
    
    private final String jsonUserInfo = "{ \"email\": \"" + TestData.EMAIL + "\"" +
            ", \"password\": \"" + TestData.PASSWORD
            + "\", \"firstName\":\"" + TestData.FIRST_NAME
            + "\", \"lastName\":\"" + TestData.LAST_NAME + "\" }";

    private final String jsonLogin = "{ \"email\": \"" + TestData.EMAIL + "\"" +
            ", \"password\": \"" + TestData.PASSWORD + "\" }";

    private final String jsonPasswordChange = "{\"email\":\"" + TestData.EMAIL
            + "\", \"oldPassword\":\"" + TestData.PASSWORD
            + "\", \"newPassword\":\"" + TestData.PASSWORD + "\" }";

    private final String jsonUpdateUserInfo = "{\"email\":\"" + TestData.EMAIL
            + "\", \"firstName\":\"" + TestData.FIRST_NAME
            + "\", \"lastName\":\"" + TestData.LAST_NAME + "\" }";

    private final String jsonUserRole = "{\"email\":\"" + TestData.EMAIL
            + "\", \"roleName\":\"" + TestData.ROLE_NAME + "\" }";

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        userSecret = Optional.of(new UserSecret(TestData.EMAIL, TestData.PASSWORD, true));
        ArrayList<GrantedAuthority> roles = new ArrayList<>(Arrays.asList(new SimpleGrantedAuthority(ROLE_TYPE.ROLE_USER.toString())));
        userDetails = (UserDetails) new User(userSecret.get().getEmail(), userSecret.get().getEncryptedPassword(), roles);
        roles.add(new SimpleGrantedAuthority(ROLE_TYPE.ROLE_ADMIN.toString()));
        userDetailsAdmin = (UserDetails) new User(userSecret.get().getEmail(), userSecret.get().getEncryptedPassword(), roles);
    }

    @Test
    @DisplayName("loginUser token for valid user")
    void loginUserTokenTest() throws Exception {
        Mockito.when(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(userSecret);
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.generateToken(userDetails)).thenReturn(TestData.TOKEN);
        
        MvcResult requestResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users/login")
                        .content(jsonLogin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json("{\"token\": \"" + TestData.TOKEN + "\"}"))
                .andReturn();
        String json = requestResult.getResponse().getContentAsString();
        Assertions.assertNotNull(json);
    }

    @Test
    @DisplayName("loginUser for invalid user")
    void loginUserInvalidUserTest() throws Exception {
        Mockito.when(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(Optional.empty());
        MvcResult requestResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users/login")
                        .content(jsonLogin)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json("{\"status\": \"Unauthorized\"}"))
                .andReturn();
        String json = requestResult.getResponse().getContentAsString();
        Assertions.assertNotNull(json);
    }

    @Test
    @DisplayName("registerUser for valid user")
    void registerUserTest() throws Exception {
        Mockito.when(userService.createUser(TestData.EMAIL, TestData.PASSWORD)).thenReturn(true);
        Mockito.when(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME)).thenReturn(true);
        MvcResult requestResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(jsonUserInfo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andReturn();
        String location = requestResult.getResponse().getContentAsString();
        Assertions.assertNotNull(location);
    }

    @Test
    @DisplayName("registerUser for invalid user")
    void registerUserInvalidUserTest() throws Exception {
        Mockito.when(userService.createUser(TestData.EMAIL, TestData.PASSWORD)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(jsonUserInfo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("registerUser for invalid user update")
    void registerUserInvalidUserUpdateTest() throws Exception {
        Mockito.when(userService.createUser(TestData.EMAIL, TestData.PASSWORD)).thenReturn(true);
        Mockito.when(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users")
                        .content(jsonUserInfo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("passwordChange for valid user")
    void passwordChangeTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(userSecret);
        Mockito.when(userService.updateUserPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/password")
                        .header(authorization, authorizationHeader)
                        .content(jsonPasswordChange)
                        .contentType(MediaType.APPLICATION_JSON)
                        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("passwordChange for valid user and password update failed")
    void passwordChangePasswordUpdateFailedTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(userSecret);
        Mockito.when(userService.updateUserPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/password")
                        .header(authorization, authorizationHeader)
                        .content(jsonPasswordChange)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("passwordChange for invalid user")
    void passwordChangeInvalidUserTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.validateEmailAndPassword(TestData.EMAIL, TestData.PASSWORD)).thenReturn(Optional.empty());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/password")
                        .header(authorization, authorizationHeader)
                        .content(jsonPasswordChange)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("updateUserInfo for valid user")
    void updateUserInfoTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/")
                        .header(authorization, authorizationHeader)
                        .content(jsonUpdateUserInfo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("updateUserInfo failure check")
    void updateUserInfoFailureTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.updateUserProfile(TestData.EMAIL, TestData.FIRST_NAME, TestData.LAST_NAME)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/")
                        .header(authorization, authorizationHeader)
                        .content(jsonUpdateUserInfo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("addUserRole with ADMIN role")
    void addUserRoleAdminTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetailsAdmin);
        Mockito.when(userDetailsService.hasRole(ROLE_TYPE.ROLE_ADMIN)).thenReturn(true);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.addRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users/role")
                        .header(authorization, authorizationHeader)
                        .content(jsonUserRole)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("addUserRole without ADMIN role")
    void addUserRoleTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.addRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users/role")
                        .header(authorization, authorizationHeader)
                        .content(jsonUserRole)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("addUserRole failure check")
    void addUserRoleFailureTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.addRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/users/role")
                        .header(authorization, authorizationHeader)
                        .content(jsonUserRole)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("deleteUserRole with ADMIN role")
    void deleteUserRoleAdminTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetailsAdmin);
        Mockito.when(userDetailsService.hasRole(ROLE_TYPE.ROLE_ADMIN)).thenReturn(true);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.deleteRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/users/role")
                        .header(authorization, authorizationHeader)
                        .content(jsonUserRole)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("deleteUserRole without ADMIN role")
    void deleteUserRoleTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.deleteRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/users/role")
                        .header(authorization, authorizationHeader)
                        .content(jsonUserRole)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("deleteUserRole failure check")
    void deleteUserRoleFailureTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.deleteRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/users/role")
                        .header(authorization, authorizationHeader)
                        .content(jsonUserRole)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("checkUserRole")
    void checkUserRoleTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.existsRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(true);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/role/{roleName}", TestData.ROLE_NAME)
                        .header(authorization, authorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("checkUserRole failure check")
    void checkUserRoleFailureTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.existsRoleMembership(TestData.EMAIL, TestData.ROLE_NAME)).thenReturn(false);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/role/{roleName}", TestData.ROLE_NAME)
                        .header(authorization, authorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }

    @Test
    @DisplayName("logout with valid user")
    void logoutTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.clearUserTokens(TestData.EMAIL)).thenReturn(1L);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/logout")
                        .header(authorization, authorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("logout failure")
    void logoutFailureTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.clearUserTokens(TestData.EMAIL)).thenReturn(0L);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .put("/users/logout")
                        .header(authorization, authorizationHeader))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
    
    @Test
    @DisplayName("loginWithToken with valid token")
    void loginWithTokenTest() throws Exception {
        Mockito.when(jwtTokenUtil.getValidUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        MvcResult requestResult = mockMvc.perform(
                MockMvcRequestBuilders
                .get("/users/loginWithToken?token=" + TestData.TOKEN))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json("{\"token\": \"" + TestData.TOKEN + "\"}"))
                .andReturn();
        String json = requestResult.getResponse().getContentAsString();
        Assertions.assertNotNull(json);        
    }

    @Test
    @DisplayName("loginWithToken with invalid token")
    void loginWithTokenFailureTest() throws Exception {
        Mockito.when(jwtTokenUtil.getValidUsername(TestData.TOKEN)).thenReturn(null);
        mockMvc.perform(
                MockMvcRequestBuilders
                .get("/users/loginWithToken?token=" + TestData.TOKEN))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized());
    }

    @Test
    @DisplayName("getSingle for valid user")
    void getSingleTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.findUserByEmail(TestData.EMAIL)).thenReturn(userSecret);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/")
                        .header(authorization, authorizationHeader)
                        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
    
    @Test
    @DisplayName("getSingle for valid user with userinfo")
    void getSingleWithInfoTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        userSecret.get().setUserProfile(new UserProfile());
        Mockito.when(userService.findUserByEmail(TestData.EMAIL)).thenReturn(userSecret);
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/")
                        .header(authorization, authorizationHeader)
                        )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("getSingle for invalid email")
    void getSingleInvalidEmailTest() throws Exception {
        Mockito.when(userDetailsService.loadUserByUsername(TestData.EMAIL)).thenReturn(userDetails);
        Mockito.when(jwtTokenUtil.validateToken(TestData.TOKEN, userDetails)).thenReturn(true);
        Mockito.when(jwtTokenUtil.extractUsername(TestData.TOKEN)).thenReturn(TestData.EMAIL);
        Mockito.when(userService.findUserByEmail(TestData.EMAIL)).thenReturn(Optional.empty());
        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/users/")
                        .header(authorization, authorizationHeader)
                        )
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

}