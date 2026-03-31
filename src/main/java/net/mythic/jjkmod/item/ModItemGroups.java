package net.mythic.jjkmod.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.mythic.jjkmod.JJKMod;

public class ModItemGroups {

    public static final RegistryKey<ItemGroup> JJK_GROUP_KEY = RegistryKey.of(
            RegistryKeys.ITEM_GROUP, Identifier.of(JJKMod.MOD_ID, "jjk_group"));

    public static void initialize() {
        Registry.register(Registries.ITEM_GROUP, JJK_GROUP_KEY,
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ModItems.PROMOTER))
                        .displayName(Text.literal("JJK Mod"))
                        .entries((context, entries) -> {
                            entries.add(ModItems.PROMOTER);
                        })
                        .build());

        JJKMod.LOGGER.info("Registered JJK Mod creative tab");
    }
}
