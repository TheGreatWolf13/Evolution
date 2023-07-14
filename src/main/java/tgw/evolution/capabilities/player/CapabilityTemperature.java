package tgw.evolution.capabilities.player;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.network.PacketSCTemperatureData;
import tgw.evolution.patches.PatchLivingEntity;
import tgw.evolution.util.Temperature;
import tgw.evolution.util.math.MathHelper;
import tgw.evolution.util.physics.ClimateZone;

public class CapabilityTemperature {

    public static final double TEMPERATURE_COEFFICIENT = 0.005;
    public static final double COMFORT_COEFFICIENT = 0.1;
    /**
     * The current max temperature the Player feels comfortable in, in Celsius.
     */
    private double currentMaxComfort = 25;
    /**
     * The current min temperature the Player feels comfortable in, in Celsius.
     */
    private double currentMinComfort = 15;
    /**
     * The current ambient temperature felt by the Player, in Celsius.
     */
    private double currentTemperature = 20;
    /**
     * The desired max temperature the Player aims to feel comfortable in, in Celsius.
     */
    private short desiredMaxComfort = 25;
    /**
     * The desired min temperature the Player aims to feel comfortable in, in Celsius.
     */
    private short desiredMinComfort = 15;
    /**
     * The ambient temperature the Player aims to achieve, in Celsius.
     */
    private double desiredTemperature = 20;
    private short effectTicks;
    /**
     * Bit 0~1: ClimateZone;<br>
     * Bit 2: isShivering;<br>
     * Bit 3: isSweating;
     */
    private byte flags;
    private boolean fullUpdate = true;
    private boolean needsUpdate = true;
    private byte ticks;

    private void calculateZone(Player player) {
        if ((this.flags & 0b11) == 0) {
            ClimateZone.Region region = ClimateZone.fromZPos(player.getZ()).getRegion();
            this.flags |= region.getId();
            this.currentTemperature = Temperature.getBaseTemperatureForRegion(region);
        }
    }

    public void deserializeNBT(@Nullable CompoundTag nbt) {
        if (nbt == null) {
            return;
        }
        this.flags = nbt.getByte("Flags");
        this.ticks = nbt.getByte("Ticks");
        this.currentTemperature = nbt.getDouble("CurrentTemp");
        this.currentMaxComfort = nbt.getDouble("CurrentMax");
        this.currentMinComfort = nbt.getDouble("CurrentMin");
        this.desiredTemperature = nbt.getDouble("DesiredTemp");
        this.desiredMaxComfort = nbt.getShort("DesiredMax");
        this.desiredMinComfort = nbt.getShort("DesiredMin");
        this.effectTicks = nbt.getShort("EffectTicks");
        this.needsUpdate = true;
    }

    public int getCurrentMaxComfort() {
        return (int) Math.round(this.currentMaxComfort);
    }

    public int getCurrentMinComfort() {
        return (int) Math.round(this.currentMinComfort);
    }

    public int getCurrentTemperature() {
        return (int) Math.round(this.currentTemperature);
    }

    public int getDesiredMaxComfort() {
        return this.desiredMaxComfort;
    }

    public int getDesiredMinComfort() {
        return this.desiredMinComfort;
    }

    public @Nullable ClimateZone.Region getRegion() {
        return ClimateZone.Region.byId(this.flags & 0b11);
    }

    private boolean isShivering() {
        return (this.flags & 4) != 0;
    }

    private boolean isSweating() {
        return (this.flags & 8) != 0;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("Flags", this.flags);
        nbt.putByte("Ticks", this.ticks);
        nbt.putDouble("CurrentTemp", this.currentTemperature);
        nbt.putDouble("CurrentMax", this.currentMaxComfort);
        nbt.putDouble("CurrentMin", this.currentMinComfort);
        nbt.putDouble("DesiredTemp", this.desiredTemperature);
        nbt.putShort("DesiredMax", this.desiredMaxComfort);
        nbt.putShort("DesiredMin", this.desiredMinComfort);
        nbt.putShort("EffectTicks", this.effectTicks);
        return nbt;
    }

    public void set(CapabilityTemperature old) {
        this.flags = old.flags;
        this.ticks = old.ticks;
        this.currentTemperature = old.currentTemperature;
        this.currentMaxComfort = old.currentMaxComfort;
        this.currentMinComfort = old.currentMinComfort;
        this.desiredTemperature = old.desiredTemperature;
        this.desiredMaxComfort = old.desiredMaxComfort;
        this.desiredMinComfort = old.desiredMinComfort;
        this.effectTicks = old.effectTicks;
        this.fullUpdate = old.fullUpdate;
        this.needsUpdate = true;
    }

    public void setCurrentMaxComfort(double maxComfort) {
        double old = this.currentMaxComfort;
        this.currentMaxComfort = MathHelper.clamp(maxComfort, Math.max(-70, this.currentMinComfort), 110);
        if (Math.round(old) != this.getCurrentMaxComfort()) {
            this.needsUpdate = true;
        }
    }

    public void setCurrentMinComfort(double minComfort) {
        double old = this.currentMinComfort;
        this.currentMinComfort = MathHelper.clamp(minComfort, -70, Math.min(110, this.currentMaxComfort));
        if (Math.round(old) != this.getCurrentMinComfort()) {
            this.needsUpdate = true;
        }
    }

    public void setCurrentTemperature(double temp) {
        double old = this.currentTemperature;
        this.currentTemperature = MathHelper.clamp(temp, -273, 1_000_000_000 - 273);
        if (Math.round(old) != this.getCurrentTemperature()) {
            this.needsUpdate = true;
        }
    }

    public void setDesiredTemperature(double temp) {
        this.desiredTemperature = Math.max(temp, -273);
    }

    private void setShivering(boolean isShivering) {
        if (isShivering) {
            this.flags |= 4;
        }
        else {
            this.flags &= ~4;
        }
    }

    private void setSweating(boolean isSweating) {
        if (isSweating) {
            this.flags |= 8;
        }
        else {
            this.flags &= ~8;
        }
    }

    public void tick(ServerPlayer player) {
        this.calculateZone(player);
        if (player.isAlive()) {
            //Update desired
            if (this.ticks == 0) {
                this.updateDesiredTemperature(player);
            }
            if (this.ticks % 10 == 0) {
                this.updateDesiredComfort(player);
            }
            if (this.fullUpdate) {
                this.currentTemperature = this.desiredTemperature;
                this.currentMinComfort = this.desiredMinComfort;
                this.currentMaxComfort = this.desiredMaxComfort;
                this.fullUpdate = false;
            }
            else {
                //Temperature calculations
                double dTemp = this.desiredTemperature - this.currentTemperature;
                this.setCurrentTemperature(this.currentTemperature + TEMPERATURE_COEFFICIENT * dTemp);
                double dMinComf = Math.signum(this.desiredMinComfort - this.currentMinComfort);
                this.setCurrentMinComfort(this.currentMinComfort + COMFORT_COEFFICIENT * dMinComf);
                double dMaxComf = Math.signum(this.desiredMaxComfort - this.currentMaxComfort);
                this.setCurrentMaxComfort(this.currentMaxComfort + COMFORT_COEFFICIENT * dMaxComf);
            }
            //Handle Shivering
            boolean shouldBeShivering = this.currentTemperature < this.currentMinComfort - 5;
            if (this.isShivering()) {
                if (!shouldBeShivering) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMinComfort + 10) / 5 * 2;
                    if (this.effectTicks >= 0) {
                        this.effectTicks = 0;
                        player.removeEffect(EvolutionEffects.SHIVERING);
                        this.setShivering(false);
                    }
                }
            }
            else {
                if (shouldBeShivering) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMinComfort) / 5;
                    if (this.effectTicks <= -500) {
                        this.effectTicks = -500;
                        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.SHIVERING, 0, false, false, true));
                        this.setShivering(true);
                    }
                }
            }
            //Handle Sweating
            boolean shouldBeSweating = this.currentTemperature > this.currentMaxComfort + 5;
            if (this.isSweating()) {
                if (!shouldBeSweating) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMaxComfort - 10) / 5 * 2;
                    if (this.effectTicks <= 0) {
                        this.effectTicks = 0;
                        player.removeEffect(EvolutionEffects.SWEATING);
                        this.setSweating(false);
                    }
                }
            }
            else {
                if (shouldBeSweating) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMaxComfort) / 5;
                    if (this.effectTicks >= 500) {
                        this.effectTicks = 500;
                        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.SWEATING, 0, false, false, true));
                        this.setSweating(true);
                    }
                }
            }
            //Ticking
            this.ticks++;
            if (this.ticks >= 100) {
                this.ticks = 0;
            }
        }
        else {
            this.flags &= 0b11;
            this.ticks = 0;
            this.effectTicks = 0;
            this.fullUpdate = true;
        }
        if (this.needsUpdate) {
            player.connection.send(new PacketSCTemperatureData(this));
            this.needsUpdate = false;
        }
    }

    private void updateDesiredComfort(Player player) {
        ClimateZone.Region region = this.getRegion();
        this.desiredMinComfort = Temperature.getMinComfortForRegion(region);
        this.desiredMaxComfort = Temperature.getMaxComfortForRegion(region);
        this.desiredMinComfort -= player.getAttributeValue(EvolutionAttributes.COLD_RESISTANCE);
        this.desiredMaxComfort += player.getAttributeValue(EvolutionAttributes.HEAT_RESISTANCE);
    }

    private void updateDesiredTemperature(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        try (Temperature temperature = Temperature.getInstance(level, player.getX(), player.getY() + 0.5, player.getZ(), level.getDayTime())) {
            this.desiredTemperature = Temperature.K2C(temperature.getLocalTemperature());
        }
        if (player.isSprinting()) {
            this.desiredTemperature += 2;
        }
        this.desiredTemperature += ((PatchLivingEntity) player).getEffectHelper().getTemperatureMod();
        //TODO objects
    }
}
