package topgrade.parent.com.parentseeks.Parent.Utils

/**
 * Centralized emoji constants for use throughout the application (Kotlin version).
 * Using emojis programmatically avoids AAPT compilation issues with strings.xml
 */
object Emoji {
    
    // Search & Navigation
    const val SEARCH = "🔍"
    const val ARROW_RIGHT = "➡️"
    const val ARROW_LEFT = "⬅️"
    const val ARROW_UP = "⬆️"
    const val ARROW_DOWN = "⬇️"
    
    // Calendar & Time
    const val CALENDAR = "📅"
    const val CLOCK = "🕐"
    const val ALARM = "⏰"
    
    // Education & School
    const val MEMO = "📝"
    const val BOOKS = "📚"
    const val SCHOOL = "🏫"
    const val GRADUATION = "🎓"
    const val PENCIL = "✏️"
    const val BACKPACK = "🎒"
    const val BOOK = "📖"
    
    // People
    const val PEOPLE = "👥"
    const val TEACHER = "👨‍🏫"
    const val STUDENT = "👨‍🎓"
    const val FAMILY = "👨‍👩‍👧‍👦"
    const val PERSON = "👤"
    
    // Communication
    const val MEGAPHONE = "📢"
    const val BELL = "🔔"
    const val EMAIL = "📧"
    const val PHONE = "📱"
    const val MESSAGE = "💬"
    
    // Status & Alerts
    const val WARNING = "⚠️"
    const val ERROR = "❌"
    const val SUCCESS = "✅"
    const val CHECK_MARK = "✔️"
    const val INFO = "ℹ️"
    const val EXCLAMATION = "❗"
    
    // Documents & Files
    const val DOCUMENT = "📄"
    const val FOLDER = "📁"
    const val CLIPBOARD = "📋"
    const val CHART = "📊"
    
    // Money & Finance
    const val MONEY_BAG = "💰"
    const val DOLLAR = "💵"
    const val CREDIT_CARD = "💳"
    
    // Actions
    const val TARGET = "🎯"
    const val TROPHY = "🏆"
    const val STAR = "⭐"
    const val FIRE = "🔥"
    const val LOCK = "🔒"
    const val UNLOCK = "🔓"
    const val KEY = "🔑"
    
    // Emotions
    const val SMILE = "😊"
    const val THINKING = "🤔"
    const val PARTY = "🎉"
    const val CLAP = "👏"
    
    // Other
    const val HOME = "🏠"
    const val LOCATION = "📍"
    const val SETTINGS = "⚙️"
    const val HELP = "❓"
}

/**
 * Extension function to add emoji prefix to a string
 */
fun String.withEmoji(emoji: String): String = "$emoji $this"

/**
 * Extension function to add emoji suffix to a string
 */
fun String.withEmojiSuffix(emoji: String): String = "$this $emoji"

