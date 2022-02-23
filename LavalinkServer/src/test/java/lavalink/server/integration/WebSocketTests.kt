package lavalink.server.integration

import lavalink.plugin.MockEventHandler
import lavalink.plugin.TestFilterExtension
import lavalink.plugin.TestWsExtension
import lavalink.server.config.ServerConfig
import lavalink.server.util.AwaitWebServer
import lavalink.server.util.SharedSpringContext
import lavalink.server.util.SpringContextProvider
import lavalink.server.util.TestWsClient
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.web.ServerProperties
import reactor.test.StepVerifier
import java.time.Duration
import java.util.concurrent.TimeUnit

@ExtendWith(SharedSpringContext::class, AwaitWebServer::class)
class WebSocketTests {

    @Test
    fun testEvents(appProps: ServerConfig, serverProps: ServerProperties) {
        val ws = TestWsClient("ws://localhost:${serverProps.port}", appProps.password!!)
        val eventHandler = SpringContextProvider.staticContext!!.getBean(MockEventHandler::class.java)
        eventHandler.active = true
        ws.connect{ ws.send(JSONObject().apply { put("op", "test") }) }
            .filter { it.getString("op") == "test" }
            .run { StepVerifier.create(this) }
            .expectNextCount(1)
            .expectComplete()
            .verify(Duration.ofSeconds(3))

        eventHandler.latch.await(1, TimeUnit.SECONDS)
    }

    @Test
    fun testWsExtension(appProps: ServerConfig, serverProps: ServerProperties) {
        val ws = TestWsClient("ws://localhost:${serverProps.port}", appProps.password!!)
        val extension = SpringContextProvider.staticContext!!.getBean(TestWsExtension::class.java)
        ws.connect {
            ws.send(JSONObject().apply { put("op", "extension-test") })
        }.subscribe()
        extension.latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testFilterExtension(appProps: ServerConfig, serverProps: ServerProperties) {
        val ws = TestWsClient("ws://localhost:${serverProps.port}", appProps.password!!)
        val extension = SpringContextProvider.staticContext!!.getBean(TestFilterExtension::class.java)
        ws.connect {
            ws.send(JSONObject().apply {
                put("op", "filters")
                put("guildId", "0")
                put("test", JSONObject())
            })
        }.subscribe()
        extension.latch.await(2, TimeUnit.SECONDS)
    }

}