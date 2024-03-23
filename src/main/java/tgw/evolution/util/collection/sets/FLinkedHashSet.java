package tgw.evolution.util.collection.sets;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.floats.FloatLinkedOpenHashSet;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import tgw.evolution.Evolution;

import java.util.NoSuchElementException;

public class FLinkedHashSet extends FloatLinkedOpenHashSet implements FSet {

    protected @Nullable View view;

    @Override
    public long beginIteration() {
        if (this.isEmpty()) {
            return -1;
        }
        return this.first;
    }

    @Override
    public Float first() {
        Evolution.deprecatedMethod();
        return super.first();
    }

    @Override
    public float getIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new NoSuchElementException();
        }
        return this.key[curr];
    }

    @Override
    public float getSampleElement() {
        return this.firstFloat();
    }

    @Override
    public boolean hasNextIteration(long it) {
        return (int) it != -1;
    }

    @Override
    public FloatListIterator iterator() {
        this.deprecatedMethod();
        return super.iterator();
    }

    @Override
    public Float last() {
        Evolution.deprecatedMethod();
        return super.last();
    }

    @Override
    public long nextEntry(long it) {
        int curr = (int) it;
        if ((it & 1L << 63) != 0) {
            return curr;
        }
        return (int) this.link[curr];
    }

    @Override
    public long removeIteration(long it) {
        int curr = (int) it;
        if (curr == -1) {
            throw new IllegalStateException();
        }
        long[] link = this.link;
        int next = (int) link[curr];
        int prev = (int) (link[curr] >>> 32);
        --this.size;
        //Fix pointers
        if (prev == -1) {
            this.first = next;
        }
        else {
            link[prev] ^= (link[prev] ^ next & 0xFFFF_FFFFL) & 0xFFFF_FFFFL;
        }
        if (next == -1) {
            this.last = prev;
        }
        else {
            link[next] ^= (link[next] ^ (prev & 0xFFFF_FFFFL) << 32) & 0xFFFF_FFFF_0000_0000L;
        }
        //Actually remove
        if (curr == this.n) {
            this.containsNull = false;
            this.key[this.n] = 0.0f;
        }
        else {
            float[] key = this.key;
            while (true) {
                int last = curr;
                curr = curr + 1 & this.mask;
                float currKey;
                while (true) {
                    if (Float.floatToIntBits(currKey = key[curr]) == 0) {
                        key[last] = 0.0f;
                        return next | 1L << 63;
                    }
                    int slot = HashCommon.mix(HashCommon.float2int(currKey)) & this.mask;
                    if (last <= curr) {
                        if (last >= slot || slot > curr) {
                            break;
                        }
                    }
                    else if (last >= slot && slot > curr) {
                        break;
                    }
                    curr = curr + 1 & this.mask;
                }
                key[last] = currKey;
                if (next == curr) {
                    next = last;
                }
                this.fixPointers(curr, last);
            }
        }
        return next | 1L << 63;
    }

    @Override
    public @UnmodifiableView FSet view() {
        if (this.view == null) {
            this.view = new View(this);
        }
        return this.view;
    }
}
