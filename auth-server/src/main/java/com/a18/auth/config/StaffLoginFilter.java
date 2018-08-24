package com.a18.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.a18.auth.controller.StaffController;
import com.a18.common.constant.Privilege;
import com.a18.common.exception.ApiError;
import com.a18.common.util.StringUtil;
import java.io.IOException;
import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.web.bind.MissingServletRequestParameterException;

public class StaffLoginFilter extends AbstractAuthenticationProcessingFilter {

  StaffLoginFilter(AuthenticationManager authManager, String defaultFilterProcessesUrl) {
    super(defaultFilterProcessesUrl);
    super.setAuthenticationManager(authManager);
    super.setContinueChainBeforeSuccessfulAuthentication(true);
    super.setAuthenticationSuccessHandler((request, response, authentication) -> {
      // no-op - just allow filter chain to continue to token endpoint
    });
  }

  @Override public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) req;
    if (StaffController.PATH_STAFF_LOGIN.equalsIgnoreCase(request.getServletPath())) {
      super.doFilter(req, res, chain);
    } else {
      chain.doFilter(req, res);
    }
  }

  @Override public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    if (StaffController.PATH_STAFF_LOGIN.equalsIgnoreCase(request.getServletPath())
        && HttpMethod.POST.name().equals(request.getMethod())) {
      try {
        String username = request.getParameter(OwnerPasswordTokenGranter.USERNAME_PARAM_NAME);
        String password = request.getParameter(OwnerPasswordTokenGranter.PASSWORD_PARAM_NAME);

        if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
          throw new MissingServletRequestParameterException(
              OwnerPasswordTokenGranter.USERNAME_PARAM_NAME + " / " + OwnerPasswordTokenGranter.PASSWORD_PARAM_NAME,
              String.class.getSimpleName()
          );
        }

        StaffAuthenticationToken token;
        token = new StaffAuthenticationToken(
            username,
            password,
            List.of(new SimpleGrantedAuthority(Privilege.read(Privilege.STAFF)))
        );
        SecurityContextHolder.getContext().setAuthentication(token);

        return getAuthenticationManager().authenticate(token);
      } catch (UsernameNotFoundException | InternalAuthenticationServiceException | MissingServletRequestParameterException e) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        ApiError error = new ApiError(HttpStatus.FORBIDDEN, e.getLocalizedMessage());
        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        response.getWriter().append(objectMapper.writeValueAsString(error));
      }
    }

    SecurityContextHolder.clearContext();
    return null;
  }
}
