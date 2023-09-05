package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.getOrElse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.runBlocking
import org.cufy.graphkt.java.`graphql-java`
import org.cufy.graphkt.ktor.*
import org.cufy.graphkt.schema.*

val flow = MutableSharedFlow<String>(0)
var lastMessage: String = "No messages sent yet"

fun Application.configureRouting() {
    install(DoubleReceive)

    routing {
        playground()
        graphql {
            `graphql-java`

            schema {
                query {
                    name("RootQuery")
                    description { "this is the root query" }
                    queryFields()
                }

                mutation {
                    name("RootMutation")
                    description { "this is the root mutation" }
                    field("sendGreeting") {
                        type { GraphQLVoidType }
                        description { "" }

                        val greetingArg = argument("greeting") {
                            type { GraphQLStringType }
                        }

                        get {
                            val greeting = greetingArg()

                            lastMessage = greeting
                            flow.emit(greeting)
                        }
                    }
                }

                subscription {
                    field("subscribeToGreetingRequests") {
                        type { GraphQLStringType }
                        getFlow { flow }
                    }
                }
            }
        }
    }
}

fun GraphQLRoute<*>.queryFields() {
    field("lastGreeting") {
        type { GraphQLStringType.Nullable }
        get { lastMessage }
    }
}
