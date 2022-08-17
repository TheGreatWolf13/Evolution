package tgw.evolution.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class AllocationRateCalculator {

    private static final List<GarbageCollectorMXBean> GARBAGE_COLLECTORS = ManagementFactory.getGarbageCollectorMXBeans();
    private long allocatedBytes = -1L;
    private long allocationRate;
    private long collectionCount = -1L;
    private long lastCalculated;

    private static long getCollectionCount() {
        long l = 0L;
        for (GarbageCollectorMXBean garbageCollectorMXBean : GARBAGE_COLLECTORS) {
            l += garbageCollectorMXBean.getCollectionCount();
        }
        return l;
    }

    public long get(long allocatedBytes) {
        long l = System.currentTimeMillis();
        if (l - this.lastCalculated < 500L) {
            return this.allocationRate;
        }
        long m = getCollectionCount();
        if (this.lastCalculated != 0L && m == this.collectionCount) {
            double d = 1_000L / (double) (l - this.lastCalculated);
            long n = allocatedBytes - this.allocatedBytes;
            this.allocationRate = Math.round(n * d);
        }
        this.lastCalculated = l;
        this.allocatedBytes = allocatedBytes;
        this.collectionCount = m;
        return this.allocationRate;
    }
}
