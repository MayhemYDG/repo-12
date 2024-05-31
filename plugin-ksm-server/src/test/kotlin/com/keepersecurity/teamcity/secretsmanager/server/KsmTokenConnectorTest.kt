package com.keepersecurity.teamcity.secretsmanager.server

import com.keepersecurity.teamcity.secretsmanager.common.KsmException
import com.keepersecurity.teamcity.secretsmanager.common.TokenRequestSettings
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KsmTokenConnectorTest {

  private val server = MockWebServer()

  @AfterEach
  internal fun shutdownServer() {
    server.shutdown()
  }

  @BeforeEach
  internal fun startServer() {
    server.start()
  }

  @Test
  internal fun extractedResponse() {
    val connector = KsmTokenConnector()

    server.enqueue(MockResponse().setBody(tokenResponse))
    val response = connector.requestToken(settings)

    assertThat(response.accessToken).isEqualTo(accessToken)
  }

  @Test
  internal fun submittedRequestWithErrorResponse() {
    val connector = KsmTokenConnector()
    server.enqueue(MockResponse().setResponseCode(404))

    val throwable = catchThrowable { connector.requestToken(TokenRequestSettings("")) }

    assertThat(throwable)
          .isInstanceOf(KsmException::class.java)
          .hasMessageContaining("Could not fetch KSM token")
  }

  private val settings = TokenRequestSettings(
        "AAAAAAAA"
  )

  private val accessToken = "AAAAAAAA"

  private val tokenResponse = """
        {
          "access_token": "$accessToken"
        }
      """.trimIndent()
}
