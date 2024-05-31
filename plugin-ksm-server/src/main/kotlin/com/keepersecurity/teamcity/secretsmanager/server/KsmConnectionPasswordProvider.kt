package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmTokenConstants
import com.keepersecurity.teamcity.secretsmanager.common.KsmConstants
import jetbrains.buildServer.serverSide.Parameter
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SimpleParameter
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider
import jetbrains.buildServer.util.StringUtil

class KsmConnectionPasswordProvider : PasswordsProvider {

    override fun getPasswordParameters(build: SBuild): MutableCollection<Parameter> {
        val secret = findSecret(build)

        return if (StringUtil.isEmptyOrSpaces(secret)) {
            mutableListOf()
        } else {
            mutableListOf(SimpleParameter(KsmTokenConstants.CLIENT_SECRET, secret!!))
        }
    }

    private fun findSecret(build: SBuild): String? {
        return build.buildType
            ?.project
            ?.getAvailableFeaturesOfType(OAuthConstants.FEATURE_TYPE)
            ?.firstOrNull { it.parameters[OAuthConstants.OAUTH_TYPE_PARAM] == KsmConstants.FEATURE_TYPE }
            ?.parameters
            ?.get(KsmTokenConstants.CLIENT_SECRET)
    }
}
