package com.keepersecurity.teamcity.secretsmanager.common

import jetbrains.buildServer.agent.Constants

/** Used to request KSM tokens. */
object KsmTokenConstants {
  // Parameter keys from KsmTokenJspKeys
  @JvmField val CLIENT_SECRET = Constants.SECURE_PROPERTY_PREFIX + "client-secret"
}
