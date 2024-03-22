package tgw.evolution.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.Evolution;
import tgw.evolution.hooks.asm.DeleteMethod;
import tgw.evolution.patches.PatchLocationPredicate;
import tgw.evolution.util.collection.sets.RHashSet;
import tgw.evolution.util.collection.sets.RSet;

import java.util.Set;

@Mixin(BlockPredicate.class)
public abstract class Mixin_M_BlockPredicate implements PatchLocationPredicate {

    @Shadow @Final private @Nullable Set<Block> blocks;
    @Shadow @Final private NbtPredicate nbt;
    @Shadow @Final private StatePropertiesPredicate properties;
    @Shadow @Final private @Nullable TagKey<Block> tag;

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    public static BlockPredicate fromJson(@Nullable JsonElement json) {
        if (json != null && !json.isJsonNull()) {
            JsonObject blockJson = GsonHelper.convertToJsonObject(json, "block");
            NbtPredicate nbtPredicate = NbtPredicate.fromJson(blockJson.get("nbt"));
            RSet<Block> blocks = null;
            JsonArray jsonArray = GsonHelper.getAsJsonArray(blockJson, "blocks", null);
            if (jsonArray != null) {
                blocks = new RHashSet<>();
                for (int i = 0, len = jsonArray.size(); i < len; ++i) {
                    ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.convertToString(jsonArray.get(i), "block"));
                    Block block = (Block) Registry.BLOCK.getNullable(resourceLocation);
                    if (block == null) {
                        throw new JsonSyntaxException("Unknown block id '" + resourceLocation + "'");
                    }
                    blocks.add(block);
                }
                blocks = blocks.view();
            }
            TagKey<Block> tagKey = null;
            if (blockJson.has("tag")) {
                ResourceLocation resourceLocation2 = new ResourceLocation(GsonHelper.getAsString(blockJson, "tag"));
                tagKey = TagKey.create(Registry.BLOCK_REGISTRY, resourceLocation2);
            }
            StatePropertiesPredicate statePropertiesPredicate = StatePropertiesPredicate.fromJson(blockJson.get("state"));
            return new BlockPredicate(tagKey, blocks, statePropertiesPredicate, nbtPredicate);
        }
        return BlockPredicate.ANY;
    }

    /**
     * @author TheGreatWolf
     * @reason _
     */
    @Overwrite
    @DeleteMethod
    private static JsonSyntaxException method_33185(ResourceLocation par1) {
        throw new AbstractMethodError();
    }

    /**
     * @reason _
     * @author TheGreatWolf
     */
    @Overwrite
    public boolean matches(ServerLevel level, BlockPos pos) {
        Evolution.deprecatedMethod();
        return this.matches_(level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean matches_(ServerLevel level, int x, int y, int z) {
        if ((Object) this == BlockPredicate.ANY) {
            return true;
        }
        if (!level.isLoaded_(x, y, z)) {
            return false;
        }
        BlockState state = level.getBlockState_(x, y, z);
        if (this.tag != null && !state.is(this.tag)) {
            return false;
        }
        if (this.blocks != null && !this.blocks.contains(state.getBlock())) {
            return false;
        }
        if (!this.properties.matches(state)) {
            return false;
        }
        if (this.nbt != NbtPredicate.ANY) {
            BlockEntity tile = level.getBlockEntity_(x, y, z);
            return tile != null && this.nbt.matches(tile.saveWithFullMetadata());
        }
        return true;
    }
}
