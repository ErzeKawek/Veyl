package net.erzekawek.veyl.utils;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LimitedContainer implements Container {

    private final Container inner;
    private final int size;

    public LimitedContainer(Container inner, int paddedSize) {
        this.inner = inner; this.size = paddedSize;
    }

    private boolean real(int i) {
        return i < inner.getContainerSize();
    }

    @Override public int getContainerSize() {
        return size;
    }

    @Override public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override public ItemStack getItem(int i) {
        return real(i) ? inner.getItem(i) : ItemStack.EMPTY;
    }

    @Override public ItemStack removeItem(int i, int n) {
        return real(i) ? inner.removeItem(i, n) : ItemStack.EMPTY;
    }

    @Override public ItemStack removeItemNoUpdate(int i) {
        return real(i) ? inner.removeItemNoUpdate(i) : ItemStack.EMPTY;
    }

    @Override public void setItem(int i, ItemStack s) {
        if (real(i)) inner.setItem(i, s);
    }

    @Override public void setChanged() {
        inner.setChanged();
    }

    @Override public boolean stillValid(Player p) {
        return inner.stillValid(p);
    }

    @Override public void clearContent() {
        inner.clearContent();
    }

    @Override public boolean canPlaceItem(int i, ItemStack s) {
        return real(i) && inner.canPlaceItem(i, s);
    }

}
