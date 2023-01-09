//package tgw.evolution.items;
//
//import net.minecraft.world.item.Item;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.material.Fluid;
//import tgw.evolution.blocks.fluids.FluidGeneric;
//import tgw.evolution.init.EvolutionItems;
//
//import java.util.function.Supplier;
//
//public class ItemBucketCeramic extends ItemGenericBucket {
//
//    public ItemBucketCeramic(Supplier<? extends Fluid> fluid, Properties properties) {
//        super(fluid, properties);
//    }
//
//    @Override
//    public ItemStack emptyBucket() {
//        return new ItemStack(EvolutionItems.bucket_ceramic_empty.get());
//    }
//
//    @Override
//    public int getAmount(ItemStack stack) {
//        if (!stack.hasTag()) {
//            return 0;
//        }
//        return stack.getTag().getInt("Amount");
//    }
//
//    @Override
//    public Item getFullBucket(Fluid fluid) {
//        if (fluid instanceof FluidGeneric) {
//            switch (((FluidGeneric) fluid).getId()) {
//                case FluidGeneric.FRESH_WATER:
//                    return EvolutionItems.bucket_ceramic_fresh_water.get();
//                case FluidGeneric.SALT_WATER:
//                    return EvolutionItems.bucket_ceramic_salt_water.get();
//            }
//        }
//        return EvolutionItems.bucket_ceramic_empty.get();
//    }
//
//    @Override
//    public int getMaxAmount() {
//        return 1_000;
//    }
//}
