package net.erzekawek.veyl.items;

import net.minecraft.world.item.Item;

public class AbstractUsableItem extends Item {
    private final boolean consume;

    public AbstractUsableItem(Properties properties, boolean consume) {
        super(properties);
        this.consume = consume;
    }

    public boolean isConsume() {
        return consume;
    }
}
