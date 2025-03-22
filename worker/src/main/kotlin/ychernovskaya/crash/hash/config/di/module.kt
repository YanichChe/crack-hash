package ychernovskaya.crash.hash.config.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import ychernovskaya.crash.hash.services.WorkerService
import ychernovskaya.crash.hash.services.WorkerServiceImpl

fun appModule() = module {
    services()
}

private fun Module.services() {
    factoryOf(::WorkerServiceImpl) bind WorkerService::class
}