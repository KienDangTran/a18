package com.a18.auth.model.repository;

import com.a18.auth.model.TokenBlacklist;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

@RestResource(exported = false)
public interface TokenBlackListRepository extends JpaRepository<TokenBlacklist, String> {
  Optional<TokenBlacklist> findByJti(String jti);

  Set<TokenBlacklist> findAllByUsername(@NotNull String username);

  Set<TokenBlacklist> findAllByUsernameAndBlacklistedTrue(@NotNull String username);

  void deleteAllByUsernameAndExpiresInBefore(@NotNull String username, Long expiresIn);
}
