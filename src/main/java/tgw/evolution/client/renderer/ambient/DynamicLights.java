package tgw.evolution.client.renderer.ambient;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.lighting.LevelLightEngine;
import tgw.evolution.util.collection.maps.I2LBHashMap;
import tgw.evolution.util.collection.maps.L2BHashMap;
import tgw.evolution.util.collection.maps.L2BMap;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

public class DynamicLights {

    private final L2BMap added = new L2BHashMap();
    private final I2LBHashMap entityEmission = new I2LBHashMap();
    private final ClientLevel level;
    private final L2BMap lights = new L2BHashMap();
    private final LSet modified = new LHashSet();
    private final ISet notTicked = new IHashSet();
    private final LSet removed = new LHashSet();

    public DynamicLights(ClientLevel level) {
        this.level = level;
    }

    public void clear() {
        this.entityEmission.clear();
        L2BMap lights = this.lights;
        LSet removed = this.removed;
        removed.clear();
        for (L2BMap.Entry e = lights.fastEntries(); e != null; e = lights.fastEntries()) {
            removed.add(e.key());
        }
        lights.clear();
        LevelLightEngine lightEngine = this.level.getLightEngine();
        for (LSet.Entry e = removed.fastEntries(); e != null; e = removed.fastEntries()) {
            lightEngine.checkBlock_(e.get());
        }
        removed.clear();
    }

    public int get(long pos) {
        return this.lights.get(pos);
    }

    private void handleAdd(long pos, byte light) {
        byte currLight = this.lights.get(pos);
        if (light > currLight) {
            this.lights.put(pos, light);
            this.modified.add(pos);
        }
        else {
            byte maxAdded = this.added.get(pos);
            if (light > maxAdded) {
                this.added.put(pos, light);
            }
        }
    }

    private void handleRemove(long pos, byte light) {
        byte currLight = this.lights.get(pos);
        if (currLight == light) {
            this.removed.add(pos);
        }
    }

    private void handleReplace(long pos, byte oldLight, byte light) {
        byte currLight = this.lights.get(pos);
        if (light > currLight) {
            this.lights.put(pos, light);
            this.modified.add(pos);
        }
        else if (oldLight == currLight) {
            byte maxAdded = this.added.get(pos);
            if (light > maxAdded) {
                this.added.put(pos, light);
            }
        }
    }

    public void tickEnd() {
        ISet notTicked = this.notTicked;
        LSet removed = this.removed;
        for (ISet.Entry e = notTicked.fastEntries(); e != null; e = notTicked.fastEntries()) {
            int index = this.entityEmission.getIndexFor(e.get());
            long oldPos = this.entityEmission.getLongByIndex(index);
            byte oldLight = this.entityEmission.getByteByIndex(index);
            this.entityEmission.remove(e.get());
            this.handleRemove(oldPos, oldLight);
        }
        notTicked.clear();
        L2BMap added = this.added;
        LSet modified = this.modified;
        for (LSet.Entry e = removed.fastEntries(); e != null; e = removed.fastEntries()) {
            long pos = e.get();
            byte maxAdded = added.get(pos);
            if (maxAdded == 0) {
                this.lights.remove(pos);
            }
            else {
                this.lights.put(pos, maxAdded);
            }
            modified.add(pos);
        }
        added.clear();
        removed.clear();
        LevelLightEngine lightEngine = this.level.getLightEngine();
        for (LSet.Entry e = modified.fastEntries(); e != null; e = modified.fastEntries()) {
            lightEngine.checkBlock_(e.get());
        }
        modified.clear();
    }

    public void tickStart() {
        this.entityEmission.getAll(this.notTicked);
    }

    public void update(Entity entity) {
        this.notTicked.remove(entity.getId());
        byte light = entity.getLightEmission();
        long pos = entity.getLightEmissionPos();
        int index = this.entityEmission.getIndexFor(entity.getId());
        if (index < 0) {
            //There was no registry of this entity
            if (light > 0) {
                this.entityEmission.put(entity.getId(), pos, light);
                this.handleAdd(pos, light);
            }
            return;
        }
        //This entity already had light
        long oldPos = this.entityEmission.getLongByIndex(index);
        byte oldLight = this.entityEmission.getByteByIndex(index);
        if (oldPos != pos) {
            //Moved
            if (light == 0) {
                this.entityEmission.remove(entity.getId());
                this.handleRemove(oldPos, oldLight);
            }
            else {
                this.entityEmission.put(entity.getId(), pos, light);
                this.handleRemove(oldPos, oldLight);
                this.handleAdd(pos, light);
            }
            return;
        }
        if (oldLight != light) {
            //Didn't move but light changed
            if (light == 0) {
                this.entityEmission.remove(entity.getId());
                this.handleRemove(pos, oldLight);
            }
            else {
                this.entityEmission.put(entity.getId(), pos, light);
                this.handleReplace(pos, oldLight, light);
            }
            return;
        }
        byte maxAdded = this.added.get(pos);
        if (light > maxAdded) {
            this.added.put(pos, light);
        }
    }
}
