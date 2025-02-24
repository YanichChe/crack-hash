package ychernovskaya.crash.hash.api

interface WorkerApi {
    fun getEncoded(workerUrl: String, hash: String, maxLength: Int): List<String>?
}

class WorkerApiImpl : WorkerApi {
    override fun getEncoded(workerUrl: String, hash: String, maxLength: Int): List<String>? {
        //TODO: add worker request
        return listOf("example")
    }
}