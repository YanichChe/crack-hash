package ychernovskaya.crash.hash

interface MongoConfiguration {
    val mongoLogin: String
    val mongoPassword: String
    val mongoPort: Int
    val mongoHost: String
}
