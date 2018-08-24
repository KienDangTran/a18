package com.a18.auth.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.a18.common.model.BaseEntity;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.userdetails.UserDetails;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"password"})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@MappedSuperclass
public abstract class AbstractUserDetails extends BaseEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private String username;

  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  @Column(updatable = false, nullable = false)
  private String password; // TODO: use Character[] instead

  @Column(nullable = false)
  private String fullname;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false, unique = true)
  private String phone;

  private LocalDate dateOfBirth;

  private Boolean enabled = true;

  private Boolean accountNonLocked = true;

  private Boolean accountNonExpired = true;

  private Boolean credentialsNonExpired = true;

  @Override public String getPassword() {
    return this.password;
  }

  @Override public String getUsername() {
    return this.username;
  }

  @Override public boolean isAccountNonExpired() {
    return this.accountNonExpired;
  }

  @Override public boolean isAccountNonLocked() {
    return this.accountNonLocked;
  }

  @Override public boolean isCredentialsNonExpired() {
    return this.credentialsNonExpired;
  }

  @Override public boolean isEnabled() {
    return this.enabled;
  }
}
