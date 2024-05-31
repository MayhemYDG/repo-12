package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmTokenConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.oauth.OAuthConstants

object BuildContexts {

  internal fun featureDescriptor(featureParams: Map<String, String>): SProjectFeatureDescriptor {
    return mock {
      on { parameters }.doReturn(featureParams)
    }
  }

  internal fun parametersProvider(params: Map<String, String>): ParametersProvider {
    return mock {
      on { all }.doReturn(params)
    }
  }

  internal fun featureParams(): Map<String, String> {
    return mapOf(
          OAuthConstants.OAUTH_TYPE_PARAM to KsmConstants.FEATURE_TYPE,
          KsmTokenConstants.CLIENT_SECRET to "c"
    )
  }

  internal fun featureParamsWith(pair: Pair<String, String>): Map<String, String> {
    val params = HashMap(featureParams())
    params[pair.first] = pair.second
    return params
  }

  internal fun buildContextWithRelevantParams(): BuildStartContext {
    val params = mapOf(
          "example-key" to "some relevant %keeper:UID/secret%"
    )
    return buildContextWithParams(params)
  }

  internal fun buildContextWithParams(params: Map<String, String>): BuildStartContext {
    val descriptor = featureDescriptor(featureParams())
    val paramsProvider = parametersProvider(params)

    return buildContextWith(descriptor, paramsProvider)
  }

  internal fun buildContextWith(
        featureDescriptor: SProjectFeatureDescriptor,
        paramsProvider: ParametersProvider): BuildStartContext {

    val build = buildWith(featureDescriptor, paramsProvider)

    return buildContextWith(build)
  }

  internal fun buildContextWith(build: SRunningBuild): BuildStartContext {
    return mock {
      on { this.build }.doReturn(build)
    }
  }

  internal fun buildWith(
        featureDescriptor: SProjectFeatureDescriptor,
        paramsProvider: ParametersProvider): SRunningBuild {

    return buildWith(listOf(featureDescriptor), paramsProvider)
  }

  internal fun buildWith(
        featureDescriptors: List<SProjectFeatureDescriptor>,
        parametersProvider: ParametersProvider): SRunningBuild {

    val project: SProject = mock {
      on { getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE) }.doReturn(featureDescriptors.toList())
    }

    val buildType: SBuildType = mock {
      on { this.project }.doReturn(project)
    }

    return mock {
      on { this.parametersProvider }.doReturn(parametersProvider)
      on { this.buildType }.doReturn(buildType)
    }
  }
}
