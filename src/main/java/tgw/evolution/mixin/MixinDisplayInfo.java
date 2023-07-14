package tgw.evolution.mixin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.gui.advancements.TagDisplayInfo;

@Mixin(DisplayInfo.class)
public abstract class MixinDisplayInfo {

    @Shadow @Final private @Nullable ResourceLocation background;
    @Shadow @Final private Component description;
    @Shadow @Final private FrameType frame;
    @Shadow @Final private boolean hidden;
    @Shadow @Final private ItemStack icon;
    @Shadow @Final private boolean showToast;
    @Shadow @Final private Component title;
    @Shadow private float x;
    @Shadow private float y;

    /**
     * @author TheGreatWolf
     * @reason Add support for ForgeCaps and tags.
     */
    @Overwrite
    public static DisplayInfo fromJson(JsonObject pJson) {
        Component title = Component.Serializer.fromJson(pJson.get("title"));
        Component description = Component.Serializer.fromJson(pJson.get("description"));
        if (title == null || description == null) {
            throw new JsonSyntaxException("Both title and description must be set");
        }
        JsonObject jsonIcon = GsonHelper.getAsJsonObject(pJson, "icon");
        ItemStack icon = getIcon(jsonIcon);
        ResourceLocation background = pJson.has("background") ? new ResourceLocation(GsonHelper.getAsString(pJson, "background")) : null;
        FrameType frameType = pJson.has("frame") ? FrameType.byName(GsonHelper.getAsString(pJson, "frame")) : FrameType.TASK;
        boolean showToast = GsonHelper.getAsBoolean(pJson, "show_toast", true);
        boolean announceToChat = GsonHelper.getAsBoolean(pJson, "announce_to_chat", true);
        boolean hidden = GsonHelper.getAsBoolean(pJson, "hidden", false);
        if (icon != null) {
            return new DisplayInfo(icon, title, description, background, frameType, showToast, announceToChat, hidden);
        }
        return new TagDisplayInfo(getTag(jsonIcon), title, description, background, frameType, showToast, announceToChat, hidden);
    }

    /**
     * @author TheGreatWolf
     * @reason Add support for tags.
     */
    @Overwrite
    public static DisplayInfo fromNetwork(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            Component title = buffer.readComponent();
            Component description = buffer.readComponent();
            ItemStack icon = buffer.readItem();
            FrameType frameType = buffer.readEnum(FrameType.class);
            int i = buffer.readVarInt();
            ResourceLocation background = (i & 1) != 0 ? buffer.readResourceLocation() : null;
            boolean showToast = (i & 2) != 0;
            boolean hidden = (i & 4) != 0;
            DisplayInfo displayInfo = new DisplayInfo(icon, title, description, background, frameType, showToast, false, hidden);
            displayInfo.setLocation(buffer.readFloat(), buffer.readFloat());
            return displayInfo;
        }
        Component title = buffer.readComponent();
        Component description = buffer.readComponent();
        TagKey<Item> tag = TagKey.create(Registry.ITEM_REGISTRY, buffer.readResourceLocation());
        FrameType frameType = buffer.readEnum(FrameType.class);
        int i = buffer.readVarInt();
        ResourceLocation background = (i & 1) != 0 ? buffer.readResourceLocation() : null;
        boolean showToast = (i & 2) != 0;
        boolean hidden = (i & 4) != 0;
        TagDisplayInfo displayInfo = new TagDisplayInfo(tag, title, description, background, frameType, showToast, false, hidden);
        displayInfo.setLocation(buffer.readFloat(), buffer.readFloat());
        return displayInfo;
    }

    @Shadow
    private static ItemStack getIcon(JsonObject jsonObject) {
        throw new AbstractMethodError();
    }

    private static TagKey<Item> getTag(JsonObject json) {
        String name = GsonHelper.getAsString(json, "tag");
        return TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation(name));
    }

    /**
     * @author TheGreatWolf
     * @reason Add support for tags.
     */
    @Overwrite
    public void serializeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(false);
        buffer.writeComponent(this.title);
        buffer.writeComponent(this.description);
        buffer.writeItem(this.icon);
        buffer.writeEnum(this.frame);
        int i = 0;
        //noinspection VariableNotUsedInsideIf
        if (this.background != null) {
            i |= 1;
        }
        if (this.showToast) {
            i |= 2;
        }
        if (this.hidden) {
            i |= 4;
        }
        buffer.writeVarInt(i);
        if (this.background != null) {
            buffer.writeResourceLocation(this.background);
        }
        buffer.writeFloat(this.x);
        buffer.writeFloat(this.y);
    }
}
