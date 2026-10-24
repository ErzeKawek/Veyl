package net.erzekawek.veyl.items;

import net.minecraft.world.item.Item;

public class AbstractConsumableItem extends Item {
    private final boolean consume;

    public AbstractConsumableItem(Properties properties, boolean consume) {
        super(properties);
        this.consume = consume;
    }

    public boolean isConsume() {
        return consume;
    }
}
