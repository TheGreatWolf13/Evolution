package tgw.evolution.mixin;

import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.lists.OArrayList;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

@Mixin(ServerRecipeBook.class)
public abstract class MixinServerRecipeBook extends RecipeBook {

    @Shadow @Final private static Logger LOGGER;

    @Overwrite
    public int addRecipes(Collection<Recipe<?>> collection, ServerPlayer serverPlayer) {
        List<ResourceLocation> list = new OArrayList<>();
        int i = 0;
        for (Recipe<?> value : collection) {
            ResourceLocation key = value.getId();
            if (!this.known.contains(key) && !value.isSpecial()) {
                this.add(key);
                this.addHighlight(key);
                list.add(key);
                CriteriaTriggers.RECIPE_UNLOCKED.trigger(serverPlayer, value);
                ++i;
            }
        }
        this.sendRecipes(ClientboundRecipePacket.State.ADD, serverPlayer, list);
        return i;
    }

    @Overwrite
    private void loadRecipes(ListTag listTag, Consumer<Recipe<?>> consumer, RecipeManager recipeManager) {
        for (int i = 0, len = listTag.size(); i < len; ++i) {
            String name = listTag.getString(i);
            try {
                //noinspection ObjectAllocationInLoop
                ResourceLocation key = new ResourceLocation(name);
                Recipe<?> recipe = recipeManager.byKey_(key);
                if (recipe == null) {
                    LOGGER.error("Tried to load unrecognized recipe: {} removed now.", key);
                }
                else {
                    consumer.accept(recipe);
                }
            }
            catch (ResourceLocationException e) {
                LOGGER.error("Tried to load improperly formatted recipe: {} removed now.", name);
            }
        }
    }

    @Shadow
    protected abstract void sendRecipes(ClientboundRecipePacket.State state,
                                        ServerPlayer serverPlayer,
                                        List<ResourceLocation> list);
}
