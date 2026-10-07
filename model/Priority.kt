package model

enum class Priority(val level: Int, val title: String) {
    VERY_LOW(1, "очень низкий"),
    LOW(2, "низкий"),
    MEDIUM(3, "средний"),
    HIGH(4, "высокий"),
    CRITICAL(5, "критический");

    companion object {
        val MIN_LEVEL = 1
        val MAX_LEVEL = 5

        fun fromLevel(level: Int): Priority? = values().find { it.level == level }
    }
}
