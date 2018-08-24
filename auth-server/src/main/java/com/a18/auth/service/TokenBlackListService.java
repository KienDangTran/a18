package com.a18.auth.service;

import com.a18.auth.model.repository.TokenBlackListRepository;
import com.a18.auth.model.TokenBlacklist;
import java.util.Date;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class TokenBlackListService {

  @Autowired TokenBlackListRepository tokenBlackListRepo;

  public Boolean isBlacklisted(String jti) {
    Optional<TokenBlacklist> token = this.tokenBlackListRepo.findByJti(jti);
    if (token.isPresent()) {
      return token.get().getBlacklisted();
    } else {
      throw new EntityNotFoundException(String.format("Token with jti[%s] not found.", jti));
    }
  }

  @Async
  public void addToEnabledList(@NotNull String username, @NotNull String jti, @NotNull Long expired) {
    // clean all black listed tokens for user
    this.tokenBlackListRepo.findAllByUsernameAndBlacklistedTrue(username).forEach(token -> {
      token.setBlacklisted(true);
      tokenBlackListRepo.save(token);
    });

    // Add new token white listed
    TokenBlacklist tokenBlacklist = new TokenBlacklist();
    tokenBlacklist.setJti(jti);
    tokenBlacklist.setUsername(username);
    tokenBlacklist.setExpiresIn(expired);
    tokenBlacklist.setBlacklisted(false);
    this.tokenBlackListRepo.save(tokenBlacklist);

    //delete all expired token
    this.tokenBlackListRepo.deleteAllByUsernameAndExpiresInBefore(username, new Date().getTime());
  }

  @Async
  public void addToBlackList(@NotNull String jti) {
    Optional<TokenBlacklist> tokenBlackList = tokenBlackListRepo.findByJti(jti);
    if (tokenBlackList.isPresent()) {
      tokenBlackList.get().setBlacklisted(true);
      this.tokenBlackListRepo.save(tokenBlackList.get());
    } else {
      throw new EntityNotFoundException(String.format("Token with jti[%s] not found.", jti));
    }
  }

  public void addAllToBlacklistByUsername(@NotNull String username) {
    this.tokenBlackListRepo.findAllByUsername(username).forEach(token -> {
      token.setBlacklisted(true);
      this.tokenBlackListRepo.save(token);
    });
  }
}
