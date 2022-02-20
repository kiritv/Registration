package com.ishtec.server.data;

import com.ishtec.server.entities.LookupRole;
import com.ishtec.server.repository.LookupRoleRepository;
import com.ishtec.server.types.ROLE_TYPE;
import com.ishtec.server.entities.LookupStatus;
import com.ishtec.server.repository.LookupStatusRepository;
import com.ishtec.server.types.STATUS_TYPE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class DataLoader implements CommandLineRunner {
    private final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    private final LookupRoleRepository roleRepository;
    private final LookupStatusRepository statusRepository;

    public static final String SUCCESS_MESSAGE = "Success";
    public static final String FAILURE_MESSAGE = "Failed";
    
	public static final String EMAIL_SUBJECT = "Password Reset:";
	public static final String EMAIL_MESSAGE_PREFIX = "Please click on this link to reset your password: ";

    public DataLoader(LookupRoleRepository roleRepository,
            LookupStatusRepository statusRepository) {
        this.roleRepository = roleRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public void run(String... args) {
        setupLookup();
    }
    @Transactional
    void setupLookup() {
        logger.info("Loading lookup data...");
        // Role
        for(ROLE_TYPE role : ROLE_TYPE.values()) {
            if(! roleRepository.findByRoleName(role.toString()).isPresent()) {
                roleRepository.save(new LookupRole(role.toString()));
            }
        }
        // Status
        for(STATUS_TYPE status : STATUS_TYPE.values()) {
            if(! statusRepository.findByStatusName(status.toString()).isPresent()) {
                statusRepository.save(new LookupStatus(status.toString()));
            }
        }
        logger.info("Loading lookup data done.");
    }
}
