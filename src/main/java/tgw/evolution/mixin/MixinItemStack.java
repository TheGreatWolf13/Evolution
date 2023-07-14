package tgw.evolution.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
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
}
