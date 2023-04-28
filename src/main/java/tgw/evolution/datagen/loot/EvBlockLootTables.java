package tgw.evolution.datagen.loot;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.RegistryObject;
import tgw.evolution.blocks.util.IntProperty;
import tgw.evolution.init.EvolutionBStates;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.util.constants.RockVariant;
import tgw.evolution.util.constants.WoodVariant;

import java.util.function.IntUnaryOperator;

public class EvBlockLootTables extends BlockLoot {

    protected static LootTable.Builder createDropByIntProperty(Block block, ItemLike drop, IntProperty property, int delta) {
        int min = property.getMinValue();
        if (min + delta < 0) {
            throw new IllegalStateException("Min + delta should be at least zero (" + (min + delta) + ")!");
        }
        return createDropByIntProperty(block, drop, property, p -> p + delta);
    }

    protected static LootTable.Builder createDropByIntProperty(Block block, ItemLike drop, IntProperty property) {
        return createDropByIntProperty(block, drop, property, 0);
    }

    protected static LootTable.Builder createDropByIntProperty(Block block, ItemLike drop, IntProperty property, IntUnaryOperator countMaker) {
        LootPoolSingletonContainer.Builder<?> builder = LootItem.lootTableItem(drop);
        int min = property.getMinValue();
        int max = property.getMaxValue();
        for (int i = min; i <= max; i++) {
            int count = countMaker.applyAsInt(i);
            if (count < 0) {
                throw new IllegalStateException("Count cannot be less than zero! (" + count + ")");
            }
            //noinspection ObjectAllocationInLoop
            builder.apply(SetItemCountFunction.setCount(ConstantValue.exactly(count))
                                              .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                                                                       .setProperties(StatePropertiesPredicate.Builder
                                                                                                              .properties()
                                                                                                              .hasProperty(property, i))));
        }
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add(builder));
    }

    protected static LootTable.Builder createSingleItemTable(ItemLike drop) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(drop)));
    }

    protected static LootTable.Builder createSingleItemTable(ItemLike drop, NumberProvider count) {
        return LootTable.lootTable().withPool(LootPool.lootPool()
                                                      .setRolls(ConstantValue.exactly(1.0F))
                                                      .add(LootItem.lootTableItem(drop).apply(SetItemCountFunction.setCount(count))));
    }

    @Override
    protected void addTables() {
        this.dropPlaceholder(EvolutionBlocks.GLASS.get());
        this.dropSelf(EvolutionBlocks.BLOCK_METAL_COPPER.get());
        this.dropSelf(EvolutionBlocks.BLOCK_METAL_COPPER_E.get());
        this.dropSelf(EvolutionBlocks.BLOCK_METAL_COPPER_W.get());
        this.dropSelf(EvolutionBlocks.BLOCK_METAL_COPPER_O.get());
        this.dropSelf(EvolutionBlocks.BRICK_CLAY.get());
        this.dropOther(EvolutionBlocks.CLAY.get(), EvolutionItems.CLAYBALL.get(), 4);
        this.dropSelf(EvolutionBlocks.CLIMBING_HOOK.get());
        this.dropSelf(EvolutionBlocks.CLIMBING_STAKE.get());
        this.dropSelf(EvolutionBlocks.CRUCIBLE_CLAY.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_AXE.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_GUARD.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_HAMMER.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_HOE.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_INGOT.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_KNIFE.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_PICKAXE.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_SAW.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_SHOVEL.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_SPEAR.get());
        this.dropSelf(EvolutionBlocks.MOLD_CLAY_SWORD.get());
        this.add(EvolutionBlocks.MOLDING_BLOCK.get(), b -> createDropByIntProperty(b, EvolutionItems.CLAYBALL.get(), EvolutionBStates.LAYERS_1_5));
        this.dropSelf(EvolutionBlocks.PEAT.get());
        this.dropSelf(EvolutionBlocks.ROPE.get());
        this.dropOther(EvolutionBlocks.ROPE_GROUND.get(), EvolutionItems.ROPE.get());
        this.dropSelf(EvolutionBlocks.STICK.get());
        for (WoodVariant variant : WoodVariant.VALUES) {
            this.dropSelf(variant.get(EvolutionBlocks.CHOPPING_BLOCKS));
            Block sapling = variant.get(EvolutionBlocks.SAPLINGS);
            this.dropOther(variant.get(EvolutionBlocks.LEAVES), sapling, 0.05F);
            this.dropSelf(variant.get(EvolutionBlocks.LOGS));
            this.dropSelf(variant.get(EvolutionBlocks.PLANKS));
            this.dropSelf(sapling);
        }
        for (RockVariant variant : RockVariant.VALUES_STONE) {
            this.dropSelf(variant.get(EvolutionBlocks.COBBLESTONES));
            Block dirt = variant.get(EvolutionBlocks.DIRTS);
            this.dropSelf(dirt);
            this.dropOther(variant.get(EvolutionBlocks.DRY_GRASSES), dirt);
            this.dropOther(variant.get(EvolutionBlocks.GRASSES), dirt);
            this.dropSelf(variant.get(EvolutionBlocks.GRAVELS));
            Block rock = variant.get(EvolutionBlocks.ROCKS);
            this.dropOther(variant.get(EvolutionBlocks.KNAPPING_BLOCKS), rock);
            this.dropSelf(variant.get(EvolutionBlocks.POLISHED_STONES));
            this.dropSelf(rock);
            this.dropSelf(variant.get(EvolutionBlocks.SANDS));
            this.dropSelf(variant.get(EvolutionBlocks.STONEBRICKS));
            this.dropOther(variant.get(EvolutionBlocks.STONES), rock, 4);
        }
        this.dropOther(EvolutionBlocks.GRASSES.get(RockVariant.PEAT).get(), EvolutionItems.PEAT.get());
        this.dropOther(EvolutionBlocks.GRASSES.get(RockVariant.CLAY).get(), EvolutionItems.CLAYBALL.get(), 4);
    }

    @Override
    public void dropOther(Block block, ItemLike drop) {
        this.add(block, createSingleItemTable(drop));
    }

    public void dropOther(Block block, ItemLike drop, int count) {
        this.add(block, createSingleItemTable(drop, ConstantValue.exactly(count)));
    }

    public void dropOther(Block block, ItemLike drop, float chance) {
        this.add(block, b -> LootTable.lootTable().withPool(LootPool.lootPool()
                                                                    .setRolls(ConstantValue.exactly(1.0f))
                                                                    .add(LootItem.lootTableItem(drop))
                                                                    .when(LootItemRandomChanceCondition.randomChance(chance))));
    }

    public void dropPlaceholder(Block block) {
        this.dropOther(block, EvolutionItems.DEBUG_ITEM.get());
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return EvolutionBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
