package tgw.evolution.datagen;

import net.minecraft.data.DataGenerator;
import org.jetbrains.annotations.Nullable;
import tgw.evolution.Evolution;
import tgw.evolution.datagen.util.ExistingFileHelper;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;
import tgw.evolution.util.collection.sets.OHashSet;
import tgw.evolution.util.collection.sets.OSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DataGenerators {

    private static final OSet<String> FOUND_FILES = new OHashSet<>();

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

    private static void recordAllExistingFiles(OList<Path> existingPaths) {
        for (int i = 0, len = existingPaths.size(); i < len; ++i) {
            Path path = existingPaths.get(i);
            File file = path.toFile();
            if (file.isDirectory()) {
                String fileName = file.getAbsolutePath() + "\\";
                recordSubfolder(path, fileName, "assets/evolution/blockstates");
                recordSubfolder(path, fileName, "assets/evolution/models/block");
                recordSubfolder(path, fileName, "assets/evolution/models/item");
                recordSubfolder(path, fileName, "data/evolution/tags/blocks");
                recordSubfolder(path, fileName, "data/evolution/tags/items");
                recordSubfolder(path, fileName, "data/evolution/tags/fluids");
                recordSubfolder(path, fileName, "data/evolution/advancements");
                recordSubfolder(path, fileName, "data/evolution/recipes");
            }
        }
    }

    private static void recordSubfolder(Path basePath, String baseName, String subfolderName) {
        Path subfolderPath = basePath.resolve(subfolderName);
        File subfolder = subfolderPath.toFile();
        if (subfolder.isDirectory()) {
            //noinspection DataFlowIssue
            for (File file : subfolder.listFiles()) {
                recursivelyRecord(file, FOUND_FILES, baseName);
            }
        }
    }

    private static void recursivelyRecord(File file, OSet<String> set, String name) {
        if (file.isDirectory()) {
            //noinspection DataFlowIssue
            for (File f : file.listFiles()) {
                recursivelyRecord(f, set, name);
            }
        }
        else {
            if (file.getName().endsWith(".json")) {
                String fullName = file.getAbsolutePath();
                int index = fullName.indexOf(name);
                set.add(fullName.substring(index + name.length()).replace('\\', '/'));
            }
        }
    }

    public static void run() {
        String out = System.clearProperty("evolution.datagen.out");
        if (out == null) {
            throw new NullPointerException("Datagen output cannot be null. Set it using -Devolution.datagen.out=\"path\\to\\location\"");
        }
        OList<Path> existingPaths = readPaths(System.clearProperty("evolution.datagen.existing"));
        recordAllExistingFiles(existingPaths);
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
        if (!FOUND_FILES.isEmpty()) {
            Evolution.warn("The following files were found in the existing directories and are probably unused: ");
            OList<String> list = new OArrayList<>(FOUND_FILES);
            list.sort(null);
            for (int i = 0, len = list.size(); i < len; ++i) {
                Evolution.warn(list.get(i));
            }
        }
    }

    public static void submitPath(String path) {
        FOUND_FILES.remove(path);
    }
}
