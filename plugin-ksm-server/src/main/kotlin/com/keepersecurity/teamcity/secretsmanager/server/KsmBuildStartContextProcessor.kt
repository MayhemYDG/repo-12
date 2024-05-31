package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings
import com.keepersecurity.teamcity.secretsmanager.common.KsmRefs
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.oauth.OAuthConstants

class KsmBuildStartContextProcessor(
      private val connector: TokenConnector = KsmTokenConnector()
) : BuildStartContextProcessor  {

  companion object {
    val LOG: Logger = Logger.getInstance(
          Loggers.SERVER_CATEGORY + "." + KsmBuildStartContextProcessor::class.java.name)
  }

  init {
    LOG.info("Keeper Secrets Manager integration enabled for fetching access tokens")
  }

  override fun updateParameters(context: BuildStartContext) {
    val feature = findKeyVaultFeature(context)
    if (feature != null) {
      if (requiresAccessToken(feature, context.build)) {
        updateParametersWithToken(feature, context)
      } else {
        LOG.debug("No KSM variables found for for build ${context.build}")
      }
    } else {
      LOG.debug("No KSM feature enabled for build ${context.build}")
    }
  }

  private fun findKeyVaultFeature(context: BuildStartContext): SProjectFeatureDescriptor? {
    val project = context.build.buildType?.project

    return project
          ?.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE)
          ?.firstOrNull { isKeyVaultType(it) }
  }

  private fun isKeyVaultType(it: SProjectFeatureDescriptor) =
        it.parameters[OAuthConstants.OAUTH_TYPE_PARAM] == KsmConstants.FEATURE_TYPE

  private fun requiresAccessToken(
        feature: SProjectFeatureDescriptor, build: SRunningBuild): Boolean {

    return containsProvideTokenParam(feature) ||
          KsmRefs.containsRef(build.parametersProvider.all)
  }

  private fun containsProvideTokenParam(feature: SProjectFeatureDescriptor): Boolean {
    val override = feature.parameters[KsmConstants.PROVIDE_TOKEN_PROPERTY]
    return override?.toBoolean() ?: false
  }

  private fun updateParametersWithToken(
        feature: SProjectFeatureDescriptor, context: BuildStartContext) {

    val settings = TokenRequestSettings.fromMap(feature.parameters)

    try {
      LOG.debug("Fetch KSM token for Key Vault")
      val token = connector.requestToken(settings)

      context.addSharedParameter(
            KsmConstants.ACCESS_TOKEN_PROPERTY,
            token.accessToken)
    } catch (e: Throwable) {
      reportError("Error fetching KSM access token: ${e.message}", e, context.build)
    }
  }

  private fun reportError(message: String, e: Throwable, build: SRunningBuild) {
    LOG.warn(message, e)
    build.addBuildProblem(
          BuildProblemData.createBuildProblem(
                "KVS_${build.buildTypeId}",
                "KsmBuildStartContextProcessor",
              "$message: $e, see teamcity-server.log for details"
          ))
  }
}
