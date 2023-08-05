package tgw.evolution.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorStandItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.hooks.asm.DeleteMethod;

import java.util.Random;

@Mixin(ArmorStandItem.class)
public abstract class Mixin_M_ArmorStandItem extends Item {

    public Mixin_M_ArmorStandItem(Properties properties) {
        super(properties);
    }

    @Shadow
    protected abstract void randomizePose(ArmorStand armorStand, Random random);

    @Override
    @Overwrite
    @DeleteMethod
    public InteractionResult useOn(UseOnContext useOnContext) {
        throw new AbstractMethodError();
    }

    @Override
    public InteractionResult useOn_(Level level, int x, int y, int z, Player player, InteractionHand hand, BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        }
        ItemStack stack = player.getItemInHand(hand);
        AABB aABB = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(x + 0.5, y, z + 0.5);
        if (level.noCollision(null, aABB) && level.getEntities(null, aABB).isEmpty()) {
            if (level instanceof ServerLevel serverLevel) {
                ArmorStand armorStand = EntityType.ARMOR_STAND.create(serverLevel, stack.getTag(), null, player, new BlockPos(x, y, z),
                                                                      MobSpawnType.SPAWN_EGG, true, true);
                if (armorStand == null) {
                    return InteractionResult.FAIL;
                }
                float f = Mth.floor((Mth.wrapDegrees(player.getYRot() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                armorStand.moveTo(armorStand.getX(), armorStand.getY(), armorStand.getZ(), f, 0.0F);
                this.randomizePose(armorStand, level.random);
                serverLevel.addFreshEntityWithPassengers(armorStand);
                level.playSound(null, armorStand.getX(), armorStand.getY(), armorStand.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS,
                                0.75F, 0.8F);
                level.gameEvent(player, GameEvent.ENTITY_PLACE, armorStand);
            }
            stack.shrink(1);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.FAIL;
    }
}
