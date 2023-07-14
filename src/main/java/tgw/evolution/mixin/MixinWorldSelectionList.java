package tgw.evolution.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Mixin(WorldSelectionList.class)
public abstract class MixinWorldSelectionList extends ObjectSelectionList<WorldSelectionList.WorldListEntry> {

    @Shadow @Final static Logger LOGGER;
    @Shadow private @Nullable List<LevelSummary> cachedList;

    public MixinWorldSelectionList(Minecraft minecraft, int i, int j, int k, int l, int m) {
        super(minecraft, i, j, k, l, m);
    }

    /**
     * @author TheGreatWolf
     * @reason Avoid allocations
     */
    @Overwrite
    public void refreshList(Supplier<String> filter, boolean updateCache) {
        this.clearEntries();
        LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
        if (this.cachedList == null || updateCache) {
            try {
                this.cachedList = levelStorageSource.getLevelList();
            }
            catch (LevelStorageException e) {
                LOGGER.error("Couldn't load level list", e);
                this.minecraft.setScreen(new ErrorScreen(new TranslatableComponent("selectWorld.unable_to_load"), new TextComponent(e.getMessage())));
                return;
            }
            Collections.sort(this.cachedList);
        }
        if (this.cachedList.isEmpty()) {
            this.minecraft.setScreen(CreateWorldScreen.createFresh(null));
        }
        else {
            String s = filter.get().toLowerCase(Locale.ROOT);
            for (int i = 0, len = this.cachedList.size(); i < len; ++i) {
                LevelSummary summary = this.cachedList.get(i);
                if (summary.getLevelName().toLowerCase(Locale.ROOT).contains(s) || summary.getLevelId().toLowerCase(Locale.ROOT).contains(s)) {
                    //noinspection resource,ObjectAllocationInLoop
                    this.addEntry(((WorldSelectionList) (Object) this).new WorldListEntry((WorldSelectionList) (Object) this, summary));
                }
            }
        }
    }
}
