package net.erzekawek.veyl.utils;

import net.minecraft.world.food.FoodProperties;

public class ConsumptionComponent {

    public static final FoodProperties EXTREMELY_BAD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.2F)
            .alwaysEdible()
            .build();

    public static final FoodProperties BAD = new FoodProperties.Builder()
            .nutrition(2)
            .saturationModifier(0.4F)
            .alwaysEdible()
            .build();

    public static final FoodProperties NOT_BAD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.6F)
            .alwaysEdible()
            .build();

    public static final FoodProperties GOOD = new FoodProperties.Builder()
            .nutrition(6)
            .saturationModifier(0.6F)
            .alwaysEdible()
            .build();

    public static final FoodProperties GREAT = new FoodProperties.Builder()
            .nutrition(8)
            .saturationModifier(0.8F)
            .alwaysEdible()
            .build();

    public static final FoodProperties BEST = new FoodProperties.Builder()
            .nutrition(10)
            .saturationModifier(1.0F)
            .alwaysEdible()
            .build();
}
