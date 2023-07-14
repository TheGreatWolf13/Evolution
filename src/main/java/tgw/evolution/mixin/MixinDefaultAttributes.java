package tgw.evolution.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

import java.util.Map;

@Mixin(DefaultAttributes.class)
public abstract class MixinDefaultAttributes {

    @Mutable @Shadow @Final private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        R2OMap<EntityType<? extends LivingEntity>, AttributeSupplier> map = new R2OHashMap<>();
        map.putAll(SUPPLIERS);
//        map.put(EvolutionEntities.COW, Mob.createMobAttributes().build());
        SUPPLIERS = map.view();
    }
}
