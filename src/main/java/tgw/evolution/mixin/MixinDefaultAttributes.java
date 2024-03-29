package tgw.evolution.mixin;

import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.jetbrains.annotations.Contract;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;

import java.util.Map;

@Mixin(DefaultAttributes.class)
public abstract class MixinDefaultAttributes {

    @Mutable @Shadow @Final private static Map<EntityType<? extends LivingEntity>, AttributeSupplier> SUPPLIERS;

    @Contract(value = "_ -> _", pure = true)
    @Shadow
    public static boolean hasSupplier(EntityType<?> entityType) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static void validate() {
        for (long it = Registry.ENTITY_TYPE.beginIteration(); Registry.ENTITY_TYPE.hasNextIteration(it); it = Registry.ENTITY_TYPE.nextEntry(it)) {
            EntityType<?> entityType = (EntityType<?>) Registry.ENTITY_TYPE.getIteration(it);
            if (entityType.getCategory() != MobCategory.MISC && !hasSupplier(entityType)) {
                //noinspection ObjectAllocationInLoop
                Util.logAndPauseIfInIde("Entity " + Registry.ENTITY_TYPE.getKey(entityType) + " has no attributes");
            }
        }
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        R2OMap<EntityType<? extends LivingEntity>, AttributeSupplier> map = new R2OHashMap<>();
        map.putAll(SUPPLIERS);
//        map.put(EvolutionEntities.COW, Mob.createMobAttributes().build());
        SUPPLIERS = map.view();
    }
}
