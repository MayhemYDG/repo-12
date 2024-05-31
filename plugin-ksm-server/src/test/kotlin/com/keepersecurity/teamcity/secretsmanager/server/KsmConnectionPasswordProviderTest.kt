package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmTokenConstants
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.buildWith
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.featureDescriptor
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.featureParamsWith
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.parametersProvider
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class KsmConnectionPasswordProviderTest {

  @Test
  fun connectionSecretIsAddedToPasswordProvider() {
    val secret = "service-principal-password"
    val build = buildWithSecret(secret)
    val passwordProvider = KsmConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results)
          .hasOnlyOneElementSatisfying{
            it.name == KsmTokenConstants.CLIENT_SECRET && it.value == secret
          }
  }

  @Test
  fun connectionSecretIsNotAddedToPasswordProviderWhenEmpty() {
    val secret = "  "
    val build = buildWithSecret(secret)
    val passwordProvider = KsmConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results).isEmpty()
  }

  @Test
  fun buildIsIgnoredWhenProviderNotConfigured() {
    val build = buildWith(featureDescriptor(mapOf()), parametersProvider(mapOf()))
    val passwordProvider = KsmConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results).isEmpty()
  }

  @Test
  fun connectionSecretIsAddedToPasswordProviderWhenMultipleFeatureDescriptors() {
    val secret = "service-principal-password"
    val descriptors = listOf(
          featureDescriptor(mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "other type")),
          descriptorWithSecret(secret)
    )
    val build = buildWith(descriptors, parametersProvider(mapOf()))
    val passwordProvider = KsmConnectionPasswordProvider()

    val results = passwordProvider.getPasswordParameters(build)

    assertThat(results)
          .hasOnlyOneElementSatisfying {
            it.name == KsmTokenConstants.CLIENT_SECRET && it.value == secret
          }
  }

  private fun buildWithSecret(secret: String): SBuild {
    return buildWith(descriptorWithSecret(secret), parametersProvider(mapOf()))
  }

  private fun descriptorWithSecret(secret: String): SProjectFeatureDescriptor {
    return featureDescriptor(
          featureParamsWith(KsmTokenConstants.CLIENT_SECRET to secret))
  }
}
