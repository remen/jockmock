package io.github.remen.jockmock

import net.bytebuddy.ByteBuddy
import net.bytebuddy.description.modifier.Visibility
import net.bytebuddy.implementation.InvocationHandlerAdapter
import net.bytebuddy.matcher.ElementMatchers
import org.objenesis.ObjenesisHelper
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KFunction0
import kotlin.reflect.KFunction1

object JockMock {
    const val invocationHandlerFieldName = "__jockmock_invocation_handler"

    inline fun <reified T : Any> jockMock(): T {
        val clazz = ByteBuddy()
            .subclass(T::class.java)
            .defineField(invocationHandlerFieldName, JockMockInvocationHandler::class.java, Visibility.PUBLIC)
            .method(ElementMatchers.any())
            .intercept(InvocationHandlerAdapter.toField(invocationHandlerFieldName))
            .make()
            .load(T::class.java.classLoader)
            .loaded

        // In order to support instantiation of classes without a default (zero-args) constructor
        // we instantiate the class using Objenesis
        val newInstance = ObjenesisHelper.newInstance(clazz)

        clazz.getDeclaredField(invocationHandlerFieldName).set(newInstance, JockMockInvocationHandler())
        return newInstance
    }

    @JvmName("stub0")
    fun <ReturnType> KFunction0<ReturnType>.stub(implementation: () -> ReturnType) {
        val jockMock = getInvocationHandler(this as CallableReference)
        jockMock.stubbedMethods[this.name] = { implementation() }
    }

    @JvmName("stub1")
    fun <Arg0, ReturnType> KFunction1<Arg0, ReturnType>.stub(implementation: (arg0: Arg0) -> ReturnType) {
        val jockMock = getInvocationHandler(this as CallableReference)
        jockMock.stubbedMethods[this.name] = { args ->
            @Suppress("UNCHECKED_CAST")
            implementation(args[0] as Arg0)
        }
    }

    private fun getInvocationHandler(reference: CallableReference): JockMockInvocationHandler {
        val instance = reference.boundReceiver
        return instance::class.java.getDeclaredField(invocationHandlerFieldName)
            .get(instance) as JockMockInvocationHandler
    }

    class JockMockInvocationHandler : InvocationHandler {
        val stubbedMethods = mutableMapOf<String, (args: Array<out Any>) -> Any?>()

        override fun invoke(proxy: Any?, method: Method, args: Array<out Any>?): Any? {
            return stubbedMethods[method.name]!!.invoke(args ?: emptyArray())
        }
    }
}
