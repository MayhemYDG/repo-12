package com.keepersecurity.teamcity.secretsmanager.common

import com.keepersecurity.teamcity.secretsmanager.common.KsmTokenConstants.CLIENT_SECRET

data class TokenRequestSettings(val clientSecret: String) {
  companion object {
    fun fromMap(map: Map<String, String>) = TokenRequestSettings(
          map[CLIENT_SECRET] ?: ""
    )
  }

  fun toMap() = hashMapOf(CLIENT_SECRET to clientSecret)
}
