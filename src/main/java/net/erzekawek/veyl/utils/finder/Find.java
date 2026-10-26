package net.erzekawek.veyl.utils.finder;

import net.minecraft.resources.Identifier;

public class Find {

    public static Identifier findNBT(String modid, String space, String name) {
        return Identifier.fromNamespaceAndPath(modid,space + "/" + name);
    }

    private static Identifier inMC(String id) {
        return Identifier.withDefaultNamespace(id);
    }

}
