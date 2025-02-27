package ychernovskaya.crash.hash.api

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.slf4j.LoggerFactory
import ychernovskaya.crash.hash.model.CrackHashWorkerResponse

interface ManagerApi {
    suspend fun sentEncodedData(managerUrl: String, requestId: String, partNumber: Int, encodedData: List<String>)
}

class ManagerApiImpl : ManagerApi {
    private val logger = LoggerFactory.getLogger(ManagerApiImpl::class.java)
    private val httpClient = HttpClient(CIO)
    private val xmlMapper = XmlMapper()

    override suspend fun sentEncodedData(
        managerUrl: String,
        requestId: String,
        partNumber: Int,
        encodedData: List<String>
    ) {
        val answers = CrackHashWorkerResponse.Answers()
        answers.words.addAll(encodedData)

        val xmlBody = CrackHashWorkerResponse()
        xmlBody.partNumber = partNumber
        xmlBody.answers = answers
        xmlBody.requestId = requestId

        try {
            val response: HttpResponse = httpClient.patch("$managerUrl/internal/api/hash-crack/request") {
                contentType(ContentType.Application.Xml)
                setBody(xmlMapper.writeValueAsString(xmlBody))
            }

            logger.debug("Response: {} - {}", response.status, response.bodyAsText())
        } catch (e: Exception) {
            logger.error("Error while sending request", e)
        }
    }
}