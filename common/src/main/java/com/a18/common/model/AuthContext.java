package com.a18.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthContext {
  public Long id;

  public String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  public String password;
}
