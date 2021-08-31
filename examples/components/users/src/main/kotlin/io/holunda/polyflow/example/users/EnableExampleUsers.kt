package io.holunda.polyflow.example.users

import org.springframework.context.annotation.Import

@MustBeDocumented
@Import(UsersConfiguration::class)
annotation class EnableExampleUsers
