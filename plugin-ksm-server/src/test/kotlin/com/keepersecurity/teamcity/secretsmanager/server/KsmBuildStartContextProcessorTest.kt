package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants.ACCESS_TOKEN_PROPERTY
import com.keepersecurity.teamcity.secretsmanager.common.KsmException
import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.buildContextWith
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.buildContextWithParams
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.buildContextWithRelevantParams
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.buildWith
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.featureDescriptor
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.featureParams
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.featureParamsWith
import com.keepersecurity.teamcity.secretsmanager.server.BuildContexts.parametersProvider
import com.nhaarman.mockitokotlin2.*
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.junit.jupiter.api.Test

internal class KsmBuildStartContextProcessorTest {

  @Test
  fun tokenNotRequestedBuildDoesNotHaveKeyVaultFeature() {
    val context = buildContextWithIrrelevantOAuthFeature()
    val connector: TokenConnector = mock()
    val processor = KsmBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenNotRequestedWhenBuildDoesNotHaveRelevantParameters() {
    val params = mapOf(
      "key" to "some irrelevant %other:secret% message"
    )
    val context = buildContextWithParams(params)
    val connector: TokenConnector = mock()
    val processor = KsmBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenRequestedWhenBuildFeatureAndParametersExist() {
    val params = mapOf(
      "key" to "some relevant %keeper:UID1/title% message"
    )
    val context = buildContextWithParams(params)
    val connector = tokenConnectorWithResponse("param-secret-token")
    val processor = KsmBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(TokenRequestSettings.fromMap(featureParams()))
    verify(context).addSharedParameter(ACCESS_TOKEN_PROPERTY, "param-secret-token")
  }

  @Test
  fun tokenRequestedWhenMultipleFeatureDescriptors() {
    val params = mapOf(
      "key" to "some relevant %keeper:UID1/title% message"
    )
    val descriptors = listOf(
      featureDescriptor(mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "other type")),
      featureDescriptor(featureParams())
    )
    val context = buildContextWith(buildWith(descriptors, parametersProvider(params)))
    val connector = tokenConnectorWithResponse("param-secret-token")
    val processor = KsmBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(TokenRequestSettings.fromMap(featureParams()))
    verify(context).addSharedParameter(ACCESS_TOKEN_PROPERTY, "param-secret-token")
  }

  @Test
  fun tokenRequestedWhenProvideTokenIsSpecified() {
    val featureParams = featureParamsWith(
      KsmConstants.PROVIDE_TOKEN_PROPERTY to "true"
    )

    val context = buildContextWith(
      featureDescriptor(featureParams),
      parametersProvider(emptyMap())
    ) // No key vault params

    val connector = tokenConnectorWithResponse("provide-secret-token")
    val processor = KsmBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(TokenRequestSettings.fromMap(featureParams))
    verify(context).addSharedParameter(ACCESS_TOKEN_PROPERTY, "provide-secret-token")
  }

  @Test
  internal fun reportBuildProblemWhenFailedToFetchToken() {
    val connector: TokenConnector = mock {
      on { requestToken(any()) }.doThrow(KsmException("Something went wrong"))
    }
    val processor = KsmBuildStartContextProcessor(connector)
    val context = buildContextWithRelevantParams()

    processor.updateParameters(context)

    verify(context.build).addBuildProblem(any())
  }

  private fun tokenConnectorWithResponse(accessToken: String): TokenConnector {
    return mock {
      on { requestToken(any()) }.doReturn(TokenResponse(accessToken))
    }
  }

  private fun buildContextWithIrrelevantOAuthFeature(): BuildStartContext {
    val params = mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "irrelevant")
    val descriptor = featureDescriptor(params)

    val paramsProvider: ParametersProvider = mock {
      on { all }.doReturn(emptyMap())
    }

    return buildContextWith(descriptor, paramsProvider)
  }
}
