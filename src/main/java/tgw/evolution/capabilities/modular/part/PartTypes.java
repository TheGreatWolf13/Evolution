package tgw.evolution.capabilities.modular.part;

import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMap;
import it.unimi.dsi.fastutil.bytes.Byte2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IAttachmentType;
import tgw.evolution.capabilities.modular.IGrabType;
import tgw.evolution.capabilities.modular.IToolType;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.items.modular.part.*;
import tgw.evolution.util.collection.maps.B2RHashMap;
import tgw.evolution.util.collection.maps.B2RMap;
import tgw.evolution.util.math.MathHelper;

import java.util.random.RandomGenerator;

public final class PartTypes {

    private PartTypes() {
    }

    /**
     * Used by Swords.<br>
     * A sword has 4 parts: its {@link Blade}, its {@link Guard}, its {@link Hilt} and its {@link Pommel}.<br>
     * A sword's guard and / or pommel are optional.<br>
     */
    public enum Blade implements IAttachmentType<Blade, ItemPartBlade, PartBlade> {
        NULL(0, "null"),
        ARMING_SWORD(1, "arming_sword"),
        KNIFE(2, "knife");

        public static final Blade[] VALUES = values();
        private static final Byte2ReferenceMap<Blade> REGISTRY;

        static {
            B2RMap<Blade> map = new B2RHashMap<>();
            for (Blade blade : VALUES) {
                if (map.put(blade.id, blade) != null) {
                    throw new IllegalStateException("Blade " + blade + " has duplicate id: " + blade.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final byte id;
        private final String name;

        Blade(int id, String name) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.component = new TranslatableComponent("evolution.part.blade." + this.name);
        }

        public static Blade byId(byte id) {
            Blade blade = REGISTRY.get(id);
            if (blade == null) {
                return NULL;
            }
            return blade;
        }

        public static Blade getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return this != NULL;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            //TODO implementation
            return 0;
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            //TODO implementation
            return switch (this) {
                case NULL -> 0;
                case ARMING_SWORD -> 57.5;
                case KNIFE -> Double.NaN;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public ItemPartBlade partItem() {
            return EvolutionItems.PART_BLADE;
        }
    }

    /**
     * Used by Swords.<br>
     * A sword has 4 parts: its {@link Blade}, its {@link Guard}, its {@link Hilt} and its {@link Pommel}.<br>
     * A sword's guard and / or pommel are optional.<br>
     */
    public enum Guard implements IAttachmentType<Guard, ItemPartGuard, PartGuard> {
        NULL(0, "null"),
        CROSSGUARD(1, "crossguard");

        public static final Guard[] VALUES = values();
        private static final Byte2ReferenceMap<Guard> REGISTRY;

        static {
            B2RMap<Guard> map = new B2RHashMap<>();
            for (Guard guard : VALUES) {
                if (map.put(guard.id, guard) != null) {
                    throw new IllegalStateException("Guard " + guard + " has duplicate id: " + guard.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final byte id;
        private final String name;

        Guard(int id, String name) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.component = new TranslatableComponent("evolution.part.guard." + this.name);
        }

        public static Guard byId(byte id) {
            Guard guard = REGISTRY.get(id);
            if (guard == null) {
                return NULL;
            }
            return guard;
        }

        public static Guard getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return false;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            //TODO implementation
            return 0;
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case CROSSGUARD -> Double.NaN;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public ItemPartGuard partItem() {
            return EvolutionItems.PART_GUARD;
        }
    }

    /**
     * Used by pole-arms.<br>
     * A pole-arm has 4 parts: its {@link Head}, its left {@link HalfHead}, its right {@link HalfHead} and its {@link Pole}.<br>
     * A pole-arm's head must be {@link Head#SPEAR}.<br>
     * A pole-arm's left and / or right half-heads are optional.
     */
    public enum HalfHead implements IToolType<HalfHead, ItemPartHalfHead, PartHalfHead> {
        NULL(0, "null", ReferenceSet.of()),
        AXE(1, "axe", ReferenceSet.of(Material.WOOD)),
        HAMMER(2, "hammer", ReferenceSet.of()),
        PICKAXE(3, "pickaxe", ReferenceSet.of(Material.STONE, Material.METAL));

        public static final HalfHead[] VALUES = values();
        private static final Byte2ReferenceMap<HalfHead> REGISTRY;

        static {
            B2RMap<HalfHead> map = new B2RHashMap<>();
            for (HalfHead halfHead : VALUES) {
                if (map.put(halfHead.id, halfHead) != null) {
                    throw new IllegalStateException("HalfHead " + halfHead + " has duplicate id: " + halfHead.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final ReferenceSet<Material> effectiveMaterials;
        private final byte id;
        private final String name;

        HalfHead(int id, String name, ReferenceSet<Material> effectiveMaterials) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.effectiveMaterials = effectiveMaterials;
            this.component = new TranslatableComponent("evolution.part.halfhead." + this.name);
        }

        public static HalfHead byId(byte id) {
            HalfHead halfHead = REGISTRY.get(id);
            if (halfHead == null) {
                return NULL;
            }
            return halfHead;
        }

        public static HalfHead getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return this == AXE;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public ReferenceSet<Material> getEffectiveMaterials() {
            return this.effectiveMaterials;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            //TODO implementation
            return 0;
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case AXE, PICKAXE, HAMMER -> Double.NaN;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public ItemPartHalfHead partItem() {
            return EvolutionItems.PART_HALFHEAD;
        }
    }

    /**
     * Used by {@link tgw.evolution.items.modular.ItemModularTool}s.<br>
     * A tool has 2 parts: its {@link Head} and its {@link Handle}.<br>
     */
    public enum Handle implements IGrabType<Handle, ItemPartHandle, PartHandle> {
        NULL(0, "null"),
        ONE_HANDED(1, "one_handed"),
        TWO_HANDED(2, "two_handed");

        public static final Handle[] VALUES = values();
        private static final Byte2ReferenceMap<Handle> REGISTRY;

        static {
            B2RMap<Handle> map = new B2RHashMap<>();
            for (Handle handle : VALUES) {
                if (map.put(handle.id, handle) != null) {
                    throw new IllegalStateException("Handle " + handle + " has duplicate id: " + handle.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final byte id;
        private final String name;

        Handle(int id, String name) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.component = new TranslatableComponent("evolution.part.handle." + this.name);
        }

        public static Handle byId(byte id) {
            Handle handle = REGISTRY.get(id);
            if (handle == null) {
                return NULL;
            }
            return handle;
        }

        public static Handle getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return false;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public double getGrabPoint() {
            return switch (this) {
                case NULL -> 0;
                case ONE_HANDED -> 2;
                case TWO_HANDED -> Float.NaN;
            };
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public int getLength() {
            return switch (this) {
                case NULL -> 0;
                case ONE_HANDED -> 11;
                case TWO_HANDED -> Integer.MIN_VALUE;
            };
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case ONE_HANDED -> 26;
                case TWO_HANDED -> Double.NaN;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ONE_HANDED -> false;
                case TWO_HANDED -> true;
            };
        }

        @Override
        public ItemPartHandle partItem() {
            return EvolutionItems.PART_HANDLE;
        }
    }

    /**
     * Used by {@link tgw.evolution.items.modular.ItemModularTool}s.<br>
     * A tool has 2 parts: its {@link Head} and its {@link Handle}.<br>
     */
    public enum Head implements IToolType<Head, ItemPartHead, PartHead> {
        NULL(0, "null", ReferenceSet.of()),
        AXE(1, "axe", ReferenceSet.of(Material.WOOD)),
        HAMMER(2, "hammer", ReferenceSet.of()),
        HOE(3, "hoe", ReferenceSet.of(Material.GRASS)),
        MACE(4, "mace", ReferenceSet.of()),
        PICKAXE(5, "pickaxe", ReferenceSet.of(Material.STONE, Material.METAL)),
        SHOVEL(6, "shovel", ReferenceSet.of(Material.DIRT, Material.SAND)),
        SPEAR(7, "spear", ReferenceSet.of());

        public static final Head[] VALUES = values();
        private static final Byte2ReferenceMap<Head> REGISTRY;

        static {
            B2RMap<Head> map = new B2RHashMap<>();
            for (Head head : VALUES) {
                if (map.put(head.id, head) != null) {
                    throw new IllegalStateException("Head " + head + " has duplicate id: " + head.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final ReferenceSet<Material> effectiveMaterials;
        private final byte id;
        private final String name;

        Head(int id, String name, ReferenceSet<Material> effectiveMaterials) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.effectiveMaterials = effectiveMaterials;
            this.component = new TranslatableComponent("evolution.part.head." + this.name);
        }

        public static Head byId(byte id) {
            Head head = REGISTRY.get(id);
            if (head == null) {
                return NULL;
            }
            return head;
        }

        public static Head getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return this == AXE;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public ReferenceSet<Material> getEffectiveMaterials() {
            return this.effectiveMaterials;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            return switch (this) {
                case NULL -> 0;
                case AXE -> grabLength - 2;
                case HOE -> grabLength - 1.5;
                case PICKAXE -> grabLength - 2.828_125;
                case SHOVEL -> grabLength - 1;
                case SPEAR -> grabLength + 2;
                case HAMMER, MACE -> Float.NaN;
            };
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case AXE -> material.isStone() ? 58.5f : 58;
                case HAMMER, MACE -> Double.NaN;
                case HOE -> 48.5;
                case PICKAXE -> 67.5;
                case SHOVEL -> material.isStone() ? 20.5 : 19.5;
                case SPEAR -> material.isStone() ? 28.5 : 28;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public String modelSuffix(EvolutionMaterials material) {
            if (this == AXE || this == SHOVEL || this == SPEAR) {
                return material.isStone() ? "__stone" : "";
            }
            return "";
        }

        @Override
        public ItemPartHead partItem() {
            return EvolutionItems.PART_HEAD;
        }
    }

    /**
     * Used by Swords.<br>
     * A sword has 4 parts: its {@link Blade}, its {@link Guard}, its {@link Hilt} and its {@link Pommel}.<br>
     * A sword's guard and / or pommel are optional.<br>
     */
    public enum Hilt implements IGrabType<Hilt, ItemPartHilt, PartHilt> {
        NULL(0, "null"),
        ONE_HANDED(1, "one_handed");

        public static final Hilt[] VALUES = values();
        private static final Byte2ReferenceMap<Hilt> REGISTRY;

        static {
            B2RMap<Hilt> map = new B2RHashMap<>();
            for (Hilt hilt : VALUES) {
                if (map.put(hilt.id, hilt) != null) {
                    throw new IllegalStateException("Hilt " + hilt + " has duplicate id: " + hilt.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final byte id;
        private final String name;

        Hilt(int id, String name) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.component = new TranslatableComponent("evolution.part.hilt." + this.name);
        }

        public static Hilt byId(byte id) {
            Hilt hilt = REGISTRY.get(id);
            if (hilt == null) {
                return NULL;
            }
            return hilt;
        }

        public static Hilt getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return false;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public double getGrabPoint() {
            //TODO implementation
            return 0;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public int getLength() {
            //TODO implementation
            return 0;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case ONE_HANDED -> 17.5;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ONE_HANDED -> false;
            };
        }

        @Override
        public ItemPartHilt partItem() {
            return EvolutionItems.PART_GRIP;
        }
    }

    /**
     * Used by pole-arms.<br>
     * A pole-arm has 4 parts: its {@link Head}, its left {@link HalfHead}, its right {@link HalfHead} and its {@link Pole}.<br>
     * A pole-arm's head must be {@link Head#SPEAR}.<br>
     * A pole-arm's left and / or right half-heads are optional.
     */
    public enum Pole implements IGrabType<Pole, ItemPartPole, PartPole> {
        NULL(0, "null"),
        POLE(1, "pole");

        public static final Pole[] VALUES = values();
        private static final Byte2ReferenceMap<Pole> REGISTRY;

        static {
            B2RMap<Pole> map = new B2RHashMap<>();
            for (Pole pole : VALUES) {
                if (map.put(pole.id, pole) != null) {
                    throw new IllegalStateException("Pole " + pole + " has duplicate id: " + pole.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final byte id;
        private final String name;

        Pole(int id, String name) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.component = new TranslatableComponent("evolution.part.pole." + this.name);
        }

        public static Pole byId(byte id) {
            Pole pole = REGISTRY.get(id);
            if (pole == null) {
                return NULL;
            }
            return pole;
        }

        public static Pole getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return false;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public double getGrabPoint() {
            //TODO implementation
            return 0;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public int getLength() {
            //TODO implementation
            return 0;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case POLE -> Double.NaN;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public boolean isTwoHanded() {
            return true;
        }

        @Override
        public ItemPartPole partItem() {
            return EvolutionItems.PART_POLE;
        }
    }

    /**
     * Used by Swords.<br>
     * A sword has 4 parts: its {@link Blade}, its {@link Guard}, its {@link Hilt} and its {@link Pommel}.<br>
     * A sword's guard and / or pommel are optional.<br>
     */
    public enum Pommel implements IAttachmentType<Pommel, ItemPartPommel, PartPommel> {
        NULL(0, "null"),
        POMMEL(1, "pommel");

        public static final Pommel[] VALUES = values();
        private static final Byte2ReferenceMap<Pommel> REGISTRY;

        static {
            B2RMap<Pommel> map = new B2RHashMap<>();
            for (Pommel pommel : VALUES) {
                if (map.put(pommel.id, pommel) != null) {
                    throw new IllegalStateException("Pommel " + pommel + " has duplicate id: " + pommel.id);
                }
            }
            map.trimCollection();
            REGISTRY = Byte2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final byte id;
        private final String name;

        Pommel(int id, String name) {
            this.name = name;
            this.id = MathHelper.toByteExact(id);
            this.component = new TranslatableComponent("evolution.part.pommel." + this.name);
        }

        public static Pommel byId(byte id) {
            Pommel pommel = REGISTRY.get(id);
            if (pommel == null) {
                return NULL;
            }
            return pommel;
        }

        public static Pommel getRandom(RandomGenerator random) {
            return VALUES[random.nextInt(VALUES.length - 1) + 1];
        }

        @Override
        public boolean canBeSharpened() {
            return false;
        }

        @Override
        public Component getComponent() {
            return this.component;
        }

        @Override
        public byte getId() {
            return this.id;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            return switch (this) {
                case NULL -> 0;
                case POMMEL -> 1.5;
            };
        }

        @Override
        public double getVolume(EvolutionMaterials material) {
            return switch (this) {
                case NULL -> 0;
                case POMMEL -> 12.5;
            };
        }

        @Override
        public boolean hasVariantIn(EvolutionMaterials material) {
            return material.isAllowedBy(this);
        }

        @Override
        public ItemPartPommel partItem() {
            return EvolutionItems.PART_POMMEL;
        }
    }
}
