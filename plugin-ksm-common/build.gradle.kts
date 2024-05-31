plugins {
  kotlin("jvm")
}

repositories {
    mavenCentral()
    maven(url = "https://download.jetbrains.com/teamcity-repository")
}

dependencies {
  implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.24")
  implementation("com.keepersecurity.secrets-manager:core:16.6.4")

  compileOnly("org.jetbrains.teamcity:common-api:${rootProject.extra["teamcityVersion"]}")

  testImplementation("org.jetbrains.teamcity:server-api:${rootProject.extra["teamcityVersion"]}")
  testImplementation("org.assertj:assertj-core:3.25.3")
  testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
  testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
}
