package tgw.evolution.hooks.asm;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import tgw.evolution.util.collection.lists.OArrayList;
import tgw.evolution.util.collection.lists.OList;

public final class Transformers {

    private static final OList<IClassTransformer> TRANSFORMERS = new OArrayList<>();

    private Transformers() {
    }

    public static void registerTransformer(IClassTransformer transformer) {
        TRANSFORMERS.add(transformer);
    }

    public static void transform(String name, ClassNode classNode, String mixinClassName, IMixinInfo info) {
        for (int i = 0; i < TRANSFORMERS.size(); i++) {
            IClassTransformer transformer = TRANSFORMERS.get(i);
            if (transformer.handlesClass(name, mixinClassName)) {
                try {
                    transformer.transformClass(classNode, name, mixinClassName, info);
                }
                catch (Throwable t) {
                    CoreModLoader.LOGGER.error("Exception while transforming class!");
                    CoreModLoader.LOGGER.error("Class Name: {}", name);
                    CoreModLoader.LOGGER.error("Transformer Name: {}", transformer.name());
                    CoreModLoader.LOGGER.error("Exception: ", t);
                }
                if (transformer.shouldRemoveAfterPatch()) {
                    TRANSFORMERS.remove(i--);
                }
                CoreModLoader.LOGGER.info("Successfully patched class {}", name);
            }
        }
    }
}
