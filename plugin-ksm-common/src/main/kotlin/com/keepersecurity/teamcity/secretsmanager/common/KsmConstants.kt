package com.keepersecurity.teamcity.secretsmanager.common

object KsmConstants {
  /** Indicates should provide the KSM access token itself to running builds. */
  val PROVIDE_TOKEN_PROPERTY = "teamcity.ksm.allow_token"

  /** Property to hold the access token gained from Secrets Manager application. */
  val ACCESS_TOKEN_PROPERTY = "teamcity.ksm.access_token"

  /** Users specify this special variable prefix to obtain KSM secrets.   *  For example: %keeper:<recordID>/path% */
  @JvmField
  val VAR_PREFIX = "keeper:"

  /** Allows associating the KSM TeamCity feature with the corresponding params. */
  @JvmField
  val FEATURE_TYPE = "teamcity-ksm"
}
