package com.project.ems.auth.util;

import org.mindrot.jbcrypt.BCrypt;
import com.project.ems.common.exception.InvalidPasswordFormatException;



/*
 * Provides password hashing and verification utilities.
 *
 * Responsibilities:
 * - Enforce password strength requirements
 * - Securely hash passwords using BCrypt
 * - Verify user credentials against stored hashes
 *
 * Centralizes credential handling to ensure consistent security rules.
 */
public final class PasswordUtil {

	private PasswordUtil(){
		
	}
	public static String hashPassword(String plainPassword)  throws InvalidPasswordFormatException{
		if(!plainPassword.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{8,}$")) {
			throw new InvalidPasswordFormatException("Invalid password format");
		}
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}