package com.a18.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.a18.common.constant.PreDefinedRole;
import java.util.Collection;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "user", schema = "auth")
public class User extends AbstractUserDetails {

  @JsonIgnore
  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    return Set.of(new SimpleGrantedAuthority(PreDefinedRole.ROLE_USER));
  }
}
