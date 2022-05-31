package com.lovely.bear.laboratory.continuation

//import kotlinx.coroutines.*
import kotlinx.coroutines.*

suspend fun main() {

//   val ints= sequence<Int> {
//        yield(1)
//        yield(2)
//        yield(1)
//        yield(1)
//    }
//    for (i in ints) {
//        println(i)
//    }


//    withContext(Executors.newSingleThreadExecutor { Thread("test-thread") }
//        .asCoroutineDispatcher()) {
//        flow<Unit> {
//            println("flow a element on Thread:${Thread.currentThread().name}")
//            emit(Unit)
//            println("flow a element on Thread:${Thread.currentThread().name}")
//            emit(Unit)
//            println("flow a element on Thread:${Thread.currentThread().name}")
//            emit(Unit)
//            println("end flow emit")
//        }.flowOn(Dispatchers.IO)
//            .collect {
//                println("received a element on Thread:${Thread.currentThread().name}")
//            }
//    }

//    val channel = Channel<Int>(Channel.Factory.UNLIMITED)
//    val producer = GlobalScope.launch {
//        repeat(20) {
//            println("send a element")
//            channel.send(it)
//            println("send a element already")
//            //delay(1_000)
//        }
//    }
//    val consumer = GlobalScope.launch {
//        repeat(20) {
//            println(channel.receive())
//            delay(1000)
//        }
//    }
//    producer.join()
//    consumer.join()
//    flow<Int> {
//        emit(1)
//        throw IllegalArgumentException("1")
//    }.onCompletion { e->
//        println("comp ${e==null}")
//        emit(2)
//    }.catch {
//        println("got e")
//        emit(3)
//    }.collect {
//        println("got $it")
//    }
//    val channels = List(5) {
//        Channel<String>()
//    }
//    GlobalScope.launch(Dispatchers.IO) {
//        delay(1000)
//        channels[0].send("0")
//        channels[1].send("1")
//        channels[2].send("2")
//    }
//    val s = select<String> {
//        channels.forEach { channel ->
//            channel.onReceive { it }
//        }
//    }
//    println(s)

//    var count:Int=0
//    val m= Mutex()
//    (0..1000).toList().map {
//        GlobalScope.launch {
//            m.withLock {
//                count++
//            }
//        }
//    }.joinAll()
//    println(count)
    val count=0
    val result=count+ List(1000) {
        GlobalScope.async { 1 }
    }.sumOf { it.await() }
    println(result)
}