package tgw.evolution.world;

import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;

public final class Tickets {

    public static final TicketType<ChunkPos> CAMERA = TicketType.create("camera", Comparator.comparingLong(ChunkPos::toLong), 200);

    private Tickets() {}
}
