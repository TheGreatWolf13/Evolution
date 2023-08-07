package tgw.evolution.commands.vanilla;

import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.server.commands.FillCommand;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

public enum FillMode {
    REPLACE,
    OUTLINE,
    HOLLOW,
    DESTROY;

    public static FillMode fromVanilla(FillCommand.Mode mode) {
        return switch (mode) {
            case HOLLOW -> HOLLOW;
            case DESTROY -> DESTROY;
            case OUTLINE -> OUTLINE;
            case REPLACE -> REPLACE;
        };
    }

    public @Nullable BlockInput filter(BoundingBox bb, int x, int y, int z, BlockInput input, LevelWriter level) {
        return switch (this) {
            case REPLACE -> input;
            case OUTLINE -> x != bb.minX() && x != bb.maxX() && y != bb.minY() && y != bb.maxY() && z != bb.minZ() && z != bb.maxZ() ? null : input;
            case HOLLOW -> x != bb.minX() && x != bb.maxX() && y != bb.minY() && y != bb.maxY() && z != bb.minZ() && z != bb.maxZ() ? FillCommand.HOLLOW_CORE : input;
            case DESTROY -> {
                level.destroyBlock_(x, y, z, true);
                yield input;
            }
        };
    }
}
