package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmTokenConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.oauth.OAuthConnectionDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthProvider
import jetbrains.buildServer.web.openapi.PluginDescriptor

class KsmConnectionProvider(private val descriptor: PluginDescriptor) : OAuthProvider() {

  override fun getType(): String = KsmConstants.FEATURE_TYPE

  override fun getDisplayName(): String = "Keeper Vault"

  override fun describeConnection(connection: OAuthConnectionDescriptor): String {
    return "Connection to Keeper Vault server"
  }

  override fun getDefaultProperties(): Map<String, String> {
    return emptyMap()
  }

  override fun getEditParametersUrl(): String {
    return descriptor.getPluginResourcesPath("editConnectionKeyVault.jsp")
  }

  override fun getPropertiesProcessor(): PropertiesProcessor {
    return KeyVaultPropertiesProcessor()
  }

  class KeyVaultPropertiesProcessor : PropertiesProcessor {

    override fun process(properties: MutableMap<String, String>): MutableCollection<InvalidProperty> {
      val keys = listOf(KsmTokenConstants.CLIENT_SECRET)

      val errors = keys.mapNotNull {
        if (properties[it].isNullOrBlank()) InvalidProperty(it, "Should not be empty")
        else null
      }

      val settings = TokenRequestSettings.fromMap(properties)
      properties.putAll(settings.toMap())

      return errors.toMutableList()
    }
  }
}
