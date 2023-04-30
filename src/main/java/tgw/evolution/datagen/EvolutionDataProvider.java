package tgw.evolution.datagen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import tgw.evolution.Evolution;
import tgw.evolution.util.math.MathHelper;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface EvolutionDataProvider<T> extends DataProvider {

    Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    default <R> boolean checkForExistance(R key, JsonElement json, Function<R, String> pathMaker) {
        List<Path> pathToCheck = this.getPathToCheck(key, pathMaker);
        for (int i = 0, len = pathToCheck.size(); i < len; i++) {
            Path path = pathToCheck.get(i);
            if (Files.exists(path)) {
                //noinspection ObjectAllocationInLoop
                try (FileReader reader = new FileReader(path.toString())) {
                    JsonElement j = GSON.fromJson(reader, JsonElement.class);
                    if (MathHelper.jsonEquals(json, j)) {
                        return false;
                    }
                }
                catch (IOException ignored) {
                }
            }
        }
        return true;
    }

    Collection<Path> existingPaths();

    default <R> List<Path> getPathToCheck(R id, Function<R, String> pathMaker) {
        return this.existingPaths().stream()
                   .map(f -> f.resolve(pathMaker.apply(id)))
                   .collect(Collectors.toList());
    }

    String makePath(T id);

    default void save(HashCache cache, JsonElement json, Path path, T key) {
        this.save(cache, json, path, key, this::makePath);
    }

    default <R> void save(HashCache cache, JsonElement json, Path path, R key, Function<R, String> pathMaker) {
        try {
            String jsonString = GSON.toJson(json);
            String hash = SHA1.hashUnencodedChars(jsonString).toString();
            if (!Objects.equals(cache.getHash(path), hash) || this.checkForExistance(key, json, pathMaker)) {
                Files.createDirectories(path.getParent());
                try (BufferedWriter bufferedwriter = Files.newBufferedWriter(path)) {
                    bufferedwriter.write(jsonString);
                }
            }
            cache.putNew(path, hash);
        }
        catch (IOException e) {
            Evolution.error("Couldn't save {} {}", this.type(), path, e);
        }
    }

    String type();
}
