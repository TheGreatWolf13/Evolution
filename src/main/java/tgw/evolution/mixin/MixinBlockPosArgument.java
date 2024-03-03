package tgw.evolution.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPosArgument.class)
public abstract class MixinBlockPosArgument implements ArgumentType<Coordinates> {

    @Shadow @Final public static SimpleCommandExceptionType ERROR_NOT_LOADED;

    @Shadow @Final public static SimpleCommandExceptionType ERROR_OUT_OF_WORLD;

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static BlockPos getLoadedBlockPos(CommandContext<CommandSourceStack> context, String string) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        BlockPos pos = context.getArgument(string, Coordinates.class).getBlockPos(source);
        ServerLevel level = source.getLevel();
        int x = pos.getX();
        int z = pos.getZ();
        if (!level.hasChunkAt(x, z)) {
            throw ERROR_NOT_LOADED.create();
        }
        if (!level.isInWorldBounds_(x, pos.getY(), z)) {
            throw ERROR_OUT_OF_WORLD.create();
        }
        return pos;
    }
}
