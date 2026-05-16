package org.czellmer1324.proxyPlugin

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.registerSuspend
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import org.czellmer1324.proxyPlugin.listeners.PreConnect
import org.slf4j.Logger

@Plugin (
    id = "aetherProxy",
    name = "Aether Sky Proxy"
)
class ProxyPlugin {
    @Inject
    lateinit var server: ProxyServer
    @Inject
    lateinit var logger: Logger

@Inject constructor(suspendingPluginContainer: SuspendingPluginContainer) {
    suspendingPluginContainer.initialize(this)
}

    @Subscribe
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
        val servers : HashMap<String, RegisteredServer> = server.allServers.associateBy { it.serverInfo.name } as HashMap<String, RegisteredServer>
        server.eventManager.registerSuspend(this, PreConnect(logger, servers))
        logger.info("Proxy has been initialized")
    }
}
