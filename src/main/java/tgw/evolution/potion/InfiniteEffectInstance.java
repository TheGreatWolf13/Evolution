package tgw.evolution.potion;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.EvolutionHook;
import tgw.evolution.util.NBTTypes;
import tgw.evolution.util.reflection.FieldHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InfiniteEffectInstance extends EffectInstance {

    private static final FieldHandler<EffectInstance, Integer> AMPLIFIER = new FieldHandler<>(EffectInstance.class, "field_76461_c");
    private static final FieldHandler<EffectInstance, Integer> DURATION = new FieldHandler<>(EffectInstance.class, "field_76460_b");
    private static final FieldHandler<EffectInstance, Boolean> AMBIENT = new FieldHandler<>(EffectInstance.class, "field_193467_c");
    private static final FieldHandler<EffectInstance, Boolean> SHOW_PARTICLES = new FieldHandler<>(EffectInstance.class, "field_188421_h");
    private static final FieldHandler<EffectInstance, Boolean> SHOW_ICON = new FieldHandler<>(EffectInstance.class, "field_205349_i");
    private boolean infinite = true;
    private int tick = 1_200;

    public InfiniteEffectInstance(Effect potion) {
        super(potion, Integer.MAX_VALUE);
    }

    public InfiniteEffectInstance(Effect potion, int amplifier) {
        super(potion, Integer.MAX_VALUE, amplifier);
    }

    public InfiniteEffectInstance(Effect potion, int amplifier, boolean ambient, boolean showParticles) {
        super(potion, Integer.MAX_VALUE, amplifier, ambient, showParticles);
    }

    public InfiniteEffectInstance(Effect potion, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        super(potion, Integer.MAX_VALUE, amplifier, ambient, showParticles, showIcon);
    }

    public InfiniteEffectInstance(Effect potion, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, int tick) {
        super(potion, Integer.MAX_VALUE, amplifier, ambient, showParticles, showIcon);
        this.tick = tick;
    }

    public InfiniteEffectInstance(EffectInstance other) {
        super(other.getPotion(),
              other instanceof InfiniteEffectInstance && ((InfiniteEffectInstance) other).infinite ? Integer.MAX_VALUE : other.getDuration(),
              other.getAmplifier(),
              other.isAmbient(),
              other.doesShowParticles(),
              other.isShowIcon());
        this.infinite = other instanceof InfiniteEffectInstance && ((InfiniteEffectInstance) other).infinite;
        if (this.infinite) {
            this.tick = ((InfiniteEffectInstance) other).tick;
        }
    }

    @Nullable
    @EvolutionHook
    public static EffectInstance read(CompoundNBT nbt) {
        int id = nbt.getByte("Id") & 0xFF;
        Effect effect = Effect.get(id);
        if (effect == null) {
            return null;
        }
        int amplifier = nbt.getByte("Amplifier");
        int duration = nbt.getInt("Duration");
        boolean ambient = nbt.getBoolean("Ambient");
        boolean showParticles = true;
        if (nbt.contains("ShowParticles", NBTTypes.BOOLEAN)) {
            showParticles = nbt.getBoolean("ShowParticles");
        }
        boolean showIcon = showParticles;
        if (nbt.contains("ShowIcon", NBTTypes.BOOLEAN)) {
            showIcon = nbt.getBoolean("ShowIcon");
        }
        if (nbt.getBoolean("Infinite")) {
            int tick = nbt.getShort("Tick");
            return new InfiniteEffectInstance(effect, Math.max(amplifier, 0), ambient, showParticles, showIcon, tick);
        }
        return readCurativeItems(new EffectInstance(effect, duration, Math.max(amplifier, 0), ambient, showParticles, showIcon), nbt);
    }

    private static EffectInstance readCurativeItems(EffectInstance effect, CompoundNBT nbt) {
        if (nbt.contains("CurativeItems", NBTTypes.LIST_NBT)) {
            List<ItemStack> items = new ArrayList<>();
            ListNBT list = nbt.getList("CurativeItems", NBTTypes.COMPOUND_NBT);
            for (int i = 0; i < list.size(); i++) {
                items.add(ItemStack.read(list.getCompound(i)));
            }
            effect.setCurativeItems(items);
        }
        return effect;
    }

    @Override
    public boolean combine(EffectInstance other) {
        if (!this.infinite) {
            return super.combine(other);
        }
        if (this.getPotion() != other.getPotion()) {
            Evolution.LOGGER.warn("This method should only be called for matching effects!");
            return false;
        }
        boolean worked = false;
        if (other.getAmplifier() > this.getAmplifier()) {
            AMPLIFIER.set(this, other.getAmplifier());
            if (!(other instanceof InfiniteEffectInstance)) {
                this.infinite = false;
                DURATION.set(this, other.getDuration());
            }
            worked = true;
        }
        if (!other.isAmbient() && this.isAmbient() || worked) {
            AMBIENT.set(this, other.isAmbient());
            worked = true;
        }
        if (other.doesShowParticles() != this.doesShowParticles()) {
            SHOW_PARTICLES.set(this, other.doesShowParticles());
            worked = true;
        }
        if (other.isShowIcon() != this.isShowIcon()) {
            SHOW_ICON.set(this, other.isShowIcon());
            return true;
        }
        return worked;
    }

    private void deincrementDuration() {
        this.tick--;
        if (this.tick == 0) {
            this.tick = 1_200;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!this.infinite) {
            return super.equals(o);
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof InfiniteEffectInstance)) {
            return false;
        }
        InfiniteEffectInstance other = (InfiniteEffectInstance) o;
        return this.getAmplifier() == other.getAmplifier() && this.isAmbient() == other.isAmbient() && this.getPotion().equals(other.getPotion());
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public int hashCode() {
        if (!this.infinite) {
            return super.hashCode();
        }
        return super.hashCode() * 31;
    }

    @Override
    public void performEffect(LivingEntity entity) {
        if (!this.infinite) {
            super.performEffect(entity);
            return;
        }
        this.getPotion().performEffect(entity, this.getAmplifier());
    }

    @Override
    public boolean tick(LivingEntity entityIn) {
        if (!this.infinite) {
            return super.tick(entityIn);
        }
        if (this.getPotion().isReady(this.tick, this.getAmplifier())) {
            this.performEffect(entityIn);
        }
        this.deincrementDuration();
        return true;
    }

    @Override
    public String toString() {
        if (!this.infinite) {
            return super.toString();
        }
        if (this.getAmplifier() > 0) {
            return this.getEffectName() + " x " + (this.getAmplifier() + 1) + ", Duration: Infinite";
        }
        return this.getEffectName() + ", Duration: Infinite";
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        if (!this.infinite) {
            return super.write(nbt);
        }
        nbt.putBoolean("Infinite", true);
        nbt.putShort("Tick", (short) this.tick);
        return super.write(nbt);
    }
}
