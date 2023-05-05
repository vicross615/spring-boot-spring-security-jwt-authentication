package com.bezkoder.springjwt.util;

import org.passay.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by USER on 5/5/2023.
 */
public class PasswordValidatorUtil {

    public static String validatePassword(String password) {
        // Create a password validator with a list of rules
        org.passay.PasswordValidator validator = new org.passay.PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
//                new UppercaseCharacterRule(1),
//                new DigitCharacterRule(1),
//                new SpecialCharacterRule(1),
//                new NumericalSequenceRule(3, false),
//                new AlphabeticalSequenceRule(3, false),
//                new QwertySequenceRule(3, false),
                new WhitespaceRule()));

        // Validate the password using the validator
        RuleResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return null;
        } else {
            // Return the error messages if the password doesn't meet the requirements
            List<String> messages = validator.getMessages(result);
            return messages.stream().collect(Collectors.joining(", "));
        }
    }
}