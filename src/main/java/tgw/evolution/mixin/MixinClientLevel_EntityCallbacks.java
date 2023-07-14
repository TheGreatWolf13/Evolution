package tgw.evolution.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net.minecraft.client.multiplayer.ClientLevel$EntityCallbacks")
public abstract class MixinClientLevel_EntityCallbacks {

    @Shadow(aliases = "this$0") @Final ClientLevel field_27735;

    /**
     * @author TheGreatWolf
     * @reason Call onRemovedFromWorld on entities.
     */
    @Overwrite
    public void onTrackingEnd(Entity entity) {
        entity.unRide();
        this.field_27735.players().remove(entity);
        entity.onRemovedFromWorld();
    }
}
