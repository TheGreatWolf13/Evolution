package tgw.evolution.mixin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntList;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.patches.PatchVertexFormat;
import tgw.evolution.util.collection.lists.IArrayList;
import tgw.evolution.util.collection.lists.IList;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.O2OLinkedHashMap;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.stream.Collectors;

@Mixin(VertexFormat.class)
public abstract class Mixin_CF_VertexFormat implements PatchVertexFormat {

    @Shadow @Final @DeleteField private ImmutableMap<String, VertexFormatElement> elementMapping;
    @Unique private final O2OMap<String, VertexFormatElement> elementMapping_;
    @Shadow @Final @DeleteField private ImmutableList<VertexFormatElement> elements;
    @Unique private final OList<VertexFormatElement> elements_;
    @Shadow @Final @DeleteField private IntList offsets;
    @Unique private final IList offsets_;
    @Mutable @Shadow @Final @RestoreFinal private int vertexSize;

    @ModifyConstructor
    public Mixin_CF_VertexFormat(ImmutableMap<String, VertexFormatElement> map) {
        this.offsets_ = new IArrayList();
        this.elementMapping_ = new O2OLinkedHashMap<>(map);
        this.elements_ = new OArrayList<>(map.values());
        int i = 0;
        for (long it = this.elementMapping_.beginIteration(); this.elementMapping_.hasNextIteration(it); it = this.elementMapping_.nextEntry(it)) {
            this.offsets_.add(i);
            i += this.elementMapping_.getIterationValue(it).getByteSize();
        }
        this.offsets_.trim();
        this.elementMapping_.trim();
        this.elements_.trim();
        this.vertexSize = i;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object != null && this.getClass() == object.getClass()) {
            VertexFormat vertexFormat = (VertexFormat) object;
            return this.vertexSize == ((Mixin_CF_VertexFormat) (Object) vertexFormat).vertexSize && this.elementMapping_.equals(((Mixin_CF_VertexFormat) (Object) vertexFormat).elementMapping_);
        }
        return false;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ImmutableList<String> getElementAttributeNames() {
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        O2OMap<String, VertexFormatElement> elementMapping = this.elementMapping_;
        for (long it = elementMapping.beginIteration(); elementMapping.hasNextIteration(it); it = elementMapping.nextEntry(it)) {
            builder.add(elementMapping.getIterationKey(it));
        }
        return builder.build();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ImmutableList<VertexFormatElement> getElements() {
        Evolution.deprecatedMethod();
        return ImmutableList.copyOf(this.elements_);
    }

    @Override
    public OList<VertexFormatElement> getElements_() {
        return this.elements_.view();
    }

    @Shadow
    public abstract int getVertexSize();

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public int hashCode() {
        return this.elementMapping_.hashCode();
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Override
    @Overwrite
    public String toString() {
        return "format: " + this.elementMapping_.size() + " elements: " + this.elementMapping_.entrySet().stream().map(Object::toString).collect(Collectors.joining(" "));
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void _clearBufferState() {
        OList<VertexFormatElement> list = this.getElements_();
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).clearBufferState(i);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void _setupBufferState() {
        int size = this.getVertexSize();
        OList<VertexFormatElement> list = this.getElements_();
        for (int i = 0, len = list.size(); i < len; ++i) {
            list.get(i).setupBufferState(i, this.offsets_.getInt(i), size);
        }
    }
}
