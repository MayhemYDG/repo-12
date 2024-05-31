package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings

interface TokenConnector {
  fun requestToken(settings: TokenRequestSettings): TokenResponse
}
