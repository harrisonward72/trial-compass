package com.TrialCompass.TrialCompass.service;

//unit tests for account registration

import com.TrialCompass.TrialCompass.DTO.RegisterRequest;
import com.TrialCompass.TrialCompass.entity.User;
import com.TrialCompass.TrialCompass.exception.EmailAlreadyExistsException;
import com.TrialCompass.TrialCompass.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class) //this allows mockito to create mock objects - this way SQL isn't required for unit tests
public class UserServiceTest {

    //Mock database
    @Mock
    private UserRepository userRepository;

    //This is a real Bcrypt encoder - by passing the unit test is proves that the encoder used in the app actually works
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    //TEST 1 ============================================================
    //runs through the registration process - cleans up input, hashes the password, saves the data

    @Test
    @DisplayName("Valid registration clean the email, hashes the password and stores the hashed version")
    void register_withValidRequest_hashesPasswordAndCleansEmail() {
        //email and name trimmed, email to lowercase, password hashed
        RegisterRequest request = new RegisterRequest("    fake@email.COM  ", "samplePassword", "   Harry   ");

        //programs the fake database with the sample input
        when(userRepository.existsByEmail("fake@email.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.register(request);

        assertThat(saved.getEmail()).isEqualTo("fake@email.com"); //trimmed and lower case
        assertThat(saved.getFirstName()).isEqualTo("Harry"); //trimmed
        assertThat(saved.getPasswordHash()).isNotEqualTo("samplePassword"); //should be the hashed version not the plain text password
        assertThat(saved.getPasswordHash()).startsWith("$2"); //bycrpt hashes always start with $2
        assertThat(passwordEncoder.matches("samplePassword", saved.getPasswordHash())).isTrue(); //verifies the password

        verify(userRepository).save(any(User.class)); //verifies only one copy of the information was saved
    }

    //TEST 2===============================================================
    //tests that an email cannot be used with more than one account - should be rejected

    @Test
    @DisplayName("Only one email per account - duplicate emails are rejected and not saved")
    void register_withExistingEmailIsRejected () {

        //service cleans and saves the email - checking to make sure that the service is cleaning data input, would fail if input was not cleaned
        RegisterRequest request = new RegisterRequest("Harry@fakeemail.com", "fakePassword", "Harry");
        when(userRepository.existsByEmail("harry@fakeemail.com")).thenReturn(true);

        //if an existing email exists looking for the custom exception to be thrown when the user tries to create account with the same email
        assertThatThrownBy(() -> userService.register(request)).isInstanceOf(EmailAlreadyExistsException.class).hasMessageContaining("harry@fakeemail.com");

        //checking the fake database to ensure duplicate emails do not exist
        verify(userRepository, never()).save(any(User.class));
    }
}
