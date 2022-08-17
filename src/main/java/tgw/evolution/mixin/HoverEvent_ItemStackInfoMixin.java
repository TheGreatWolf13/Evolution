package tgw.evolution.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tgw.evolution.Evolution;
import tgw.evolution.patches.IItemStackInfoPatch;
import tgw.evolution.patches.IItemStackPatch;

import javax.annotation.Nullable;
import java.util.Objects;

@SuppressWarnings("NonFinalFieldReferencedInHashCode")
@Mixin(HoverEvent.ItemStackInfo.class)
public abstract class HoverEvent_ItemStackInfoMixin implements IItemStackInfoPatch {

    @Nullable
    private CompoundTag capNBT;
    @Shadow
    @Final
    private int count;
    @Shadow
    @Final
    private Item item;
    @Shadow
    @Nullable
    private ItemStack itemStack;
    @Shadow
    @Final
    @Nullable
    private CompoundTag tag;

    /**
     * @author TheGreatWolf
     * <p>
     * Handle new field CapNBT
     */
    @Overwrite
    private static HoverEvent.ItemStackInfo create(JsonElement element) {
        if (element.isJsonPrimitive()) {
            return new HoverEvent.ItemStackInfo(Registry.ITEM.get(new ResourceLocation(element.getAsString())), 1, null);
        }
        JsonObject json = GsonHelper.convertToJsonObject(element, "item");
        Item item = Registry.ITEM.get(new ResourceLocation(GsonHelper.getAsString(json, "id")));
        int count = GsonHelper.getAsInt(json, "count", 1);
        CompoundTag tag = null;
        if (json.has("tag")) {
            String s = GsonHelper.getAsString(json, "tag");
            try {
                tag = TagParser.parseTag(s);
            }
            catch (CommandSyntaxException warn) {
                Evolution.warn("Failed to parse tag: {}", s, warn);
            }
        }
        CompoundTag capNBT = null;
        if (json.has("ForgeCaps")) {
            String s = GsonHelper.getAsString(json, "ForgeCaps");
            try {
                capNBT = TagParser.parseTag(s);
            }
            catch (CommandSyntaxException warn) {
                Evolution.warn("Failed to parse ForgeCaps: {}", s, warn);
            }
        }
        HoverEvent.ItemStackInfo info = new HoverEvent.ItemStackInfo(item, count, tag);
        ((IItemStackInfoPatch) info).setCapNBT(capNBT);
        return info;
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Handle new field CapNBT
     */
    @Override
    @Overwrite
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof IItemStackInfoPatch itemStackInfo) {
            return this.count == itemStackInfo.getCount() &&
                   this.item.equals(itemStackInfo.getItem()) &&
                   Objects.equals(this.tag, itemStackInfo.getTag()) &&
                   Objects.equals(this.capNBT, itemStackInfo.getCapNBT());
        }
        return false;
    }

    @Nullable
    @Override
    public CompoundTag getCapNBT() {
        return this.capNBT;
    }

    @Override
    public int getCount() {
        return this.count;
    }

    @Override
    public Item getItem() {
        return this.item;
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Handle new field CapNBT
     */
    @Overwrite
    public ItemStack getItemStack() {
        if (this.itemStack == null) {
            this.itemStack = new ItemStack(this.item, this.count, this.capNBT);
            if (this.tag != null) {
                this.itemStack.setTag(this.tag);
            }
        }
        return this.itemStack;
    }

    @Nullable
    @Override
    public CompoundTag getTag() {
        return this.tag;
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Handle new field CapNBT
     */
    @Overwrite
    @Override
    public int hashCode() {
        int hash = this.item.hashCode();
        hash = 31 * hash + this.count;
        hash = 31 * hash + (this.tag != null ? this.tag.hashCode() : 0);
        hash = 31 * hash + (this.capNBT != null ? this.capNBT.hashCode() : 0);
        return hash;
    }

    @Inject(method = "<init>(Lnet/minecraft/world/item/ItemStack;)V", at = @At("TAIL"))
    private void onConstructor(ItemStack stack, CallbackInfo ci) {
        this.capNBT = ((IItemStackPatch) (Object) stack).getCapNBT();
    }

    /**
     * @author TheGreatWolf
     * <p>
     * Handle new field CapNBT
     */
    @Overwrite
    private JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", Registry.ITEM.getKey(this.item).toString());
        if (this.count != 1) {
            json.addProperty("count", this.count);
        }
        if (this.tag != null) {
            json.addProperty("tag", this.tag.toString());
        }
        if (this.capNBT != null) {
            json.addProperty("ForgeCaps", this.capNBT.toString());
        }
        return json;
    }

    @Override
    public void setCapNBT(@Nullable CompoundTag capNBT) {
        this.capNBT = capNBT;
    }
}
