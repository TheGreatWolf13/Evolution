package tgw.evolution.client.renderer.ambient;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.lighting.LevelLightEngine;
import tgw.evolution.util.collection.maps.I2LSHashMap;
import tgw.evolution.util.collection.maps.L2SHashMap;
import tgw.evolution.util.collection.maps.L2SMap;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;

public class DynamicLights {

    private final L2SMap added = new L2SHashMap();
    private final I2LSHashMap entityEmission = new I2LSHashMap();
    private final ClientLevel level;
    private final L2SMap lights = new L2SHashMap();
    private final LSet modified = new LHashSet();
    private final ISet notTicked = new IHashSet();
    private final LSet removed = new LHashSet();

    public DynamicLights(ClientLevel level) {
        this.level = level;
    }

    private static boolean atLeastOneMatches(short l1, short l2) {
        if ((l1 & 0b1111) == (l2 & 0b1111)) {
            return true;
        }
        if ((l1 & 0b1111_0_0000) == (l2 & 0b1111_0_0000)) {
            return true;
        }
        return (l1 & 0b1111_0_0000_0_0000) == (l2 & 0b1111_0_0000_0_0000);
    }

    public static short combine(int l1, int l2) {
        int rr = Math.max(l1 & 0b1111, l2 & 0b1111);
        int rs = Math.max(l1 & 0b1_0000, l2 & 0b1_0000);
        int gr = Math.max(l1 & 0b1111_0_0000, l2 & 0b1111_0_0000);
        int gs = Math.max(l1 & 0b1_0000_0_0000, l2 & 0b1_0000_0_0000);
        int br = Math.max(l1 & 0b1111_0_0000_0_0000, l2 & 0b1111_0_0000_0_0000);
        int bs = Math.max(l1 & 0b1_0000_0_0000_0_0000, l2 & 0b1_0000_0_0000_0_0000);
        return (short) (rr | rs | gr | gs | br | bs);
    }

    private static boolean isLightGreater(short l1, short l2) {
        if ((l1 & 0b1111) > (l2 & 0b1111)) {
            return true;
        }
        if ((l1 & 0b1_0000) > (l2 & 0b1_0000)) {
            return true;
        }
        if ((l1 & 0b1111_0_0000) > (l2 & 0b1111_0_0000)) {
            return true;
        }
        if ((l1 & 0b1_0000_0_0000) > (l2 & 0b1_0000_0_0000)) {
            return true;
        }
        if ((l1 & 0b1111_0_0000_0_0000) > (l2 & 0b1111_0_0000_0_0000)) {
            return true;
        }
        return (l1 & 0b1_0000_0_0000_0_0000) > (l2 & 0b1_0000_0_0000_0_0000);
    }

    public void clear() {
        this.entityEmission.clear();
        L2SMap lights = this.lights;
        LSet removed = this.removed;
        removed.clear();
        for (L2SMap.Entry e = lights.fastEntries(); e != null; e = lights.fastEntries()) {
            removed.add(e.key());
        }
        lights.clear();
        LevelLightEngine lightEngine = this.level.getLightEngine();
        for (LSet.Entry e = removed.fastEntries(); e != null; e = removed.fastEntries()) {
            lightEngine.checkBlock_(e.get());
        }
        removed.clear();
    }

    public int getBlueRange(long pos) {
        return this.lights.get(pos) >> 10 & 0b1111;
    }

    public int getGreenRange(long pos) {
        return this.lights.get(pos) >> 5 & 0b1111;
    }

    public int getRedRange(long pos) {
        return this.lights.get(pos) & 0b1111;
    }

    private void handleAdd(long pos, short light) {
        short currLight = this.lights.get(pos);
        if (isLightGreater(light, currLight)) {
            this.lights.put(pos, combine(light, currLight));
            this.modified.add(pos);
        }
        else {
            short maxAdded = this.added.get(pos);
            if (isLightGreater(light, maxAdded)) {
                this.added.put(pos, combine(light, maxAdded));
            }
        }
    }

    private void handleRemove(long pos, short light) {
        short currLight = this.lights.get(pos);
        if (atLeastOneMatches(currLight, light)) {
            this.removed.add(pos);
        }
    }

    private void handleReplace(long pos, short oldLight, short light) {
        short currLight = this.lights.get(pos);
        if (isLightGreater(light, currLight)) {
            this.lights.put(pos, light);
            this.modified.add(pos);
        }
        else if (atLeastOneMatches(oldLight, currLight)) {
            this.handleRemove(pos, oldLight);
            short maxAdded = this.added.get(pos);
            if (isLightGreater(light, maxAdded)) {
                this.added.put(pos, combine(light, maxAdded));
            }
        }
    }

    public void tickEnd() {
        ISet notTicked = this.notTicked;
        LSet removed = this.removed;
        for (ISet.Entry e = notTicked.fastEntries(); e != null; e = notTicked.fastEntries()) {
            int index = this.entityEmission.getIndexFor(e.get());
            long oldPos = this.entityEmission.getLongByIndex(index);
            short oldLight = this.entityEmission.getShortByIndex(index);
            this.entityEmission.remove(e.get());
            this.handleRemove(oldPos, oldLight);
        }
        notTicked.clear();
        L2SMap added = this.added;
        LSet modified = this.modified;
        for (LSet.Entry e = removed.fastEntries(); e != null; e = removed.fastEntries()) {
            long pos = e.get();
            short maxAdded = added.get(pos);
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
        short light = entity.getLightEmission();
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
        short oldLight = this.entityEmission.getShortByIndex(index);
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
        short maxAdded = this.added.get(pos);
        if (isLightGreater(light, maxAdded)) {
            this.added.put(pos, combine(light, maxAdded));
        }
    }
}
