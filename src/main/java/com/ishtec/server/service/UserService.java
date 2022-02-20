package com.ishtec.server.service;

import com.ishtec.server.repository.LookupRoleRepository;
import com.ishtec.server.types.ROLE_TYPE;
import com.ishtec.server.data.DataLoader;
import com.ishtec.server.entities.LookupStatus;
import com.ishtec.server.entities.UserProfile;
import com.ishtec.server.entities.UserSecret;
import com.ishtec.server.entities.UserStatusLog;
import com.ishtec.server.exceptions.LookupStatusNotFoundException;
import com.ishtec.server.repository.LookupStatusRepository;
import com.ishtec.server.repository.UserSecretRepository;
import com.ishtec.server.repository.UserStatusLogRepository;
import com.ishtec.server.repository.UserTokenRepository;
import com.ishtec.server.types.STATUS_TYPE;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Handle all user related services
 */
@Service
public class UserService {
    private final EmailService emailService;
    private final UserSecretRepository userSecretRepository;
    private final LookupRoleRepository roleRepository;
    private final LookupStatusRepository statusRepository;
    private final UserStatusLogRepository userStatusLogRepository;
    private final UserTokenRepository userTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(EmailService emailService,
    		UserSecretRepository userSecretRepository,
            LookupRoleRepository roleRepository,
            LookupStatusRepository statusRepository,
            UserStatusLogRepository userStatusLogRepository,
            UserTokenRepository userTokenRepository,
            BCryptPasswordEncoder passwordEncoder) {
    	this.emailService = emailService;
        this.userSecretRepository = userSecretRepository;
        this.roleRepository = roleRepository;
        this.statusRepository = statusRepository;
        this.userStatusLogRepository = userStatusLogRepository;
        this.passwordEncoder = passwordEncoder;
        this.userTokenRepository = userTokenRepository;
    }

    /**
     * Encodes password
     * 
     * @param password
     * @return Encoded password
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Matches password with encoded password
     * 
     * @param password
     * @param encodedPassword
     * @return true if password matches with encoded one
     */
    public boolean matchesPassword(String password, String encodedPassword) {
        return passwordEncoder.matches(password, encodedPassword);
    }

    /**
     * Validates email and password for a user and retrieves user, if validated
     * 
     * @param email
     * @param password
     * @return valid user or empty based on user validation
     */
    public Optional<UserSecret> validateEmailAndPassword(String email, String password) {
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if (userSecret.isPresent()
                && !(userSecret.get().getEnabled()
                    && matchesPassword(password, userSecret.get().getEncryptedPassword()))) {

                userSecret = Optional.empty();
        }
        return userSecret;
    }

    /**
     * find user by email address
     * 
     * @param email
     * @return UserSecret object if present and active
     */
    public Optional<UserSecret> findUserByEmail(String email) {
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if (userSecret.isPresent() && !(userSecret.get().getEnabled())) {
            userSecret = Optional.empty();
        }
        return userSecret;
    }

    /**
     * Checks user's role membership
     * @param email
     * @param roleType
     * @return true if user is active and belongs to the role, false otherwise
     */
    public boolean existsRoleMembership(String email, ROLE_TYPE roleType) {
        boolean result = false;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if(userSecret.isPresent()
                && userSecret.get().getEnabled()) {
            if(userSecret.get().getUserRoles()
                    .stream()
                    .filter(r -> r.getRoleName().equals(roleType.toString()))
                    .collect(Collectors.toSet())
                    .size() == 1) {
                result = true;
            }
        }
        return result;
    }

    /**
     * Creates a NEW user and assigns to ROLE_USER by default
     * 
     * @param email
     * @param password
     * @return true if user created successfully
     */
    @Transactional
    public boolean createUser(String email, String password) {
        boolean result = false;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if (!userSecret.isPresent()) {
            // create new user with encrypted password
            UserSecret newUserSecret = new UserSecret(email, encodePassword(password), true);
            // add new user to default user role
            newUserSecret.getUserRoles().add(roleRepository.findByRoleName(ROLE_TYPE.ROLE_USER.toString()).get());
            userSecretRepository.save(newUserSecret);
            // save user with status as new
            String newStatusString = STATUS_TYPE.NEW_STATUS.toString();
            LookupStatus newStatus = statusRepository.findByStatusName(newStatusString)
                .orElseThrow(() -> new LookupStatusNotFoundException(newStatusString));
            UserStatusLog userStatusLog = new UserStatusLog(newUserSecret, newStatus);
            userStatusLogRepository.save(userStatusLog);
            // success
            result = true;
        }
        return result;
    }

    /**
     * adds role to the user (if role is missing)
     * @param email
     * @param roleType
     * @return true is added/verified
     */
    @Transactional
    public boolean addRoleMembership(String email, ROLE_TYPE roleType) {
        boolean result = false;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if(userSecret.isPresent()
                && userSecret.get().getEnabled()) {
            if(userSecret.get().getUserRoles()
                    .stream()
                    .filter(r -> r.getRoleName().equals(roleType.toString()))
                    .collect(Collectors.toSet())
                    .size() == 0) {
                // add role to the user
                userSecret.get().getUserRoles().add(roleRepository.findByRoleName(roleType.toString()).get());
                userSecretRepository.save(userSecret.get());
            }
            // success
            result = true;
        }
        return result;
    }

    /**
     * removes role from the user (if role is missing)
     * @param email
     * @param roleType
     * @return true is deleted/verified
     */
    @Transactional
    public boolean deleteRoleMembership(String email, ROLE_TYPE roleType) {
        boolean result = false;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if(userSecret.isPresent()
                && userSecret.get().getEnabled()) {
            if(userSecret.get().getUserRoles()
                    .stream()
                    .filter(r -> r.getRoleName().equals(roleType.toString()))
                    .collect(Collectors.toSet())
                        .size() == 1) {
                // delete role to the user
                userSecret.get().getUserRoles().remove(roleRepository.findByRoleName(roleType.toString()).get());
                userSecretRepository.save(userSecret.get());
            }
            // success
            result = true;
        }
        return result;
    }

    /**
     * Update user password
     * 
     * @param email
     * @param newPassword
     * @return true if succeeded in password update
     */
    @Transactional
    public boolean updateUserPassword(String email, String newPassword) {
        boolean result = false;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if (userSecret.isPresent()) {
            userSecret.get().setEncryptedPassword(encodePassword(newPassword));
            userSecretRepository.save(userSecret.get());
            // success, clear all tokens for this user
            userTokenRepository.deleteByEmail(email);
            result = true;
        }
        return result;
    }

    /**
     * Updates user profile information
     * 
     * @param email
     * @param firstName
     * @param lastName
     * @return true on success
     */
    @Transactional
    public boolean updateUserProfile(String email, String firstName, String lastName) {
        boolean result = false;
        Optional<UserSecret> userSecret = userSecretRepository.findByEmailIgnoreCase(email);
        if (userSecret.isPresent()) {
            if (userSecret.get().getUserProfile() == null) {
                UserProfile userProfile = new UserProfile(userSecret.get(), firstName, lastName);
                userSecret.get().setUserProfile(userProfile);
            } else {
                userSecret.get().getUserProfile().setFirstName(firstName);
                userSecret.get().getUserProfile().setLastName(lastName);
            }
            userSecretRepository.save(userSecret.get());
            // success
            result = true;
        }
        return result;
    }

    /**
     * Clear user tokens
     * 
     * @param email
     */
    @Transactional
    public long clearUserTokens(String email) {
        return userTokenRepository.deleteByEmail(email);
    }

    /**
     * Sends reset password email message
     * @param email
     * @param location
     * @return
     */
	public boolean sendResetPasswordMessage(String email, String location) {
		String message = DataLoader.EMAIL_MESSAGE_PREFIX + location;
		return emailService.sendEmail(email, email, DataLoader.EMAIL_SUBJECT, message);
	}

}
