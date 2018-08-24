package com.a18.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.a18.common.constant.Privilege;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"roles"})
@ToString(callSuper = true, exclude = {"roles"})
@Entity
@Table(name = "staff", schema = "auth")
public class Staff extends AbstractUserDetails {

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "staff_role",
      schema = "auth",
      joinColumns = @JoinColumn(name = "staff_id", referencedColumnName = "id"),
      inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
  )
  private Set<Role> roles = new HashSet<>();

  @JsonIgnore
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    this.roles
        .stream()
        .filter(role -> Role.RoleStatus.ACTIVE.equals(role.getStatus()) && !role.getRoleAuthorities().isEmpty())
        .forEach(role ->
            role.getRoleAuthorities()
                .stream()
                .filter(ra -> Authority.AuthorityStatus.ACTIVE.equals(ra.getAuthority().getStatus()))
                .forEach(ra -> {
                  if (ra.isRead()) {
                    authorities.add(
                        new SimpleGrantedAuthority(Privilege.READ + ra.getAuthority().getCode())
                    );
                  }
                  if (ra.isWrite()) {
                    authorities.add(
                        new SimpleGrantedAuthority(Privilege.WRITE + ra.getAuthority().getCode())
                    );
                  }
                  if (ra.isExecute()) {
                    authorities.add(
                        new SimpleGrantedAuthority(Privilege.EXEC + ra.getAuthority().getCode())
                    );
                  }
                })

        );
    return authorities;
  }
}
