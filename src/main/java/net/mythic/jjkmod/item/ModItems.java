package net.mythic.jjkmod.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.mythic.jjkmod.JJKMod;

public class ModItems {

    public static final Item PROMOTER = Registry.register(
            Registries.ITEM,
            Identifier.of(JJKMod.MOD_ID, "promoter"),
            new PromoterItem(new Item.Settings())
    );

    public static void initialize() {
        JJKMod.LOGGER.info("Registering JJK Mod items");
    }
}
