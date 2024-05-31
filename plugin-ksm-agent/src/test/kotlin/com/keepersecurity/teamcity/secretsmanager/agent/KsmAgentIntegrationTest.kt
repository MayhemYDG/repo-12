package com.keepersecurity.teamcity.secretsmanager.agent

import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmRef
import com.nhaarman.mockitokotlin2.*
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildParametersMap
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import org.junit.jupiter.api.Test

class KsmAgentIntegrationTest {
  @Test
  internal fun secretsAreAddedAsBuildParameters() {
    val connector: VaultConnector = mock {
      on { it.requestValues(any(), any()) }.doReturn(
        mapOf(
          KsmRef("keeper:UID1/pathA") to "secretA",
          KsmRef("keeper:UID2/pathB") to "secretB")
        )
      }

    val buildRef = mapOf("secretA" to "%keeper:UID1/pathA%")
    val configRef = mapOf("secretB" to "%keeper:UID2/pathB%")
    val build = build(buildParams = buildRef, configParams = configRef)
    val feature = buildFeature(connector)
    feature.buildStarted(build)

    verify(build).addSharedConfigParameter("teamcity.ksm.access_token", "********")
    verify(build).addSharedConfigParameter("keeper:UID1/pathA", "secretA")
    verify(build).addSharedConfigParameter("keeper:UID2/pathB", "secretB")
  }

  private fun build(
        configParams: Map<String, String> = emptyMap(),
        buildParams: Map<String, String> = emptyMap()
  ): AgentRunningBuild {

    val configAndTokenParams = configParams
          .plus(KsmConstants.ACCESS_TOKEN_PROPERTY to "access-token")

    val paramMap: BuildParametersMap = mock {
      on { allParameters }.doReturn(buildParams)
    }

    val passwordReplacer: PasswordReplacer = mock()
    val buildLogger: BuildProgressLogger = mock()

    // Unfortunately there doesn't seem to be a TeamCity agent build implementation class we can use
    return mock {
      on { it.sharedBuildParameters }.doReturn(paramMap)
      on { it.sharedConfigParameters }.doReturn<Map<String, String>>(configAndTokenParams)
      on { it.passwordReplacer }.doReturn(passwordReplacer)
      on { it.buildLogger }.doReturn(buildLogger)
    }
  }

  private fun buildFeature(connector: VaultConnector): KsmBuildFeature {
    val dispatcher: EventDispatcher<AgentLifeCycleListener> = mock()
    return KsmBuildFeature(dispatcher, connector)
  }
}
