package com.example.cinemabooking.config.auth;
import java.util.Arrays;
import java.util.List;
public class FacebookAuthProviderConfig {
    public FacebookAuthProviderConfig(){}

    public List<String> getFacebookReadPermissions()
    {
        return Arrays.asList("email", "public_profile");
    }
}
