package com.a18.auth.model.repository;

import com.a18.auth.model.AbstractUserDetails;
import java.io.Serializable;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface UserDetailsRepository<T extends AbstractUserDetails, ID extends Serializable>
    extends JpaRepository<T, ID> {

  long countAllByUsername(String username);

  long countAllByEmail(String email);

  long countAllByPhone(String phone);

  Optional<T> findDistinctFirstByUsername(String username);

  @Query("UPDATE #{#entityName} u SET u.password = ?1 WHERE u.id = ?2")
  @Modifying
  void changePassword(String encryptedPassword, ID id);

  @Query("SELECT u.password FROM #{#entityName} u WHERE u.id = ?1")
  String getPassword(ID id);
}
