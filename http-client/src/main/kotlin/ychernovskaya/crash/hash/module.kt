package ychernovskaya.crash.hash

import org.koin.core.module.Module
import org.koin.dsl.module

fun networkModule(): Module = module {
    factory { HttpClient() }
}