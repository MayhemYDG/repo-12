package com.keepersecurity.teamcity.secretsmanager.agent

import com.keepersecurity.teamcity.secretsmanager.common.KsmException
import com.keepersecurity.teamcity.secretsmanager.common.KsmRef
import com.keepersecurity.secretsManager.core.*
import java.util.stream.Stream
import kotlinx.serialization.ExperimentalSerializationApi
import java.util.stream.Collectors

class KsmVaultConnector() : VaultConnector {
    @OptIn(ExperimentalSerializationApi::class)
    private fun createKsmOptions(config: String): SecretsManagerOptions {
        try {
            val storage = InMemoryStorage(config)
            return SecretsManagerOptions(storage)
        } catch (e: Exception) {
            throw KsmException("""Could not initialize storage: ${e.message}""")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun requestValue(ref: KsmRef, accessToken: String): SecretResponse {
        if (accessToken.trim().isEmpty()) {
            KsmBuildFeature.LOG.warn("Could not fetch Keeper Secrets Manager access token.")
            return SecretResponse(value = "")
        }
        val notation = "keeper://${ref.keeperNotation}"
        try {
            val uidFilter = listOf(ref.recordId)
            val options = createKsmOptions(accessToken)
            val secrets = getSecrets(options, uidFilter)
            val secret = getValue(secrets, notation)
            // NB! Empty value is valid
            //if (secret == "") throw KsmException("""Could not fetch secret $notation""")
            return SecretResponse(value = secret)
        } catch (e: Exception) {
            throw KsmException("""Could not fetch secret $notation Error:  ${e.message}""")
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun requestValues(refs: Stream<KsmRef>, accessToken: String): Map<KsmRef, String> {
        if (accessToken.trim().isEmpty()) {
            KsmBuildFeature.LOG.warn("Could not fetch Keeper Secrets Manager access token")
            return emptyMap()
        }

        try {
            val result = refs.collect(Collectors.toMap({ it },{ "" }))

            val rids: Set<String> = result.map{ it.key.recordId }.toSet()
            val hasTitles = rids.any { !it.matches(Regex("""^[A-Za-z0-9_-]{22}$""")) }
            var uidFilter: List<String> = if (hasTitles) emptyList() else rids.toList()

            val options = createKsmOptions(accessToken)
            var secrets = getSecrets(options, uidFilter)

            // there's a slight chance a valid title to match a recordUID (22 url-safe base64 chars)
            // or a missing record or record not shared to the KSM App - we need to pull all records
            if (uidFilter.isNotEmpty() && secrets.records.size < rids.size) {
                KsmBuildFeature.LOG.warn("KSM Didn't get expected num records - requesting all (search by title or missing UID /not shared to the app/)")
                uidFilter = emptyList()
                secrets = getSecrets(options, uidFilter)
            }

            result.onEach {
                val notation = "keeper://${it.key.keeperNotation}"
                try {
                    val value = getValue(secrets, notation)
                    if (value.isNotEmpty())
                        result[it.key] = value
                    // NB! Empty value is valid
                    //if (value == "") throw KsmException("""Could not fetch secret $notation""")
                } catch (e:Exception) {
                    KsmBuildFeature.LOG.error("""Error retrieving secret '${notation}' - Message: ${e.message}""")
                }
            }

            return result
        } catch (e: Exception) {
            throw KsmException("""Could not fetch secrets. Error:  ${e.message}""")
        }
    }
}
