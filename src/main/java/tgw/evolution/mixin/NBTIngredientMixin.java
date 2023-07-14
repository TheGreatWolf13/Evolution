//package tgw.evolution.mixin;
//
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.common.crafting.AbstractIngredient;
//import net.minecraftforge.common.crafting.NBTIngredient;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Overwrite;
//import org.spongepowered.asm.mixin.Shadow;
//
//import javax.annotation.Nullable;
//
//@Mixin(NBTIngredient.class)
//public abstract class NBTIngredientMixin extends AbstractIngredient {
//
//    @Shadow
//    @Final
//    private ItemStack stack;
//
//    /**
//     * @author TheGreatWolf
//     * @reason Force size comparison as well.
//     */
//    @Override
//    @Overwrite
//    public boolean test(@Nullable ItemStack input) {
//        if (input == null) {
//            return false;
//        }
//        //Can't use areItemStacksEqualUsingNBTShareTag because it compares stack size as well
//        return this.stack.getItem() == input.getItem() &&
//               this.stack.getCount() <= input.getCount() &&
//               this.stack.getDamageValue() == input.getDamageValue() &&
//               this.stack.areShareTagsEqual(input);
//    }
//}
