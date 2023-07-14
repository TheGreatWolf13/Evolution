package tgw.evolution.mixin;

import com.google.common.collect.ImmutableSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(EntityType.Builder.class)
public abstract class MixinEntityType_Builder<T extends Entity> {

    @Shadow private boolean canSpawnFarFromPlayer;
    @Shadow @Final private MobCategory category;
    @Shadow private int clientTrackingRange;
    @Shadow private EntityDimensions dimensions;
    @Shadow @Final private EntityType.EntityFactory<T> factory;
    @Shadow private boolean fireImmune;
    @Shadow private ImmutableSet<Block> immuneTo;
    @Shadow private boolean serialize;
    @Shadow private boolean summon;
    @Shadow private int updateInterval;

    /**
     * @author TheGreatWolf
     * @reason Remove fetching data fixers
     */
    @Overwrite
    public EntityType<T> build(String string) {
        return new EntityType(this.factory, this.category, this.serialize, this.summon, this.fireImmune, this.canSpawnFarFromPlayer, this.immuneTo,
                              this.dimensions, this.clientTrackingRange, this.updateInterval);
    }
}
