//package tgw.evolution.hooks;
//
//import net.minecraft.entity.Entity;
//import tgw.evolution.init.EvolutionDamage;
//
//public final class EntityHooks {
//
//    private EntityHooks() {
//    }
//
//    /**
//     * Hooks from {@link Entity#dealFireDamage(int)}
//     */
//    @EvolutionHook
//    public static void dealFireDamage(Entity entity, int amount) {
//        if (!entity.isImmuneToFire()) {
//            entity.attackEntityFrom(EvolutionDamage.IN_FIRE, amount * 2.5f);
//        }
//    }
//}
