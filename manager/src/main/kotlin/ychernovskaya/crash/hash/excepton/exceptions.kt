package ychernovskaya.crash.hash.excepton

class CallIdAlreadyExistsException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)
class NotSuchCallIdException(message: String? = null, cause: Throwable? = null) : Exception(message, cause)