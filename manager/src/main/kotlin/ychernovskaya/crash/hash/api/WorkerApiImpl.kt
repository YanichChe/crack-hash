package ychernovskaya.crash.hash.api

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.headers
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.HashData
import ychernovskaya.crash.hash.model.CrackHashManagerRequest

interface WorkerApi {
    suspend fun sendEncodeTask(
        workerUrl: String,
        hashData: HashData,
        partCount: Int,
        partNumber: Int,
        requestId: String
    )
}

class WorkerApiImpl : WorkerApi {
    private val logger = LoggerFactory.getLogger(WorkerApiImpl::class.java)
    private val httpClient = HttpClient(CIO)
    private val xmlMapper = XmlMapper()

    override suspend fun sendEncodeTask(
        workerUrl: String,
        hashData: HashData,
        partCount: Int,
        partNumber: Int,
        requestId: String
    ) {
        val alphabet = CrackHashManagerRequest.Alphabet()
        alphabet.symbols.addAll(hashData.symbols)

        val xmlBody = CrackHashManagerRequest()
        xmlBody.maxLength = hashData.maxLength
        xmlBody.hash = hashData.hash
        xmlBody.alphabet = alphabet
        xmlBody.partCount = partCount
        xmlBody.partNumber = partNumber
        xmlBody.requestId = requestId

        try {
            logger.debug("REQUEST ID: {}", requestId)
            val response: HttpResponse = httpClient.post("$workerUrl/internal/api/hash-crack/task") {
                contentType(ContentType.Application.Xml)
                setBody(xmlMapper.writeValueAsString(xmlBody))
            }

            logger.debug("Response: {} - {}", response.status, response.bodyAsText())
        } catch (e: Exception) {
            logger.error("Error while sending request", e)
        }
    }
}
