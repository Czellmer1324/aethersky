package org.czellmer1324.aetherskyPlugin.island.loader

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.infernalsuite.asp.api.loaders.SlimeLoader
import com.infernalsuite.asp.loaders.api.APILoader
import com.infernalsuite.asp.loaders.api.MapStructure
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.time.Duration
import java.util.*
import java.util.stream.Collectors
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class CustomApiLoader (private var apiUrl: String,
                       username: String? = null,
                       token: String? = null,
                       private val ignoreSslCertificate : Boolean)
    : SlimeLoader {

    private val logger: Logger = LoggerFactory.getLogger(APILoader::class.java)
    private var authorizationHeader: String
    private val gson = Gson()

    init {
        if (!apiUrl.endsWith("/")) apiUrl += "/"
        this.apiUrl = apiUrl

        if (!username.isNullOrEmpty() && !token.isNullOrEmpty()) {
            val auth = "$username:$token"
            val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            this.authorizationHeader = "Basic $encodedAuth"
        } else {
            this.authorizationHeader = ""
        }
    }


    override fun readWorld(worldName: String): ByteArray {
        try {
            return downloadFile(worldName)
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException(e)
        }
    }

    override fun worldExists(worldName: String): Boolean {
        // TODO: Need to implement this functionality
        return false
    }

    override fun listWorlds(): List<String> {
        val mapList: MutableList<MapStructure?>?

        try {
            mapList = getMapList()
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        } catch (e: InterruptedException) {
            throw java.lang.RuntimeException(e)
        }

        return mapList!!.stream()
            .map { c: MapStructure? -> c!!.name.substring(0, c.name.length - 6) }
            .collect(Collectors.toList())
    }

    override fun saveWorld(worldName: String, serializedWorld: ByteArray) {
        // TODO: Need to implement custom logic for this with my mastercontrol api
        logger.warn("Illegal call to saveWorld: API Worlds cannot be saved. They're always read-only.")
    }

    override fun deleteWorld(worldName: String) {
        // TODO: need to implement this logic with my mastercontrol api
        logger.warn("Illegal call to deleteWorld: API Worlds cannot be deleted through the loader.")
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun downloadFile(worldId: String?): ByteArray {
        val client: HttpClient = createHttpClient()

        // Check file size with HEAD request
        val headRequest = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl + worldId))
            .method("HEAD", HttpRequest.BodyPublishers.noBody())
            .header("Authorization", authorizationHeader)
            .build()

        val headResponse = client.send(headRequest, HttpResponse.BodyHandlers.discarding())
        val fileSize = headResponse.headers().firstValueAsLong("content-length").orElse(0L)

        if (fileSize > Int.MAX_VALUE) {
            throw IndexOutOfBoundsException("World is too big!")
        }

        val getRequest = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl + worldId))
            .GET()
            .header("Authorization", authorizationHeader)
            .build()

        val getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofInputStream())

        getResponse.body().use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int

                while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                return outputStream.toByteArray()
            }
        }
    }

    private fun createHttpClient(): HttpClient {
        try {
            if (this.ignoreSslCertificate) {
                val sslContext: SSLContext = createTrustAllSSLContext()

                return HttpClient.newBuilder()
                    .sslContext(sslContext)
                    .connectTimeout(Duration.ofSeconds(30))
                    .build()
            }

            return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        } catch (e: KeyManagementException) {
            throw RuntimeException(e)
        }
    }

    @Throws(NoSuchAlgorithmException::class, KeyManagementException::class)
    private fun createTrustAllSSLContext(): SSLContext {
        val trustAllCerts: Array<TrustManager> = arrayOf(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<X509Certificate?>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate?> {
                return arrayOf()
            }
        }
        )

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun getMapList(): MutableList<MapStructure?>? {
        val client = createHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .GET()
            .header("Authorization", this.authorizationHeader)
            .build()

        val response = client.send(request, HttpResponse.BodyHandlers.ofString())

        val listType = object : TypeToken<MutableList<MapStructure?>?>() {}.type
        return gson.fromJson<MutableList<MapStructure?>?>(response.body(), listType)
    }
}