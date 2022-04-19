package com.lovely.bear.laboratory.conntinuation.lite

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 无返回值类型的Job
 * @author guoyixiong
 */
class StandaloneCoroutine(context: CoroutineContext = EmptyCoroutineContext) :
    AbstractCoroutine<Unit>(context)