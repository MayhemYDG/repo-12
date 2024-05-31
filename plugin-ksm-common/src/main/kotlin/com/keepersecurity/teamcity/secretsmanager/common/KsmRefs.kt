package com.keepersecurity.teamcity.secretsmanager.common

import jetbrains.buildServer.parameters.ReferencesResolverUtil
import java.util.stream.Stream

object KsmRefs {
  private val prefix = "%${KsmConstants.VAR_PREFIX}"

  fun containsRef(map: Map<String, String>): Boolean {
    return map.values.any { it.contains(prefix) }
  }

  fun searchRefs(paramValues: Stream<String>): Stream<KsmRef> {
    val prefixes = arrayOf(KsmConstants.VAR_PREFIX)

    return paramValues
          .filter { value -> value.contains(KsmConstants.VAR_PREFIX) }
          .flatMap { value -> references(value, prefixes) }
          .distinct()
          .sorted()
          .map(::KsmRef)
  }

  private fun references(value: String, prefixes: Array<String>) =
      ReferencesResolverUtil.getReferences(value, prefixes, true).stream()
}
