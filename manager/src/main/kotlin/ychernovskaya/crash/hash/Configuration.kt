package ychernovskaya.crash.hash

interface Configuration {
    val mongoLogin: String
    val mongoPassword: String
    val mongoPort: Int
    val mongoHost: String
}
