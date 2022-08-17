package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import tgw.evolution.entities.INeckPosition;

@Mixin(ZombifiedPiglin.class)
public abstract class ZombifiedPiglinMixin extends Zombie implements INeckPosition {

    public ZombifiedPiglinMixin(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    @Override
    public float getCameraYOffset() {
        return 5 / 16.0f;
    }

    @Override
    public float getCameraZOffset() {
        return 4 / 16.0f;
    }
}
