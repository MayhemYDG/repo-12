package com.keepersecurity.teamcity.secretsmanager.common

import com.keepersecurity.teamcity.secretsmanager.common.KsmRefs.containsRef
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.stream.Stream

internal class KsmRefsTest {
  @Test
  internal fun noReferenceExists() {
    assertThat(containsRef(emptyMap())).isFalse()
    assertThat(containsRef(mapOf("key" to "value"))).isFalse()
  }

  @Test
  internal fun irrelevantReferenceAreIgnored() {
    assertThat(containsRef(mapOf("key" to "%var%"))).isFalse()
    assertThat(containsRef(mapOf("key" to "Pan %var% Gargle Blaster"))).isFalse()
    assertThat(containsRef(mapOf("key" to "Never lose your %key:object%"))).isFalse()
  }

  @Test
  internal fun ksmReferenceExists() {
    assertThat(containsRef(mapOf("key" to "%keeper:%"))).isTrue()
    assertThat(containsRef(mapOf("key" to "The answer is a %keeper:number%..."))).isTrue()
  }

  @Test
  internal fun searchExtractsReferences() {
    val paramValues: Stream<String> = Stream.of(
      "a) %keeper:UID1/title%, b) %keeper:UID1/notes% c) %keeper:UID2/title% and that's all"
    )

    val refs = KsmRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
      KsmRef("keeper:UID1/notes"),
      KsmRef("keeper:UID1/title"),
      KsmRef("keeper:UID2/title")
    )
  }

  @Test
  internal fun searchRemovesDuplicates() {
    val paramValues = Stream.of(
      "a) %keeper:UID1/notes% "+
      "b) %keeper:UID1/notes% "+
      "c) %keeper:UID2/title% "+
      "d) %keeper:UID2/title%"
    )

    val refs = KsmRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
      KsmRef("keeper:UID1/notes"),
      KsmRef("keeper:UID2/title")
    )
  }

  @Test
  internal fun searchSortsItemsForPossiblyMoreEfficientQueries() {
    val paramValues = Stream.of(
      "a) %keeper:UID1/title% "+
      "c) %keeper:UID2/title% "+
      "b) %keeper:UID1/notes%"
    )

    val refs = KsmRefs.searchRefs(paramValues)

    assertThat(refs).containsExactly(
      KsmRef("keeper:UID1/notes"),
      KsmRef("keeper:UID1/title"),
      KsmRef("keeper:UID2/title")
    )
  }
}
