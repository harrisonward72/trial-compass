package com.TrialCompass.TrialCompass.service;

import com.TrialCompass.TrialCompass.DTO.LoginRequest;
import com.TrialCompass.TrialCompass.DTO.RegisterRequest;
import com.TrialCompass.TrialCompass.entity.User;
import com.TrialCompass.TrialCompass.exception.EmailAlreadyExistsException;
import com.TrialCompass.TrialCompass.exception.InvalidCredentialsException;
import com.TrialCompass.TrialCompass.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//business rules for users - controller handles incoming http, the repo handles
//mySql, this handles the inbetween
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //everything either succeeds together or fails together, no half saved user
    @Transactional
    public User register(RegisterRequest request) {
        //handle input
        String email = request.email().trim().toLowerCase();
        String firstName = request.firstName().trim().toLowerCase();
        //String lastName = request.lastName().trim().toLowerCase();

        //check if email already exists
        if(userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        //hash password with Bcrypt - salted one way hash
        String hash = passwordEncoder.encode(request.password());

        User user = new User(email, hash, firstName);

        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    //checks email and password - returns the user if they match
    //read only boolean will tell the database were only reading
    @Transactional(readOnly = true)
    public User authenticate(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        // throw an error if no one has this email
        User user = userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);

        //matches() hashes the typed password with the salt stored inside the saved hash and compares them
        if(!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }

    //finds user by email
    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(InvalidCredentialsException::new);
    }


}
