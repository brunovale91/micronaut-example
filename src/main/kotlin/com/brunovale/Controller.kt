package com.brunovale

import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.sse.Event
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.reactivex.Flowable
import org.reactivestreams.Publisher
import java.util.concurrent.TimeUnit

@Controller("/test")
class Controller(
    @Value("\${testProperty}")
    val testProperty: String
) {

    @ExecuteOn(TaskExecutors.IO)
    @Get("stream", produces = [MediaType.TEXT_EVENT_STREAM])
    fun testStream(): Publisher<Event<Counter>> {
        return Flowable.intervalRange(
            0,
            11,
            5,
            5,
            TimeUnit.SECONDS
        ).map {
            createCounterEvent(it, Counter(it))
        }
    }

    @Get(produces = [MediaType.TEXT_PLAIN])
    fun test() = "test"

    private fun createCounterEvent(index: Long, counter: Counter): Event<Counter> {
        return Event.of(counter).id(index.toString()).name("counter")
    }

    @Error(status = HttpStatus.NOT_FOUND)
    fun notFound(request: HttpRequest<*>): HttpResponse<String> {
        return HttpResponse.status<String>(HttpStatus.NOT_FOUND, "Not found")
            .body("Not found")
    }

    // the loop only happens if throwable error handling present
    // It keeps trying to initialize the throwable error handler
    @Error
    fun error(request: HttpRequest<*>, e: Throwable): HttpResponse<String> {
        return HttpResponse.status<String>(HttpStatus.INTERNAL_SERVER_ERROR, "Server error")
            .body("Server error")
    }

}

data class Counter(var number: Long)
