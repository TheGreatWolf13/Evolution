package tgw.evolution.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.server.bossevents.CustomBossEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.maps.O2OMap;

import java.util.Map;

@Mixin(CustomBossEvents.class)
public abstract class MixinCustomBossEvents {

    @Shadow @Final private Map<ResourceLocation, CustomBossEvent> events;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public void load(CompoundTag tag) {
        O2OMap<String, Tag> tags = tag.tags();
        for (long it = tags.beginIteration(); tags.hasNextIteration(it); it = tags.nextEntry(it)) {
            String key = tags.getIterationKey(it);
            //noinspection ObjectAllocationInLoop
            ResourceLocation resourceLocation = new ResourceLocation(key);
            //noinspection ObjectAllocationInLoop
            this.events.put(resourceLocation, CustomBossEvent.load(tag.getCompound(key), resourceLocation));
        }
    }
}
