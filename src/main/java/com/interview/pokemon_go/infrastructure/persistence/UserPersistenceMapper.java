package com.interview.pokemon_go.infrastructure.persistence;

import com.interview.pokemon_go.domain.model.UserAccount;

final class UserPersistenceMapper {

    private UserPersistenceMapper() {
    }

    static UserEntity toEntity(UserAccount account) {
        return new UserEntity(account.username(), account.passwordHash());
    }

    static UserAccount toDomain(UserEntity entity) {
        return new UserAccount(entity.getUsername(), entity.getPasswordHash());
    }
}
