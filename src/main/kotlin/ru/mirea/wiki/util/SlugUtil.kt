package ru.mirea.wiki.util

object SlugUtil {

    private val translit: Map<Char, String> = mapOf(
        'а' to "a", 'б' to "b", 'в' to "v", 'г' to "g", 'д' to "d", 'е' to "e", 'ё' to "e",
        'ж' to "zh", 'з' to "z", 'и' to "i", 'й' to "y", 'к' to "k", 'л' to "l", 'м' to "m",
        'н' to "n", 'о' to "o", 'п' to "p", 'р' to "r", 'с' to "s", 'т' to "t", 'у' to "u",
        'ф' to "f", 'х' to "h", 'ц' to "ts", 'ч' to "ch", 'ш' to "sh", 'щ' to "sch", 'ъ' to "",
        'ы' to "y", 'ь' to "", 'э' to "e", 'ю' to "yu", 'я' to "ya"
    )

    fun slugify(text: String): String {
        val sb = StringBuilder()
        for (ch in text.lowercase()) {
            when {
                ch in 'a'..'z' || ch in '0'..'9' -> sb.append(ch)
                translit.containsKey(ch) -> sb.append(translit[ch])
                else -> sb.append('-')
            }
        }
        val slug = sb.toString().replace(Regex("-+"), "-").trim('-')
        return if (slug.isEmpty()) "article" else slug.take(100)
    }
}
