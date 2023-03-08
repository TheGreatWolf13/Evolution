package tgw.evolution.capabilities.temperature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.init.EvolutionAttributes;
import tgw.evolution.init.EvolutionEffects;
import tgw.evolution.init.EvolutionNetwork;
import tgw.evolution.network.PacketSCTemperatureData;
import tgw.evolution.patches.ILivingEntityPatch;
import tgw.evolution.util.Temperature;
import tgw.evolution.util.earth.ClimateZone;
import tgw.evolution.util.math.MathHelper;

public class TemperatureStats implements ITemperature {

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
    private boolean needsUpdate = true;
    private byte ticks;

    private void calculateZone(ServerPlayer player) {
        if ((this.flags & 0b11) == 0) {
            ClimateZone.Region region = ClimateZone.fromZPos(player.getZ()).getRegion();
            this.flags |= region.getId();
            this.currentTemperature = Temperature.getBaseTemperatureForRegion(region);
        }
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
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

    @Override
    public int getCurrentMaxComfort() {
        return (int) Math.round(this.currentMaxComfort);
    }

    @Override
    public int getCurrentMinComfort() {
        return (int) Math.round(this.currentMinComfort);
    }

    @Override
    public int getCurrentTemperature() {
        return (int) Math.round(this.currentTemperature);
    }

    @Override
    public int getDesiredMaxComfort() {
        return this.desiredMaxComfort;
    }

    @Override
    public int getDesiredMinComfort() {
        return this.desiredMinComfort;
    }

    @Override
    @Nullable
    public ClimateZone.Region getRegion() {
        return ClimateZone.Region.byId(this.flags & 0b11);
    }

    private boolean isShivering() {
        return (this.flags & 4) != 0;
    }

    private boolean isSweating() {
        return (this.flags & 8) != 0;
    }

    @Override
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

    @Override
    public void setCurrentMaxComfort(double maxComfort) {
        double old = this.currentMaxComfort;
        this.currentMaxComfort = MathHelper.clamp(maxComfort, Math.max(-70, this.currentMinComfort), 110);
        if (Math.round(old) != this.getCurrentMaxComfort()) {
            this.needsUpdate = true;
        }
    }

    @Override
    public void setCurrentMinComfort(double minComfort) {
        double old = this.currentMinComfort;
        this.currentMinComfort = MathHelper.clamp(minComfort, -70, Math.min(110, this.currentMaxComfort));
        if (Math.round(old) != this.getCurrentMinComfort()) {
            this.needsUpdate = true;
        }
    }

    @Override
    public void setCurrentTemperature(double temp) {
        double old = this.currentTemperature;
        this.currentTemperature = MathHelper.clamp(temp, -273, 1_000_000_000 - 273);
        if (Math.round(old) != this.getCurrentTemperature()) {
            this.needsUpdate = true;
        }
    }

    @Override
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

    @Override
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
            //Temperature calculations
            double dTemp = this.desiredTemperature - this.currentTemperature;
            this.setCurrentTemperature(this.currentTemperature + TEMPERATURE_COEFFICIENT * dTemp);
            double dMinComf = Math.signum(this.desiredMinComfort - this.currentMinComfort);
            this.setCurrentMinComfort(this.currentMinComfort + COMFORT_COEFFICIENT * dMinComf);
            double dMaxComf = Math.signum(this.desiredMaxComfort - this.currentMaxComfort);
            this.setCurrentMaxComfort(this.currentMaxComfort + COMFORT_COEFFICIENT * dMaxComf);
            //Handle Shivering
            boolean shouldBeShivering = this.currentTemperature < this.currentMinComfort - 5;
            if (this.isShivering()) {
                if (!shouldBeShivering) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMinComfort + 10) / 5 * 2;
                    if (this.effectTicks >= 0) {
                        this.effectTicks = 0;
                        player.removeEffect(EvolutionEffects.SHIVERING.get());
                        this.setShivering(false);
                    }
                }
            }
            else {
                if (shouldBeShivering) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMinComfort) / 5;
                    if (this.effectTicks <= -500) {
                        this.effectTicks = -500;
                        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.SHIVERING.get(), 0, false, false, true));
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
                        player.removeEffect(EvolutionEffects.SWEATING.get());
                        this.setSweating(false);
                    }
                }
            }
            else {
                if (shouldBeSweating) {
                    this.effectTicks += (int) (this.currentTemperature - this.currentMaxComfort) / 5;
                    if (this.effectTicks >= 500) {
                        this.effectTicks = 500;
                        player.addEffect(EvolutionEffects.infiniteOf(EvolutionEffects.SWEATING.get(), 0, false, false, true));
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
            this.ticks = 0;
        }
        if (this.needsUpdate) {
            EvolutionNetwork.send(player, new PacketSCTemperatureData(this));
            this.needsUpdate = false;
        }
    }

    private void updateDesiredComfort(ServerPlayer player) {
        ClimateZone.Region region = this.getRegion();
        this.desiredMinComfort = Temperature.getMinComfortForRegion(region);
        this.desiredMaxComfort = Temperature.getMaxComfortForRegion(region);
        this.desiredMinComfort -= player.getAttributeValue(EvolutionAttributes.COLD_RESISTANCE.get());
        this.desiredMaxComfort += player.getAttributeValue(EvolutionAttributes.HEAT_RESISTANCE.get());
    }

    private void updateDesiredTemperature(ServerPlayer player) {
        ServerLevel level = player.getLevel();
        try (Temperature temperature = Temperature.getInstance(level, player.getX(), player.getY() + 0.5, player.getZ(), level.getDayTime())) {
            this.desiredTemperature = Temperature.K2C(temperature.getLocalTemperature());
        }
        if (player.isSprinting()) {
            this.desiredTemperature += 2;
        }
        this.desiredTemperature += ((ILivingEntityPatch) player).getEffectHelper().getTemperatureMod();
        //TODO objects
    }
}
