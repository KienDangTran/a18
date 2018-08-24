package com.a18.auth.config;

import com.a18.auth.controller.StaffController;
import com.a18.auth.controller.UserController;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableResourceServer
public class ResourceServerConfig extends ResourceServerConfigurerAdapter {

  private final DefaultTokenServices tokenServices;

  @Value("${security.oauth2.client.client-id}")
  private String clientId;

  @Value("${security.oauth2.client.client-secret}")
  private String secret;

  @Value("${security.oauth2.client.authentication-scheme}")
  private AuthenticationScheme authenticationScheme;

  @Value("${security.oauth2.client.grant-type}")
  private String grantType;

  @Value("${security.oauth2.client.scope}")
  private List<String> scope;

  @Value("${security.oauth2.client.token-name}")
  private String tokenName;

  @Value("${security.oauth2.resource.id}")
  private String resourceId;

  private final AuthenticationManager authenticationManager;

  private final ClientDetailsService clientDetails;

  @Autowired public ResourceServerConfig(
      DefaultTokenServices tokenServices,
      AuthenticationManager authenticationManager,
      ClientDetailsService clientDetails
  ) {
    this.tokenServices = tokenServices;
    this.authenticationManager = authenticationManager;
    this.clientDetails = clientDetails;
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer configurer) {
    configurer
        .resourceId(this.resourceId)
        .tokenServices(this.tokenServices);
  }

  @Override
  public void configure(HttpSecurity http) throws Exception {
    http
        .cors().disable()
        .csrf().disable()
        .formLogin().disable()
        .httpBasic().disable()
        .authorizeRequests()
        .antMatchers(UserController.PATH_REGISTER).permitAll()
        .antMatchers(UserController.PATH_USER_LOGIN).permitAll()
        .antMatchers(StaffController.PATH_STAFF_LOGIN).permitAll()
        .anyRequest().fullyAuthenticated()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
        .accessDeniedHandler((request, response, authException) ->
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED));

    http.addFilterAfter(
        new StaffLoginFilter(this.authenticationManager, StaffController.PATH_STAFF + "/**"),
        BasicAuthenticationFilter.class
    );
  }

  @Bean
  OwnerPasswordTokenGranter ownerPasswordTokenGranter() {
    return new OwnerPasswordTokenGranter(
        this.tokenServices,
        this.clientDetails,
        new DefaultOAuth2RequestFactory(clientDetails),
        this.authenticationManager,
        this.resourceDetails()
    );
  }

  @Bean
  ResourceOwnerPasswordResourceDetails resourceDetails() {
    ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
    resource.setClientAuthenticationScheme(this.authenticationScheme);
    resource.setClientId(this.clientId);
    resource.setClientSecret(this.secret);
    resource.setGrantType(this.grantType);
    resource.setScope(this.scope);
    resource.setTokenName(this.tokenName);
    return resource;
  }
}
