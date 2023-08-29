package tgw.evolution.datagen.util;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class CustomLoaderBuilder<T extends ModelBuilder<T>> {
    protected final ExistingFileHelper existingFileHelper;
    protected final ResourceLocation loaderId;
    protected final T parent;
    protected final Map<String, Boolean> visibility = new LinkedHashMap<>();

    protected CustomLoaderBuilder(ResourceLocation loaderId, T parent, ExistingFileHelper existingFileHelper) {
        this.loaderId = loaderId;
        this.parent = parent;
        this.existingFileHelper = existingFileHelper;
    }

    public T end() {
        return this.parent;
    }

    public JsonObject toJson(JsonObject json) {
        json.addProperty("loader", this.loaderId.toString());
        if (!this.visibility.isEmpty()) {
            JsonObject visibilityObj = new JsonObject();
            for (Map.Entry<String, Boolean> entry : this.visibility.entrySet()) {
                visibilityObj.addProperty(entry.getKey(), entry.getValue());
            }
            json.add("visibility", visibilityObj);
        }
        return json;
    }

    public CustomLoaderBuilder<T> visibility(String partName, boolean show) {
        Preconditions.checkNotNull(partName, "partName must not be null");
        this.visibility.put(partName, show);
        return this;
    }
}
