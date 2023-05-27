package com.lovely.bear.laboratory

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

data class Box<T>(var data:T)

/**
 * 这个示例很明显的体现了上界和下界的作用
 *
 * 举例 T = String
 * 需求是source中的元素要能放入dest
 * 显然存在 Box<in String> 可以被 Box<CharSequence> 安全替代的情况，string当然能放入CharSequence
 * 这种替代关系表明 Box<CharSequence> 是 Box<in String> 的子类型，因为只有子类型可以替代父类型
 * 参数类型关系和泛型类型关系发生了逆转，这就是逆变
 *
 * @param source 如果用 List 来演示，编译器会发出警告，因为 List 泛型已经声明 out 了，这里再使用 out 属于重复
 */
fun <T> copyData(source:Box< out T>,dest:Box<in T>){
    dest.data = source.data
    //source.data = dest.data;❌
}

fun main() {
//    val box1=Box("i'm created in box1,is a String");
//    val box2=Box<CharSequence>(StringBuilder("i'm created in box2,is a StringBuilder"));
//    copyData<String>(box1,box2)
//    println(box2.data)
}