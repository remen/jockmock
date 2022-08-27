/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package io.github.remen.jockmock

import io.github.remen.jockmock.JockMock.jockMock
import io.github.remen.jockmock.JockMock.stub
import kotlin.test.Test
import kotlin.test.assertEquals

class JockMockTest {
    @Test
    fun simpleInterface() {
        val mock = jockMock<SimpleInterface>()
        mock::helloWorld.stub { "Hello fellow engineer" }
        mock::helloWorlds.stub { s -> "Hello fellow $s" }

        assertEquals("Hello fellow engineer", mock.helloWorld())
        assertEquals("Hello fellow astronaut", mock.helloWorlds("astronaut"))
    }

    @Test
    fun classWithNoDefaultConstructor() {
        val mock = jockMock<ClassWithNoDefaultConstructor>()
        mock::helloWorld.stub { "Hello fellow engineer" }
        mock::helloWorlds.stub { s -> "Hello fellow $s" }

        assertEquals("Hello fellow engineer", mock.helloWorld())
        assertEquals("Hello fellow astronaut", mock.helloWorlds("astronaut"))
    }

    interface SimpleInterface {
        fun helloWorld(): String
        fun helloWorlds(s: String): String
    }

    open class ClassWithNoDefaultConstructor(someArgument: String) {
        open fun helloWorld(): String {
            return "something"
        }

        open fun helloWorlds(s: String): String {
            return "something else"
        }
    }
}
