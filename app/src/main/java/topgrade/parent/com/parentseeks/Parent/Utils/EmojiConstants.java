package topgrade.parent.com.parentseeks.Parent.Utils;

/**
 * Centralized emoji constants for use throughout the application.
 * Using emojis programmatically avoids AAPT compilation issues with strings.xml
 */
public class EmojiConstants {
    
    // Search & Navigation
    public static final String SEARCH = "🔍";
    public static final String ARROW_RIGHT = "➡️";
    public static final String ARROW_LEFT = "⬅️";
    public static final String ARROW_UP = "⬆️";
    public static final String ARROW_DOWN = "⬇️";
    
    // Calendar & Time
    public static final String CALENDAR = "📅";
    public static final String CLOCK = "🕐";
    public static final String ALARM = "⏰";
    
    // Education & School
    public static final String MEMO = "📝";
    public static final String BOOKS = "📚";
    public static final String SCHOOL = "🏫";
    public static final String GRADUATION = "🎓";
    public static final String PENCIL = "✏️";
    public static final String BACKPACK = "🎒";
    public static final String BOOK = "📖";
    
    // People
    public static final String PEOPLE = "👥";
    public static final String TEACHER = "👨‍🏫";
    public static final String STUDENT = "👨‍🎓";
    public static final String FAMILY = "👨‍👩‍👧‍👦";
    public static final String PERSON = "👤";
    
    // Communication
    public static final String MEGAPHONE = "📢";
    public static final String BELL = "🔔";
    public static final String EMAIL = "📧";
    public static final String PHONE = "📱";
    public static final String MESSAGE = "💬";
    
    // Status & Alerts
    public static final String WARNING = "⚠️";
    public static final String ERROR = "❌";
    public static final String SUCCESS = "✅";
    public static final String CHECK_MARK = "✔️";
    public static final String INFO = "ℹ️";
    public static final String EXCLAMATION = "❗";
    
    // Documents & Files
    public static final String DOCUMENT = "📄";
    public static final String FOLDER = "📁";
    public static final String CLIPBOARD = "📋";
    public static final String CHART = "📊";
    
    // Money & Finance
    public static final String MONEY_BAG = "💰";
    public static final String DOLLAR = "💵";
    public static final String CREDIT_CARD = "💳";
    
    // Actions
    public static final String TARGET = "🎯";
    public static final String TROPHY = "🏆";
    public static final String STAR = "⭐";
    public static final String FIRE = "🔥";
    public static final String LOCK = "🔒";
    public static final String UNLOCK = "🔓";
    public static final String KEY = "🔑";
    
    // Emotions
    public static final String SMILE = "😊";
    public static final String THINKING = "🤔";
    public static final String PARTY = "🎉";
    public static final String CLAP = "👏";
    
    // Other
    public static final String HOME = "🏠";
    public static final String LOCATION = "📍";
    public static final String SETTINGS = "⚙️";
    public static final String HELP = "❓";
    
    /**
     * Helper method to add emoji prefix to a string
     * @param emoji The emoji to add
     * @param text The text to prefix
     * @return Formatted string with emoji
     */
    public static String withEmoji(String emoji, String text) {
        return emoji + " " + text;
    }
    
    /**
     * Helper method to add emoji suffix to a string
     * @param text The text
     * @param emoji The emoji to add
     * @return Formatted string with emoji at end
     */
    public static String withEmojiSuffix(String text, String emoji) {
        return text + " " + emoji;
    }
}

