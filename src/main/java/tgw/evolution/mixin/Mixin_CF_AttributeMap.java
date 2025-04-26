package tgw.evolution.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.ai.attributes.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.hooks.asm.DeleteField;
import tgw.evolution.hooks.asm.ModifyConstructor;
import tgw.evolution.hooks.asm.RestoreFinal;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.maps.R2OHashMap;
import tgw.evolution.util.collection.maps.R2OMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mixin(AttributeMap.class)
public abstract class Mixin_CF_AttributeMap {

    @Shadow @Final @DeleteField private Map<Attribute, AttributeInstance> attributes;
    @Unique private final R2OMap<Attribute, AttributeInstance> attributes_;
    @Shadow @Final @DeleteField private Set<AttributeInstance> dirtyAttributes;
    @Unique private final OSet<AttributeInstance> dirtyAttributes_;
    @Mutable @Shadow @Final @RestoreFinal private AttributeSupplier supplier;

    @ModifyConstructor
    public Mixin_CF_AttributeMap(AttributeSupplier supplier) {
        this.attributes_ = new R2OHashMap<>();
        this.dirtyAttributes_ = new OHashSet<>();
        this.supplier = supplier;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public double getBaseValue(Attribute attribute) {
        AttributeInstance instance = this.attributes_.get(attribute);
        return instance != null ? instance.getBaseValue() : this.supplier.getBaseValue(attribute);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Set<AttributeInstance> getDirtyAttributes() {
        return this.dirtyAttributes_;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public @Nullable AttributeInstance getInstance(Attribute attribute) {
        R2OMap<Attribute, AttributeInstance> attributes = this.attributes_;
        AttributeInstance instance = attributes.get(attribute);
        if (instance == null) {
            instance = this.supplier.createInstance(this::onAttributeModified, attribute);
            attributes.put(attribute, instance);
        }
        return instance;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public double getModifierValue(Attribute attribute, UUID uUID) {
        AttributeInstance instance = this.attributes_.get(attribute);
        //noinspection DataFlowIssue
        return instance != null ? instance.getModifier(uUID).getAmount() : this.supplier.getModifierValue(attribute, uUID);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public Collection<AttributeInstance> getSyncableAttributes() {
        OList<AttributeInstance> list = null;
        R2OMap<Attribute, AttributeInstance> attributes = this.attributes_;
        for (long it = attributes.beginIteration(); attributes.hasNextIteration(it); it = attributes.nextEntry(it)) {
            AttributeInstance instance = attributes.getIterationValue(it);
            if (instance.getAttribute().isClientSyncable()) {
                if (list == null) {
                    list = new OArrayList<>();
                }
                list.add(instance);
            }
        }
        return list == null ? OList.emptyList() : list;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public double getValue(Attribute attribute) {
        AttributeInstance instance = this.attributes_.get(attribute);
        return instance != null ? instance.getValue() : this.supplier.getValue(attribute);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean hasAttribute(Attribute attribute) {
        return this.attributes_.get(attribute) != null || this.supplier.hasAttribute(attribute);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public boolean hasModifier(Attribute attribute, UUID uUID) {
        AttributeInstance instance = this.attributes_.get(attribute);
        return instance != null ? instance.getModifier(uUID) != null : this.supplier.hasModifier(attribute, uUID);
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    private void onAttributeModified(AttributeInstance instance) {
        if (instance.getAttribute().isClientSyncable()) {
            this.dirtyAttributes_.add(instance);
        }
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void removeAttributeModifiers(Multimap<Attribute, AttributeModifier> multimap) {
        multimap.asMap().forEach((attribute, collection) -> {
            AttributeInstance instance = this.attributes_.get(attribute);
            if (instance != null) {
                collection.forEach(instance::removeModifier);
            }
        });
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public ListTag save() {
        ListTag listTag = new ListTag();
        R2OMap<Attribute, AttributeInstance> attributes = this.attributes_;
        for (long it = attributes.beginIteration(); attributes.hasNextIteration(it); it = attributes.nextEntry(it)) {
            listTag.add(attributes.getIterationValue(it).save());
        }
        return listTag;
    }
}
