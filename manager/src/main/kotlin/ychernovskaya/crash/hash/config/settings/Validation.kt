package ychernovskaya.crash.hash.config.settings

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult
import ychernovskaya.crash.hash.model.HashInfo

private const val MaxLengthValue = 100
private const val MinLengthValue = 0

fun Application.configureValidation() {
    install(RequestValidation) {
        validate<HashInfo> { hashData ->
            if (hashData.maxLength > MaxLengthValue)
                ValidationResult.Invalid("Max length more than $MaxLengthValue characters.")
            else if (hashData.maxLength < MinLengthValue)
                ValidationResult.Invalid("Min length less than $MinLengthValue characters.")
            else if (isMD5Hash(hashData.hash).not())
                ValidationResult.Invalid("This is not MD5 hash.")
            else ValidationResult.Valid
        }
    }
}

private fun isMD5Hash(input: String): Boolean {
    return Regex("^[a-fA-F0-9]{32}$").matches(input)
}