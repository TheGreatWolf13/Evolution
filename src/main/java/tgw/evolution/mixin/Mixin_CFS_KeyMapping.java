package tgw.evolution.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import org.spongepowered.asm.mixin.*;
import tgw.evolution.EvolutionClient;
import tgw.evolution.client.util.Key;
import tgw.evolution.hooks.asm.*;
import tgw.evolution.patches.PatchKeyMapping;
import tgw.evolution.util.collection.maps.O2IHashMap;
import tgw.evolution.util.collection.maps.O2IMap;
import tgw.evolution.util.collection.maps.O2OHashMap;
import tgw.evolution.util.collection.maps.O2OMap;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(KeyMapping.class)
public abstract class Mixin_CFS_KeyMapping implements PatchKeyMapping, Comparable<KeyMapping> {

    @Shadow @Final @DeleteField private static Map<String, Integer> CATEGORY_SORT_ORDER;
    @Unique @RestoreFinal private static O2IMap<String> CATEGORY_SORT_ORDER_;
    @Shadow @Final @DeleteField private static Set<String> CATEGORIES;
    @Unique @RestoreFinal private static OSet<String> CATEGORIES_;
    @Shadow @Final @DeleteField private static Map<InputConstants.Key, KeyMapping> MAP;
    @Unique @RestoreFinal private static O2OMap<InputConstants.Key, KeyMapping> MAP_;
    @Shadow @Final @DeleteField private static Map<String, KeyMapping> ALL;
    @Mutable @Shadow @Final @RestoreFinal public String category;
    @Shadow public int clickCount;
    @Shadow public InputConstants.Key key;
    @Mutable @Shadow @Final @RestoreFinal public String name;
    @Mutable @Shadow @Final @RestoreFinal private InputConstants.Key defaultKey;

    @DummyConstructor
    public Mixin_CFS_KeyMapping() {
    }

    @ModifyConstructor
    public Mixin_CFS_KeyMapping(String name, InputConstants.Type type, @Key int key, String category) {
        this.name = name;
        this.key = type.getOrCreate(key);
        this.defaultKey = this.key;
        this.category = category;
        EvolutionClient.ALL_KEYMAPPING.put(name, (KeyMapping) (Object) this);
        MAP_.put(this.key, (KeyMapping) (Object) this);
        CATEGORIES_.add(category);
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void click(InputConstants.Key key) {
        KeyMapping keyMapping = MAP_.get(key);
        if (keyMapping != null) {
            ++keyMapping.clickCount;
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static Supplier<Component> createNameSupplier(String string) {
        KeyMapping keyMapping = EvolutionClient.ALL_KEYMAPPING.get(string);
        if (keyMapping == null) {
            return () -> new TranslatableComponent(string);
        }
        return keyMapping::getTranslatedKeyMessage;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void releaseAll() {
        O2OMap<String, KeyMapping> allKeymapping = EvolutionClient.ALL_KEYMAPPING;
        for (long it = allKeymapping.beginIteration(); allKeymapping.hasNextIteration(it); it = allKeymapping.nextEntry(it)) {
            allKeymapping.getIterationValue(it).release();
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void resetMapping() {
        MAP_.clear();
        O2OMap<String, KeyMapping> allKeymapping = EvolutionClient.ALL_KEYMAPPING;
        for (long it = allKeymapping.beginIteration(); allKeymapping.hasNextIteration(it); it = allKeymapping.nextEntry(it)) {
            KeyMapping keyMapping = allKeymapping.getIterationValue(it);
            MAP_.put(keyMapping.key, keyMapping);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void set(InputConstants.Key key, boolean isDown) {
        KeyMapping keyMapping = MAP_.get(key);
        if (keyMapping != null) {
            keyMapping.setDown(isDown);
        }
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public static void setAll() {
        long window = Minecraft.getInstance().getWindow().getWindow();
        O2OMap<String, KeyMapping> allKeymapping = EvolutionClient.ALL_KEYMAPPING;
        for (long it = allKeymapping.beginIteration(); allKeymapping.hasNextIteration(it); it = allKeymapping.nextEntry(it)) {
            KeyMapping keyMapping = allKeymapping.getIterationValue(it);
            if (keyMapping.key.getType() == InputConstants.Type.KEYSYM && keyMapping.key.getValue() != InputConstants.UNKNOWN.getValue()) {
                keyMapping.setDown(InputConstants.isKeyDown(window, keyMapping.key.getValue()));
            }
        }
    }

    @Unique
    @ModifyStatic
    private static void clinit() {
        MAP_ = new O2OHashMap<>();
        CATEGORIES_ = new OHashSet<>();
        O2IMap<String> map = new O2IHashMap<>();
        map.put("key.categories.movement", 1);
        map.put("key.categories.gameplay", 2);
        map.put("key.categories.inventory", 3);
        map.put("key.categories.creative", 4);
        map.put("key.categories.multiplayer", 5);
        map.put("key.categories.ui", 6);
        map.put("key.categories.misc", 7);
        map.trim();
        CATEGORY_SORT_ORDER_ = map;
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Override
    @Overwrite
    public int compareTo(KeyMapping keyMapping) {
        //noinspection ObjectInstantiationInEqualsHashCode
        return this.category.equals(keyMapping.category) ? I18n.get(this.name).compareTo(I18n.get(keyMapping.name)) : Integer.compare(CATEGORY_SORT_ORDER_.getInt(this.category), CATEGORY_SORT_ORDER_.getInt(keyMapping.category));
    }

    @Override
    public boolean consumeAllClicks() {
        boolean consumeClick = this.consumeClick();
        this.clickCount = 0;
        return consumeClick;
    }

    @Shadow
    public abstract boolean consumeClick();
}
