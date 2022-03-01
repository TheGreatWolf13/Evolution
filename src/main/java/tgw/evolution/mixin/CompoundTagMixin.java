package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@Mixin(CompoundTag.class)
public abstract class CompoundTagMixin {

    @Nullable
    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap =
            false))
    private static HashMap<?, ?> removeOldMapAlloc() {
        return null;
    }

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V"))
    private static Map<String, Tag> useFasterCollection(Map<String, Tag> oldMap) {
        return new Object2ObjectOpenHashMap<>();
    }

    /**
     * @author MGSchultz
     * <p>
     * Use faster collection
     */
    @ModifyArg(method = "copy()Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>" +
                                                                                                     "(Ljava/util/Map;)V"))
    public Map<?, ?> copy(Map<?, ?> oldMap) {
        return new Object2ObjectOpenHashMap<>(oldMap);
    }

    @SuppressWarnings("MethodMayBeStatic")
    @Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
    static class TagType$VariableSize {

        @Nullable
        @Redirect(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value = "INVOKE",
                target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
        private HashMap<?, ?> removeOldMapAlloc() {
            return null;
        }

        @ModifyVariable(method = "load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/CompoundTag;", at = @At(value =
                "INVOKE_ASSIGN", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap = false))
        private Map<String, Tag> useFasterCollection(Map<String, Tag> map) {
            return new Object2ObjectOpenHashMap<>();
        }
    }
}
