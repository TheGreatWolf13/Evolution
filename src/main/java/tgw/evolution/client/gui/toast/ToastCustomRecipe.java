package tgw.evolution.client.gui.toast;

import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import tgw.evolution.util.toast.ToastHolderRecipe;

public class ToastCustomRecipe extends ToastGeneric<ToastHolderRecipe> {

    private static final Component TITLE_TEXT = new TranslatableComponent("recipe.toast.title");
    private static final Component DESCRIPTION_TEXT = new TranslatableComponent("recipe.toast.description");

    protected ToastCustomRecipe(ToastHolderRecipe holder) {
        super(holder);
    }

    public static void addOrUpdate(ToastComponent gui, ToastHolderRecipe recipe) {
        ToastCustomRecipe toast = gui.getToast(ToastCustomRecipe.class, NO_TOKEN);
        if (toast == null) {
            gui.addToast(new ToastCustomRecipe(recipe));
        }
        else {
            toast.addItem(recipe);
        }
    }

    @Override
    protected Component getDescription() {
        return DESCRIPTION_TEXT;
    }

    @Override
    protected Component getTitle() {
        return TITLE_TEXT;
    }
}
