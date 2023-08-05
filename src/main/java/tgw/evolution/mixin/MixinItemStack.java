package tgw.evolution.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.patches.PatchItemStack;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements PatchItemStack {

    @Shadow @Final public static ItemStack EMPTY;
    @Shadow private int count;
    @Shadow private boolean emptyCacheFlag;
    @Shadow @Final private Item item;
    @Shadow private @Nullable CompoundTag tag;

    /**
     * @author TheGreatWolf
     * @reason Make Item attributes depend on ItemStack
     */
    @Overwrite
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        //noinspection ConstantConditions
        if (this.hasTag() && this.tag.contains("AttributeModifiers", Tag.TAG_LIST)) {
            Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
            ListTag listTag = this.tag.getList("AttributeModifiers", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                if (!compoundTag.contains("Slot", Tag.TAG_STRING) || compoundTag.getString("Slot").equals(slot.getName())) {
                    Attribute attribute = Registry.ATTRIBUTE.get(ResourceLocation.tryParse(compoundTag.getString("AttributeName")));
                    if (attribute != null) {
                        AttributeModifier modifier = AttributeModifier.load(compoundTag);
                        if (modifier != null && modifier.getId().getLeastSignificantBits() != 0L && modifier.getId().getMostSignificantBits() != 0L) {
                            multimap.put(attribute, modifier);
                        }
                    }
                }
            }
            return multimap;
        }
        return this.getItem().getAttributeModifiers(slot, (ItemStack) (Object) this);
    }

    @Shadow
    public abstract Item getItem();

    @Shadow
    public abstract boolean hasAdventureModePlaceTagForBlock(Registry<Block> registry,
                                                             BlockInWorld blockInWorld);

    @Shadow
    public abstract boolean hasTag();

    @Shadow
    public abstract boolean is(Item item);

    /**
     * @author TheGreatWolf
     * @reason Use cached state
     */
    @Overwrite
    public boolean isEmpty() {
        return this.emptyCacheFlag;
    }

    @Overwrite
    public void mineBlock(Level level, BlockState state, BlockPos pos, Player player) {
        Evolution.deprecatedMethod();
        this.mineBlock_(level, state, pos.getX(), pos.getY(), pos.getZ(), player);
    }

    @Override
    public void mineBlock_(Level level, BlockState state, int x, int y, int z, LivingEntity entity) {
        Item item = this.getItem();
        if (item.mineBlock_((ItemStack) (Object) this, level, state, x, y, z, entity) && entity instanceof Player player) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
    }

    @Override
    public void onUsingTick(LivingEntity entity, int useRemaining) {
        this.getItem().onUsingTick((ItemStack) (Object) this, entity, useRemaining);
    }

    /**
     * @author TheGreatWolf
     * @reason Use cached state
     */
    @Overwrite
    private void updateEmptyCacheFlag() {
        //noinspection ConstantConditions
        if (this.item == null || this.item == Items.AIR) {
            this.emptyCacheFlag = true;
        }
        else {
            this.emptyCacheFlag = this.count <= 0;
        }
    }

    @Overwrite
    public InteractionResult useOn(UseOnContext context) {
        Evolution.deprecatedMethod();
        return this.useOn_(context.getPlayer(), context.getHand(), context.getHitResult());
    }

    @Override
    public InteractionResult useOn_(Player player, InteractionHand hand, BlockHitResult hitResult) {
        int x = hitResult.posX();
        int y = hitResult.posY();
        int z = hitResult.posZ();
        Level level = player.level;
        if (!player.getAbilities().mayBuild &&
            !this.hasAdventureModePlaceTagForBlock(level.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY),
                                                   new BlockInWorld(level, new BlockPos(x, y, z), false))) {
            return InteractionResult.PASS;
        }
        Item item = this.getItem();
        InteractionResult result = item.useOn_(level, x, y, z, player, hand, hitResult);
        if (result.shouldAwardStats()) {
            player.awardStat(Stats.ITEM_USED.get(item));
        }
        return result;
    }
}
