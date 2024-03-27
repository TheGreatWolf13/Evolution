package tgw.evolution.client.renderer.ambient;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.lighting.LevelLightEngine;
import tgw.evolution.util.collection.maps.L2SHashMap;
import tgw.evolution.util.collection.maps.L2SMap;
import tgw.evolution.util.collection.maps.custom.EntityLightEmissionMap;
import tgw.evolution.util.collection.sets.IHashSet;
import tgw.evolution.util.collection.sets.ISet;
import tgw.evolution.util.collection.sets.LHashSet;
import tgw.evolution.util.collection.sets.LSet;
import tgw.evolution.world.lighting.RGB;

public class DynamicLights {

    public static final int FULL_LIGHTMAP = 31 | 31 << 5 | 15 << 16 | 31 << 20;
    public static final int FULL_LIGHTMAP_NO_SKY = 31 | 31 << 5 | 31 << 20;
    private final L2SMap added = new L2SHashMap();
    private final EntityLightEmissionMap entityEmission = new EntityLightEmissionMap();
    private final ClientLevel level;
    private final L2SMap lights = new L2SHashMap();
    private final LSet modified = new LHashSet();
    private final ISet notTicked = new IHashSet();
    private final LSet removed = new LHashSet();

    public DynamicLights(ClientLevel level) {
        this.level = level;
    }

    public static boolean canSpread(int light) {
        return (light & 0xF) > 1;
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

    public static int combineComponent(int l1, int l2) {
        int range = Math.max(l1 & 0b1111, l2 & 0b1111);
        int strength = Math.max(l1 & 0b1_0000, l2 & 0b1_0000);
        return range | strength;
    }

    public static int componentsToLightmap(int red, int green, int blue, int sky) {
        assert 0 <= red && red < 32 : "Red out of bounds: " + red;
        assert 0 <= green && green < 32 : "Green out of bounds: " + green;
        assert 0 <= blue && blue < 32 : "Blue out of bounds: " + blue;
        assert 0 <= sky && sky < 16 : "Sky out of bounds: " + sky;
        return red | green << 5 | sky << 16 | blue << 20;
    }

    public static int decreaseComponent(int lightColour, int decreaseAmount) {
        int range = Math.max(0, (lightColour & 15) - decreaseAmount);
        if (range == 0) {
            return 0;
        }
        return range | lightColour & 16;
    }

    public static int decreaseLight(int original, int decreaseAmount) {
        int rr = Math.max(0, (original & 0xF) - decreaseAmount);
        int rs = rr == 0 ? 0 : original & 16;
        int gr = Math.max(0, (original >>> 5 & 0xF) - decreaseAmount);
        int gs = gr == 0 ? 0 : original & 512;
        int br = Math.max(0, (original >>> 10 & 0xF) - decreaseAmount);
        int bs = br == 0 ? 0 : original & 16_384;
        return rr | rs | gr << 5 | gs | br << 10 | bs;
    }

    public static int getComponent(int light, @RGB int colour) {
        return switch (colour) {
            case RGB.RED -> light & 31;
            case RGB.GREEN -> light >>> 5 & 31;
            case RGB.BLUE -> light >>> 10 & 31;
            default -> throw new IllegalArgumentException("Unknown colour!");
        };
    }

    public static boolean isComponentGreaterInRange(int l1, int l2) {
        if ((l1 & 0b1111) > (l2 & 0b1111)) {
            return true;
        }
        if ((l1 & 0b1111) == (l2 & 0b1111)) {
            return (l1 & 0b1_0000) > (l2 & 0b1_0000);
        }
        return false;
    }

    public static boolean isComponentTotallyGreater(int l1, int l2) {
        if ((l1 & 0b1111) > (l2 & 0b1111)) {
            return (l1 & 0b1_0000) > (l2 & 0b1_0000);
        }
        return false;
    }

    public static boolean isLightGreater(int l1, int l2) {
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

    public static int recombine(int original, int lightColour, @RGB int colour) {
        return switch (colour) {
            case RGB.RED -> original & -32 | lightColour;
            case RGB.GREEN -> original & -993 | lightColour << 5;
            case RGB.BLUE -> original & -31_745 | lightColour << 10;
            default -> throw new IllegalArgumentException("Unknown colour!");
        };
    }

    public static int removeComponent(int light, @RGB int colour) {
        return switch (colour) {
            case RGB.RED -> light & -32;
            case RGB.GREEN -> light & -993;
            case RGB.BLUE -> light & -31_745;
            default -> throw new IllegalArgumentException("Unknown colour!");
        };
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

    public void clear() {
        this.entityEmission.clear();
        L2SMap lights = this.lights;
        LSet removed = this.removed;
        removed.clear();
        for (long it = lights.beginIteration(); lights.hasNextIteration(it); it = lights.nextEntry(it)) {
            removed.add(lights.getIterationKey(it));
        }
        lights.clear();
        LevelLightEngine lightEngine = this.level.getLightEngine();
        for (long it = removed.beginIteration(); removed.hasNextIteration(it); it = removed.nextEntry(it)) {
            lightEngine.checkBlock_(removed.getIteration(it));
        }
        removed.clear();
    }

    public short getLight(long pos) {
        return this.lights.get(pos);
    }

    public void tickEnd() {
        ISet notTicked = this.notTicked;
        LSet removed = this.removed;
        for (long it = notTicked.beginIteration(); (it & 0xFFFF_FFFFL) != 0; it = notTicked.nextEntry(it)) {
            int id = notTicked.getIteration(it);
            int index = this.entityEmission.getIndexFor(id);
            long oldPos = this.entityEmission.getLongByIndex(index);
            short oldLight = this.entityEmission.getShortByIndex(index);
            this.entityEmission.remove(id);
            this.handleRemove(oldPos, oldLight);
        }
        notTicked.clear();
        L2SMap added = this.added;
        LSet modified = this.modified;
        for (long it = removed.beginIteration(); removed.hasNextIteration(it); it = removed.nextEntry(it)) {
            long pos = removed.getIteration(it);
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
        for (long it = modified.beginIteration(); modified.hasNextIteration(it); it = modified.nextEntry(it)) {
            lightEngine.checkBlock_(modified.getIteration(it));
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
}
