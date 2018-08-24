package com.a18.auth.config;

import com.a18.auth.service.TokenBlackListService;
import com.a18.common.constant.GrantType;
import com.a18.common.constant.PreDefinedRole;
import com.a18.common.util.StringUtil;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

/**
 * responsible for generating tokens specific to a client
 * <pre>
 * grant types:
 *  - "authorization_code", // used by web server apps(server-to-server communication)
 *  - "client_credentials", // used by the client themselves to get an access token
 *  - "refresh_token",
 *  - "password", // used with trusted Applications, such as those owned by the service itself
 *  - "implicit" // used in browser based application (that run on the user's device)
 * </pre>
 */
@Configuration
@EnableAuthorizationServer
public class OAuth2ServerConfig extends AuthorizationServerConfigurerAdapter {

  @Value("${jwt.certificate.store.file}")
  private Resource keystore;

  @Value("${jwt.certificate.store.password}")
  private String keystorePassword;

  @Value("${jwt.certificate.key.alias}")
  private String keyAlias;

  @Value("${jwt.certificate.key.password}")
  private String keyPassword;

  @Value("${security.oauth2.resource.id}")
  private String resourceId;

  @Value("${check-user-scopes}")
  private Boolean checkUserScopes;

  @Value("${secret}")
  private String secret;

  @Value("${security.oauth2.client.access-token-validity-seconds:6000}")
  private int accessTokenValiditySeconds;

  @Value("${security.oauth2.client.refresh-token-validity-seconds:86400}")
  private int refreshTokenValiditySeconds;

  @Autowired private AuthenticationManager authenticationManager;

  @Autowired private TokenBlackListService blackListService;

  @Bean
  public TokenEnhancer tokenEnhancer() {
    return new CustomTokenEnhancer();
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
    tokenEnhancerChain.setTokenEnhancers(Arrays.asList(this.tokenEnhancer(), accessTokenConverter()));
    endpoints
        .authenticationManager(this.authenticationManager)
        .tokenServices(this.tokenServices())
        .tokenStore(this.tokenStore())
        .tokenEnhancer(tokenEnhancerChain)
        .accessTokenConverter(this.accessTokenConverter());
  }

  @Override
  public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
    oauthServer
        // we're allowing access to the token only for clients with 'ROLE_TRUSTED_CLIENT' authority
        .tokenKeyAccess("hasAuthority('" + PreDefinedRole.ROLE_TRUSTED_CLIENT + "')")
        .checkTokenAccess("hasAuthority('" + PreDefinedRole.ROLE_TRUSTED_CLIENT + "')");
  }

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients
        .inMemory()

        .withClient("trusted-client")
        .authorizedGrantTypes(
            GrantType.AUTHORIZATION_CODE,
            GrantType.PASSWORD,
            GrantType.REFRESH_TOKEN
        )
        .scopes(PreDefinedRole.ROLE_TRUSTED_CLIENT)
        .authorities(PreDefinedRole.ROLE_TRUSTED_CLIENT)
        .resourceIds(resourceId)
        .secret(this.secret)
        .autoApprove(true)
        .accessTokenValiditySeconds(accessTokenValiditySeconds)
        .refreshTokenValiditySeconds(refreshTokenValiditySeconds)

        .and()
        .withClient("general-client")
        .authorizedGrantTypes(GrantType.IMPLICIT)
        .scopes(PreDefinedRole.ROLE_CLIENT)
        .authorities(PreDefinedRole.ROLE_CLIENT)
        .resourceIds(this.resourceId)
        .autoApprove(true)
        .accessTokenValiditySeconds(accessTokenValiditySeconds)
        .refreshTokenValiditySeconds(refreshTokenValiditySeconds);
  }

  @Bean
  public TokenStore tokenStore() {
    return new JwtTokenStore(this.accessTokenConverter());
  }

  @Bean
  public JwtAccessTokenConverter accessTokenConverter() {
    JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
    KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(this.keystore, this.keystorePassword.toCharArray());
    KeyPair keyPair = keyStoreKeyFactory.getKeyPair(this.keyAlias, this.keyPassword.toCharArray());
    converter.setKeyPair(keyPair);

    return converter;
  }

  @Bean
  @Primary
  public DefaultTokenServices tokenServices() {
    CustomerTokenService tokenService = new CustomerTokenService();
    tokenService.setTokenStore(this.tokenStore());
    tokenService.setSupportRefreshToken(true);
    tokenService.setTokenEnhancer(this.accessTokenConverter());
    return tokenService;
  }

  class CustomerTokenService extends DefaultTokenServices {

    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) {
      DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(super.createAccessToken(authentication));
      UserDetails user = (UserDetails) authentication.getPrincipal();
      token.setScope(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));

      String jti = (String) token.getAdditionalInformation().get("jti");
      blackListService.addToEnabledList(user.getUsername(), jti, token.getExpiration().getTime());

      return token;
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest)
        throws AuthenticationException {
      String jti = tokenRequest.getRequestParameters().get("jti");
      if (StringUtil.isBlank(jti) || blackListService.isBlacklisted(jti)) return null;
      blackListService.addToBlackList(jti);

      return super.refreshAccessToken(refreshTokenValue, tokenRequest);
    }

    @Override public OAuth2Authentication loadAuthentication(String accessTokenValue)
        throws AuthenticationException, InvalidTokenException {
      OAuth2AccessToken token = this.readAccessToken(accessTokenValue);

      String jti = (String) token.getAdditionalInformation().get("jti");
      if (StringUtil.isBlank(jti) || blackListService.isBlacklisted(jti)) {
        throw new InvalidTokenException("Token was blacklisted");
      }

      return super.loadAuthentication(accessTokenValue);
    }
  }
}
