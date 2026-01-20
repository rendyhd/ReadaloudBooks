package com.pekempy.ReadAloudbooks.data.local

import androidx.room.TypeConverter
import com.pekempy.ReadAloudbooks.data.local.entities.*

class Converters {
    @TypeConverter
    fun fromReadingStatus(value: ReadingStatus): String {
        return value.name
    }

    @TypeConverter
    fun toReadingStatus(value: String): ReadingStatus {
        return ReadingStatus.valueOf(value)
    }

    @TypeConverter
    fun fromGoalType(value: GoalType): String {
        return value.name
    }

    @TypeConverter
    fun toGoalType(value: String): GoalType {
        return GoalType.valueOf(value)
    }

    @TypeConverter
    fun fromPeriodType(value: PeriodType): String {
        return value.name
    }

    @TypeConverter
    fun toPeriodType(value: String): PeriodType {
        return PeriodType.valueOf(value)
    }
}
