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
import tgw.evolution.util.hitbox.EvolutionEntityHitboxes;
import tgw.evolution.util.hitbox.hitboxes.HitboxEntity;

@Mixin(AbstractClientPlayer.class)
public abstract class MixinAbstractClientPlayer extends Player {

    public MixinAbstractClientPlayer(Level pLevel,
                                     BlockPos pPos,
                                     float pYRot,
                                     GameProfile pGameProfile) {
        super(pLevel, pPos, pYRot, pGameProfile);
    }

    @Override
    public @Nullable HitboxEntity<? extends Entity> getHitboxes() {
        return "default".equals(this.getModelName()) ? EvolutionEntityHitboxes.PLAYER_STEVE.get(this) : EvolutionEntityHitboxes.PLAYER_ALEX.get(this);
    }

    @Shadow
    public abstract String getModelName();
}
