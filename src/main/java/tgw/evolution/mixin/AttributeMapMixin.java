package tgw.evolution.mixin;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.Set;

@Mixin(AttributeMap.class)
public abstract class AttributeMapMixin {

    @Mutable
    @Shadow
    @Final
    private Map<Attribute, AttributeInstance> attributes;

    @Mutable
    @Shadow
    @Final
    private Set<AttributeInstance> dirtyAttributes;

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;" +
                                                                    "dirtyAttributes:Ljava/util/Set;", opcode = Opcodes.PUTFIELD))
    private void onInit(AttributeMap instance, Set<AttributeInstance> value) {
        this.dirtyAttributes = new ReferenceOpenHashSet<>();
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/AttributeMap;" +
                                                                    "attributes:Ljava/util/Map;", opcode = Opcodes.PUTFIELD))
    private void onInit(AttributeMap instance, Map<Attribute, AttributeInstance> value) {
        this.attributes = new Reference2ReferenceOpenHashMap<>();
    }
}
