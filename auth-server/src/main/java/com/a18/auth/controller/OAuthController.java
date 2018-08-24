package com.a18.auth.controller;

import com.a18.auth.service.TokenBlackListService;
import com.a18.common.util.StringUtil;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OAuthController {
  public static final String PATH_OAUTH = "/oauth";

  public static final String PATH_CHECK_TOKEN = PATH_OAUTH + "/check_token";

  @Autowired private DefaultTokenServices tokenServices;

  @Autowired private AccessTokenConverter accessTokenConverter;

  @Autowired private TokenBlackListService tokenBlackListService;

  @RequestMapping(PATH_CHECK_TOKEN)
  @ResponseBody
  public Map<String, ?> checkToken(@RequestParam("token") String value) {
    if (StringUtil.isBlank(value)) throw new InvalidTokenException("Token was empty");

    OAuth2AccessToken token = tokenServices.readAccessToken(value);
    if (token == null) {
      throw new InvalidTokenException("Token was not recognised");
    }

    if (token.isExpired()) {
      throw new InvalidTokenException("Token has expired");
    }

    OAuth2Authentication authentication = tokenServices.loadAuthentication(token.getValue());
    Map<String, Object> response = (Map<String, Object>) accessTokenConverter.convertAccessToken(token, authentication);

    response.put("active", !this.tokenBlackListService.isBlacklisted(response.get("jti").toString()));

    return response;
  }
}
