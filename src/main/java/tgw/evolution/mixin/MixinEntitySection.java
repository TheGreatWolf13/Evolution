package tgw.evolution.mixin;

import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.lists.OList;

import java.util.List;
import java.util.function.Consumer;

@Mixin(EntitySection.class)
public abstract class MixinEntitySection<T extends EntityAccess> {

    @Shadow @Final private ClassInstanceMultiMap<T> storage;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public <U extends T> void getEntities(EntityTypeTest<T, U> typeTest, AABB bb, Consumer<? super U> consumer) {
        OList<? extends T> list = this.storage.find_(typeTest.getBaseClass());
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
        List<T> allInstances = this.storage.getAllInstances();
        for (int i = 0, len = allInstances.size(); i < len; ++i) {
            T t = allInstances.get(i);
            if (t.getBoundingBox().intersects(bb)) {
                consumer.accept(t);
            }
        }
    }
}
