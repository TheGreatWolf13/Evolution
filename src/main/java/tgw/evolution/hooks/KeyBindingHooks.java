package tgw.evolution.hooks;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;

public final class KeyBindingHooks {

    private KeyBindingHooks() {
    }

    /**
     * Hooks from {@link KeyBinding#getLocalizedName()}, replacing the method.
     */
    @EvolutionHook
    public static String getLocalizedName(KeyBinding keyBinding, InputMappings.Input keyCode) {
        return keyBinding.getKeyModifier().getLocalizedComboName(keyCode, () -> {
            String translationKey = keyCode.getTranslationKey();
            String formattedString = I18n.format(translationKey);
            int i = keyCode.getKeyCode();
            String generatedName = null;
            switch (keyCode.getType()) {
                case KEYSYM:
                    generatedName = InputMappings.getKeynameFromKeycode(i);
                    break;
                case SCANCODE:
                    generatedName = InputMappings.getKeyNameFromScanCode(i);
                    break;
                case MOUSE:
                    generatedName = formattedString.equals(translationKey) ? I18n.format(InputMappings.Type.MOUSE.getName(), i + 1) : formattedString;
            }
            return formattedString.equals(translationKey) ? generatedName == null ? translationKey : generatedName : formattedString;
        });
    }
}
