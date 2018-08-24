package com.a18.auth.config;

import com.a18.auth.model.User;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

public class CustomTokenEnhancer extends JwtAccessTokenConverter {

  @Override
  public OAuth2AccessToken enhance(
      OAuth2AccessToken accessToken,
      OAuth2Authentication authentication
  ) {
    User user = (User) authentication.getPrincipal();

    Map<String, Object> info = new HashMap<>(accessToken.getAdditionalInformation());

    info.put("userId", user.getId());
    info.put("fullname", user.getFullname());

    DefaultOAuth2AccessToken customAccessToken = new DefaultOAuth2AccessToken(accessToken);
    customAccessToken.getAdditionalInformation().putAll(info);

    return super.enhance(customAccessToken, authentication);
  }
}