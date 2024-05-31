package com.keepersecurity.teamcity.secretsmanager.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TokenRequestSettingsTest {
  @Test
  fun symmetricData() {
    val map = hashMapOf(KsmTokenConstants.CLIENT_SECRET to "cs")
    val resultMap = TokenRequestSettings.fromMap(map).toMap()
    assertThat(resultMap).isEqualTo(map)
  }

  @Test
  fun defaultedToEmptyStrings() {
    val map = emptyMap<String, String>()
    val resultMap = TokenRequestSettings.fromMap(map).toMap()
    assertThat(resultMap).isEqualTo(hashMapOf(KsmTokenConstants.CLIENT_SECRET to ""))
  }
}
