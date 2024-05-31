package com.keepersecurity.teamcity.secretsmanager.common

import java.lang.RuntimeException

class KsmException(message: String, cause: Throwable?): RuntimeException(message, cause) {
  constructor(message: String): this(message, null)
}
