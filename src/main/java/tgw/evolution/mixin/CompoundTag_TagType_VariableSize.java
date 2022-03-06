package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(targets = "net.minecraft.nbt.CompoundTag$1")
public abstract class CompoundTag_TagType_VariableSize {

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

