package io.holunda.polyflow.view.jpa.auth

import org.springframework.data.repository.CrudRepository

interface AuthorizationPrincipalRepository : CrudRepository<AuthorizationPrincipal, AuthorizationPrincipalId>
