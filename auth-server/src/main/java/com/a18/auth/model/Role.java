package com.a18.auth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.a18.common.model.BaseEntity;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"roleAuthorities"})
@ToString(exclude = {"roleAuthorities"})
@Entity
@Table(name = "role", schema = "auth")
public class Role extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Short id;

  @Column(nullable = false, unique = true)
  private String code;

  private Short parentRoleId;

  @JsonIgnore
  @OneToMany(mappedBy = "role",
             fetch = FetchType.EAGER,
             cascade = CascadeType.ALL,
             orphanRemoval = true)
  private Set<RoleAuthority> roleAuthorities = Set.of();

  @Enumerated(EnumType.STRING)
  private RoleStatus status = RoleStatus.ACTIVE;

  public enum RoleStatus {
    ACTIVE, SUSPENDED
  }
}
