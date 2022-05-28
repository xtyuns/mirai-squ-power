package com.xtyuns.http

import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*

object Client {
    val INSTANCE = HttpClient(Java) {
        install(ContentNegotiation) {
            json()
        }
    }
}