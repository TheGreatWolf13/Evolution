package tgw.evolution.datagen;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import tgw.evolution.Evolution;
import tgw.evolution.capabilities.modular.part.PartTypes;
import tgw.evolution.datagen.util.ExistingFileHelper;
import tgw.evolution.datagen.util.ItemModelBuilder;
import tgw.evolution.init.EvolutionBlocks;
import tgw.evolution.init.EvolutionItems;
import tgw.evolution.init.EvolutionMaterials;
import tgw.evolution.util.constants.WoodVariant;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiFunction;

import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.*;

public class ItemModelProvider extends ModelProvider<ItemModelBuilder> {

    public ItemModelProvider(DataGenerator generator, Collection<Path> existingPaths, ExistingFileHelper existingFileHelper) {
        this(generator, existingPaths, existingFileHelper, Evolution.MODID, ITEM_FOLDER, ItemModelBuilder::new);
    }

    public ItemModelProvider(DataGenerator generator, Collection<Path> existingPaths, ExistingFileHelper existingFileHelper, String modid, String folder, BiFunction<ResourceLocation, ExistingFileHelper, ItemModelBuilder> builderFromModId) {
        super(generator, existingPaths, existingFileHelper, modid, folder, builderFromModId);
    }

    @Override
    public String getName() {
        return "Evolution Item Models";
    }

    @SuppressWarnings("ObjectAllocationInLoop")
    @Override
    protected void registerModels() {
        //Dev
        this.simpleItem(EvolutionItems.CLOCK);
        this.simpleItem(EvolutionItems.DEBUG_ITEM);
        this.simpleItem(EvolutionItems.DEV_DRINK);
        this.simpleItem(EvolutionItems.DEV_FOOD);
        this.simpleItem(EvolutionItems.SEXTANT);
        this.simpleItem(EvolutionItems.SPEEDOMETER);
        //Independent
        this.simpleItem(EvolutionItems.CLAYBALL);
        this.simpleItem(EvolutionItems.FIRE_STARTER);
        this.getBuilder("evolution:item/modular_tool")
            .transforms()
            .transform(THIRD_PERSON_RIGHT_HAND).rotation(-45.0f, 180.0f, 0.0f).translation(-1.0f, -2.0f, 6.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(THIRD_PERSON_LEFT_HAND).rotation(-45.0f, 180.0f, 0.0f).translation(-1.0f, -2.0f, 6.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(GROUND).translation(0.0f, 0.0f, 3.0f).scale(0.5f, 0.5f, 0.5f).end()
            .transform(HEAD).rotation(90.0f, 45.0f, -90.0f).translation(0.0f, -7.0f, 0.0f).end()
            .transform(GUI).rotation(10.0f, -100.0f, 10.0f).translation(-3.5f, -5.5f, 0.0f).end()
            .transform(FIXED).rotation(0.0f, 90.0f, 0.0f).translation(3.5f, -5.5f, 0.0f).end()
            .end();
        this.getBuilder("evolution:item/part_blade")
            .transforms()
            .transform(THIRD_PERSON_RIGHT_HAND).translation(0.0f, -2.0f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(THIRD_PERSON_LEFT_HAND).translation(0.0f, -2.0f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(GROUND).scale(0.5f, 0.5f, 0.5f).end()
            .transform(GUI).translation(-5.5f, -5.5f, 0.0f).end()
            .transform(FIXED).rotation(0.0f, -180.0f, 0.0f).translation(4.0f, -4.0f, -0.5f).end()
            .end();
        this.getBuilder("evolution:item/part_guard")
            .transforms()
            .transform(THIRD_PERSON_RIGHT_HAND).translation(1.75f, 2.5f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(THIRD_PERSON_LEFT_HAND).translation(-1.75f, 2.5f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(GROUND).scale(0.5f, 0.5f, 0.5f).end()
            .transform(FIXED).rotation(0.0f, -180.0f, 0.0f).translation(-2.0f, 2.0f, -0.5f).end()
            .end();
        this.getBuilder("evolution:item/part_handle")
            .transforms()
            .transform(THIRD_PERSON_RIGHT_HAND).translation(3.25f, 2.25f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(THIRD_PERSON_LEFT_HAND).translation(-3.25f, 2.25f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(GROUND).translation(0.0f, 0.5f, 0.0f).scale(0.5f, 0.5f, 0.5f).end()
            .transform(GUI).translation(0.75f, 1.25f, 0.0f).end()
            .transform(FIXED).rotation(0.0f, -180.0f, 0.0f).translation(-0.5f, 1.5f, -0.5f).end()
            .end();
        this.getBuilder("evolution:item/part_head")
            .transforms()
            .transform(THIRD_PERSON_RIGHT_HAND).translation(-2.0f, -1.0f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(THIRD_PERSON_LEFT_HAND).translation(2.0f, -1.0f, 0.0f).scale(0.85f, 0.85f, 0.85f).end()
            .transform(GROUND).scale(0.5f, 0.5f, 0.5f).end()
            .transform(GUI).translation(-1.0f, -4.0f, 0.0f).end()
            .transform(FIXED).rotation(0.0f, -180.0f, 0.0f).translation(1.0f, -4.0f, -0.75f).end()
            .end();
        this.simpleItemFolder(EvolutionItems.TALLGRASS, "block");
        this.simpleItemFolder(EvolutionItems.TALLGRASS_HIGH, "block", "_top");
        //Collection
        for (WoodVariant variant : WoodVariant.VALUES) {
            this.withExistingParent(name(variant.get(EvolutionItems.FIREWOODS)), "evolution:item/firewood")
                .texture("side", blockTexture(variant.get(EvolutionBlocks.LOGS)))
                .texture("end", blockTexture(variant.get(EvolutionBlocks.LOGS), "_top"));
            this.simpleItem(variant.get(EvolutionItems.PLANK));
            this.simpleItemFolder(variant.get(EvolutionItems.SAPLINGS), "block");
        }
        //Modular
        //      Blade
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Blade blade : PartTypes.Blade.VALUES) {
                String baseName = "evolution:item/modular/blade/" + blade.getName();
                if (material.isAllowedBy(blade)) {
                    String name = baseName + "_" + material.getName();
                    try {
                        this.withExistingParent(name, baseName + blade.modelSuffix(material)).texture("0", name);
                        if (blade.canBeSharpened()) {
                            this.withExistingParent(name + "_sharp", baseName + blade.modelSuffix(material) + "__sharp").texture("0", name);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //      Guard
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Guard guard : PartTypes.Guard.VALUES) {
                String baseName = "evolution:item/modular/guard/" + guard.getName();
                if (material.isAllowedBy(guard)) {
                    String name = baseName + "_" + material.getName();
                    this.withExistingParent(name, baseName + guard.modelSuffix(material)).texture("0", name);
                }
            }
        }
        //      Half-Head
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.HalfHead halfhead : PartTypes.HalfHead.VALUES) {
                String baseName = "evolution:item/modular/halfhead/" + halfhead.getName();
                if (material.isAllowedBy(halfhead)) {
                    String name = baseName + "_" + material.getName();
                    try {
                        this.withExistingParent(name, baseName + halfhead.modelSuffix(material)).texture("0", name);
                        if (halfhead.canBeSharpened()) {
                            this.withExistingParent(name + "_sharp", baseName + halfhead.modelSuffix(material) + "__sharp").texture("0", name);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //      Handle
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Handle handle : PartTypes.Handle.VALUES) {
                String baseName = "evolution:item/modular/handle/" + handle.getName();
                if (material.isAllowedBy(handle)) {
                    String name = baseName + "_" + material.getName();
                    try {
                        this.withExistingParent(name, baseName + handle.modelSuffix(material)).texture("0", name);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //      Head
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Head head : PartTypes.Head.VALUES) {
                String baseName = "evolution:item/modular/head/" + head.getName();
                if (material.isAllowedBy(head)) {
                    String name = baseName + "_" + material.getName();
                    try {
                        this.withExistingParent(name, baseName + head.modelSuffix(material)).texture("0", name);
                        if (head.canBeSharpened()) {
                            this.withExistingParent(name + "_sharp", baseName + head.modelSuffix(material) + "__sharp").texture("0", name);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //      Hilt
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Hilt hilt : PartTypes.Hilt.VALUES) {
                String baseName = "evolution:item/modular/hilt/" + hilt.getName();
                if (material.isAllowedBy(hilt)) {
                    String name = baseName + "_" + material.getName();
                    this.withExistingParent(name, baseName + hilt.modelSuffix(material)).texture("0", name);
                }
            }
        }
        //      Pole
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Pole pole : PartTypes.Pole.VALUES) {
                String baseName = "evolution:item/modular/pole/" + pole.getName();
                if (material.isAllowedBy(pole)) {
                    String name = baseName + "_" + material.getName();
                    try {
                        this.withExistingParent(name, baseName + pole.modelSuffix(material)).texture("0", name);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        //      Pommel
        for (EvolutionMaterials material : EvolutionMaterials.VALUES) {
            for (PartTypes.Pommel pommel : PartTypes.Pommel.VALUES) {
                String baseName = "evolution:item/modular/pommel/" + pommel.getName();
                if (material.isAllowedBy(pommel)) {
                    String name = baseName + "_" + material.getName();
                    this.withExistingParent(name, baseName + pommel.modelSuffix(material)).texture("0", name);
                }
            }
        }
    }

    @CanIgnoreReturnValue
    protected ItemModelBuilder simpleBlock(Block block) {
        String path = Registry.BLOCK.getKey(block).getPath();
        return this.withExistingParent(path, new ResourceLocation(this.modId, "block/" + path));
    }

    @CanIgnoreReturnValue
    protected ItemModelBuilder simpleBlock(Block block, String post) {
        String path = Registry.BLOCK.getKey(block).getPath();
        return this.withExistingParent(path, new ResourceLocation(this.modId, "block/" + path + post));
    }

    protected ItemModelBuilder simpleItem(Item item) {
        return this.simpleItemFolder(item, "item");
    }

    protected ItemModelBuilder simpleItemFolder(Item item, String folder) {
        String path = Registry.ITEM.getKey(item).getPath();
        return this.withExistingParent(path, new ResourceLocation("item/generated"))
                   .texture("layer0", new ResourceLocation(this.modId, folder + "/" + path));
    }

    protected ItemModelBuilder simpleItemFolder(Item item, String folder, String post) {
        String path = Registry.ITEM.getKey(item).getPath();
        return this.withExistingParent(path, new ResourceLocation("item/generated")).texture("layer0", new ResourceLocation(this.modId, folder + "/" + path + post));
    }

    @Override
    public String type() {
        return "ItemModel";
    }
}
