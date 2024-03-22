package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.Evolution;
import tgw.evolution.entities.EntityUtils;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.init.EvolutionEntities;

import java.util.Optional;
import java.util.function.Function;

@Mixin(EntityType.class)
public abstract class Mixin_M_EntityType {

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static Optional<Entity> create(CompoundTag nbt, Level level) {
        Evolution.deprecatedMethod();
        return Optional.ofNullable(EntityUtils.create(nbt, level));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static @Nullable Entity loadEntityRecursive(CompoundTag nbt, Level level, Function<Entity, Entity> function) {
        Entity entity;
        try {
            entity = EntityUtils.create(nbt, level);
        }
        catch (RuntimeException e) {
            Evolution.warn("Exception loading entity: ", e);
            entity = null;
        }
        if (entity != null) {
            entity = function.apply(entity);
            if (nbt.contains("Passengers", Tag.TAG_LIST)) {
                ListTag passengers = nbt.getList("Passengers", Tag.TAG_COMPOUND);
                for (int i = 0, len = passengers.size(); i < len; ++i) {
                    Entity rider = loadEntityRecursive(passengers.getCompound(i), level, function);
                    if (rider != null) {
                        rider.startRiding(entity, true);
                    }
                }
            }
            return entity;
        }
        return null;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static Optional<Entity> loadStaticEntity(CompoundTag nbt, Level level) {
        throw new AbstractMethodError();
    }

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onClinit(CallbackInfo ci) {
        EvolutionEntities.register();
    }

    @ModifyArg(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType$Builder;sized(FF)" +
                                                                        "Lnet/minecraft/world/entity/EntityType$Builder;"), index = 0)
    private static float onClinit(float width) {
        if (width == 0.7f) {
            return 0.75f;
        }
        return width;
    }
}
