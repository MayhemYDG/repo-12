//import com.github.rodm.teamcity.tasks.ServerPlugin
import com.github.rodm.teamcity.ServerPluginConfiguration
import com.github.rodm.teamcity.ServerPluginDescriptor
import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityEnvironments
import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
  kotlin("jvm")
  id("io.github.rodm.teamcity-server") version "1.5.3"
  id("io.github.rodm.teamcity-environments") version "1.5.3"
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "$rootDir/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "$rootDir/servers"

val agent: Configuration = configurations.getByName("agent")

repositories {
  mavenCentral()
  maven(url = "https://download.jetbrains.com/teamcity-repository")
}

dependencies {
  implementation(project(":plugin-ksm-common"))
  agent(project(path = ":plugin-ksm-agent", configuration = "plugin"))

  provided("org.jetbrains.teamcity.internal:server:${rootProject.extra["teamcityVersion"]}")

  compileOnly("org.jetbrains.teamcity.internal:web:${rootProject.extra["teamcityVersion"]}")
  testImplementation("org.jetbrains.teamcity.internal:web:${rootProject.extra["teamcityVersion"]}")
  compileOnly("org.jetbrains.teamcity.internal:server:${rootProject.extra["teamcityVersion"]}")
  compileOnly("org.jetbrains.teamcity:oauth:${rootProject.extra["teamcityVersion"]}")
  testImplementation("org.jetbrains.teamcity:oauth:${rootProject.extra["teamcityVersion"]}")

  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

  implementation("com.squareup.moshi:moshi:1.15.1")
  compileOnly("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}

tasks.serverPlugin {
  archiveBaseName.set("keeper-secrets-manager")
}

teamcity {
  version = rootProject.extra["teamcityVersion"] as String

  server {
    descriptor = project.file("teamcity-plugin.xml")
    tokens = mapOf("VERSION" to rootProject.version, "VENDOR_NAME" to "Keeper Security")
  }

  environments {
    //baseDownloadUrl = "https://download.jetbrains.com/teamcity" // default
    downloadsDir = "$rootDir/env/downloads"
    baseHomeDir = "$rootDir/env/servers"
    baseDataDir = "$rootDir/env/data"

    operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) {
        environments.create(this, block)
    }

    "teamcity2023" {
      version = "2023.05.4"
      serverOptions("-DTC.res.disableAll=true -Dteamcity.development.mode=true")
      agentOptions()
    }

    "teamcity2023Debug" {
      version = "2023.05.4"
      serverOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5600 -DTC.res.disableAll=true -Dteamcity.development.mode=true")
      agentOptions("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5601")
    }
  }
}

// Extension function to allow cleaner configuration
fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
  configure(configuration)
}

fun TeamCityPluginExtension.environments(configuration: TeamCityEnvironments.() -> Unit) {
    this.environments(configuration)
}

fun ServerPluginConfiguration.descriptor(configuration: ServerPluginDescriptor.() -> Unit) {
    this.descriptor(configuration)
}

configurations.implementation {
    exclude(group = "org.slf4j", module= "slf4j-api")
}
