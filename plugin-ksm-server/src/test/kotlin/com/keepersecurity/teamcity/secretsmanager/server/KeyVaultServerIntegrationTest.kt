package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.buildWith
import com.nhaarman.mockitokotlin2.mock
import jetbrains.buildServer.parameters.impl.MapParametersProviderImpl
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.RunTypesProvider
import jetbrains.buildServer.serverSide.impl.ProjectFeatureDescriptorImpl
import jetbrains.buildServer.serverSide.impl.build.steps.BuildStartContextImpl
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KeyVaultServerIntegrationTest {

  private val server = MockWebServer()

  @AfterEach
  internal fun shutdownServer() {
    server.shutdown()
  }

  @BeforeEach
  internal fun startServer() {
    server.start()
  }

  private val settings = TokenRequestSettings(
    "AAAAAAAA"
  )

  @Test
  internal fun accessTokenIsPopulatedWhenKeyVaultVariableExists() {
    val context = buildContext()
    val connector = KsmTokenConnector()
    val processor = KsmBuildStartContextProcessor(connector)

    server.enqueue(MockResponse().setBody(tokenResponse))
    processor.updateParameters(context)

    verifyTokenConnectorRequest(connector)
    verifyAccessTokenParameterPopulated(context)
  }

  private fun verifyTokenConnectorRequest(connector: KsmTokenConnector) {
    val response = connector.requestToken(settings)
    assertThat(response.accessToken).isEqualTo(accessToken)
  }

  private fun verifyAccessTokenParameterPopulated(context: BuildStartContext) {
    assertThat(
      context.sharedParameters[KsmConstants.ACCESS_TOKEN_PROPERTY]
    ).isEqualTo(accessToken)
  }

  private fun buildContext(): BuildStartContext {
    val build = buildWith(featureDescriptor(), parametersProvider())
    val runTypesProvider: RunTypesProvider = mock()
    val parameters = HashMap<String, String>()
    // Using real TeamCity objects where possible for integration test
    return BuildStartContextImpl(runTypesProvider, build, parameters)
  }

  private fun featureDescriptor(): ProjectFeatureDescriptorImpl {
    return ProjectFeatureDescriptorImpl(
      "id",
      "type",
      featureParams(),
      "someProjectId"
    )
  }

  private fun featureParams(): HashMap<String, String> {
    val params = HashMap(settings.toMap())
    params[OAuthConstants.OAUTH_TYPE_PARAM] = KsmConstants.FEATURE_TYPE
    return params
  }

  private fun parametersProvider(): MapParametersProviderImpl {
    return MapParametersProviderImpl(
      mapOf(
        "secretA" to "%keeper:UID1/pathA/title%",
        "secretB" to "%keeper:UID2/pathB/title%"
      )
    )
  }

  private val accessToken = "AAAAAAAA"

  private val tokenResponse = """
        {
          "access_token": "$accessToken"
        }
      """.trimIndent()
}
