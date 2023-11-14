package ru.ntl.gunk.cntrl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestUtils {
    @Test
    void printEncodedPass(){
        var passwordEncoder = new BCryptPasswordEncoder();
        System.out.println("PASSWORD " + passwordEncoder.encode("admin"));
    }
}
