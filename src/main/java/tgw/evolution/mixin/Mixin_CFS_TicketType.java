package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.longs.LongComparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.*;
import tgw.evolution.patches.PatchTicketType;

import java.util.Comparator;

@Mixin(TicketType.class)
public abstract class Mixin_CFS_TicketType<T> implements PatchTicketType {

    @Mutable @Shadow @Final @RestoreFinal public static TicketType<Unit> START;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<Unit> DRAGON;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<ChunkPos> PLAYER;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<ChunkPos> FORCED;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<ChunkPos> LIGHT;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<BlockPos> PORTAL;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<Integer> POST_TELEPORT;
    @Mutable @Shadow @Final @RestoreFinal public static TicketType<ChunkPos> UNKNOWN;
    @Shadow @Final @DeleteField private Comparator<T> comparator;
    @Unique private LongComparator longComparator;
    @Mutable @Shadow @Final @RestoreFinal private String name;
    @Mutable @Shadow @Final @RestoreFinal private long timeout;

    @ModifyConstructor
    protected Mixin_CFS_TicketType(String string, Comparator<T> comparator, long l) {
        this.name = string;
        this.timeout = l;
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        START = PatchTicketType.create("start", Long::compare, 0);
        DRAGON = PatchTicketType.create("dragon", Long::compare, 0);
        PLAYER = PatchTicketType.create("player", Long::compare, 0);
        FORCED = PatchTicketType.create("forced", Long::compare, 0);
        LIGHT = PatchTicketType.create("light", Long::compare, 0);
        PORTAL = PatchTicketType.create("portal", (a, b) -> {
            int ya = BlockPos.getY(a);
            int yb = BlockPos.getY(b);
            if (ya == yb) {
                int za = BlockPos.getZ(a);
                int zb = BlockPos.getZ(b);
                return za == zb ? BlockPos.getX(a) - BlockPos.getX(b) : za - zb;
            }
            return ya - yb;
        }, 300);
        POST_TELEPORT = PatchTicketType.create("post_teleport", Long::compare, 5);
        UNKNOWN = PatchTicketType.create("unknown", Long::compare, 1);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static <T> TicketType<T> create(String string, Comparator<T> comparator, int i) {
        throw new RuntimeException("Deprecated. Call the method in PatchTicketType!");
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static <T> TicketType<T> create(String string, Comparator<T> comparator) {
        throw new RuntimeException("Deprecated. Call the method in PatchTicketType!");
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static int method_17315(Unit par1, Unit par2) {
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static int method_17316(Unit par1, Unit par2) {
        throw new AbstractMethodError();
    }

    @Override
    public void _setLongComparator(LongComparator comparator) {
        this.longComparator = comparator;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    public Comparator<T> getComparator() {
        throw new AbstractMethodError();
    }

    @Override
    public LongComparator longComparator() {
        return this.longComparator;
    }
}
