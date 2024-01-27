package eu.kanade.tachiyomi.data.track.myanimelist

import eu.kanade.tachiyomi.BuildConfig
import eu.kanade.tachiyomi.network.parseAs
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import uy.kohesive.injekt.injectLazy
import java.io.IOException

class MyAnimeListInterceptor(private val myanimelist: MyAnimeList) : Interceptor {

    private val json: Json by injectLazy()

    private var oauth: OAuth? = myanimelist.loadOAuth()
    private val tokenExpired get() = myanimelist.getIfAuthExpired()

    override fun intercept(chain: Interceptor.Chain): Response {
        if (tokenExpired) {
            throw MALTokenExpired()
        }

        val originalRequest = chain.request()

        // Refresh access token if expired
        if (oauth != null && oauth!!.isExpired()) {
            setAuth(refreshToken(chain))
        }

        if (oauth == null) {
            throw IOException("MAL: User is not authenticated")
        }

        // Add the authorization header to the original request
        val authRequest = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer ${oauth!!.access_token}")
            .header("User-Agent", "null2264/yokai/${BuildConfig.VERSION_NAME} (${BuildConfig.APPLICATION_ID})")
            .build()

        return chain.proceed(authRequest)
    }

    /**
     * Called when the user authenticates with MyAnimeList for the first time. Sets the refresh token
     * and the oauth object.
     */
    fun setAuth(oauth: OAuth?) {
        this.oauth = oauth
        myanimelist.saveOAuth(oauth)
    }

    private fun refreshToken(chain: Interceptor.Chain): OAuth {
        return runCatching {
            val oauthResponse = chain.proceed(MyAnimeListApi.refreshTokenRequest(oauth!!))
            if (oauthResponse.code == 401) {
                myanimelist.setAuthExpired()
            }
            if (oauthResponse.isSuccessful) {
                with(json) { oauthResponse.parseAs<OAuth>() }
            } else {
                oauthResponse.close()
                null
            }
        }
            .getOrNull()
            ?: throw MALTokenExpired()
    }
}

class MALTokenExpired: IOException("MAL: Login has expired")
