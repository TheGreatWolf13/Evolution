package tgw.evolution.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import tgw.evolution.capabilities.chunk.IntegrityStorage;
import tgw.evolution.capabilities.chunk.StabilityStorage;
import tgw.evolution.network.PacketSCLoadFactor;
import tgw.evolution.util.collection.maps.L2OHashMap;
import tgw.evolution.util.collection.maps.L2OMap;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;

import java.lang.ref.WeakReference;
import java.util.List;

public final class CommandLoadFactor {

    private static final ISet HELPER_SET = new IHashSet();
    private static final ISet PLAYERS = new IHashSet();
    private static final L2OMap<WeakReference<LevelChunkSection>> SECTIONS = new L2OHashMap<>();

    private CommandLoadFactor() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("loadfactor")
                                    .requires(cs -> cs.hasPermission(2))
                                    .then(Commands.literal("clear")
                                                  .executes(CommandLoadFactor::clear)
                                    )
                                    .then(Commands.literal("leave")
                                                  .requires(cs -> cs.getEntity() instanceof Player)
                                                  .executes(CommandLoadFactor::leave)
                                    )
                                    .then(Commands.literal("join")
                                                  .requires(cs -> cs.getEntity() instanceof Player)
                                                  .executes(CommandLoadFactor::join)
                                    )
                                    .then(Commands.literal("add")
                                                  .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                                .executes(CommandLoadFactor::add)
                                                  )
                                    )
                                    .then(Commands.literal("remove")
                                                  .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                                                .executes(CommandLoadFactor::remove)
                                                  )
                                    )
        );
    }

    public static void tick(PlayerList playerList) {
        if (PLAYERS.isEmpty() || SECTIONS.isEmpty()) {
            return;
        }
        List<ServerPlayer> players = playerList.getPlayers();
        for (int i = 0, len = players.size(); i < len; ++i) {
            HELPER_SET.add(players.get(i).getId());
        }
        for (long it = PLAYERS.beginIteration(); PLAYERS.hasNextIteration(it); it = PLAYERS.nextEntry(it)) {
            int i = PLAYERS.getIteration(it);
            if (!HELPER_SET.contains(i)) {
                it = PLAYERS.removeIteration(it);
            }
        }
        HELPER_SET.clear();
        if (PLAYERS.isEmpty()) {
            return;
        }
        for (long it = SECTIONS.beginIteration(); SECTIONS.hasNextIteration(it); it = SECTIONS.nextEntry(it)) {
            long pos = SECTIONS.getIterationKey(it);
            LevelChunkSection section = SECTIONS.getIterationValue(it).get();
            if (section == null) {
                //noinspection ObjectAllocationInLoop
                sendToPlayers(playerList, new PacketSCLoadFactor(pos));
                SECTIONS.remove(pos);
            }
            else {
                IntegrityStorage loadFactorStorage = section.getLoadFactorStorage();
                IntegrityStorage integrityStorage = section.getIntegrityStorage();
                StabilityStorage stabilityStorage = section.getStabilityStorage();
                boolean loadChanged = loadFactorStorage.hadChanges();
                boolean intChanged = integrityStorage.hadChanges();
                boolean stabChanged = stabilityStorage.hadChanges();
                if (loadChanged || intChanged || stabChanged) {
                    //noinspection ObjectAllocationInLoop
                    sendToPlayers(playerList, new PacketSCLoadFactor(pos, loadFactorStorage, integrityStorage, stabilityStorage));
                }
            }
        }
    }

    private static int add(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        CommandSourceStack source = context.getSource();
        PlayerList playerList = source.getServer().getPlayerList();
        LevelChunk chunk = source.getLevel().getChunk(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
        if (source.getEntity() instanceof Player player) {
            PLAYERS.add(player.getId());
        }
        long sec = SectionPos.asLong(pos);
        if (chunk.isEmpty()) {
            sendToPlayers(playerList, new PacketSCLoadFactor(sec));
            SECTIONS.remove(sec);
            return 0;
        }
        LevelChunkSection section = chunk.getSection(chunk.getSectionIndex(pos.getY()));
        sendToPlayers(playerList, new PacketSCLoadFactor(sec, section.getLoadFactorStorage(), section.getIntegrityStorage(), section.getStabilityStorage()));
        SECTIONS.put(sec, new WeakReference<>(section));
        return 1;
    }

    private static int clear(CommandContext<CommandSourceStack> context) {
        sendToPlayers(context.getSource().getServer().getPlayerList(), new PacketSCLoadFactor());
        PLAYERS.clear();
        SECTIONS.clear();
        return 1;
    }

    private static int join(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (PLAYERS.add(player.getId())) {
            for (long it = SECTIONS.beginIteration(); SECTIONS.hasNextIteration(it); it = SECTIONS.nextEntry(it)) {
                long pos = SECTIONS.getIterationKey(it);
                LevelChunkSection value = SECTIONS.getIterationValue(it).get();
                if (value != null) {
                    //noinspection ObjectAllocationInLoop
                    player.connection.send(new PacketSCLoadFactor(pos, value.getLoadFactorStorage(), value.getIntegrityStorage(), value.getStabilityStorage()));
                }
            }
            return 1;
        }
        return 0;
    }

    private static int leave(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        if (PLAYERS.remove(player.getId())) {
            if (PLAYERS.isEmpty()) {
                SECTIONS.clear();
            }
            player.connection.send(new PacketSCLoadFactor());
            return 1;
        }
        return 0;
    }

    private static int remove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        long sec = SectionPos.asLong(BlockPosArgument.getLoadedBlockPos(context, "pos"));
        sendToPlayers(context.getSource().getServer().getPlayerList(), new PacketSCLoadFactor(sec));
        SECTIONS.remove(sec);
        return 1;
    }

    private static void sendToPlayers(PlayerList list, Packet<ClientGamePacketListener> packet) {
        List<ServerPlayer> players = list.getPlayers();
        for (int i = 0, len = players.size(); i < len; ++i) {
            ServerPlayer p = players.get(i);
            if (PLAYERS.contains(p.getId())) {
                p.connection.send(packet);
            }
        }
    }
}
