package tgw.evolution.datagen.util;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemModelBuilder extends ModelBuilder<ItemModelBuilder> {

    protected List<OverrideBuilder> overrides = new ArrayList<>();

    public ItemModelBuilder(ResourceLocation outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }

    public OverrideBuilder override() {
        OverrideBuilder ret = new OverrideBuilder();
        this.overrides.add(ret);
        return ret;
    }

    /**
     * Get an existing override builder
     *
     * @param index the index of the existing override builder
     * @return the override builder
     * @throws IndexOutOfBoundsException if {@code} index is out of bounds
     */
    public OverrideBuilder override(int index) {
        Preconditions.checkElementIndex(index, this.overrides.size(), "override");
        return this.overrides.get(index);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = super.toJson();
        if (!this.overrides.isEmpty()) {
            JsonArray overridesJson = new JsonArray();
            this.overrides.stream().map(OverrideBuilder::toJson).forEach(overridesJson::add);
            root.add("overrides", overridesJson);
        }
        return root;
    }

    public class OverrideBuilder {

        private final Map<ResourceLocation, Float> predicates = new LinkedHashMap<>();
        private ModelFile model;

        public ItemModelBuilder end() {return ItemModelBuilder.this;}

        public OverrideBuilder model(ModelFile model) {
            this.model = model;
            model.assertExistence();
            return this;
        }

        public OverrideBuilder predicate(ResourceLocation key, float value) {
            this.predicates.put(key, value);
            return this;
        }

        JsonObject toJson() {
            JsonObject ret = new JsonObject();
            JsonObject predicatesJson = new JsonObject();
            this.predicates.forEach((key, val) -> predicatesJson.addProperty(key.toString(), val));
            ret.add("predicate", predicatesJson);
            ret.addProperty("model", this.model.getLocation().toString());
            return ret;
        }
    }
}
