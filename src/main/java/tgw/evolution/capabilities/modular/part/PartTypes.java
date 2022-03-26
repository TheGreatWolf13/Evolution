package tgw.evolution.capabilities.modular.part;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IAttachmentType;
import tgw.evolution.capabilities.modular.IGrabType;
import tgw.evolution.capabilities.modular.IToolType;
import tgw.evolution.init.ItemMaterial;

public final class PartTypes {

    private PartTypes() {
    }

    public enum Blade implements IAttachmentType<Blade> {
        NULL("null"),
        ARMING_SWORD("arming_sword");

        public static final Blade[] VALUES = values();
        private static final Object2ReferenceMap<String, Blade> REGISTRY;

        static {
            Object2ReferenceMap<String, Blade> map = new Object2ReferenceOpenHashMap<>();
            for (Blade blade : VALUES) {
                map.put(blade.name, blade);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final String name;

        Blade(String name) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.blade." + this.name);
        }

        public static Blade byName(String name) {
            Blade blade = REGISTRY.get(name);
            if (blade == null) {
                return NULL;
            }
            return blade;
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
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            //TODO implementation
            return 0;
        }

        @Override
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case ARMING_SWORD -> 57.5;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ARMING_SWORD -> false;
            };
        }
    }

    public enum Guard implements IAttachmentType<Guard> {
        NULL("null"),
        CROSSGUARD("crossguard");

        public static final Guard[] VALUES = values();
        private static final Object2ReferenceMap<String, Guard> REGISTRY;

        static {
            Object2ReferenceMap<String, Guard> map = new Object2ReferenceOpenHashMap<>();
            for (Guard guard : VALUES) {
                map.put(guard.name, guard);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final String name;

        Guard(String name) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.guard." + this.name);
        }

        public static Guard byName(String name) {
            Guard guard = REGISTRY.get(name);
            if (guard == null) {
                return NULL;
            }
            return guard;
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
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            //TODO implementation
            return 0;
        }

        @Override
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case CROSSGUARD -> Double.NaN;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }

    public enum HalfHead implements IToolType<HalfHead> {
        NULL("null", ReferenceSet.of()),
        AXE("axe", ReferenceSet.of(Material.WOOD)),
        HAMMER("hammer", ReferenceSet.of()),
        PICKAXE("pickaxe", ReferenceSet.of(Material.STONE, Material.METAL));

        public static final HalfHead[] VALUES = values();
        private static final Object2ReferenceMap<String, HalfHead> REGISTRY;

        static {
            Object2ReferenceMap<String, HalfHead> map = new Object2ReferenceOpenHashMap<>();
            for (HalfHead halfHead : VALUES) {
                map.put(halfHead.name, halfHead);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final ReferenceSet<Material> effectiveMaterials;
        private final String name;

        HalfHead(String name, ReferenceSet<Material> effectiveMaterials) {
            this.name = name;
            this.effectiveMaterials = effectiveMaterials;
            this.component = new TranslatableComponent("evolution.part.halfhead." + this.name);
        }

        public static HalfHead byName(String name) {
            HalfHead halfHead = REGISTRY.get(name);
            if (halfHead == null) {
                return NULL;
            }
            return halfHead;
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
        public String getName() {
            return this.name;
        }

        @Override
        public double getRelativeCenterOfMass(int grabLength) {
            //TODO implementation
            return 0;
        }

        @Override
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case AXE -> Double.NaN;
                case HAMMER -> Double.NaN;
                case PICKAXE -> Double.NaN;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }

    public enum Handle implements IGrabType<Handle> {
        NULL("null"),
        ONE_HANDED("one_handed"),
        TWO_HANDED("two_handed");

        public static final Handle[] VALUES = values();
        private static final Object2ReferenceMap<String, Handle> REGISTRY;

        static {
            Object2ReferenceMap<String, Handle> map = new Object2ReferenceOpenHashMap<>();
            for (Handle handle : VALUES) {
                map.put(handle.name, handle);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final String name;

        Handle(String name) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.handle." + this.name);
        }

        public static Handle byName(String name) {
            Handle handle = REGISTRY.get(name);
            if (handle == null) {
                return NULL;
            }
            return handle;
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
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case ONE_HANDED -> 26;
                case TWO_HANDED -> Double.NaN;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ONE_HANDED -> false;
                case TWO_HANDED -> true;
            };
        }
    }

    public enum Head implements IToolType<Head> {
        NULL("null", ReferenceSet.of()),
        AXE("axe", ReferenceSet.of(Material.WOOD)),
        HAMMER("hammer", ReferenceSet.of()),
        HOE("hoe", ReferenceSet.of(Material.GRASS)),
        MACE("mace", ReferenceSet.of()),
        PICKAXE("pickaxe", ReferenceSet.of(Material.STONE, Material.METAL)),
        SHOVEL("shovel", ReferenceSet.of(Material.DIRT, Material.SAND)),
        SPEAR("spear", ReferenceSet.of());

        public static final Head[] VALUES = values();
        private static final Object2ReferenceMap<String, Head> REGISTRY;

        static {
            Object2ReferenceMap<String, Head> map = new Object2ReferenceOpenHashMap<>();
            for (Head head : VALUES) {
                map.put(head.name, head);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final ReferenceSet<Material> effectiveMaterials;
        private final String name;

        Head(String name, ReferenceSet<Material> effectiveMaterials) {
            this.name = name;
            this.effectiveMaterials = effectiveMaterials;
            this.component = new TranslatableComponent("evolution.part.head." + this.name);
        }

        public static Head byName(String name) {
            Head head = REGISTRY.get(name);
            if (head == null) {
                return NULL;
            }
            return head;
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
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case AXE -> material.isStone() ? 58.5f : 58;
                case HAMMER -> Double.NaN;
                case HOE -> 48.5;
                case MACE -> Double.NaN;
                case PICKAXE -> 67.5;
                case SHOVEL -> material.isStone() ? 20.5 : 19.5;
                case SPEAR -> material.isStone() ? 28.5 : 28;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }

    public enum Hilt implements IGrabType<Hilt> {
        NULL("null"),
        ONE_HANDED("one_handed");

        public static final Hilt[] VALUES = values();
        private static final Object2ReferenceMap<String, Hilt> REGISTRY;

        static {
            Object2ReferenceMap<String, Hilt> map = new Object2ReferenceOpenHashMap<>();
            for (Hilt hilt : VALUES) {
                map.put(hilt.name, hilt);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final String name;

        Hilt(String name) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.hilt." + this.name);
        }

        public static Hilt byName(String name) {
            Hilt hilt = REGISTRY.get(name);
            if (hilt == null) {
                return NULL;
            }
            return hilt;
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
        public int getLength() {
            //TODO implementation
            return 0;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case ONE_HANDED -> 17.5;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ONE_HANDED -> false;
            };
        }
    }

    public enum Pole implements IGrabType<Pole> {
        NULL("null");

        public static final Pole[] VALUES = values();
        private static final Object2ReferenceMap<String, Pole> REGISTRY;

        static {
            Object2ReferenceMap<String, Pole> map = new Object2ReferenceOpenHashMap<>();
            for (Pole pole : VALUES) {
                map.put(pole.name, pole);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final String name;

        Pole(String name) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.pole." + this.name);
        }

        public static Pole byName(String name) {
            Pole pole = REGISTRY.get(name);
            if (pole == null) {
                return NULL;
            }
            return pole;
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
        public int getLength() {
            //TODO implementation
            return 0;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return true;
        }
    }

    public enum Pommel implements IAttachmentType<Pommel> {
        NULL("null"),
        POMMEL("pommel");

        public static final Pommel[] VALUES = values();
        private static final Object2ReferenceMap<String, Pommel> REGISTRY;

        static {
            Object2ReferenceMap<String, Pommel> map = new Object2ReferenceOpenHashMap<>();
            for (Pommel pommel : VALUES) {
                map.put(pommel.name, pommel);
            }
            REGISTRY = Object2ReferenceMaps.unmodifiable(map);
        }

        private final Component component;
        private final String name;

        Pommel(String name) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.pommel." + this.name);
        }

        public static Pommel byName(String name) {
            Pommel pommel = REGISTRY.get(name);
            if (pommel == null) {
                return NULL;
            }
            return pommel;
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
        public double getVolume(ItemMaterial material) {
            return switch (this) {
                case NULL -> 0;
                case POMMEL -> 12.5;
            };
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }
}
