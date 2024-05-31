package com.keepersecurity.teamcity.secretsmanager.agent

import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmException
import com.keepersecurity.teamcity.secretsmanager.common.KsmRef
import com.nhaarman.mockitokotlin2.*
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.PasswordReplacer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.stream.Stream

internal class  KsmBuildFeatureTest {
  private val accessToken = "access-token-123"

  @Test
  internal fun alwaysActivatePlugin() {
    val dispatcher: EventDispatcher<AgentLifeCycleListener> = mock()
    val feature = KsmBuildFeature(dispatcher, connector())

    verify(dispatcher).addListener(feature)
  }

  @Test
  internal fun startingBuildBlanksOutToken() {
    val feature = buildFeature()
    val build = build()

    feature.buildStarted(build)
    verify(build).addSharedConfigParameter(
      KsmConstants.ACCESS_TOKEN_PROPERTY, "********"
    )
  }

  @Test
  internal fun tokenShouldBeObfuscated() {
    val feature = buildFeature()
    val passwordReplacer: PasswordReplacer = mock()
    val build = build()
    whenever(build.passwordReplacer).thenReturn(passwordReplacer)

    feature.buildStarted(build)
    verify(build.passwordReplacer).addPassword(accessToken)
  }

  @Test
  internal fun allReferencesPresent() {
    val build = build(
      mapOf(
        "config1" to "%keeper:UID1/title%",
        "config2" to "%keeper:UID2/title%"
      ),
      mapOf(
        "build1" to "%keeper:UID1/notes%",
        "build2" to "%keeper:UID2/notes%"
      )
    )

    val refs: Stream<KsmRef> = buildFeature().allReferences(build)
    assertThat(refs).containsOnly(
      KsmRef("keeper:UID1/title"),
      KsmRef("keeper:UID2/title"),
      KsmRef("keeper:UID1/notes"),
      KsmRef("keeper:UID2/notes")
    )
  }

  @Test
  internal fun secretsAreFetchedFromConnector() {
    val secretRefs = mapOf(
      KsmRef("keeper:UID1/title") to "build",
      KsmRef("keeper:UID2/title") to "config"
    )

    val connector: VaultConnector = mock {
      on { it.requestValues(any(), any()) }.doReturn(secretRefs)
      on { it.requestValue(any(), any()) }.doReturn(SecretResponse("build"))
    }

    val buildRefs = mapOf("build" to "%keeper:UID1/pathA%")
    val configRefs = mapOf("config" to "%keeper:UID2/pathB%")
    val build = build(buildParams = buildRefs, configParams = configRefs)

    buildFeature(connector).buildStarted(build)
    verify(connector).requestValues(any(), eq(accessToken))
    assertThat(connector.requestValues(secretRefs.keys.toList().stream(), accessToken)).isEqualTo(secretRefs)
  }

  @Test
  internal fun secretsAreAddedAsPasswordsToObfuscate() {
    val secretRef = mapOf(
      KsmRef("keeper:UID1/title") to "secretA1",
      KsmRef("keeper:UID2/title") to "secretB1",
      KsmRef("keeper:UID2/notes") to "secretB2"
    )

    val connector: VaultConnector = mock {
      on { it.requestValues(any(), any()) }.doReturn(secretRef)
    }

    val configRef = mapOf(
      "usage-a" to " %keeper:UID1/title% ",
      "usage-b" to " %keeper:UID2/title% ",
      "usage-c" to " %keeper:UID2/notes% "
    )
    val passwords: PasswordReplacer = mock()
    val build = build(buildParams = configRef, passwordReplacer = passwords)

    buildFeature(connector).buildStarted(build)
    verify(passwords).addPassword("secretA1")
    verify(passwords).addPassword("secretB1")
    verify(passwords).addPassword("secretB2")
  }

  @Test
  internal fun secretsAreAddedAsBuildParameters() {
    val secretRef = mapOf(
      KsmRef("keeper:UID/type") to "secretA",
      KsmRef("keeper:UID/title") to "secretB",
      KsmRef("keeper:UID/notes") to "secretC"
    )
    val connector: VaultConnector = mock {
      on { it.requestValues(any(), any()) }.doReturn(secretRef)
    }
    val configRef = mapOf(
      "usage-a" to " %keeper:UID/type% ",
      "usage-b" to " %keeper:UID/title% ",
      "usage-c" to " %keeper:UID/notes% "
    )
    val build = build(buildParams = configRef)

    buildFeature(connector).buildStarted(build)

    verify(build).addSharedConfigParameter("keeper:UID/type", "secretA")
    verify(build).addSharedConfigParameter("keeper:UID/title", "secretB")
    verify(build).addSharedConfigParameter("keeper:UID/notes", "secretC")
  }

  @Test
  internal fun reportBuildErrorWhenFailedToFetchSecret() {
    val connector: VaultConnector = mock {
      on { it.requestValue(any(), any()) }.doThrow(KsmException("Something went wrong"))
      on { it.requestValues(any(), any()) }.doThrow(KsmException("Something went wrong"))
    }
    val build = build(buildParams = mapOf("param-name" to " %keeper:UID/title% "))

    buildFeature(connector).buildStarted(build)

    verify(build.buildLogger).internalError(
          eq(KsmConstants.FEATURE_TYPE),
          eq("Error processing parameters for Keeper Secrets Manager: Something went wrong"),
          any()
    )
  }

  private fun buildFeature(connector: VaultConnector = connector()): KsmBuildFeature {
    val dispatcher: EventDispatcher<AgentLifeCycleListener> = mock()
    return KsmBuildFeature(dispatcher, connector)
  }

  private fun build(
    configParams: Map<String, String> = emptyMap(),
    buildParams: Map<String, String> = emptyMap(),
    passwordReplacer: PasswordReplacer = mock()
  ): AgentRunningBuild {

    val configAndTokenParams = configParams
      .plus(KsmConstants.ACCESS_TOKEN_PROPERTY to accessToken)

    val paramMap: BuildParametersMap = mock {
      on { allParameters }.doReturn(buildParams)
    }

    val buildLogger: BuildProgressLogger = mock()

    return mock {
      on { it.sharedBuildParameters }.doReturn(paramMap)
      on { it.sharedConfigParameters }.doReturn(configAndTokenParams)
      on { it.passwordReplacer }.doReturn(passwordReplacer)
      on { it.buildLogger }.doReturn(buildLogger)
    }
  }

  private fun connector(): VaultConnector {
    return mock {
      on { it.requestValue(any(), any()) }.doReturn(SecretResponse(""))
      on { it.requestValues(any(), any()) }.doReturn(emptyMap())
    }
  }
}
