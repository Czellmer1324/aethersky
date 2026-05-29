package org.czellmer1324.proxyPlugin

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.registerSuspend
import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.proxy.server.RegisteredServer
import org.czellmer1324.proxyPlugin.listeners.PreConnect
import org.slf4j.Logger

@Plugin(
    id = "aetherproxy",
    name = "Aether Sky Proxy Plugin"
)
class ProxyPlugin {
    private var container: PluginContainer
    @Inject
    private lateinit var proxyServer: ProxyServer
    @Inject
    private lateinit var logger: Logger

    @Inject
    constructor(suspendingPluginContainer: SuspendingPluginContainer) {
        suspendingPluginContainer.initialize(this)
        container = suspendingPluginContainer.pluginContainer
    }

    @Subscribe
    suspend fun onProxyInitialization(event: ProxyInitializeEvent) {
        val servers = proxyServer.allServers.associateBy { it.serverInfo.name } as HashMap<String, RegisteredServer>
        PlayerTransfer.init(container, servers, proxyServer)
        proxyServer.eventManager.registerSuspend(this, PreConnect(logger, servers))
        logger.info("Proxy has been initialized")
    }
}
