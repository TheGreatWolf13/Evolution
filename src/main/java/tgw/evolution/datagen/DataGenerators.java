package tgw.evolution.datagen;

import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.datagen.util.ExistingFileHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DataGenerators {

    private DataGenerators() {
    }

    private static String read(String original, int start, int end) {
        try {
            return original.substring(start, end);
        }
        catch (StringIndexOutOfBoundsException e) {
            throw new IllegalStateException("Invalid existing paths!", e);
        }
    }

    private static OList<Path> readPaths(@Nullable String paths) {
        if (paths == null) {
            return OList.emptyList();
        }
        OList<Path> list = new OArrayList<>();
        int status = 0;
        int startIndex = -1;
        for (int i = 0, len = paths.length(); i < len; ++i) {
            char ch = paths.charAt(i);
            switch (status) {
                case 0 -> {
                    if (Character.isSpaceChar(ch)) {
                        continue;
                    }
                    if (ch != '"') {
                        throw new IllegalStateException("Invalid existing paths! Should be in quotation marks! String: <<" + paths + ">> Index: " + i);
                    }
                    status = 1;
                    startIndex = i + 1;
                }
                case 1 -> {
                    if (ch == '"') {
                        list.add(Path.of(read(paths, startIndex, i)));
                        status = 2;
                        startIndex = -1;
                    }
                }
                case 2 -> {
                    if (ch != ',') {
                        throw new IllegalStateException("Invalid existing paths! Should be separated by commas! String: <<" + paths + ">> Index: " + i);
                    }
                    status = 0;
                }
            }
        }
        if (status != 2) {
            throw new IllegalStateException("Invalid existing paths! Should end with quotation marks! String: <<" + paths + ">>");
        }
        return list;
    }

    public static void run() {
        String out = System.clearProperty("evolution.datagen.out");
        if (out == null) {
            throw new NullPointerException("Datagen output cannot be null. Set it using -Devolution.datagen.out=\"path\\to\\location\"");
        }
        OList<Path> existingPaths = readPaths(System.clearProperty("evolution.datagen.existing"));
        DataGenerator generator = new DataGenerator(Path.of(out), List.of());
        generator.addProvider(new RecipeProvider(generator, existingPaths));
        generator.addProvider(new AdvancementProvider(generator, existingPaths));
        ExistingFileHelper existingFileHelper = new ExistingFileHelper(existingPaths, false, null, null);
        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(generator, existingPaths, existingFileHelper);
        generator.addProvider(blockTagsProvider);
        generator.addProvider(new ItemTagsProvider(generator, existingPaths, existingFileHelper, blockTagsProvider));
        generator.addProvider(new FluidTagsProvider(generator, existingPaths, existingFileHelper));
        generator.addProvider(new BuiltinItemModelProvider(generator, existingPaths, existingFileHelper));
        generator.addProvider(new BlockModelProvider(generator, existingPaths, existingFileHelper));
        generator.addProvider(new ItemModelProvider(generator, existingPaths, existingFileHelper));
        generator.addProvider(new BlockStateProvider(generator, existingPaths, existingFileHelper));
        try {
            generator.run();
        }
        catch (IOException e) {
            Evolution.error("Exception while running DataGenerators!", e);
        }
    }
}
