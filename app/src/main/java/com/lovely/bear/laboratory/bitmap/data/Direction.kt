package com.lovely.bear.laboratory.bitmap.data


sealed class Direction(  val d: Int) : Comparable<Direction> {
    class LEFT(d: Int) : Direction(d)
    class TOP(d: Int) : Direction(d)
    class RIGHT(d: Int) : Direction(d)
    class BOTTOM(d: Int) : Direction(d)

    override fun compareTo(other: Direction): Int {
        return this.d.compareTo(other.d)
    }
}

sealed class CornerDirection(  val d: Int) : Comparable<CornerDirection> {
    class LeftTop(d: Int) : CornerDirection(d)
    class RightTop(d: Int) : CornerDirection(d)
    class RightBottom(d: Int) : CornerDirection(d)
    class LeftBottom(d: Int) : CornerDirection(d)

    override fun compareTo(other: CornerDirection): Int {
        return this.d.compareTo(other.d)
    }
}