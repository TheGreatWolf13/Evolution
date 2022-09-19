package tgw.evolution.client.gui.recipebook;

import tgw.evolution.Evolution;

public class ComponentRecipeBookSmall extends ComponentRecipeBook {

    public ComponentRecipeBookSmall() {
        super(10, Evolution.getResource("textures/gui/recipe_book_small.png"), 147, 117);
    }

    @Override
    protected boolean areTabsOnTheRight() {
        return true;
    }

    @Override
    protected int getTabsDx() {
        return 146;
    }

    @Override
    protected void initCoordinates() {
        this.xOffset = this.isWidthTooNarrow() ? 0 : -86;
        this.cornerX = (this.getWidth() - 148) / 2 - this.xOffset;
        this.cornerY = (this.getHeight() - 105) / 2 + 28;
    }

    @Override
    public int updateScreenPosition(int width, int imageWidth) {
        if (this.isVisible() && !this.isWidthTooNarrow()) {
            return -177 + (width - imageWidth + 200) / 2;
        }
        return (width - imageWidth) / 2;
    }
}
