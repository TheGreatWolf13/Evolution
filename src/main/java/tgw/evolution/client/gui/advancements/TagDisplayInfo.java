package tgw.evolution.client.gui.advancements;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.Util;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.util.collection.OArrayList;

import java.util.List;

public class TagDisplayInfo extends DisplayInfo {

    private final TagKey<Item> tag;
    private @Nullable List<ItemStack> icons;
    private int index;
    private long lastTime = -1;

    public TagDisplayInfo(TagKey<Item> tag,
                          Component title,
                          Component description,
                          @Nullable ResourceLocation background,
                          FrameType frame, boolean showToast, boolean announceChat, boolean hidden) {
        super(ItemStack.EMPTY, title, description, background, frame, showToast, announceChat, hidden);
        this.tag = tag;
    }

    @Override
    public ItemStack getIcon() {
        if (this.icons == null) {
            List<ItemStack> list = new OArrayList<>();
            for (Item item : ForgeRegistries.ITEMS.getValues()) {
                if (item.builtInRegistryHolder().is(this.tag)) {
                    list.add(item.getDefaultInstance());
                }
            }
            if (list.isEmpty()) {
                this.icons = List.of();
            }
            else {
                this.icons = list;
            }
        }
        if (this.icons.isEmpty()) {
            return ItemStack.EMPTY;
        }
        long now = Util.getMillis();
        if (this.lastTime == -1) {
            this.lastTime = now;
        }
        else if (now >= this.lastTime + 1_500) {
            this.lastTime = now;
            if (++this.index >= this.icons.size()) {
                this.index = 0;
            }
        }
        return this.icons.get(this.index);
    }

    private JsonObject serializeIcon() {
        JsonObject json = new JsonObject();
        json.addProperty("tag", this.tag.location().toString());
        return json;
    }

    @Override
    public JsonElement serializeToJson() {
        JsonObject json = new JsonObject();
        json.add("icon", this.serializeIcon());
        json.add("title", Component.Serializer.toJsonTree(this.getTitle()));
        json.add("description", Component.Serializer.toJsonTree(this.getDescription()));
        json.addProperty("frame", this.getFrame().getName());
        json.addProperty("show_toast", this.shouldShowToast());
        json.addProperty("announce_to_chat", this.shouldAnnounceChat());
        json.addProperty("hidden", this.isHidden());
        ResourceLocation background = this.getBackground();
        if (background != null) {
            json.addProperty("background", background.toString());
        }
        return json;
    }

    @Override
    public void serializeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeBoolean(true);
        buffer.writeComponent(this.getTitle());
        buffer.writeComponent(this.getDescription());
        buffer.writeResourceLocation(this.tag.location());
        buffer.writeEnum(this.getFrame());
        int i = 0;
        ResourceLocation background = this.getBackground();
        //noinspection VariableNotUsedInsideIf
        if (background != null) {
            i |= 1;
        }
        if (this.shouldShowToast()) {
            i |= 2;
        }
        if (this.isHidden()) {
            i |= 4;
        }
        buffer.writeInt(i);
        if (background != null) {
            buffer.writeResourceLocation(background);
        }
        buffer.writeFloat(this.getX());
        buffer.writeFloat(this.getY());
    }
}
