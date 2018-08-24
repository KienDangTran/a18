package com.a18.auth.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "token_blacklist", schema = "auth")
public class TokenBlacklist {

  @Id
  private String jti;

  private String username;

  private Long expiresIn;

  private Boolean blacklisted;
}

