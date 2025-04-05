package ychernovskaya.crack.hach.messagedigest

import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import java.security.MessageDigest

fun messageDigestModule(): Module = module {
    val md = MessageDigest.getInstance("MD5")
    single { md } bind MessageDigest::class
}

@OptIn(ExperimentalStdlibApi::class)
fun String.md5(md: MessageDigest): String {
    val digest = md.digest(this.toByteArray())
    return digest.toHexString()
}