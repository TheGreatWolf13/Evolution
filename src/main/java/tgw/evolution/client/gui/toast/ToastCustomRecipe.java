package tgw.evolution.client.gui.toast;

import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import tgw.evolution.util.toast.ToastHolderRecipe;

public class ToastCustomRecipe extends ToastGeneric<ToastHolderRecipe> {

    private static final ITextComponent TITLE_TEXT = new TranslationTextComponent("recipe.toast.title");
    private static final ITextComponent DESCRIPTION_TEXT = new TranslationTextComponent("recipe.toast.description");

    protected ToastCustomRecipe(ToastHolderRecipe holder) {
        super(holder);
    }

    public static void addOrUpdate(ToastGui gui, ToastHolderRecipe recipe) {
        ToastCustomRecipe toast = gui.getToast(ToastCustomRecipe.class, NO_TOKEN);
        if (toast == null) {
            gui.addToast(new ToastCustomRecipe(recipe));
        }
        else {
            toast.addItem(recipe);
        }
    }

    @Override
    protected ITextComponent getDescription() {
        return DESCRIPTION_TEXT;
    }

    @Override
    protected ITextComponent getTitle() {
        return TITLE_TEXT;
    }
}
