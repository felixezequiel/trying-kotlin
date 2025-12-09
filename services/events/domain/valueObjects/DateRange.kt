package events.domain.valueObjects

import java.time.Instant

data class DateRange private constructor(val start: Instant, val end: Instant) {
    init {
        require(end.isAfter(start)) { "Data de término deve ser após a data de início" }
    }

    companion object {
        fun of(start: Instant, end: Instant): DateRange = DateRange(start, end)

        fun future(start: Instant, end: Instant): DateRange {
            require(start.isAfter(Instant.now())) { "Data de início deve ser futura" }
            return DateRange(start, end)
        }

        fun fromStrings(startDate: String, endDate: String): DateRange {
            val start = Instant.parse(startDate)
            val end = Instant.parse(endDate)
            return future(start, end)
        }
    }

    fun contains(instant: Instant): Boolean = instant.isAfter(start) && instant.isBefore(end)

    fun hasStarted(): Boolean = Instant.now().isAfter(start)

    fun hasEnded(): Boolean = Instant.now().isAfter(end)
}
