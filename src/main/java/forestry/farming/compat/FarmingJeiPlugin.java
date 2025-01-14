package forestry.farming.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.core.ClientsideCode;
import forestry.core.circuits.EnumCircuitBoardType;
import forestry.core.config.Constants;
import forestry.core.features.CoreItems;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;

import java.util.List;

@JeiPlugin
@OnlyIn(Dist.CLIENT)
public class FarmingJeiPlugin implements IModPlugin {
	@Override
	public ResourceLocation getPluginUid() {
		return new ResourceLocation(Constants.MOD_ID, "farming");
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistration registration) {
		//        BlockRegistryFarming blocks = ModuleFarming.getBlocks();
		//        Item farmBlock = Item.getItemFromBlock(blocks.farm);
		//        registration.registerSubtypeInterpreter(farmBlock, itemStack -> {
		//            CompoundNBT nbt = itemStack.getTag();
		//            EnumFarmBlockTexture texture = EnumFarmBlockTexture.getFromCompound(nbt);
		//            return itemStack.getItemDamage() + "." + texture.getUid();
		//        });
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registration) {
		IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
		registration.addRecipeCategories(new FarmingInfoRecipeCategory(guiHelper));
	}

	@Override
	public void registerRecipes(IRecipeRegistration registration) {
		RecipeManager recipeManager = ClientsideCode.getRecipeManager();

		List<FarmingInfoRecipe> recipes = FarmingInfoRecipeMaker.getRecipes(recipeManager);
		registration.addRecipes(FarmingInfoRecipeCategory.TYPE, recipes);
	}

	@Override
	public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
		registration.addRecipeCatalyst(new ItemStack(CoreItems.CIRCUITBOARDS.get(EnumCircuitBoardType.INTRICATE)), FarmingInfoRecipeCategory.TYPE);
	}
}
