/*
 *       WolfyUtilities, APIs and Utilities for Minecraft Spigot plugins
 *                      Copyright (C) 2021  WolfyScript
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.wolfyscript.utilities.api.nms.v1_20_R1;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import me.wolfyscript.utilities.api.nms.NMSUtil;
import me.wolfyscript.utilities.api.nms.inventory.RecipeType;
import me.wolfyscript.utilities.api.nms.v1_20_R1.inventory.RecipeIterator;
import me.wolfyscript.utilities.util.NamespacedKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.craftbukkit.v1_20_R1.block.CraftFurnace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryCrafting;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventorySmithing;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftInventoryView;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftNamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

public class RecipeUtilImpl extends me.wolfyscript.utilities.api.nms.RecipeUtil {

    private static final Field FURNACE_BLOCK_ENTITY_FIELD;
    private static final Field CACHED_CHECK_FURNACE_FIELD;
    private static final Field CACHED_CHECK_LAST_RECIPE_FIELD;
    private static final Field SMITHING_MENU_SELECTED_RECIPE_FIELD;

    static {
        FURNACE_BLOCK_ENTITY_FIELD = Arrays.stream(CraftFurnace.class.getFields()).filter(field -> field.getType().equals(AbstractFurnaceBlockEntity.class)).findFirst().orElse(null);
        CACHED_CHECK_LAST_RECIPE_FIELD = Arrays.stream(RecipeManager.CachedCheck.class.getFields()).filter(field -> field.getType().equals(ResourceLocation.class)).findFirst().orElse(null);
        CACHED_CHECK_FURNACE_FIELD = Arrays.stream(AbstractFurnaceBlockEntity.class.getFields()).filter(field -> field.getType().equals(RecipeManager.CachedCheck.class)).findFirst().orElse(null);
        SMITHING_MENU_SELECTED_RECIPE_FIELD = Arrays.stream(SmithingMenu.class.getDeclaredFields()).filter(field -> field.getType().equals(SmithingRecipe.class)).findFirst().orElse(null);
    }

    protected RecipeUtilImpl(NMSUtil nmsUtil) {
        super(nmsUtil);
    }

    @Override
    public @NotNull Iterator<Recipe> recipeIterator(RecipeType recipeType) {
        return new RecipeIterator(recipeType);
    }

    @Override
    public void setCurrentRecipe(InventoryView view, NamespacedKey namespacedKey) {
        if (view == null) return;
        net.minecraft.world.item.crafting.Recipe<?> recipe;
        if (namespacedKey == null) {
            recipe = null;
        } else {
            Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> recipeOptional = MinecraftServer.getServer().getRecipeManager().byKey((CraftNamespacedKey.toMinecraft(namespacedKey.bukkit())));
            if (recipeOptional.isEmpty()) return;
            recipe = recipeOptional.get();
        }
        Inventory inventory = view.getTopInventory();
        if (inventory instanceof CraftInventoryCrafting crafting) {
            crafting.getInventory().setCurrentRecipe(recipe);
        } else if (inventory instanceof CraftInventorySmithing smithing) {
            if (view instanceof CraftInventoryView craftView) {
                if (craftView.getHandle() instanceof SmithingMenu smithingMenu) {
                    try {
                        SMITHING_MENU_SELECTED_RECIPE_FIELD.setAccessible(true);
                        SMITHING_MENU_SELECTED_RECIPE_FIELD.set(smithingMenu, recipe);
                    } catch (IllegalAccessException e) {
                        return;
                    }
                }
            }
            smithing.getResultInventory().setRecipeUsed(recipe);
        } else if (inventory instanceof CraftInventoryFurnace inventoryFurnace) {
            if (inventoryFurnace.getHolder() instanceof CraftFurnace<?> craftFurnace) {
                try {
                    AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) FURNACE_BLOCK_ENTITY_FIELD.get(craftFurnace);
                    if (blockEntity == null) return;
                    RecipeManager.CachedCheck<?, ?> check = (RecipeManager.CachedCheck<?, ?>) CACHED_CHECK_FURNACE_FIELD.get(blockEntity);
                    if (check == null) return;
                    CACHED_CHECK_LAST_RECIPE_FIELD.setAccessible(true);
                    CACHED_CHECK_LAST_RECIPE_FIELD.set(check, recipe == null ? null : recipe.getId());
                } catch (IllegalAccessException e) {
                    return;
                }
            }
        }
    }

    @Override
    public void setCurrentRecipe(Inventory inventory, NamespacedKey namespacedKey) {
        if (inventory == null) return;
        net.minecraft.world.item.crafting.Recipe<?> recipe;
        if (namespacedKey == null) {
            recipe = null;
        } else {
            Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> recipeOptional = MinecraftServer.getServer().getRecipeManager().byKey((CraftNamespacedKey.toMinecraft(namespacedKey.bukkit())));
            if (recipeOptional.isEmpty()) return;
            recipe = recipeOptional.get();
        }

        if (inventory instanceof CraftInventoryCrafting crafting) {
            crafting.getInventory().setCurrentRecipe(recipe);
        } else if (inventory instanceof CraftInventorySmithing smithing) {
            smithing.getResultInventory().setRecipeUsed(recipe);
        } else if (inventory instanceof CraftInventoryFurnace inventoryFurnace) {
            if (inventoryFurnace.getHolder() instanceof CraftFurnace<?> craftFurnace) {
                try {
                    AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) FURNACE_BLOCK_ENTITY_FIELD.get(craftFurnace);
                    if (blockEntity == null) return;
                    RecipeManager.CachedCheck<?, ?> check = (RecipeManager.CachedCheck<?, ?>) CACHED_CHECK_FURNACE_FIELD.get(blockEntity);
                    if (check == null) return;
                    CACHED_CHECK_LAST_RECIPE_FIELD.setAccessible(true);
                    CACHED_CHECK_LAST_RECIPE_FIELD.set(check, recipe == null ? null : recipe.getId());
                } catch (IllegalAccessException e) {
                    return;
                }
            }
        }
    }
}
