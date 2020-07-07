package tgw.evolution.blocks.tileentities;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import tgw.evolution.entities.EntityShadowHound;
import tgw.evolution.init.EvolutionTileEntities;

public class TEShadowHound extends TileEntity implements ITickableTileEntity {

    public float health = 6;

    public TEShadowHound() {
        super(EvolutionTileEntities.TE_SHADOWHOUND.get());
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putFloat("Health", this.health);
        return super.write(compound);
    }

    @Override
    public void read(CompoundNBT compound) {
        this.health = compound.getFloat("Health");
        super.read(compound);
    }

    @Override
    public void tick() {
        if (this.world.isRemote()) {
            return;
        }
        if (this.world.getDayTime() % 24000 > 13000 && this.world.getDayTime() % 24000 < 23000) {
            this.spawnShadowHound();
        }
    }

    public void spawnShadowHound() {
        this.world.removeBlock(this.getPos(), false);
        EntityShadowHound shadowHound = new EntityShadowHound(this.world);
        shadowHound.setHealth(this.health);
        shadowHound.setPosition(this.getPos().getX() + 0.5, this.getPos().getY(), this.getPos().getZ() + 0.5);
        shadowHound.hideCooldown = 1000;
        this.world.addEntity(shadowHound);
    }
}
