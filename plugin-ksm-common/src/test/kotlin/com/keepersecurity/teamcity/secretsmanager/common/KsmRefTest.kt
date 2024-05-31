package com.keepersecurity.teamcity.secretsmanager.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class KsmRefTest {

  @Test
  internal fun originalReferenceAvailable() {
    val ref = "keeper:UID/type"
    assertThat(KsmRef(ref).ref).isEqualTo(ref)
  }

  @Test
  internal fun recordId() {
    val ref = KsmRef("keeper:UID/type")
    assertThat(ref.recordId).isEqualTo("UID")
  }

  @Test
  internal fun keeperNotation() {
    val ref = KsmRef("keeper:UID/type")
    assertThat(ref.keeperNotation).isEqualTo("UID/type")
  }

  @Test
  internal fun validity() {
    assertThat(KsmRef("keeper:a/type").valid()).isTrue()
    assertThat(KsmRef("keeper=a/b").valid()).isFalse()
    assertThat(KsmRef("keeper:a-b").valid()).isFalse()
    assertThat(KsmRef("keeper/a:b").valid()).isFalse()
  }
}
