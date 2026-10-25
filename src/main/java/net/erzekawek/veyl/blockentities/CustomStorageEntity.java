package net.erzekawek.veyl.blockentities;

import net.erzekawek.veyl.blocks.VeylConfigurableStorageBlock;
import net.erzekawek.veyl.utils.LimitedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class CustomStorageEntity extends BaseContainerBlockEntity {
    private NonNullList<ItemStack> items;

    public CustomStorageEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.items = NonNullList.withSize(slotCount(state), ItemStack.EMPTY);
    }

    private static int slotCount(BlockState state) {
        return state.getBlock() instanceof VeylConfigurableStorageBlock b ? b.slots() : 27;
    }

    @Override protected Component getDefaultName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }
    @Override protected NonNullList<ItemStack> getItems() {
        return items;
    }
    @Override protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        int rows = Mth.clamp((items.size() + 8) / 9, 1, 6);
        MenuType<ChestMenu> menuType = switch (rows) {
            case 1 -> MenuType.GENERIC_9x1; case 2 -> MenuType.GENERIC_9x2; case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4; case 5 -> MenuType.GENERIC_9x5; default -> MenuType.GENERIC_9x6;
        };
        return new ChestMenu(menuType, id, inventory, new LimitedContainer(this, rows * 9), rows);
    }

    @Override public int getContainerSize() {
        return items.size();
    }
    @Override public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false; return true;
    }
    @Override public ItemStack getItem(int slot) {
        return items.get(slot);
    }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack r = ContainerHelper.removeItem(items, slot, amount);
        if (!r.isEmpty()) setChanged();
        return r;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack); setChanged();
    }
    @Override public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }
    @Override public void clearContent() {
        items.clear();
    }

    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(slotCount(getBlockState()), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
    }
    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
    }
}
