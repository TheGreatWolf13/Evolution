package tgw.evolution.mixin;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.sets.OHashSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("MethodMayBeStatic")
@Mixin(AttributeMap.class)
public abstract class MixinAttributeMap {

    @Mutable @Shadow @Final private Map<Attribute, AttributeInstance> attributes;
    @Mutable @Shadow @Final private Set<AttributeInstance> dirtyAttributes;

    @Redirect(method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;" +
                                               "dirtyAttributes:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit(AttributeMap instance, Set<AttributeInstance> value) {
        this.dirtyAttributes = new OHashSet<>();
    }

    @Redirect(method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;" +
                                               "attributes:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit(AttributeMap instance, Map<Attribute, AttributeInstance> value) {
        this.attributes = new R2OHashMap<>();
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Maps;newHashMap()Ljava/util/HashMap;"),
            remap = false)
    private @Nullable HashMap onInitRemoveMap() {
        return null;
    }

    @Redirect(method = "<init>",
            at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Sets;newHashSet()Ljava/util/HashSet;"),
            remap = false)
    private @Nullable HashSet onInitRemoveSet() {
        return null;
    }
}
