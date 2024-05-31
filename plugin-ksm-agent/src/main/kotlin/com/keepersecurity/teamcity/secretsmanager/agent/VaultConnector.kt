package com.keepersecurity.teamcity.secretsmanager.agent

import com.keepersecurity.teamcity.secretsmanager.common.KsmRef
import java.util.stream.Stream

interface VaultConnector {
  fun requestValue(ref: KsmRef, accessToken: String): SecretResponse
  fun requestValues(refs: Stream<KsmRef>, accessToken: String): Map<KsmRef, String>
}
