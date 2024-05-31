package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmTokenConstants
import com.nhaarman.mockitokotlin2.mock
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KsmConnectionProviderTest {

  @Test
  internal fun populateEmptyValuesWhenMissing() {
    val processor = propertiesProcessor()
    val mutableMap = HashMap<String, String>()

    processor.process(mutableMap)

    assertThat(mutableMap)
      .containsOnlyKeys(
        KsmTokenConstants.CLIENT_SECRET
      )
      .containsValues("")
  }

  @Test
  internal fun populateExistingValues() {
    val processor = propertiesProcessor()
    val mutableMap = hashMapOf(
      KsmTokenConstants.CLIENT_SECRET to "cs"
    )
    val originalMap = HashMap(mutableMap)

    processor.process(mutableMap)

    assertThat(mutableMap).isEqualTo(originalMap)
  }

  private fun propertiesProcessor(): PropertiesProcessor {
    val descriptor: PluginDescriptor = mock()
    val provider = KsmConnectionProvider(descriptor)
    return provider.propertiesProcessor
  }
}
