package com.keepersecurity.teamcity.secretsmanager.agent

import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmRef
import com.keepersecurity.teamcity.secretsmanager.common.KsmRefs
import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import java.util.stream.Stream

class KsmBuildFeature(
      dispatcher: EventDispatcher<AgentLifeCycleListener>,
      private val connector: VaultConnector)
  : AgentLifeCycleAdapter() {

  companion object {
    val LOG: Logger = Logger.getInstance(
          Loggers.AGENT_CATEGORY + "." + KsmBuildFeature::class.java.name)
  }

  init {
    dispatcher.addListener(this)
    LOG.info("Keeper Secrets Manager integration enabled")
  }

  override fun buildStarted(build: AgentRunningBuild) {
    try {
      val token = consumeToken(build)
      val refs = allReferences(build)
      val secretByRef = fetchSecrets(refs, token)
      obfuscatePasswords(secretByRef, build)
      populateBuildSecrets(secretByRef, build)
    } catch (e: Exception) {
      build.buildLogger.internalError(
            KsmConstants.FEATURE_TYPE,
            "Error processing parameters for Keeper Secrets Manager: ${e.message}",
            e
      )
      BuildProblemData.createBuildProblem(
            "KSM_${build.buildTypeId}",
            "KsmBuildFeature",
            "Error processing parameters for Keeper Secrets Manager: ${e.message}," +
                  " see teamcity-server.log for details"
      )
    }
  }

  private fun consumeToken(build: AgentRunningBuild): String? {
    val token = build.sharedConfigParameters[KsmConstants.ACCESS_TOKEN_PROPERTY]
    if (token.isNullOrBlank()) {
        build.buildLogger.message("No access token available for Keeper Secrets Manager - KSM config is required.")
      return null
    }
    build.buildLogger.message("Retrieved access token for Keeper Secrets Manager")

    // Hide token from shown properties and build logs
    build.passwordReplacer.addPassword(token)

    // Do not allow using the token directly (for now)
    build.addSharedConfigParameter(KsmConstants.ACCESS_TOKEN_PROPERTY, "********")

    return token
  }

  internal fun allReferences(build: AgentRunningBuild): Stream<KsmRef> {
    val paramValues = Stream.concat(
          build.sharedConfigParameters.values.stream(),
          build.sharedBuildParameters.allParameters.values.stream()
    )

    return KsmRefs.searchRefs(paramValues)
  }

  private fun fetchSecrets(refs: Stream<KsmRef>, token: String?): Map<KsmRef, String> {
    if (token == null) {
      LOG.warn("Could not fetch Keeper Secrets Manager access token")
      return emptyMap()
    }

    return connector.requestValues(refs, token)
  }

  private fun obfuscatePasswords(secretByRef: Map<KsmRef, String>, build: AgentRunningBuild) {
    secretByRef.values.forEach { build.passwordReplacer.addPassword(it) }
  }

  private fun populateBuildSecrets(secretsByRef: Map<KsmRef, String>, build: AgentRunningBuild) {
    val items = StringUtil.pluralize("secret", secretsByRef.size)
    build.buildLogger.message("Retrieved ${secretsByRef.size} $items from Keeper Secrets Manager")

    for ((ref, secret) in secretsByRef) {
      build.addSharedConfigParameter(ref.ref, secret)
    }
  }
}
