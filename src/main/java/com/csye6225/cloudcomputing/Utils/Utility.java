package com.csye6225.cloudcomputing.Utils;


import com.csye6225.cloudcomputing.Models.UserModel;

import org.passay.*;
import org.passay.PasswordValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Utility {

    @Value("${STRENGTH}")
    private int strength;
    private static final String regex = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$";

    public String BCryptPassword(String value) {
        String salt = BCrypt.gensalt(strength);
        return BCrypt.hashpw(value, salt);
    }

    public boolean validatePassword(String password, String hashValue) {
        return BCrypt.checkpw(password, hashValue);
    }


    public String[] parseAuthorizationToken(String value) {
        byte[] decodedBytes = Base64.getDecoder().decode(value.split(" ")[1]);
        return new String(decodedBytes).split(":");
    }

    public boolean validateAuthorization(String value, String username, String passwordHash) {
        String[] userDetails = this.parseAuthorizationToken(value);
        return value == null || value.equals("") || userDetails.length != 2
                || !username.equalsIgnoreCase(userDetails[0])
                || !this.validatePassword(userDetails[1], passwordHash);
    }

    public boolean checkPasswordStrength(String password) {
        PasswordValidator validator = new PasswordValidator(Arrays.asList(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),// at least one upper-case character
                new CharacterRule(EnglishCharacterData.LowerCase, 1), // at least one lower-case character
                new CharacterRule(EnglishCharacterData.Digit, 1), // at least one digit character
                new CharacterRule(EnglishCharacterData.Special, 1), // at least one symbol (special character)
                new WhitespaceRule(),         // no whitespace
                new CharacterOccurrencesRule(4),
                new RepeatCharactersRule(2, 4)
        ));
        RuleResult result = validator.validate(new PasswordData(password));
        return result.isValid();
    }

    public boolean validateEmailAddress(String emailAddress) {
        Pattern pattern = Pattern.compile(this.regex);
        Matcher matcher = pattern.matcher(emailAddress);
        return matcher.matches();
    }

    public HashMap<String , String> prepareResponse(UserModel um ,String requestType)
    {
        HashMap<String , String>  response = new HashMap<>();
        if(requestType.equals("GET") || requestType.equals("POST"))
        {
            response.put("id", um.getId().toString());
            response.put("firstName", um.getFirstName());
            response.put("LastName", um.getLastName());
            response.put("username", um.getUsername());
            response.put("account_created", um.getCreatedDatetime().toString());
            response.put("account_updated", String.valueOf(um.getUpdatedDatetime()));
        }
        else if (requestType.equals("PUT"))
        {

            response.put("firstName", um.getFirstName());
            response.put("LastName", um.getLastName());
            response.put("username", um.getUsername());
            response.put("password",um.getPassword());
        }

        return response;

    }

}
