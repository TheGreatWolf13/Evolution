package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.util.INBTSerializable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.ICapabilityDispatcherPatch;
import tgw.evolution.patches.ICompoundTagPatch;

import org.jetbrains.annotations.Nullable;

@Mixin(CapabilityDispatcher.class)
public abstract class CapabilityDispatcherMixin implements ICapabilityDispatcherPatch {

    private static final ThreadLocal<CompoundTag> THIS_TAG = ThreadLocal.withInitial(CompoundTag::new);
    private static final ThreadLocal<CompoundTag> OTHER_TAG = ThreadLocal.withInitial(CompoundTag::new);
    @Shadow
    private String[] names;
    @Shadow
    private INBTSerializable<Tag>[] writers;

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations when comparing the CompoundTags
     */
    @Overwrite
    public boolean areCompatible(@Nullable CapabilityDispatcher other) {
        if (other == null) {
            return this.writers.length == 0;
        }
        if (this.writers.length == 0) {
            return ((ICapabilityDispatcherPatch) (Object) other).getWriters().length == 0;
        }
        return this.serializeNBTNoAlloc(THIS_TAG.get()).equals(((ICapabilityDispatcherPatch) (Object) other).serializeNBTNoAlloc(OTHER_TAG.get()));
    }

    @Override
    public INBTSerializable<Tag>[] getWriters() {
        return this.writers;
    }

    @Override
    public CompoundTag serializeNBTNoAlloc(CompoundTag nbt) {
        ((ICompoundTagPatch) nbt).clear();
        for (int x = 0; x < this.writers.length; x++) {
            nbt.put(this.names[x], this.writers[x].serializeNBT());
        }
        return nbt;
    }
}
