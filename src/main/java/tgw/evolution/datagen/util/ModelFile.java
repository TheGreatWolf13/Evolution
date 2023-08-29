package tgw.evolution.datagen.util;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public abstract class ModelFile {

    protected static final ExistingFileHelper.ResourceType MODEL = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".json", "models");
    protected static final ExistingFileHelper.ResourceType MODEL_WITH_EXTENSION = new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, "", "models");
    protected ResourceLocation location;

    protected ModelFile(ResourceLocation location) {
        this.location = location;
    }

    /**
     * Assert that this model exists.
     *
     * @throws IllegalStateException if this model does not exist
     */
    public void assertExistence() {
        Preconditions.checkState(this.exists(), "Model at %s does not exist", this.location);
    }

    protected abstract boolean exists();

    public ResourceLocation getLocation() {
        this.assertExistence();
        return this.location;
    }

    public ResourceLocation getUncheckedLocation() {
        return this.location;
    }

    public static class UncheckedModelFile extends ModelFile {

        public UncheckedModelFile(String location) {
            this(new ResourceLocation(location));
        }

        public UncheckedModelFile(ResourceLocation location) {
            super(location);
        }

        @Override
        protected boolean exists() {
            return true;
        }
    }

    public static class ExistingModelFile extends ModelFile {
        private final ExistingFileHelper existingHelper;

        public ExistingModelFile(ResourceLocation location, ExistingFileHelper existingHelper) {
            super(location);
            this.existingHelper = existingHelper;
        }

        @Override
        protected boolean exists() {
            if (this.getUncheckedLocation().getPath().contains(".")) {
                return this.existingHelper.exists(this.getUncheckedLocation(), MODEL_WITH_EXTENSION);
            }
            return this.existingHelper.exists(this.getUncheckedLocation(), MODEL);
        }
    }
}
