package forestry.apiculture;

import forestry.api.apiculture.genetics.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

import com.mojang.authlib.GameProfile;

import forestry.api.apiculture.BeeManager;
import forestry.api.genetics.ForestryComponentKeys;
import forestry.api.genetics.IResearchHandler;
import forestry.apiculture.features.ApicultureItems;
import forestry.apiculture.genetics.BeeBranchDefinition;
import forestry.apiculture.genetics.BeeDefinition;
import forestry.apiculture.genetics.BeeDisplayHandler;
import forestry.apiculture.genetics.BeeHelper;
import forestry.apiculture.genetics.BeeRoot;
import forestry.apiculture.genetics.BeekeepingMode;
import forestry.apiculture.genetics.alleles.AlleleEffects;
import forestry.apiculture.items.ItemHoneyComb;
import forestry.core.config.Constants;
import forestry.core.genetics.alleles.EnumAllele;
import forestry.core.genetics.root.IResearchPlugin;
import forestry.core.genetics.root.ResearchHandler;
import forestry.core.items.ItemOverlay;
import forestry.core.utils.ItemStackUtil;

import genetics.api.GeneticPlugin;
import genetics.api.GeneticsAPI;
import genetics.api.IGeneticApiInstance;
import genetics.api.IGeneticFactory;
import genetics.api.IGeneticPlugin;
import genetics.api.alleles.IAlleleRegistry;
import genetics.api.alleles.IAlleleSpecies;
import genetics.api.classification.IClassification;
import genetics.api.classification.IClassification.EnumClassLevel;
import genetics.api.classification.IClassificationRegistry;
import genetics.api.individual.IIndividual;
import genetics.api.organism.IOrganismTypes;
import genetics.api.root.IGeneticListenerRegistry;
import genetics.api.root.IIndividualRootBuilder;
import genetics.api.root.IRootDefinition;
import genetics.api.root.IRootManager;
import genetics.api.root.components.ComponentKeys;

@GeneticPlugin(modId = Constants.MOD_ID)
public class BeePlugin implements IGeneticPlugin {
	@Override
	public void registerClassifications(IClassificationRegistry registry) {
		IClassification hymnoptera = registry.createAndRegisterClassification(EnumClassLevel.ORDER, "hymnoptera", "Hymnoptera");
		registry.getClassification("class.insecta").addMemberGroup(hymnoptera);

		IClassification apidae = registry.createAndRegisterClassification(EnumClassLevel.FAMILY, "apidae", "Apidae");
		hymnoptera.addMemberGroup(apidae);

		for (BeeBranchDefinition beeBranch : BeeBranchDefinition.values()) {
			apidae.addMemberGroup(beeBranch.getBranch());
		}
	}

	@Override
	public void registerListeners(IGeneticListenerRegistry registry) {
		registry.add(BeeRoot.UID, BeeDefinition.values());
	}

	@Override
	public void registerAlleles(IAlleleRegistry registry) {
		registry.registerAlleles(EnumAllele.Fertility.values(), BeeChromosomes.FERTILITY);
		registry.registerAlleles(EnumAllele.Flowering.values(), BeeChromosomes.FLOWERING);
		registry.registerAlleles(EnumAllele.Territory.values(), BeeChromosomes.TERRITORY);
		AlleleEffects.registerAlleles(registry);
	}

	@Override
	public void createRoot(IRootManager rootManager, IGeneticFactory geneticFactory) {
		IIndividualRootBuilder<IBee> rootBuilder = rootManager.createRoot(BeeRoot.UID);
		rootBuilder
			.setRootFactory(BeeRoot::new)
			.setSpeciesType(BeeChromosomes.SPECIES)
			.addListener(ComponentKeys.TYPES, (IOrganismTypes<IBee> builder) -> {
				builder.registerType(EnumBeeType.DRONE, ApicultureItems.BEE_DRONE::stack);
				builder.registerType(EnumBeeType.PRINCESS, ApicultureItems.BEE_PRINCESS::stack);
				builder.registerType(EnumBeeType.QUEEN, ApicultureItems.BEE_QUEEN::stack);
				builder.registerType(EnumBeeType.LARVAE, ApicultureItems.BEE_LARVAE::stack);
			})
			.addComponent(ComponentKeys.TRANSLATORS)
			.addComponent(ComponentKeys.MUTATIONS)
			.addComponent(ForestryComponentKeys.RESEARCH, ResearchHandler::new)
			.addListener(ForestryComponentKeys.RESEARCH, (IResearchHandler<IBee> builder) -> builder.addPlugin(new IResearchPlugin() {
				@Override
				public float getResearchSuitability(IAlleleSpecies species, ItemStack itemStack) {
					Item item = itemStack.getItem();
					if (item instanceof ItemOverlay && ApicultureItems.HONEY_DROPS.itemEqual(item)) {
						return 0.5f;
					} else if (ApicultureItems.HONEYDEW.itemEqual(item)) {
						return 0.7f;
						//TODO tag lookup?
					} else if (item instanceof ItemHoneyComb) {
						return 0.4f;
					}

					IAlleleBeeSpecies beeSpecies = (IAlleleBeeSpecies) species;
					for (ItemStack stack : beeSpecies.getProducts().getPossibleStacks()) {
						if (stack.sameItem(itemStack)) {
							return 1.0f;
						}
					}
					for (ItemStack stack : beeSpecies.getSpecialties().getPossibleStacks()) {
						if (stack.sameItem(itemStack)) {
							return 1.0f;
						}
					}

					return 0.0F;
				}

				@Override
				public NonNullList<ItemStack> getResearchBounty(IAlleleSpecies species, Level world, GameProfile researcher, IIndividual individual, int bountyLevel) {
					IAlleleBeeSpecies beeSpecies = (IAlleleBeeSpecies) species;
					NonNullList<ItemStack> bounty = NonNullList.create();
					if (bountyLevel > 10) {
						for (ItemStack stack : beeSpecies.getSpecialties().getPossibleStacks()) {
							bounty.add(ItemStackUtil.copyWithRandomSize(stack, (int) ((float) bountyLevel / 2), world.random));
						}
					}
					for (ItemStack stack : beeSpecies.getProducts().getPossibleStacks()) {
						bounty.add(ItemStackUtil.copyWithRandomSize(stack, (int) ((float) bountyLevel / 2), world.random));
					}
					return bounty;
				}
			}))
			.setDefaultTemplate(BeeHelper::createDefaultTemplate);
	}

	@Override
	public void onFinishRegistration(IRootManager manager, IGeneticApiInstance instance) {
		BeeManager.beeRoot = instance.<IBeeRoot>getRoot(BeeRoot.UID).get();

		// Modes
		BeeManager.beeRoot.registerBeekeepingMode(BeekeepingMode.easy);
		BeeManager.beeRoot.registerBeekeepingMode(BeekeepingMode.normal);
		BeeManager.beeRoot.registerBeekeepingMode(BeekeepingMode.hard);
		BeeManager.beeRoot.registerBeekeepingMode(BeekeepingMode.hardcore);
		BeeManager.beeRoot.registerBeekeepingMode(BeekeepingMode.insane);

		BeeDisplayHandler.init(DisplayHelper.getInstance());
	}
}
