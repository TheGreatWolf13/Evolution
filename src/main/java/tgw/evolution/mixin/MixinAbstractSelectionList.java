package tgw.evolution.mixin;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import tgw.evolution.util.collection.lists.custom.TrackedList;

import java.util.List;

@Mixin(AbstractSelectionList.class)
public abstract class MixinAbstractSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractContainerEventHandler {

    @Mutable @Shadow @Final private final List<E> children = new TrackedList<>((AbstractSelectionList<E>) (Object) this);
}
