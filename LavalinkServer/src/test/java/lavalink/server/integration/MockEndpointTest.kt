package lavalink.server.integration

import lavalink.server.config.ServerConfig
import lavalink.server.util.AwaitWebServer
import lavalink.server.util.SharedSpringContext
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.web.ServerProperties
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@ExtendWith(SharedSpringContext::class, AwaitWebServer::class)
class MockEndpointTest {

    @Test
    fun testCustomEndpoint(appProps: ServerConfig, serverProps: ServerProperties) {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .GET()
            .header("Authorization", appProps.password)
            .uri(URI("http://localhost:${serverProps.port}/mock"))
            .build()
        val body = client.send(request, HttpResponse.BodyHandlers.ofString()).body()
        Assertions.assertEquals("Hello, world!", body)
    }

}