package pe.edu.upc.viora.testing

import java.lang.reflect.Proxy

/**
 * An implementation of [T] whose every member fails: delegate to it (`object : Repo by unused<Repo>()`) and override
 * only what the test reaches, so a large repository needs no wall of `error("not used")`.
 */
inline fun <reified T : Any> unused(): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
        throw UnsupportedOperationException("${T::class.simpleName}.${method.name} is not used by this test")
    } as T
