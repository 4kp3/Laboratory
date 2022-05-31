package com.lovely.bear.laboratory.lambda

data class Person(val name: String, val age: Int)

fun runFunc(f: () -> Unit): Unit {
    f()
}

fun Person.show() {

}

fun createPerson(f: (String, Int) -> Person): Person {
    return f("suiji", 1)
}

fun printlnTime() {
    println(System.currentTimeMillis())
}

fun main() {
//    val p = Person("xx", 12)
//    runFunc(p::toString)
//    runFunc(::printlnTime)
//    createPerson(::Person)
//    runFunc(p::show)

//    GlobalScope.launch {
//        generateSequence<Int>(1) {
//            it + 1
//        }.filter { it % 2 == 0 }.takeWhile { it <= 100 }.forEach { print("$it ") }
//    }
//
//    fun join(vararg ss: String) {
//        println(ss.joinToString())
//    }
//
//    join(*listOf("1", "23").toTypedArray())

//    val threeDays = LocalDate.now()..(LocalDate.now().plusDays(3))
//    val tm = LocalDate.now().plusDays(1)
//    println(tm in threeDays)

    val (num1, num2) = "1,2".split(",")

    //"".isNullOrBlank()
    //为集合创建
//    val l = ArrayList<String>(5)
//    l.add("haha")
//    l.add("xiaoxiao")
//    l.add("hongdulasi")
//    l.add("goline")
//    l.add("setup")
    //l.parallelStream().filter()


}