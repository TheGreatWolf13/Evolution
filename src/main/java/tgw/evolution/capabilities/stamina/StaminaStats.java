package tgw.evolution.capabilities.stamina;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import tgw.evolution.entities.misc.ISittableEntity;
import tgw.evolution.util.math.MathHelper;

public class StaminaStats implements IStamina {

    public static final int MAX_STAMINA = 24_000;
    private long awakeTicks;
    private boolean needsUpdate = true;
    private double stamina = MAX_STAMINA;

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.stamina = nbt.getDouble("Stamina");
        this.awakeTicks = nbt.getLong("AwakeTicks");
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putDouble("Stamina", this.stamina);
        nbt.putLong("AwakeTicks", this.awakeTicks);
        return nbt;
    }

    @Override
    public void setStamina(int stamina) {
        this.stamina = MathHelper.clamp(stamina, 0, MAX_STAMINA);
    }

    @Override
    public void tick(ServerPlayer player) {
        if (player.isAlive()) {
            double baseConsumption = 1;
            if (player.getPose() == Pose.SLEEPING) {
                //Bed comfort
//                baseConsumption -= bedComfort / 100.0;
            }
            else {
                Entity vehicle = player.getVehicle();
                if (vehicle instanceof ISittableEntity sittable) {
                    //Seat comfort
                    baseConsumption -= sittable.getComfort() * 0.005; //As comfort is a percent and we'll cap in 50%
                }
            }
            this.awakeTicks++;
        }
        else {
            this.awakeTicks = 0;
        }
        if (this.needsUpdate) {
            this.needsUpdate = false;
        }
    }
}
