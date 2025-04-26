package tgw.evolution.patches.replace.net.minecraft.client.searchtree;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntComparator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.OLinkedHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public class SuffixArray<T> {
    private static final boolean DEBUG_COMPARISONS = Boolean.parseBoolean(System.getProperty("SuffixArray.printComparisons", "false"));
    private static final boolean DEBUG_ARRAY = Boolean.parseBoolean(System.getProperty("SuffixArray.printArray", "false"));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int END_OF_TEXT_MARKER = -1;
    private static final int END_OF_DATA = -2;
    private final IList chars = new IArrayList();
    protected final OList<T> list = new OArrayList<>();
    private int maxStringLength;
    private IList offsets = new IArrayList();
    private IList suffixToT = new IArrayList();
    private final IList wordStarts = new IArrayList();

    public void add(T object, String string) {
        this.maxStringLength = Math.max(this.maxStringLength, string.length());
        int i = this.list.size();
        this.list.add(object);
        this.wordStarts.add(this.chars.size());
        for (int j = 0; j < string.length(); ++j) {
            this.suffixToT.add(i);
            this.offsets.add(j);
            this.chars.add(string.charAt(j));
        }
        this.suffixToT.add(i);
        this.offsets.add(string.length());
        this.chars.add(END_OF_TEXT_MARKER);
    }

    private int compare(String string, int i) {
        int j = this.wordStarts.getInt(this.suffixToT.getInt(i));
        int k = this.offsets.getInt(i);
        for (int l = 0; l < string.length(); ++l) {
            char d;
            int m = this.chars.getInt(j + k + l);
            if (m == -1) {
                return 1;
            }
            char c = string.charAt(l);
            if (c < (d = (char) m)) {
                return -1;
            }
            if (c <= d) {
                continue;
            }
            return 1;
        }
        return 0;
    }

    public void generate() {
        int i = this.chars.size();
        int[] is = new int[i];
        int[] js = new int[i];
        int[] ks = new int[i];
        int[] ls = new int[i];
        IntComparator intComparator = (ix, jx) -> js[ix] == js[jx] ? Integer.compare(ks[ix], ks[jx]) : Integer.compare(js[ix], js[jx]);
        Swapper swapper = (ix, jx) -> {
            if (ix != jx) {
                int k = js[ix];
                js[ix] = js[jx];
                js[jx] = k;
                k = ks[ix];
                ks[ix] = ks[jx];
                ks[jx] = k;
                k = ls[ix];
                ls[ix] = ls[jx];
                ls[jx] = k;
            }
        };
        int j;
        for (j = 0; j < i; ++j) {
            is[j] = this.chars.getInt(j);
        }
        j = 1;
        for (int k = Math.min(i, this.maxStringLength); j * 2 < k; j *= 2) {
            int l;
            for (l = 0; l < i; ls[l] = l++) {
                js[l] = is[l];
                ks[l] = l + j < i ? is[l + j] : END_OF_DATA;
            }
            it.unimi.dsi.fastutil.Arrays.quickSort(0, i, intComparator, swapper);
            for (l = 0; l < i; ++l) {
                if (l > 0 && js[l] == js[l - 1] && ks[l] == ks[l - 1]) {
                    is[ls[l]] = is[ls[l - 1]];
                }
                else {
                    is[ls[l]] = l;
                }
            }
        }
        IList suffixToT = this.suffixToT;
        IList offsets = this.offsets;
        this.suffixToT = new IArrayList(suffixToT.size());
        this.offsets = new IArrayList(offsets.size());
        for (int m = 0; m < i; ++m) {
            int n = ls[m];
            this.suffixToT.add(suffixToT.getInt(n));
            this.offsets.add(offsets.getInt(n));
        }
        if (DEBUG_ARRAY) {
            this.print();
        }
    }

    private String getString(int i) {
        int j = this.offsets.getInt(i);
        int k = this.wordStarts.getInt(this.suffixToT.getInt(i));
        StringBuilder stringBuilder = new StringBuilder();
        for (int l = 0; k + l < this.chars.size(); ++l) {
            if (l == j) {
                stringBuilder.append('^');
            }
            int m = this.chars.getInt(k + l);
            if (m == END_OF_TEXT_MARKER) {
                break;
            }
            stringBuilder.append((char) m);
        }
        return stringBuilder.toString();
    }

    private void print() {
        for (int i = 0; i < this.suffixToT.size(); ++i) {
            LOGGER.debug("{} {}", i, this.getString(i));
        }
        LOGGER.debug("");
    }

    public OList<T> search(String string) {
        int m;
        int l;
        int i = this.suffixToT.size();
        int j = 0;
        int k = i;
        while (j < k) {
            l = j + (k - j) / 2;
            m = this.compare(string, l);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing lower \"{}\" with {} \"{}\": {}", string, l, this.getString(l), m);
            }
            if (m > 0) {
                j = l + 1;
                continue;
            }
            k = l;
        }
        if (j < 0 || j >= i) {
            return OList.emptyList();
        }
        l = j;
        k = i;
        while (j < k) {
            m = j + (k - j) / 2;
            int n = this.compare(string, m);
            if (DEBUG_COMPARISONS) {
                LOGGER.debug("comparing upper \"{}\" with {} \"{}\": {}", string, m, this.getString(m), n);
            }
            if (n >= 0) {
                j = m + 1;
                continue;
            }
            k = m;
        }
        m = j;
        ISet intSet = new IHashSet();
        for (int o = l; o < m; ++o) {
            intSet.add(this.suffixToT.getInt(o));
        }
        int[] is = intSet.toIntArray();
        Arrays.sort(is);
        OSet<T> set = new OLinkedHashSet<>();
        for (int p : is) {
            set.add(this.list.get(p));
        }
        return new OArrayList<>(set);
    }
}
