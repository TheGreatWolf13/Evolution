package tgw.evolution.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.patches.IEntityPatch;
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin extends Player implements IEntityPatch {

    public AbstractClientPlayerMixin(Level pLevel,
                                     BlockPos pPos,
                                     float pYRot,
                                     GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }

    @Nullable
    @Override
    public HitboxEntity<? extends Entity> getHitboxes() {
        return "default".equals(this.getModelName()) ? EvolutionEntityHitboxes.PLAYER_STEVE : EvolutionEntityHitboxes.PLAYER_ALEX;
    }

    @Shadow
    public abstract String getModelName();
}
