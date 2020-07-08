package tgw.evolution.entities.ai;

import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.Goal;
import tgw.evolution.blocks.tileentities.TEShadowHound;
import tgw.evolution.entities.EntityShadowHound;
import tgw.evolution.init.EvolutionBlocks;

public class ShadowHoundHideInBlockGoal extends Goal {

    private final EntityShadowHound shadowHound;

    public ShadowHoundHideInBlockGoal(EntityShadowHound shadowHound) {
        this.shadowHound = shadowHound;
    }

    @Override
    public boolean shouldExecute() {
        return !this.shadowHound.isDead() && this.shadowHound.hideCooldown <= 0 && (this.shadowHound.world.getDayTime() % 24000 >= 23000 || this.shadowHound.world.getDayTime() % 24000 <= 13000);
    }

    @Override
    public void tick() {
        if (this.shadowHound.world.getBlockState(this.shadowHound.getPosition()).getBlock() == Blocks.AIR) {
            this.shadowHound.world.setBlockState(this.shadowHound.getPosition(), EvolutionBlocks.SHADOWHOUND.get().getDefaultState(), 2);
            ((TEShadowHound) this.shadowHound.world.getTileEntity(this.shadowHound.getPosition())).health = this.shadowHound.getHealth();
            this.shadowHound.remove();
        }
    }
}
