package tgw.evolution.client.models.data;

import org.jetbrains.annotations.Nullable;

public interface IModelData {

    IModelData EMPTY = new EmptyModelData();

    @Nullable <T> T getData(ModelProperty<T> prop);

    /**
     * Check if this data has a property, even if the value is {@code null}. Can be
     * used by code that intends to fill in data for a render pipeline, such as the
     * forge animation system.
     * <p>
     * IMPORTANT: {@link #getData(ModelProperty)} <em>can</em> return {@code null}
     * even if this method returns {@code true}.
     *
     * @param prop The property to check for inclusion in this model data
     * @return {@code true} if this data has the given property, even if no value is present
     */
    boolean hasProperty(ModelProperty<?> prop);

    @Nullable <T> T setData(ModelProperty<T> prop, T data);

    final class EmptyModelData implements IModelData {

        private EmptyModelData() {
        }

        @Override
        public @Nullable <T> T getData(ModelProperty<T> prop) {
            return null;
        }

        @Override
        public boolean hasProperty(ModelProperty<?> prop) {
            return false;
        }

        @Override
        public @Nullable <T> T setData(ModelProperty<T> prop, T data) {
            return null;
        }
    }
}
