package tgw.evolution.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.VoidStartPlatformFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.constants.BlockFlags;

@Mixin(VoidStartPlatformFeature.class)
public abstract class MixinVoidStartPlatformFeature extends Feature<NoneFeatureConfiguration> {

    @Shadow @Final private static ChunkPos PLATFORM_ORIGIN_CHUNK;
    @Shadow @Final private static BlockPos PLATFORM_OFFSET;

    public MixinVoidStartPlatformFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Shadow
    private static int checkerboardDistance(int i, int j, int k, int l) {
        throw new AbstractMethodError();
    }

    @Override
    @Overwrite
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> featurePlaceContext) {
        WorldGenLevel level = featurePlaceContext.level();
        BlockPos origin = featurePlaceContext.origin();
        int secX = SectionPos.blockToSectionCoord(origin.getX());
        int secZ = SectionPos.blockToSectionCoord(origin.getZ());
        if (checkerboardDistance(secX, secZ, PLATFORM_ORIGIN_CHUNK.x, PLATFORM_ORIGIN_CHUNK.z) > 1) {
            return true;
        }
        int platY = origin.getY() + 3;
        int z0 = SectionPos.sectionToBlockCoord(secZ);
        int z1 = z0 + 16;
        int x0 = SectionPos.sectionToBlockCoord(secX);
        int x1 = x0 + 16;
        for (int z = z0; z < z1; ++z) {
            for (int x = x0; x < x1; ++x) {
                if (checkerboardDistance(8, 8, x, z) <= 16) {
                    if (x == 8 && z == 8) {
                        level.setBlock_(x, platY, z, Blocks.COBBLESTONE.defaultBlockState(), BlockFlags.BLOCK_UPDATE);
                    }
                    else {
                        level.setBlock_(x, platY, z, Blocks.STONE.defaultBlockState(), BlockFlags.BLOCK_UPDATE);
                    }
                }
            }
        }
        return true;
    }
}
