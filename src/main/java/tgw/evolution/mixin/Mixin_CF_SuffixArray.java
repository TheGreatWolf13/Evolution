package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.Arrays;
import it.unimi.dsi.fastutil.Swapper;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.searchtree.SuffixArray;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.OLinkedHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.List;

@Mixin(SuffixArray.class)
public abstract class Mixin_CF_SuffixArray<T> {

    @Shadow @Final private static boolean DEBUG_ARRAY;
    @Shadow @Final private static boolean DEBUG_COMPARISONS;
    @DeleteField @Shadow @Final private IntList chars;
    @Unique private final IList chars_;
    @DeleteField @Shadow @Final protected List<T> list;
    @Unique private final OList<T> list_;
    @Shadow private int maxStringLength;
    @DeleteField @Shadow private IntList offsets;
    @Unique private IList offsets_;
    @DeleteField @Shadow private IntList suffixToT;
    @Unique private IList suffixToT_;
    @DeleteField @Shadow @Final private IntList wordStarts;
    @Unique private final IList wordStarts_;

    @ModifyConstructor
    public Mixin_CF_SuffixArray() {
        this.list_ = new OArrayList<>();
        this.chars_ = new IArrayList();
        this.wordStarts_ = new IArrayList();
        this.suffixToT_ = new IArrayList();
        this.offsets_ = new IArrayList();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void add(T object, String string) {
        this.maxStringLength = Math.max(this.maxStringLength, string.length());
        int i = this.list_.size();
        this.list_.add(object);
        this.wordStarts_.add(this.chars_.size());
        for (int j = 0; j < string.length(); ++j) {
            this.suffixToT_.add(i);
            this.offsets_.add(j);
            this.chars_.add(string.charAt(j));
        }
        this.suffixToT_.add(i);
        this.offsets_.add(string.length());
        this.chars_.add(-1);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private int compare(String string, int i) {
        int j = this.wordStarts_.getInt(this.suffixToT_.getInt(i));
        int k = this.offsets_.getInt(i);
        for (int l = 0; l < string.length(); ++l) {
            int m = this.chars_.getInt(j + k + l);
            if (m == -1) {
                return 1;
            }
            char c = string.charAt(l);
            char d = (char) m;
            if (c < d) {
                return -1;
            }
            if (c > d) {
                return 1;
            }
        }
        return 0;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void generate() {
        int i = this.chars_.size();
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
            is[j] = this.chars_.getInt(j);
        }
        j = 1;
        for (int k = Math.min(i, this.maxStringLength); j * 2 < k; j *= 2) {
            int l;
            for (l = 0; l < i; ls[l] = l++) {
                js[l] = is[l];
                ks[l] = l + j < i ? is[l + j] : -2;
            }
            Arrays.quickSort(0, i, intComparator, swapper);
            for (l = 0; l < i; ++l) {
                if (l > 0 && js[l] == js[l - 1] && ks[l] == ks[l - 1]) {
                    is[ls[l]] = is[ls[l - 1]];
                }
                else {
                    is[ls[l]] = l;
                }
            }
        }
        IList suffixToT = this.suffixToT_;
        IList offsets = this.offsets_;
        this.suffixToT_ = new IArrayList(suffixToT.size());
        this.offsets_ = new IArrayList(offsets.size());
        for (int m = 0; m < i; ++m) {
            int n = ls[m];
            this.suffixToT_.add(suffixToT.getInt(n));
            this.offsets_.add(offsets.getInt(n));
        }
        if (DEBUG_ARRAY) {
            this.print();
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private String getString(int i) {
        int j = this.offsets_.getInt(i);
        int k = this.wordStarts_.getInt(this.suffixToT_.getInt(i));
        StringBuilder stringBuilder = new StringBuilder();
        for (int l = 0; k + l < this.chars_.size(); ++l) {
            if (l == j) {
                stringBuilder.append('^');
            }
            int m = this.chars_.getInt(k + l);
            if (m == -1) {
                break;
            }
            stringBuilder.append((char) m);
        }
        return stringBuilder.toString();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void print() {
        for (int i = 0; i < this.suffixToT_.size(); ++i) {
            Evolution.debug("{} {}", i, this.getString(i));
        }
        Evolution.debug("");
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public List<T> search(String string) {
        int i = this.suffixToT_.size();
        int j = 0;
        int k = i;
        int l;
        int m;
        while (j < k) {
            l = j + (k - j) / 2;
            m = this.compare(string, l);
            if (DEBUG_COMPARISONS) {
                Evolution.debug("comparing lower \"{}\" with {} \"{}\": {}", string, l, this.getString(l), m);
            }
            if (m > 0) {
                j = l + 1;
            }
            else {
                k = l;
            }
        }
        if (j >= 0 && j < i) {
            l = j;
            k = i;
            while (j < k) {
                m = j + (k - j) / 2;
                int n = this.compare(string, m);
                if (DEBUG_COMPARISONS) {
                    Evolution.debug("comparing upper \"{}\" with {} \"{}\": {}", string, m, this.getString(m), n);
                }
                if (n >= 0) {
                    j = m + 1;
                }
                else {
                    k = m;
                }
            }
            m = j;
            ISet intSet = new IHashSet();
            for (int o = l; o < m; ++o) {
                intSet.add(this.suffixToT_.getInt(o));
            }
            int[] is = intSet.toIntArray();
            java.util.Arrays.sort(is);
            OSet<T> set = new OLinkedHashSet<>();
            for (int p : is) {
                set.add(this.list.get(p));
            }
            return new OArrayList<>(set);
        }
        return OList.emptyList();
    }
}
