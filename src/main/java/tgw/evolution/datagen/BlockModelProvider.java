package tgw.evolution.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import tgw.evolution.Evolution;
import tgw.evolution.datagen.util.BlockModelBuilder;
import tgw.evolution.datagen.util.ExistingFileHelper;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.BiFunction;

import static net.minecraft.client.renderer.block.model.ItemTransforms.TransformType.*;
import static tgw.evolution.datagen.util.ModelBuilder.FaceRotation.COUNTERCLOCKWISE_90;

public class BlockModelProvider extends ModelProvider<BlockModelBuilder> {

    public BlockModelProvider(DataGenerator generator, Collection<Path> existingPaths, ExistingFileHelper existingFileHelper) {
        this(generator, existingPaths, existingFileHelper, Evolution.MODID, BLOCK_FOLDER, BlockModelBuilder::new);
    }

    public BlockModelProvider(DataGenerator generator, Collection<Path> existingPaths, ExistingFileHelper existingFileHelper, String modId, String folder, BiFunction<ResourceLocation, ExistingFileHelper, BlockModelBuilder> factory) {
        super(generator, existingPaths, existingFileHelper, modId, folder, factory);
    }

    @Override
    public String getName() {
        return "Evolution Block Models";
    }

    @Override
    protected void registerModels() {
        this.getBuilder("evolution:block/rock")
            .element()
            .from(6.5f, 0.0f, 4.5f)
            .to(10.5f, 1.0f, 5.5f)
            .face(Direction.NORTH).uvs(11.0f, 15.0f, 7.0f, 16.0f).texture("#0").end()
            .face(Direction.EAST).uvs(10.0f, 15.0f, 11.0f, 16.0f).texture("#0").end()
            .face(Direction.WEST).uvs(7.0f, 15.0f, 8.0f, 16.0f).texture("#0").end()
            .face(Direction.UP).uvs(7.0f, 15.0f, 11.0f, 16.0f).texture("#0").end()
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 4.0f, 1.0f).texture("#0").end()
            .end()
            .element()
            .from(10.5f, 0.0f, 5.5f)
            .to(11.5f, 1.0f, 8.5f)
            .face(Direction.NORTH).uvs(0.0f, 0.0f, 1.0f, 1.0f).texture("#0").end()
            .face(Direction.EAST).uvs(11.0f, 0.0f, 12.0f, 3.0f).rotation(COUNTERCLOCKWISE_90).texture("#0").end()
            .face(Direction.SOUTH).uvs(11.0f, 2.0f, 12.0f, 3.0f).texture("#0").end()
            .face(Direction.UP).uvs(11.0f, 0.0f, 12.0f, 3.0f).texture("#0").end()
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 1.0f, 3.0f).texture("#0").end()
            .end()
            .element()
            .from(6.5f, 0.0f, 10.5f)
            .to(8.5f, 1.0f, 11.5f)
            .face(Direction.EAST).uvs(8.0f, 5.0f, 9.0f, 6.0f).texture("#0").end()
            .face(Direction.SOUTH).uvs(7.0f, 5.0f, 9.0f, 6.0f).texture("#0").end()
            .face(Direction.WEST).uvs(7.0f, 5.0f, 8.0f, 6.0f).texture("#0").end()
            .face(Direction.UP).uvs(7.0f, 5.0f, 9.0f, 6.0f).texture("#0").end()
            .face(Direction.DOWN).uvs(9.0f, 5.0f, 7.0f, 6.0f).texture("#0").end()
            .end()
            .element()
            .from(5.5f, 0.0f, 9.5f)
            .to(9.5f, 1.0f, 10.5f)
            .face(Direction.EAST).uvs(9.0f, 4.0f, 10.0f, 5.0f).texture("#0").end()
            .face(Direction.SOUTH).uvs(6.0f, 4.0f, 10.0f, 5.0f).texture("#0").end()
            .face(Direction.WEST).uvs(6.0f, 4.0f, 7.0f, 5.0f).texture("#0").end()
            .face(Direction.UP).uvs(6.0f, 4.0f, 10.0f, 5.0f).texture("#0").end()
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 4.0f, 1.0f).texture("#0").end()
            .end()
            .element()
            .from(4.5f, 0.0f, 5.5f)
            .to(10.5f, 1.0f, 9.5f)
            .face(Direction.NORTH).uvs(11.0f, 0.0f, 5.0f, 1.0f).texture("#0").end()
            .face(Direction.EAST).uvs(10.0f, 0.0f, 11.0f, 4.0f).rotation(COUNTERCLOCKWISE_90).texture("#0").end()
            .face(Direction.SOUTH).uvs(5.0f, 3.0f, 11.0f, 4.0f).texture("#0").end()
            .face(Direction.WEST).uvs(5.0f, 0.0f, 6.0f, 4.0f).rotation(COUNTERCLOCKWISE_90).texture("#0").end()
            .face(Direction.UP).uvs(5.0f, 0.0f, 11.0f, 4.0f).texture("#0").end()
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 6.0f, 4.0f).texture("#0").end()
            .end()
            .element()
            .from(5.5f, 1.0f, 6.5f)
            .to(10.5f, 2.0f, 7.5f)
            .face(Direction.NORTH).uvs(6.0f, 1.0f, 11.0f, 2.0f).texture("#0").end()
            .face(Direction.EAST).uvs(10.0f, 1.0f, 11.0f, 2.0f).texture("#0").end()
            .face(Direction.SOUTH).uvs(6.0f, 1.0f, 11.0f, 2.0f).texture("#0").end()
            .face(Direction.WEST).uvs(6.0f, 1.0f, 7.0f, 2.0f).texture("#0").end()
            .face(Direction.UP).uvs(6.0f, 1.0f, 11.0f, 2.0f).texture("#0").end()
            .end()
            .element()
            .from(6.5f, 1.0f, 5.5f)
            .to(9.5f, 2.0f, 6.5f)
            .face(Direction.NORTH).uvs(7.0f, 0.0f, 10.0f, 1.0f).texture("#0").end()
            .face(Direction.EAST).uvs(9.0f, 0.0f, 10.0f, 1.0f).texture("#0").end()
            .face(Direction.WEST).uvs(7.0f, 0.0f, 8.0f, 1.0f).texture("#0").end()
            .face(Direction.UP).uvs(7.0f, 0.0f, 10.0f, 1.0f).texture("#0").end()
            .end()
            .element()
            .from(5.5f, 1.0f, 7.5f)
            .to(9.5f, 2.0f, 8.5f)
            .face(Direction.EAST).uvs(9.0f, 2.0f, 10.0f, 3.0f).texture("#0").end()
            .face(Direction.SOUTH).uvs(6.0f, 2.0f, 10.0f, 3.0f).texture("#0").end()
            .face(Direction.WEST).uvs(6.0f, 2.0f, 7.0f, 3.0f).texture("#0").end()
            .face(Direction.UP).uvs(6.0f, 2.0f, 10.0f, 3.0f).texture("#0").end()
            .end()
            .element()
            .from(5.5f, 1.0f, 8.5f)
            .to(8.5f, 2.0f, 9.5f)
            .face(Direction.EAST).uvs(8.0f, 3.0f, 9.0f, 4.0f).texture("#0").end()
            .face(Direction.SOUTH).uvs(6.0f, 3.0f, 9.0f, 4.0f).texture("#0").end()
            .face(Direction.WEST).uvs(6.0f, 3.0f, 7.0f, 4.0f).texture("#0").end()
            .face(Direction.UP).uvs(6.0f, 3.0f, 9.0f, 4.0f).texture("#0").end()
            .end()
            .element()
            .from(6.5f, 1.0f, 9.5f)
            .to(7.5f, 2.0f, 10.5f)
            .face(Direction.EAST).uvs(7.0f, 4.0f, 8.0f, 5.0f).texture("#0").end()
            .face(Direction.SOUTH).uvs(7.0f, 4.0f, 8.0f, 5.0f).texture("#0").end()
            .face(Direction.WEST).uvs(7.0f, 4.0f, 8.0f, 5.0f).texture("#0").end()
            .face(Direction.UP).uvs(7.0f, 4.0f, 8.0f, 5.0f).texture("#0").end()
            .end()
            .transforms()
            .transform(THIRD_PERSON_RIGHT_HAND).rotation(90.0f, 0.0f, 0.0f).translation(0.0f, 2.25f, 5.75f).scale(0.75f, 0.75f, 0.75f).end()
            .transform(THIRD_PERSON_LEFT_HAND).rotation(90.0f, 0.0f, 0.0f).translation(0.0f, 2.25f, 5.75f).scale(0.75f, 0.75f, 0.75f).end()
            .transform(GROUND).translation(0.0f, 5.0f, 0.0f).end()
            .transform(GUI).rotation(112.0f, 0.0f, 22.0f).translation(-4.25f, -4.0f, 0.0f).scale(1.5f, 1.5f, 1.5f).end()
            .transform(FIXED).rotation(-90.0f, -180.0f, 0.0f).translation(0.0f, 0.0f, -12.0f).scale(1.5f, 1.5f, 1.5f).end()
            .end();
        this.getBuilder("evolution:block/grass")
            .parent(this.getExistingFile(new ResourceLocation("block/block")))
            .element()
            .from(0.0f, 0.0f, 0.0f)
            .to(16.0f, 16.0f, 16.0f)
            .face(Direction.EAST).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.EAST).end()
            .face(Direction.SOUTH).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.SOUTH).end()
            .face(Direction.NORTH).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.NORTH).end()
            .face(Direction.WEST).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.WEST).end()
            .face(Direction.UP).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#top").cullface(Direction.UP).tintindex(0).end()
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#bottom").cullface(Direction.DOWN).end()
            .end()
            .element()
            .from(0.0f, 0.0f, 0.0f)
            .to(16.0f, 16.0f, 16.0f)
            .face(Direction.EAST).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#overlay").cullface(Direction.EAST).tintindex(0).end()
            .face(Direction.SOUTH).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#overlay").cullface(Direction.SOUTH).tintindex(0).end()
            .face(Direction.NORTH).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#overlay").cullface(Direction.NORTH).tintindex(0).end()
            .face(Direction.WEST).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#overlay").cullface(Direction.WEST).tintindex(0).end()
            .end();
        this.getBuilder("evolution:block/slab_4_1")
            .parent(this.getExistingFile(new ResourceLocation("block/thin_block")))
            .texture("particle", "#side")
            .element()
            .from(0.0f, 0.0f, 0.0f)
            .to(16.0f, 4.0f, 16.0f)
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#bottom").cullface(Direction.DOWN).end()
            .face(Direction.UP).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#top").end()
            .face(Direction.NORTH).uvs(0.0f, 12.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.NORTH).end()
            .face(Direction.SOUTH).uvs(0.0f, 12.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.SOUTH).end()
            .face(Direction.WEST).uvs(0.0f, 12.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.WEST).end()
            .face(Direction.EAST).uvs(0.0f, 12.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.EAST).end()
            .end();
        this.getBuilder("evolution:block/slab_4_3")
            .parent(this.getExistingFile(new ResourceLocation("block/thin_block")))
            .texture("particle", "#side")
            .element()
            .from(0.0f, 0.0f, 0.0f)
            .to(16.0f, 12.0f, 16.0f)
            .face(Direction.DOWN).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#bottom").cullface(Direction.DOWN).end()
            .face(Direction.UP).uvs(0.0f, 0.0f, 16.0f, 16.0f).texture("#top").end()
            .face(Direction.NORTH).uvs(0.0f, 4.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.NORTH).end()
            .face(Direction.SOUTH).uvs(0.0f, 4.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.SOUTH).end()
            .face(Direction.WEST).uvs(0.0f, 4.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.WEST).end()
            .face(Direction.EAST).uvs(0.0f, 4.0f, 16.0f, 16.0f).texture("#side").cullface(Direction.EAST).end()
            .end();
    }

    @Override
    public String type() {
        return "BlockModel";
    }
}
