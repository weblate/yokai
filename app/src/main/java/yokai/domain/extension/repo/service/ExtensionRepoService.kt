package yokai.domain.extension.repo.service

import androidx.core.net.toUri
import eu.kanade.tachiyomi.network.GET
import eu.kanade.tachiyomi.network.HttpException
import eu.kanade.tachiyomi.network.awaitSuccess
import eu.kanade.tachiyomi.network.parseAs
import eu.kanade.tachiyomi.util.system.withIOContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import uy.kohesive.injekt.injectLazy
import yokai.domain.extension.repo.exception.FetchExtensionRepoException
import yokai.domain.extension.repo.model.ExtensionRepo
import java.net.UnknownHostException

class ExtensionRepoService(
    private val client: OkHttpClient,
) {

    private val json: Json by injectLazy()

    suspend fun fetchRepoDetails(
        repo: String,
    ): ExtensionRepo? {
        return withIOContext {
            val url = "$repo/repo.json".toUri()

            try {
                val response = with(json) {
                    client.newCall(GET(url.toString()))
                        .awaitSuccess()
                        .parseAs<JsonObject>()
                }
                response["meta"]
                    ?.jsonObject
                    ?.let { jsonToExtensionRepo(baseUrl = repo, it) }
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> null
                    is UnknownHostException -> throw FetchExtensionRepoException(e)
                    else -> throw e
                }
            }
        }
    }

    private fun jsonToExtensionRepo(baseUrl: String, obj: JsonObject): ExtensionRepo? {
        return try {
            ExtensionRepo(
                baseUrl = baseUrl,
                name = obj["name"]!!.jsonPrimitive.content,
                shortName = obj["shortName"]?.jsonPrimitive?.content,
                website = obj["website"]!!.jsonPrimitive.content,
                signingKeyFingerprint = obj["signingKeyFingerprint"]!!.jsonPrimitive.content,
            )
        } catch (_: NullPointerException) {
            null
        }
    }
}
