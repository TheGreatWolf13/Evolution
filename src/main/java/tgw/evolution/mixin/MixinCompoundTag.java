package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.patches.PatchCompoundTag;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.HashMap;
import java.util.Map;

@Mixin(CompoundTag.class)
public abstract class MixinCompoundTag implements PatchCompoundTag {

    @Shadow @Final private Map<String, Tag> tags;

    @ModifyArg(method = "<init>()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;<init>(Ljava/util/Map;)V"))
    private static Map<String, Tag> onInit(Map<String, Tag> oldMap) {
        return new O2OHashMap<>();
    }

    @Redirect(method = "<init>()V", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;", remap =
            false))
    private static @Nullable HashMap<?, ?> onInitRemoveMap() {
        return null;
    }

    @Override
    public void clear() {
        this.tags.clear();
    }

    /**
     * @author TheGreatWolf
     * @reason Use faster collection and inline.
     */
    @Overwrite
    public CompoundTag copy() {
        Map<String, Tag> map = new O2OHashMap<>(this.tags.size());
        O2OMap<String, Tag> tags = (O2OMap<String, Tag>) this.tags;
        for (O2OMap.Entry<String, Tag> e = tags.fastEntries(); e != null; e = tags.fastEntries()) {
            map.put(e.key(), e.value().copy());
        }
        return new CompoundTag(map);
    }
}
