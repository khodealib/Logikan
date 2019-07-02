package ir.logicfan.core.util.calendar

import android.annotation.SuppressLint
import ir.logicfan.core.util.Clock
import java.io.Serializable
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*

data class JalaliCalendar(var year: Int, var month: MonthPersian, var day: Int, var clock: Clock) : Serializable {

    init {
        basicArgumentValidating(year, month.value, day)
    }

    val isLeapYear: Boolean
        get() = DateConverter().isJalaliLeapYear(year)
    val dayOfWeek: DayOfWeekPersian by lazy {
        DayOfWeekPersian.of(toGregorian().get(Calendar.DAY_OF_WEEK))
    }
    val yearLength: Int
        get() = if (isLeapYear) 366 else 365
    val monthString: String = month.toPersian
    val dayOfWeekString: String by lazy {
        dayOfWeek.toPersian
    }
    val monthLength: Int = MonthPersian.getMonthLength(month, isLeapYear)

    /**
     * Now as Jalali Date
     */
    constructor() : this(DateConverter().gregorianToJalali(GregorianCalendar()))

    constructor(jalali: JalaliCalendar) : this(jalali.year, jalali.month.value, jalali.day, jalali.clock)

    constructor(gregorian: GregorianCalendar) : this(DateConverter().gregorianToJalali(gregorian))

    constructor(year: Int, month: Int, day: Int) : this(year, month, day, Clock())

    constructor(year: Int, monthPersian: MonthPersian, day: Int) : this(year, monthPersian.value, day)

    constructor(year: Int, month: Int, day: Int, clock: Clock) : this(year, MonthPersian.of(month), day, clock)

    /**
     * validate primary constructor inputs
     */
    private fun basicArgumentValidating(year: Int, month: Int, day: Int) {
        if (day <= 0 || day > 31)
            throw IllegalArgumentException("Wrong value for day, it must be from 1 to 31")

        if (year <= 0)
            throw IllegalArgumentException("Wrong value for Year, it must be positive ")

        if (month >= 7 && day == 31)
            throw IllegalArgumentException("Wrong value for day. in second half of year, months have less than 31 days ")

        if (!isLeapYear && month == 12 && day >= 30)
            throw IllegalArgumentException("Wrong value for day, just in leap year last month must be greater than 29")
    }

    /**
     * convert current object to gregorian calendar
     */
    fun toGregorian(): GregorianCalendar = DateConverter().jalaliToGregorian(this)

    /**
     * Convert current calendar to Unix Epoch timestamp
     */
    @SuppressLint("SimpleDateFormat")
    fun toUnixTimestamp(): String {
        val gregorian = toGregorian()
        val year = gregorian.get(Calendar.YEAR)
        val month = gregorian.get(Calendar.MONTH) + 1 // gregorian calendar is zero based
        val day = gregorian.get(Calendar.DAY_OF_MONTH)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        val date = dateFormat.parse("$year-$month-$day-${clock.hour}-${clock.minute}-${clock.second}")
        return (date.time / 1000).toString()
    }

    /**
     * type 1 is the following format:
     * 12 اردیبهشت 1395
     */
    private fun getDateLabelLetterMonth(): String = MessageFormat.format(
        "{0} {1} {2}",
        this.day,
        this.monthString,
        this.year
    )

    /**
     * type 2 is the following format:
     * پنجشنبه 12 تیر 1398
     */
    private fun getDateLabelLetterMonthPlusDay(): String = MessageFormat.format(
        "{0} {1} {2} {3}",
        this.dayOfWeekString,
        this.day,
        this.monthString,
        this.year
    )

    /**
     * type 3 is the following format:
     * 1398/04/12
     */
    private fun getDateLabelDigit(): String = MessageFormat.format(
        "{0}/{1}/{2}",
        this.year,
        this.month.value,
        this.day
    )

    /**
     * To be used by [getDateLabel] method to return suitable labeled jalali calendar
     */
    enum class JalaliLabelType {
        LETTER_MONTH, LETTER_MONTH_PLUS_DAY, DIGIT
    }

    companion object {

        /**
         * Get current time as Unix epoch timestamp
         */
        @JvmStatic
        fun currentUnixTimestamp(): Long = System.currentTimeMillis() / 1000

        @JvmStatic
        fun getDateLabel(jalaliCalendar: JalaliCalendar, type: JalaliLabelType): String = when (type) {
            JalaliLabelType.LETTER_MONTH -> jalaliCalendar.getDateLabelLetterMonth()
            JalaliLabelType.LETTER_MONTH_PLUS_DAY -> jalaliCalendar.getDateLabelLetterMonthPlusDay()
            JalaliLabelType.DIGIT -> jalaliCalendar.getDateLabelDigit()
        }

        /**
         * @param timestamp Unix Timestamp with this format : 1561783493
         * @return [JalaliCalendar] object is timestamp is correct or null otherwise
         */
        @SuppressLint("SimpleDateFormat")
        @JvmStatic
        fun getJalaliCalendar(timestamp: String): JalaliCalendar? = try {
            val date = Date(timestamp.toLong() * 1000L)
            val formatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
            val dateTimeParts = formatted.split(" ")
            val dateParts = dateTimeParts[0].split("-")
            val timeParts = dateTimeParts[1].split(":")
            val gregorian = GregorianCalendar(
                dateParts[0].toInt(), // year
                dateParts[1].toInt() - 1, // month (gregorian month is zero based)
                dateParts[2].toInt(), // day of month
                timeParts[0].toInt(), // hour of day
                timeParts[1].toInt(), // minute
                timeParts[2].toInt() // second
            )
            JalaliCalendar(gregorian)
        } catch (ex: NumberFormatException) {
            // string does not contain timestamp
            null
        }
    }
}