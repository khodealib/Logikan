package ir.logicfan.core.data.network.interceptor

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import ir.logicfan.core.data.util.FileUtils
import ir.logicfan.core.di.qulifier.ApiBaseUrl
import ir.logicfan.core.di.qulifier.ExcludeMock
import ir.logicfan.core.di.qulifier.IncludeMock
import okhttp3.*
import java.io.IOException
import javax.inject.Inject

/**
 * Provide network response from json mock in local device
 * You should remove `Base Url` and `Query parameters` from requestPath in Mock module
 * If your request contains `Path parameters` eg. {product_id}, replace it with {#}
 *
 * @property apiEnableMock master setting to enable or disable mock
 * @property apiExcludeFromMock exclude specific endpoint from being mocked
 */
class OfflineMockInterceptor @Inject
constructor(
    private val context: Context,
    @ApiBaseUrl private val baseUrl: String,
    private val requestPathToJsonMap: Map<String, String>,
    private val apiEnableMock: Boolean,
    private val apiResponseLatency: Long,
    @IncludeMock private val apiIncludeIntoMock: Array<String>,
    @ExcludeMock private val apiExcludeFromMock: Array<String>
) : Interceptor {

    @SuppressLint("DefaultLocale")
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url().toString()
        val route = url
            .replace(baseUrl, "")  // remove base url
            .replace("\\?.*".toRegex(), "") // remove query parameters
            .replace("\\b\\d+((,\\d)+)?".toRegex(), "{#}") // replace dynamic path (eg. 3,5,7) with {#}

        // example requestPath -> [DELETE] shops/{#}/social-accounts/{#}
        val requestMethod = request.method().toUpperCase()
        val requestPath = "[${request.method().toUpperCase()}] $route"

        var canProceedWithMock = apiEnableMock // can we use mock or proceed with network api

        // check if requestPath is included
        for (item in apiIncludeIntoMock) {
            if (item == requestPath) {
                canProceedWithMock = true
            }
        }

        // check if requestPath is excluded
        for (item in apiExcludeFromMock) {
            if (item == requestPath) {
                canProceedWithMock = false
            }
        }

        if (canProceedWithMock) {
            val json = getMockJsonOrNull(requestPath)
            return json?.let {
                // json is found
                Log.d(TAG, "--> Mocking [$requestMethod] $url")
                val contentType = MediaType.parse(RESPONSE_MEDIA_TYPE)
                val responseBody = ResponseBody.create(contentType, it)
                Thread.sleep(apiResponseLatency)
                Response.Builder()
                    .body(responseBody)
                    .request(request)
                    .message(RESPONSE_MESSAGE)
                    .protocol(Protocol.HTTP_1_1)
                    .code(RESPONSE_CODE)
                    .build()
            } ?: run {
                // no json found, proceed request from internet
                chain.proceed(request)
            }
        } else {
            return chain.proceed(request)
        }
    }

    /**
     * read mock json or if exception occur's return null
     *
     * @param request api request
     * @return json in string object
     */
    @Throws(IOException::class)
    fun getMockJsonOrNull(request: String): String? {
        val fileName = requestPathToJsonMap[request]
        return fileName?.run {
            // json exists in assets directory
            FileUtils.readTextFileFromAssets(context, "$MOCK_PATH_IN_ASSETS/$this")
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
        private const val MOCK_PATH_IN_ASSETS = "mock_json"
        private const val RESPONSE_MEDIA_TYPE = "application/json"
        private const val RESPONSE_MESSAGE = "OK"
        private const val RESPONSE_CODE = 200
    }
}