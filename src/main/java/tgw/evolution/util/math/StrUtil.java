package tgw.evolution.util.math;

import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.sets.OSet;

import java.text.Collator;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class StrUtil {

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private StrUtil() {}

    /**
     * Compares two strings, ignoring case and accentuation.
     */
    public static int compare(String a, String b) {
        Collator collator = Collator.getInstance(Locale.ROOT);
        collator.setStrength(Collator.PRIMARY);
        return collator.compare(a, b);
    }

    /**
     * Whether string a contains string b, ignoring case and accentuation.
     */
    public static boolean contains(String a, String b, @Nullable StringBuilder builder) {
        StringBuilder sb = builder == null ? new StringBuilder() : builder;
        return stripAccents(a, sb).toLowerCase(Locale.ROOT).contains(stripAccents(b, sb).toLowerCase(Locale.ROOT));
    }

    private static void convertRemainingAccentCharacters(StringBuilder decomposed) {
        for (int i = 0; i < decomposed.length(); ++i) {
            if (decomposed.charAt(i) == 321) {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'L');
            }
            else if (decomposed.charAt(i) == 322) {
                decomposed.deleteCharAt(i);
                decomposed.insert(i, 'l');
            }
        }
    }

    public static String join(String delimiter, OSet<String> set) {
        if (set.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (long it = set.beginIteration(); set.hasNextIteration(it); it = set.nextEntry(it)) {
            if (first) {
                first = false;
            }
            else {
                builder.append(delimiter);
            }
            builder.append(set.getIteration(it));
        }
        return builder.toString();
    }

    public static String stripAccents(String input, @Nullable StringBuilder builder) {
        StringBuilder decomposed;
        if (builder == null) {
            decomposed = new StringBuilder(Normalizer.normalize(input, Normalizer.Form.NFD));
        }
        else {
            decomposed = builder;
            builder.setLength(0);
            builder.append(Normalizer.normalize(input, Normalizer.Form.NFD));
        }
        convertRemainingAccentCharacters(decomposed);
        return DIACRITICAL_MARKS.matcher(decomposed).replaceAll("");
    }
}
