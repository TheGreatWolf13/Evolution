package tgw.evolution.util.collection;

public class L2OPair<R> {

    public final long l;
    public final R r;

    public L2OPair(long l, R r) {
        this.l = l;
        this.r = r;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        L2OPair<?> l2OPair = (L2OPair<?>) o;
        if (this.l != l2OPair.l) {
            return false;
        }
        return this.r.equals(l2OPair.r);
    }

    @Override
    public int hashCode() {
        int result = (int) (this.l ^ this.l >>> 32);
        result = 31 * result + this.r.hashCode();
        return result;
    }
}
