package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmException
import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings

class KsmTokenConnector() : TokenConnector {

  override fun requestToken(settings: TokenRequestSettings): TokenResponse {
    // if we ever need to pull credentials from external (web) service
    if (settings.clientSecret == "") {
      throw KsmException("Could not fetch KSM token")
    } else {
      return TokenResponse(accessToken = settings.clientSecret)
    }
  }
}
