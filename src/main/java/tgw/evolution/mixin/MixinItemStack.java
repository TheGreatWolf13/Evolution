package tgw.evolution.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.inventory.AdditionalSlotType;
import tgw.evolution.patches.PatchItemStack;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.constants.NBTType;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements PatchItemStack {

    @Shadow @Final public static ItemStack EMPTY;
    @Shadow @Final public static DecimalFormat ATTRIBUTE_MODIFIER_FORMAT;
    @Shadow @Final private static Style LORE_STYLE;
    @Shadow private int count;
    @Shadow private boolean emptyCacheFlag;
    @Shadow @Final private Item item;
    @Shadow private @Nullable CompoundTag tag;

    @Contract(value = "_, _ -> _")
    @Shadow
    public static void appendEnchantmentNames(List<Component> list, ListTag listTag) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_ -> _")
    @Shadow
    private static Collection<Component> expandBlockState(String string) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Contract(value = "_, _ -> _")
    @Shadow
    private static boolean shouldShowInTooltip(int i, ItemStack.TooltipPart tooltipPart) {
        //noinspection Contract
        throw new AbstractMethodError();
    }

    @Overwrite
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        //noinspection ConstantConditions
        if (this.hasTag() && this.tag.contains("AttributeModifiers", NBTType.LIST)) {
            Multimap<Attribute, AttributeModifier> multimap = HashMultimap.create();
            ListTag listTag = this.tag.getList("AttributeModifiers", NBTType.COMPOUND);
            for (int i = 0; i < listTag.size(); ++i) {
                CompoundTag compoundTag = listTag.getCompound(i);
                if (!compoundTag.contains("Slot", NBTType.STRING) || compoundTag.getString("Slot").equals(slot.getName())) {
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
    public abstract int getDamageValue();

    @Shadow
    public abstract ListTag getEnchantmentTags();

    @Shadow
    protected abstract int getHideFlags();

    @Shadow
    public abstract Component getHoverName();

    @Shadow
    public abstract Item getItem();

    @Override
    public byte getLightEmission() {
        if (this.isEmpty()) {
            return 0;
        }
        return this.item.getLightEmission((ItemStack) (Object) this);
    }

    @Shadow
    public abstract int getMaxDamage();

    @Shadow
    public abstract Rarity getRarity();

    @Overwrite
    public List<Component> getTooltipLines(@Nullable Player player, TooltipFlag tooltipFlag) {
        OList<Component> list = new OArrayList<>();
        MutableComponent name = new TextComponent("").append(this.getHoverName()).withStyle(this.getRarity().color);
        if (this.hasCustomHoverName()) {
            name.withStyle(ChatFormatting.ITALIC);
        }
        list.add(name);
        if (!tooltipFlag.isAdvanced() && !this.hasCustomHoverName() && this.is(Items.FILLED_MAP)) {
            Integer integer = MapItem.getMapId((ItemStack) (Object) this);
            if (integer != null) {
                list.add(new TextComponent("#" + integer).withStyle(ChatFormatting.GRAY));
            }
        }
        int i = this.getHideFlags();
        if (shouldShowInTooltip(i, ItemStack.TooltipPart.ADDITIONAL)) {
            this.getItem().appendHoverText((ItemStack) (Object) this, player == null ? null : player.level, list, tooltipFlag);
        }
        int j;
        if (this.hasTag()) {
            assert this.tag != null;
            if (shouldShowInTooltip(i, ItemStack.TooltipPart.ENCHANTMENTS)) {
                appendEnchantmentNames(list, this.getEnchantmentTags());
            }
            if (this.tag.contains("display", NBTType.COMPOUND)) {
                CompoundTag compoundTag = this.tag.getCompound("display");
                if (shouldShowInTooltip(i, ItemStack.TooltipPart.DYE) && compoundTag.contains("color", NBTType.ANY_NUMERIC)) {
                    if (tooltipFlag.isAdvanced()) {
                        list.add(new TranslatableComponent("item.color", String.format("#%06X", compoundTag.getInt("color"))).withStyle(ChatFormatting.GRAY));
                    }
                    else {
                        list.add(new TranslatableComponent("item.dyed").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                    }
                }
                if (compoundTag.getTagType("Lore") == Tag.TAG_LIST) {
                    ListTag listTag = compoundTag.getList("Lore", NBTType.STRING);
                    for (j = 0; j < listTag.size(); ++j) {
                        String string = listTag.getString(j);
                        try {
                            MutableComponent mutableComponent2 = Component.Serializer.fromJson(string);
                            if (mutableComponent2 != null) {
                                list.add(ComponentUtils.mergeStyles(mutableComponent2, LORE_STYLE));
                            }
                        }
                        catch (Exception var19) {
                            compoundTag.remove("Lore");
                        }
                    }
                }
            }
        }
        int k;
        if (shouldShowInTooltip(i, ItemStack.TooltipPart.MODIFIERS)) {
            k = AdditionalSlotType.SLOTS.length;
            for (j = 0; j < k; ++j) {
                EquipmentSlot equipmentSlot = AdditionalSlotType.SLOTS[j];
                Multimap<Attribute, AttributeModifier> multimap = this.getAttributeModifiers(equipmentSlot);
                if (!multimap.isEmpty()) {
                    list.add(TextComponent.EMPTY);
                    //noinspection ObjectAllocationInLoop
                    list.add(new TranslatableComponent("item.modifiers." + equipmentSlot.getName()).withStyle(ChatFormatting.GRAY));
                    for (Map.Entry<Attribute, AttributeModifier> entry : multimap.entries()) {
                        AttributeModifier attributeModifier = entry.getValue();
                        double d = attributeModifier.getAmount();
                        boolean bl = false;
                        if (player != null) {
                            if (attributeModifier.getId() == Item.BASE_ATTACK_DAMAGE_UUID) {
                                d += player.getAttributeBaseValue(Attributes.ATTACK_DAMAGE);
                                d += EnchantmentHelper.getDamageBonus((ItemStack) (Object) this, MobType.UNDEFINED);
                                bl = true;
                            }
                            else if (attributeModifier.getId() == Item.BASE_ATTACK_SPEED_UUID) {
                                d += player.getAttributeBaseValue(Attributes.ATTACK_SPEED);
                                bl = true;
                            }
                        }

                        double e;
                        if (attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE && attributeModifier.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
                            if (entry.getKey().equals(Attributes.KNOCKBACK_RESISTANCE)) {
                                e = d * 10.0;
                            }
                            else {
                                e = d;
                            }
                        }
                        else {
                            e = d * 100;
                        }
                        if (bl) {
                            //noinspection ObjectAllocationInLoop
                            list.add(new TextComponent(" ").append(new TranslatableComponent("attribute.modifier.equals." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(e), new TranslatableComponent(entry.getKey().getDescriptionId()))).withStyle(ChatFormatting.DARK_GREEN));
                        }
                        else if (d > 0) {
                            //noinspection ObjectAllocationInLoop
                            list.add(new TranslatableComponent("attribute.modifier.plus." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(e), new TranslatableComponent(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.BLUE));
                        }
                        else if (d < 0) {
                            e *= -1;
                            //noinspection ObjectAllocationInLoop
                            list.add(new TranslatableComponent("attribute.modifier.take." + attributeModifier.getOperation().toValue(), ATTRIBUTE_MODIFIER_FORMAT.format(e), new TranslatableComponent(entry.getKey().getDescriptionId())).withStyle(ChatFormatting.RED));
                        }
                    }
                }
            }
        }
        if (this.hasTag()) {
            assert this.tag != null;
            if (shouldShowInTooltip(i, ItemStack.TooltipPart.UNBREAKABLE) && this.tag.getBoolean("Unbreakable")) {
                list.add(new TranslatableComponent("item.unbreakable").withStyle(ChatFormatting.BLUE));
            }

            ListTag listTag2;
            if (shouldShowInTooltip(i, ItemStack.TooltipPart.CAN_DESTROY) && this.tag.contains("CanDestroy", NBTType.LIST)) {
                listTag2 = this.tag.getList("CanDestroy", NBTType.STRING);
                if (!listTag2.isEmpty()) {
                    list.add(TextComponent.EMPTY);
                    list.add(new TranslatableComponent("item.canBreak").withStyle(ChatFormatting.GRAY));
                    for (k = 0; k < listTag2.size(); ++k) {
                        list.addAll(expandBlockState(listTag2.getString(k)));
                    }
                }
            }
            if (shouldShowInTooltip(i, ItemStack.TooltipPart.CAN_PLACE) && this.tag.contains("CanPlaceOn", NBTType.LIST)) {
                listTag2 = this.tag.getList("CanPlaceOn", NBTType.STRING);
                if (!listTag2.isEmpty()) {
                    list.add(TextComponent.EMPTY);
                    list.add(new TranslatableComponent("item.canPlace").withStyle(ChatFormatting.GRAY));
                    for (k = 0; k < listTag2.size(); ++k) {
                        list.addAll(expandBlockState(listTag2.getString(k)));
                    }
                }
            }
        }
        if (tooltipFlag.isAdvanced()) {
            if (this.isDamaged()) {
                list.add(new TranslatableComponent("item.durability", this.getMaxDamage() - this.getDamageValue(), this.getMaxDamage()));
            }
            list.add(new TextComponent(Registry.ITEM.getKey(this.getItem()).toString()).withStyle(ChatFormatting.DARK_GRAY));
            if (this.hasTag()) {
                assert this.tag != null;
                list.add(new TranslatableComponent("item.nbt_tags", this.tag.getAllKeys().size()).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return list;
    }

    @Shadow
    public abstract boolean hasAdventureModePlaceTagForBlock(Registry<Block> registry, BlockInWorld blockInWorld);

    @Shadow
    public abstract boolean hasCustomHoverName();

    @Shadow
    public abstract boolean hasTag();

    @Shadow
    public abstract boolean is(Item item);

    @Shadow
    public abstract boolean isDamaged();

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
            !this.hasAdventureModePlaceTagForBlock(level.registryAccess().registryOrThrow(Registry.BLOCK_REGISTRY), new BlockInWorld(level, new BlockPos(x, y, z), false))) {
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
