package ychernovskaya.crash.hash

import com.github.kittinunf.fuel.Fuel.post
import com.github.kittinunf.fuel.Fuel.patch
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result

class HttpClient {
    fun post(
        url: String,
        xmlBody: String,
        headers: Map<String, String> = emptyMap()
    ): Triple<String?, Int, FuelError?> {
        return post(url)
            .header(headers + mapOf("Content-Type" to "application/xml"))
            .body(xmlBody)
            .responseString()
            .let { processResponse(it) }
    }

    fun patch(
        url: String,
        xmlBody: String,
        headers: Map<String, String> = emptyMap()
    ): Triple<String?, Int, FuelError?> {
        return patch(url)
            .header(headers + mapOf("Content-Type" to "application/xml"))
            .body(xmlBody)
            .responseString()
            .let { processResponse(it) }
    }

    private fun processResponse(response: Triple<Request, Response, Result<String, FuelError>>):
            Triple<String?, Int, FuelError?> {
        val (_, responseObj, result) = response
        return when (result) {
            is Result.Success -> Triple(result.value, responseObj.statusCode, null)
            is Result.Failure -> Triple(null, responseObj.statusCode, result.error)
        }
    }
}