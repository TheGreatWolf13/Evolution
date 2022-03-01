package tgw.evolution.capabilities.modular.part;

import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.material.Material;
import tgw.evolution.capabilities.modular.IAttachmentType;
import tgw.evolution.capabilities.modular.IGrabType;
import tgw.evolution.capabilities.modular.IToolType;

public final class PartTypes {

    private PartTypes() {
    }

    public enum Blade implements IAttachmentType<Blade> {
        NULL("null", 0),
        ARMING_SWORD("arming_sword", 57.5f);

        public static final Blade[] VALUES = values();
        private final Component component;
        private final String name;
        private final float volume;

        Blade(String name, float volume) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.blade." + this.name);
            this.volume = volume;
        }

        @Override
        public Blade byName(String name) {
            return switch (name) {
                case "arming_sword" -> ARMING_SWORD;
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ARMING_SWORD -> false;
            };
        }
    }

    public enum Guard implements IAttachmentType<Guard> {
        NULL("null", 0),
        CROSSGUARD("crossguard", 39.5f);

        public static final Guard[] VALUES = values();
        private final Component component;
        private final String name;
        private final float volume;

        Guard(String name, float volume) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.guard." + this.name);
            this.volume = volume;
        }

        @Override
        public Guard byName(String name) {
            return switch (name) {
                case "crossguard" -> CROSSGUARD;
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }

    public enum HalfHead implements IToolType<HalfHead> {
        NULL("null", ReferenceSet.of(), 0),
        AXE("axe", ReferenceSet.of(Material.WOOD), 58),
        HAMMER("hammer", ReferenceSet.of(), Float.NaN),
        PICKAXE("pickaxe", ReferenceSet.of(Material.STONE, Material.METAL), Float.NaN);

        public static final HalfHead[] VALUES = values();
        private final Component component;
        private final ReferenceSet<Material> effectiveMaterials;
        private final String name;
        private final float volume;

        HalfHead(String name, ReferenceSet<Material> effectiveMaterials, float volume) {
            this.name = name;
            this.effectiveMaterials = effectiveMaterials;
            this.component = new TranslatableComponent("evolution.part.halfhead." + this.name);
            this.volume = volume;
        }

        @Override
        public HalfHead byName(String name) {
            return switch (name) {
                case "axe" -> AXE;
                case "hammer" -> HAMMER;
                case "pickaxe" -> PICKAXE;
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }

    public enum Handle implements IGrabType<Handle> {
        NULL("null", 0),
        ONE_HANDED("one_handed", 26),
        TWO_HANDED("two_handed", Float.NaN);

        public static final Handle[] VALUES = values();
        private final Component component;
        private final String name;
        private final float volume;

        Handle(String name, float volume) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.handle." + this.name);
            this.volume = volume;
        }

        @Override
        public Handle byName(String name) {
            return switch (name) {
                case "one_handed" -> ONE_HANDED;
                case "two_handed" -> TWO_HANDED;
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
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
        NULL("null", ReferenceSet.of(), 0),
        AXE("axe", ReferenceSet.of(Material.WOOD), 58),
        HAMMER("hammer", ReferenceSet.of(), Float.NaN),
        HOE("hoe", ReferenceSet.of(Material.GRASS), Float.NaN),
        MACE("mace", ReferenceSet.of(), Float.NaN),
        PICKAXE("pickaxe", ReferenceSet.of(Material.STONE, Material.METAL), 67.5f),
        SHOVEL("shovel", ReferenceSet.of(Material.DIRT, Material.SAND), 19.75f),
        SPEAR("spear", ReferenceSet.of(), Float.NaN);

        public static final Head[] VALUES = values();
        private final Component component;
        private final ReferenceSet<Material> effectiveMaterials;
        private final String name;
        private final float volume;

        Head(String name, ReferenceSet<Material> effectiveMaterials, float volume) {
            this.name = name;
            this.effectiveMaterials = effectiveMaterials;
            this.component = new TranslatableComponent("evolution.part.head." + this.name);
            this.volume = volume;
        }

        @Override
        public Head byName(String name) {
            return switch (name) {
                case "axe" -> AXE;
                case "hammer" -> HAMMER;
                case "hoe" -> HOE;
                case "mace" -> MACE;
                case "pickaxe" -> PICKAXE;
                case "shovel" -> SHOVEL;
                case "spear" -> SPEAR;
                default -> NULL;
            };
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
                case PICKAXE -> grabLength - 2.828_125;
                case SPEAR, HAMMER, SHOVEL, MACE, HOE -> Float.NaN;
            };
        }

        @Override
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }

    public enum Hilt implements IGrabType<Hilt> {
        NULL("null", 0),
        ONE_HANDED("one_handed", 17.5f);

        public static final Hilt[] VALUES = values();
        private final Component component;
        private final String name;
        private final float volume;

        Hilt(String name, float volume) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.hilt." + this.name);
            this.volume = volume;
        }

        @Override
        public Hilt byName(String name) {
            return switch (name) {
                case "one_handed" -> ONE_HANDED;
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return switch (this) {
                case NULL, ONE_HANDED -> false;
            };
        }
    }

    public enum Pole implements IGrabType<Pole> {
        NULL("null", 0);

        public static final Pole[] VALUES = values();
        private final Component component;
        private final String name;
        private final float volume;

        Pole(String name, float volume) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.pole." + this.name);
            this.volume = volume;
        }

        @Override
        public Pole byName(String name) {
            return switch (name) {
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return true;
        }
    }

    public enum Pommel implements IAttachmentType<Pommel> {
        NULL("null", 0),
        POMMEL("pommel", Float.NaN);

        public static final Pommel[] VALUES = values();
        private final Component component;
        private final String name;
        private final float volume;

        Pommel(String name, float volume) {
            this.name = name;
            this.component = new TranslatableComponent("evolution.part.pommel." + this.name);
            this.volume = volume;
        }

        @Override
        public Pommel byName(String name) {
            return switch (name) {
                case "pommel" -> POMMEL;
                default -> NULL;
            };
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
        public float getVolume() {
            return this.volume;
        }

        @Override
        public boolean isTwoHanded() {
            return false;
        }
    }
}
