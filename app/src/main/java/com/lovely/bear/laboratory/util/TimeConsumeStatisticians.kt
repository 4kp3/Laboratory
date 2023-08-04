package com.lovely.bear.laboratory.util

import kotlin.concurrent.getOrSet

/*
* Copyright (C), 2023, Nothing Technology
* FileName: Track
* Author: yixiong.guo
* Date: 2023/8/2 10:35
* Description:  
* History:
* <author> <time> <version> <desc>
*/


class TimeConsumeStatisticians {
    companion object {
        private const val TAG = "TrackTimeConsume"
        const val GROUP_NOW = "GROUP_NOW"
        private val instances = ThreadLocal<TimeConsumeStatisticians>()
        fun get(): TimeConsumeStatisticians {
            return instances.getOrSet {
                TimeConsumeStatisticians()
            }
        }
    }

    private var groups: MutableMap<String, TimeConsumeGroup> = mutableMapOf()


    @JvmOverloads
    fun startGroup(group: String = GROUP_NOW) {
        groups[group] = TimeConsumeGroup(group)
    }

    @JvmOverloads
    fun startRound(name: String, group: String = GROUP_NOW, extra: String? = null) {
//        Track.track(this, "startRound($name)")
        groups[group]?.addRound(name = name, startRound = true)
    }

    @JvmOverloads
    fun endRound(group: String = GROUP_NOW) {
        val consumeGroup = groups[group] ?: return
        val name = consumeGroup.endRound()
//        Track.track(this, "endRound($name)")
    }

    fun pinRound(group: String = GROUP_NOW) {
        val consumeGroup = groups[group] ?: return
        consumeGroup.pinRound()
    }

    @JvmOverloads
    fun addInsertPoint(tag: String, group: String = GROUP_NOW) {
        val consumeGroup = groups[group] ?: return
        return consumeGroup.addInsertPoint(tag)
    }

    @JvmOverloads
    fun addRangePoint(tag: String, group: String = GROUP_NOW): Int {
        val consumeGroup = groups[group] ?: return -1
        return consumeGroup.addRangePoint(tag)
    }

    @JvmOverloads
    fun endRangePoint(id: Int, group: String = GROUP_NOW) {
        val consumeGroup = groups[group] ?: return
        return consumeGroup.endRangePoint(id)
    }

    @JvmOverloads
    fun groupToString(pinnedOnly: Boolean=false, roundDetails: Boolean=true, group: String = GROUP_NOW): String {
        return groups[group]?.toString(pinnedOnly = pinnedOnly,roundDetails=roundDetails) ?: ""
    }
}


class TimeConsumeGroup(val group: String) {

    private val rounds = mutableListOf<TimeConsumeRound>()

    fun currentRound() = rounds.lastOrNull()

    // todo id
    fun addRound(name: String, startRound: Boolean = true) {
        val tcr = TimeConsumeRound(name = name, group = group)
        if (startRound) tcr.start()
        rounds.add(tcr)
    }

    fun addInsertPoint(tag: String) {
        val tcr = currentRound() ?: return
        return tcr.addInsertPoint(tag)
    }

    fun addRangePoint(tag: String): Int {
        val tcr = currentRound() ?: return -1
        return tcr.addRangePoint(tag)
    }

    fun endRangePoint(id: Int) {
        val tcr = currentRound() ?: return
        return tcr.endRangePoint(id)
    }

    fun endRound(): String? {
        return currentRound()?.endRound()
    }


    fun pinRound() {
        currentRound()?.pin()
    }

    fun toString(pinnedOnly: Boolean, roundDetails: Boolean):String {

        val rounds = if (pinnedOnly) rounds.filter { it.pin } else rounds

        val tagConsumes = mutableMapOf<String, MaxMinAverage>()
        val sb = java.lang.StringBuilder()
        var total: Long = 0
        rounds.forEachIndexed { index, tcr ->

            val roundConsume = tcr.consume
            total += tcr.consume

            if (index == 0) {
                sb.append(TITLE_FIX)
                sb.append(group)
                sb.append("耗时统计")
                sb.append(TITLE_FIX)
            }

            if (roundDetails) {
                sb.nextLine()
                sb.append(tcr.toString())
            }
            // tag 统计
            tcr.points.forEach {
                val mma = tagConsumes.getOrPut(it.tag) { MaxMinAverage(it.tag) }
                mma.add(it)
            }

            if (index == rounds.lastIndex) {
                sb.nextLine()
                sb.append("时间点：")
                sb.nextLine()
                val roundCount = rounds.size
                val averageRoundConsume = total / rounds.size
                tagConsumes.forEach {
                    sb.addTab()
                    sb.append(it.value.toString(averageRoundConsume.toInt()))
                    sb.nextLine()
                }

                sb.append("统计次数${roundCount}，总耗时$total，平均耗时$averageRoundConsume")
                sb.nextLine()
                sb.append(TITLE_FIX)
                sb.append("END")
                sb.append(TITLE_FIX)
            }
        }
        return sb.toString()
    }

    override fun toString(): String {
        return toString(pinnedOnly = false, roundDetails = true)
    }

}

private const val TITLE_FIX = "-------------------"

class MaxMinAverage(val tag: String) {

    private val consumes = mutableListOf<ConsumePoint>()

    fun add(point: ConsumePoint) {
        consumes.add(point)
    }

    fun toString(total: Int): String {
        val max = consumes.maxBy { it.consume }
        val min = consumes.minBy { it.consume }
        val av = consumes.map { it.consume }.average()
        return "$tag[max:${max.consume} in ${max.round.name},   min:${min.consume} in ${min.round.name},    average:${
            "%.2f".format(av)
        }， 平均占比 ${"%.2f".format(av / total)}]"
    }
}

sealed class ConsumePoint(val tag: String, val round: TimeConsumeRound) {

    abstract val start: Long
    abstract val end: Long
    private var consumeSet = false
    var consume: Long = 0
        set(value) {
            if (!consumeSet) {
                consumeSet = true
                field = value
            } else {
                throw java.lang.IllegalArgumentException("已计算过")
            }
        }

    // 自动和上一个Auto类型的点连接
    class Insert(tag: String, round: TimeConsumeRound, val startBy: Insert?) :
        ConsumePoint(tag = tag, round = round) {
        override val start: Long = startBy?.end ?: round.start
        override val end: Long = timeNow()

        init {
            consume = end - start
        }
    }

    // 指定范围
    class Range(tag: String, round: TimeConsumeRound) : ConsumePoint(tag = tag, round = round) {
        override val start: Long = timeNow()

        private var _end = 0L
        override val end: Long
            get() = _end

        fun end() {
            if (_end == 0L) {
                _end = timeNow()
                consume = end - start
            }
        }
    }

}

private fun timeNow(): Long = System.currentTimeMillis()

class TimeConsumeRound(
    val name: String,
    val group: String,
) {

    val consume: Long
        get() {
            return end - start
        }

    private var _start: Long = 0
    val start: Long
        get() {
            return _start
        }
    private var end: Long = 0
    private val _points = mutableListOf<ConsumePoint>()
    private var isMath = false

    private var lastInsertPoint: ConsumePoint.Insert? = null

    val points: List<ConsumePoint>
        get() {
            return _points
        }

    var pin: Boolean = false

    fun pin() {
        pin = true
    }

    fun start() {
        checkNotEnd()
        _start = timeNow()
    }

    fun addInsertPoint(tag: String) {
//        checkNotEnd()
        // 创建插入点，起始于上一个插入点的结束时间
        val insert = ConsumePoint.Insert(tag = tag, round = this, startBy = lastInsertPoint)
        _points.add(insert)
        lastInsertPoint = insert
    }

    fun addRangePoint(tag: String): Int {
        checkNotEnd()
        _points.add(ConsumePoint.Range(tag = tag, round = this))
        return _points.lastIndex
    }

    fun endRangePoint(id: Int) {
        checkNotEnd()
        val p = _points.getOrNull(id) ?: return
        if (p is ConsumePoint.Range) {
            p.end()
        }
    }

    fun endRound(): String {
        if (end == 0L) {
            end = timeNow()
        }
        return name
    }

    fun checkNotEnd() {
        if (end > 0L) {
//            throw IllegalArgumentException("已结束，无法添加")
        }
    }

    override fun toString(): String {
        val totalTime = end - _start

        val sb = StringBuilder(name)
        sb.append(":")
        sb.nextLine()
        sb.addTab()
        sb.append("总耗时：$totalTime")
        sb.nextLine()
        var isFirst = true

        sb.addTab()
        _points.filterIsInstance<ConsumePoint.Insert>().forEach {
            val currConsume = it.consume

            // 插入点和范围分开两行

            if (isFirst) {
                sb.append("Insert:")
                isFirst = false
            } else {
                sb.append(", ")
            }
            sb.append(it.tag)
            sb.append(":")
            sb.append(currConsume)
        }
        sb.nextLine()
        sb.addTab()
        isFirst = true
        _points.filterIsInstance<ConsumePoint.Range>().forEach {
            val currConsume = it.consume
            if (isFirst) {
                sb.append("Range:")
                isFirst = false
            } else {
                sb.append(", ")
            }
            sb.append(it.tag)
            sb.append(":")
            sb.append(currConsume)
        }

        return sb.toString()
    }

}

private fun StringBuilder.addTab(count: Int = 1) {
    for (i in 0 until count) {
        append("          ")
    }
}

private fun StringBuilder.nextLine() {
    append('\n')
}