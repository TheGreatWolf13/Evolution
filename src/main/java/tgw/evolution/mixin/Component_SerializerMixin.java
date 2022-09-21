package tgw.evolution.mixin;

import com.google.gson.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.client.text.CappedComponent;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

@Mixin(Component.Serializer.class)
public abstract class Component_SerializerMixin {

    /**
     * @author TheGreatWolf
     * @reason Add CappedComponent
     */
    @Overwrite
    public MutableComponent deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonPrimitive()) {
            return new TextComponent(json.getAsString());
        }
        if (!json.isJsonObject()) {
            if (json.isJsonArray()) {
                JsonArray array = json.getAsJsonArray();
                MutableComponent comp = null;
                for (JsonElement j : array) {
                    MutableComponent c = this.deserialize(j, j.getClass(), context);
                    if (comp == null) {
                        comp = c;
                    }
                    else {
                        comp.append(c);
                    }
                }
                assert comp != null;
                return comp;
            }
            throw new JsonParseException("Don't know how to turn " + json + " into a Component");
        }
        JsonObject o = json.getAsJsonObject();
        MutableComponent comp;
        if (o.has("text")) {
            comp = new TextComponent(GsonHelper.getAsString(o, "text"));
        }
        else if (o.has("translate")) {
            String s = GsonHelper.getAsString(o, "translate");
            if (o.has("with")) {
                JsonArray jsonarray = GsonHelper.getAsJsonArray(o, "with");
                Object[] aobject = new Object[jsonarray.size()];
                for (int i = 0; i < aobject.length; ++i) {
                    aobject[i] = this.deserialize(jsonarray.get(i), type, context);
                    if (aobject[i] instanceof TextComponent t) {
                        if (t.getStyle().isEmpty() && t.getSiblings().isEmpty()) {
                            aobject[i] = t.getText();
                        }
                    }
                }
                comp = new TranslatableComponent(s, aobject);
            }
            else {
                comp = new TranslatableComponent(s);
            }
        }
        else if (o.has("score")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(o, "score");
            if (!jsonobject1.has("name") || !jsonobject1.has("objective")) {
                throw new JsonParseException("A score component needs a least a name and an objective");
            }
            comp = new ScoreComponent(GsonHelper.getAsString(jsonobject1, "name"), GsonHelper.getAsString(jsonobject1, "objective"));
        }
        else if (o.has("selector")) {
            Optional<Component> optional = this.parseSeparator(type, context, o);
            comp = new SelectorComponent(GsonHelper.getAsString(o, "selector"), optional);
        }
        else if (o.has("keybind")) {
            comp = new KeybindComponent(GsonHelper.getAsString(o, "keybind"));
        }
        else if (o.has("base")) {
            Component base = this.deserialize(o.get("base"), type, context);
            int width = o.get("width").getAsInt();
            Component widthLimiter = null;
            if (o.has("widthLimiter")) {
                widthLimiter = this.deserialize(o.get("widthLimiter"), type, context);
            }
            comp = new CappedComponent(base, width, widthLimiter);
        }
        else {
            if (!o.has("nbt")) {
                throw new JsonParseException("Don't know how to turn " + json + " into a Component");
            }
            String s1 = GsonHelper.getAsString(o, "nbt");
            Optional<Component> optional1 = this.parseSeparator(type, context, o);
            boolean flag = GsonHelper.getAsBoolean(o, "interpret", false);
            if (o.has("block")) {
                comp = new NbtComponent.BlockNbtComponent(s1, flag, GsonHelper.getAsString(o, "block"), optional1);
            }
            else if (o.has("entity")) {
                comp = new NbtComponent.EntityNbtComponent(s1, flag, GsonHelper.getAsString(o, "entity"), optional1);
            }
            else {
                if (!o.has("storage")) {
                    throw new JsonParseException("Don't know how to turn " + json + " into a Component");
                }
                comp = new NbtComponent.StorageNbtComponent(s1, flag, new ResourceLocation(
                        GsonHelper.getAsString(o, "storage")), optional1);
            }
        }
        if (o.has("extra")) {
            JsonArray jsonarray2 = GsonHelper.getAsJsonArray(o, "extra");
            if (jsonarray2.size() <= 0) {
                throw new JsonParseException("Unexpected empty array of components");
            }
            for (int j = 0; j < jsonarray2.size(); ++j) {
                comp.append(this.deserialize(jsonarray2.get(j), type, context));
            }
        }
        comp.setStyle(context.deserialize(json, Style.class));
        return comp;
    }

    @Shadow
    protected abstract Optional<Component> parseSeparator(Type pType, JsonDeserializationContext pJsonContext, JsonObject pJsonObject);

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations, add CappedComponent
     */
    @Overwrite
    public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        if (!src.getStyle().isEmpty()) {
            this.serializeStyle(src.getStyle(), json, context);
        }
        if (!src.getSiblings().isEmpty()) {
            JsonArray array = new JsonArray();
            List<Component> siblings = src.getSiblings();
            for (int i = 0, l = siblings.size(); i < l; i++) {
                Component component = siblings.get(i);
                array.add(this.serialize(component, component.getClass(), context));
            }
            json.add("extra", array);
        }
        if (src instanceof TextComponent t) {
            json.addProperty("text", t.getText());
        }
        else if (src instanceof TranslatableComponent t) {
            json.addProperty("translate", t.getKey());
            if (t.getArgs().length > 0) {
                JsonArray array = new JsonArray();
                for (Object o : t.getArgs()) {
                    if (o instanceof Component) {
                        array.add(this.serialize((Component) o, o.getClass(), context));
                    }
                    else {
                        //noinspection ObjectAllocationInLoop
                        array.add(new JsonPrimitive(String.valueOf(o)));
                    }
                }
                json.add("with", array);
            }
        }
        else if (src instanceof ScoreComponent s) {
            JsonObject o = new JsonObject();
            o.addProperty("name", s.getName());
            o.addProperty("objective", s.getObjective());
            json.add("score", o);
        }
        else if (src instanceof SelectorComponent s) {
            json.addProperty("selector", s.getPattern());
            this.serializeSeparator(context, json, s.getSeparator());
        }
        else if (src instanceof KeybindComponent k) {
            json.addProperty("keybind", k.getName());
        }
        else if (src instanceof CappedComponent c) {
            Component parent = c.getBase();
            json.add("parent", this.serialize(parent, parent.getClass(), context));
            json.addProperty("width", c.getWidth());
            Component widthLimiter = c.getWidthLimiter();
            if (widthLimiter != null) {
                json.add("widthLimiter", this.serialize(widthLimiter, widthLimiter.getClass(), context));
            }
        }
        else {
            if (!(src instanceof NbtComponent n)) {
                throw new IllegalArgumentException("Don't know how to serialize " + src + " as a Component");
            }
            json.addProperty("nbt", n.getNbtPath());
            json.addProperty("interpret", n.isInterpreting());
            this.serializeSeparator(context, json, n.separator);
            if (src instanceof NbtComponent.BlockNbtComponent b) {
                json.addProperty("block", b.getPos());
            }
            else if (src instanceof NbtComponent.EntityNbtComponent e) {
                json.addProperty("entity", e.getSelector());
            }
            else {
                if (!(src instanceof NbtComponent.StorageNbtComponent s)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + src + " as a Component");
                }
                json.addProperty("storage", s.getId().toString());
            }
        }
        return json;
    }

    @Shadow
    protected abstract void serializeSeparator(JsonSerializationContext pContext,
                                               JsonObject pJson,
                                               Optional<Component> pSeparator);

    @Shadow
    protected abstract void serializeStyle(Style pStyle, JsonObject pObject, JsonSerializationContext pCtx);
}
