package com.lovely.bear.laboratory.main.fragment

import java.util.concurrent.locks.ReentrantLock

class LockThread : Thread("block_thread") {

    val lock = ReentrantLock()

    override fun run() {
        super.run()
        lock.lock()
        var i = 0;
        while (i < 100000000) {
            i++
        }
        lock.unlock()
    }
}