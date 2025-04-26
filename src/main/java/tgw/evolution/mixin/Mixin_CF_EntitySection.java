package tgw.evolution.mixin;

import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.Visibility;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.patches.PatchEntitySection;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.custom.ClassInstanceMultiMap;

import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(EntitySection.class)
public abstract class Mixin_CF_EntitySection<T extends EntityAccess> implements PatchEntitySection<T> {

    @Shadow private Visibility chunkStatus;
    @Shadow @Final @DeleteField private net.minecraft.util.ClassInstanceMultiMap<T> storage;
    @Unique private final ClassInstanceMultiMap<T> storage_;

    @ModifyConstructor
    public Mixin_CF_EntitySection(Class<T> baseClass, Visibility visibility) {
        this.chunkStatus = visibility;
        this.storage_ = new ClassInstanceMultiMap<>(baseClass);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void add(T t) {
        this.storage_.add(t);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public <U extends T> void getEntities(EntityTypeTest<T, U> typeTest, AABB bb, Consumer<? super U> consumer) {
        OList<T> list = this.storage_.find((Class<T>) typeTest.getBaseClass());
        for (int i = 0, len = list.size(); i < len; ++i) {
            T t = list.get(i);
            U u = typeTest.tryCast(t);
            if (u != null && t.getBoundingBox().intersects(bb)) {
                consumer.accept(u);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void getEntities(AABB bb, Consumer<T> consumer) {
        OList<T> allInstances = this.storage_.getAllInstances();
        for (int i = 0, len = allInstances.size(); i < len; ++i) {
            T t = allInstances.get(i);
            if (t.getBoundingBox().intersects(bb)) {
                consumer.accept(t);
            }
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Stream<T> getEntities() {
        Evolution.deprecatedMethod();
        return this.storage_.stream();
    }

    @Override
    public OList<T> getEntities_() {
        return this.storage_.getAllInstances();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean isEmpty() {
        return this.storage_.isEmpty();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean remove(T t) {
        return this.storage_.remove(t);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @VisibleForDebug
    public int size() {
        return this.storage_.size();
    }
}
