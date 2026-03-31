package net.mythic.jjkmod.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.mythic.jjkmod.character.CharacterSelectionManager;
import net.mythic.jjkmod.character.GradeStatsManager;
import net.mythic.jjkmod.character.JJKCharacter;
import net.mythic.jjkmod.character.JJKGrade;

public class PromoterItem extends Item {

    public PromoterItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient() && user instanceof ServerPlayerEntity serverPlayer) {
            // Must have a character selected
            JJKCharacter character = CharacterSelectionManager.getSelectedCharacter(serverPlayer);
            if (character == JJKCharacter.NONE) {
                serverPlayer.sendMessage(
                        Text.literal("\u00a7cYou must select a character first!"), true);
                return TypedActionResult.fail(stack);
            }

            // Get current grade (default to Grade 4 if somehow missing)
            JJKGrade currentGrade = CharacterSelectionManager.getGrade(serverPlayer, character);
            if (currentGrade == null) {
                currentGrade = JJKGrade.GRADE_4;
            }

            // Check if already at max
            JJKGrade nextGrade = currentGrade.getNext();
            if (nextGrade == null) {
                serverPlayer.sendMessage(
                        Text.literal("\u00a76You are already Special Grade!"), true);
                return TypedActionResult.fail(stack);
            }

            // Promote!
            CharacterSelectionManager.setGrade(serverPlayer, character, nextGrade);

            // Apply new grade stats (CE + HP)
            GradeStatsManager.applyGradeStats(serverPlayer, nextGrade);

            serverPlayer.sendMessage(
                    Text.literal("\u00a7a\u00a7lPromoted to " + nextGrade.getDisplayName() + "!"
                            + " \u00a77(CE: " + nextGrade.getMaxCE()
                            + " | HP: " + nextGrade.getMaxHP() + ")"),
                    true);

            // Play level-up sound
            world.playSound(null, serverPlayer.getBlockPos(),
                    SoundEvents.ENTITY_PLAYER_LEVELUP, SoundCategory.PLAYERS, 1.0f, 1.0f);

            // Consume one item (not in creative)
            if (!serverPlayer.getAbilities().creativeMode) {
                stack.decrement(1);
            }

            return TypedActionResult.success(stack);
        }

        return TypedActionResult.success(stack, world.isClient());
    }
}
