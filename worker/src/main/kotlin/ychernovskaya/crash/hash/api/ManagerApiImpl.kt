package ychernovskaya.crash.hash.api

interface ManagerApi {
    fun sentEncodedData(managerUrl: String, encodedData: List<String>): Boolean
}

class ManagerApiImpl : ManagerApi {
    override fun sentEncodedData(
        managerUrl: String,
        encodedData: List<String>
    ): Boolean {
        //TODO: add worker request
        return true
    }
}