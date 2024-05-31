plugins {
  kotlin("jvm")
  kotlin("plugin.serialization") version "1.9.24"
  id("io.github.rodm.teamcity-agent") version "1.5.3"
}

teamcity {
  version = rootProject.extra["teamcityVersion"] as String
  agent {
    descriptor = project.file("teamcity-plugin.xml")
  }
}

repositories {
  mavenCentral()
  maven(url = "https://download.jetbrains.com/teamcity-repository")
}

dependencies {
  implementation(project(":plugin-ksm-common"))

  implementation("com.keepersecurity.secrets-manager:core:16.6.4")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  implementation("com.squareup.okhttp3:okhttp:4.12.0")
  testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

  implementation("com.squareup.moshi:moshi:1.15.1")
  compileOnly("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

  testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
  testImplementation("org.mockito:mockito-inline:5.2.0")
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}
