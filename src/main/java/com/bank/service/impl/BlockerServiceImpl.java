package com.bank.service.impl;

import com.bank.domain.user.User;
import com.bank.service.BlockerService;
import com.bank.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.bank.service.BlockerService.UserFailStatus.*;

@Service
public class BlockerServiceImpl implements BlockerService{

    private static final Logger log = LoggerFactory.getLogger(BlockerService.class);
    private static final Map<String , Integer> attemptsRepository = new HashMap<>();
    private static final int MAX_ATTEMPTS = 3;


    @Autowired
    private UserService service;

    @Override
    public UserFailStatus failed(String loginOrEmail) {
        User user = (User) service.loadUserByUsername(loginOrEmail);
        if(user != null){
            String login = user.getUsername();
            Integer attempt = attemptsRepository.get(login);
            if(attempt == null){
                attempt = 1;
                attemptsRepository.put(login, attempt); // This mean that first attempt was failed
                log.debug("Authentication was failed. Remains " + (MAX_ATTEMPTS + 1 - attempt) + " attempts for " + loginOrEmail);
            }else if (++attempt > MAX_ATTEMPTS){
                log.debug("Number of attempts is exceed by user : " + user.getUsername() + " email : " + user.getEmail());
                service.setLocked(user);
                return BLOCK;
            }else{
                log.debug("Authentication was failed. Remains " + (MAX_ATTEMPTS + 1 - attempt) + " attempts for " + loginOrEmail);
                attemptsRepository.put(login, attempt);
            }
        }
        return WRONG;
    }


    @Override
    public void success(String loginOrEmail) {
        attemptsRepository.remove(loginOrEmail);
        log.debug("Clear authentication attempts for "  + loginOrEmail);
    }
}