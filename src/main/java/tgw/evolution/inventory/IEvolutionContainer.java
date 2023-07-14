package tgw.evolution.inventory;

import net.minecraft.world.Container;

public interface IEvolutionContainer {

    void addSlot(Container container, int index, int x, int y);

    default void addSlotBox(Container container, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0; j < verAmount; j++) {
            index = this.addSlotRange(container, index, x, y, horAmount, dx);
            y += dy;
        }
    }

    default int addSlotRange(Container container, int index, int x, int y, int amount, int dx) {
        for (int i = 0; i < amount; i++) {
            this.addSlot(container, index, x, y);
            x += dx;
            index++;
        }
        return index;
    }

    default void layoutPlayerInventorySlots(Container container, int leftCol, int topRow) {
        topRow += 58;
        this.addSlotRange(container, 0, leftCol, topRow, 9, 18);
        topRow -= 58;
        this.addSlotBox(container, 9, leftCol, topRow, 9, 18, 3, 18);
    }
}
