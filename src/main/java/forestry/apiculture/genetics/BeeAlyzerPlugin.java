package forestry.apiculture.genetics;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.genetics.BeeChromosomes;
import forestry.api.apiculture.genetics.EnumBeeType;
import forestry.api.apiculture.genetics.IAlleleBeeSpecies;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.genetics.EnumTolerance;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.alleles.AlleleManager;
import forestry.apiculture.features.ApicultureItems;
import forestry.core.config.Config;
import forestry.core.gui.GuiAlyzer;
import forestry.core.gui.TextLayoutHelper;
import forestry.core.gui.widgets.ItemStackWidget;
import forestry.core.gui.widgets.WidgetManager;

import genetics.api.GeneticHelper;
import genetics.api.alleles.IAlleleValue;
import genetics.api.individual.IGenome;
import genetics.api.organism.IOrganism;
import genetics.api.organism.IOrganismType;

public enum BeeAlyzerPlugin implements IAlyzerPlugin {
	INSTANCE;

	private final Map<ResourceLocation, ItemStack> iconStacks = new HashMap<>();

	BeeAlyzerPlugin() {
		NonNullList<ItemStack> beeList = NonNullList.create();
		ApicultureItems.BEE_DRONE.item().addCreativeItems(beeList, false);
		for (ItemStack beeStack : beeList) {
			IOrganism<?> organism = GeneticHelper.getOrganism(beeStack);
			if (organism.isEmpty()) {
				continue;
			}
			IAlleleBeeSpecies species = organism.getAllele(BeeChromosomes.SPECIES, true);
			iconStacks.put(species.getRegistryName(), beeStack);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawAnalyticsPage1(PoseStack transform, Screen gui, ItemStack itemStack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IBee bee = BeeManager.beeRoot.create(itemStack);
			if (bee == null) {
				return;
			}

			IOrganismType type = BeeManager.beeRoot.getTypes().getType(itemStack);
			if (type == null) {
				return;
			}

			TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

			textLayout.startPage(transform, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

			textLayout.drawLine(transform, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
			textLayout.drawLine(transform, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

			textLayout.newLine();
			textLayout.newLine();

			guiAlyzer.drawSpeciesRow(transform, Component.translatable("for.gui.species"), bee, BeeChromosomes.SPECIES, type);
			textLayout.newLine();

			guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.lifespan"), bee, BeeChromosomes.LIFESPAN);
			textLayout.newLine();
			guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.speed"), bee, BeeChromosomes.SPEED);
			textLayout.newLine();
			guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.pollination"), bee, BeeChromosomes.FLOWERING);
			textLayout.newLine();
			guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.flowers"), bee, BeeChromosomes.FLOWER_PROVIDER);
			textLayout.newLine();

			textLayout.drawLine(transform, Component.translatable("for.gui.fertility"), GuiAlyzer.COLUMN_0);
			IAlleleValue<Integer> primaryFertility = bee.getGenome().getActiveAllele(BeeChromosomes.FERTILITY);
			IAlleleValue<Integer> secondaryFertility = bee.getGenome().getInactiveAllele(BeeChromosomes.FERTILITY);
			guiAlyzer.drawFertilityInfo(transform, primaryFertility.getValue(), GuiAlyzer.COLUMN_1, guiAlyzer.getColorCoding(primaryFertility.isDominant()), 0);
			guiAlyzer.drawFertilityInfo(transform, secondaryFertility.getValue(), GuiAlyzer.COLUMN_2, guiAlyzer.getColorCoding(secondaryFertility.isDominant()), 0);
			textLayout.newLine();

			guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.area"), bee, BeeChromosomes.TERRITORY);
			textLayout.newLine();

			guiAlyzer.drawChromosomeRow(transform, Component.translatable("for.gui.effect"), bee, BeeChromosomes.EFFECT);
			textLayout.newLine();

			textLayout.endPage(transform);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawAnalyticsPage2(PoseStack transform, Screen gui, ItemStack itemStack) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IBee bee = BeeManager.beeRoot.create(itemStack);
			if (bee == null) {
				return;
			}

			IOrganismType type = BeeManager.beeRoot.getTypes().getType(itemStack);
			if (type == null) {
				return;
			}

			IGenome genome = bee.getGenome();
			IAlleleBeeSpecies primaryAllele = genome.getActiveAllele(BeeChromosomes.SPECIES);
			IAlleleBeeSpecies secondaryAllele = genome.getActiveAllele(BeeChromosomes.SPECIES);

			TextLayoutHelper textLayout = guiAlyzer.getTextLayout();

			textLayout.startPage(transform, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

			textLayout.drawLine(transform, Component.translatable("for.gui.active"), GuiAlyzer.COLUMN_1);
			textLayout.drawLine(transform, Component.translatable("for.gui.inactive"), GuiAlyzer.COLUMN_2);

			textLayout.newLine();

			guiAlyzer.drawRow(transform, Component.translatable("for.gui.climate"), AlleleManager.climateHelper.toDisplay(primaryAllele.getTemperature()),
					AlleleManager.climateHelper.toDisplay(secondaryAllele.getTemperature()), bee, BeeChromosomes.SPECIES);

			textLayout.newLine();

			IAlleleValue<EnumTolerance> tempToleranceActive = bee.getGenome().getActiveAllele(BeeChromosomes.TEMPERATURE_TOLERANCE);
			IAlleleValue<EnumTolerance> tempToleranceInactive = bee.getGenome().getInactiveAllele(BeeChromosomes.TEMPERATURE_TOLERANCE);
			textLayout.drawLine(transform, Component.literal("  ").append(Component.translatable("for.gui.tolerance")), GuiAlyzer.COLUMN_0);
			guiAlyzer.drawToleranceInfo(transform, tempToleranceActive, GuiAlyzer.COLUMN_1);
			guiAlyzer.drawToleranceInfo(transform, tempToleranceInactive, GuiAlyzer.COLUMN_2);

			textLayout.newLine(16);

			guiAlyzer.drawRow(transform, Component.translatable("for.gui.humidity"), AlleleManager.climateHelper.toDisplay(primaryAllele.getHumidity()),
					AlleleManager.climateHelper.toDisplay(secondaryAllele.getHumidity()), bee, BeeChromosomes.SPECIES);

			textLayout.newLine();

			IAlleleValue<EnumTolerance> humidToleranceActive = bee.getGenome().getActiveAllele(BeeChromosomes.HUMIDITY_TOLERANCE);
			IAlleleValue<EnumTolerance> humidToleranceInactive = bee.getGenome().getInactiveAllele(BeeChromosomes.HUMIDITY_TOLERANCE);
			textLayout.drawLine(transform, Component.literal("  ").append(Component.translatable("for.gui.tolerance")), GuiAlyzer.COLUMN_0);
			guiAlyzer.drawToleranceInfo(transform, humidToleranceActive, GuiAlyzer.COLUMN_1);
			guiAlyzer.drawToleranceInfo(transform, humidToleranceInactive, GuiAlyzer.COLUMN_2);

			textLayout.newLine(16);

			Component yes = Component.translatable("for.yes");
			Component no = Component.translatable("for.no");

			Component diurnal0, diurnal1, nocturnal0, nocturnal1;
			if (genome.getActiveValue(BeeChromosomes.NEVER_SLEEPS)) {
				nocturnal0 = diurnal0 = yes;
			} else {
				nocturnal0 = primaryAllele.isNocturnal() ? yes : no;
				diurnal0 = !primaryAllele.isNocturnal() ? yes : no;
			}
			if (genome.getInactiveValue(BeeChromosomes.NEVER_SLEEPS)) {
				nocturnal1 = diurnal1 = yes;
			} else {
				nocturnal1 = secondaryAllele.isNocturnal() ? yes : no;
				diurnal1 = !secondaryAllele.isNocturnal() ? yes : no;
			}

			textLayout.drawLine(transform, Component.translatable("for.gui.diurnal"), GuiAlyzer.COLUMN_0);
			textLayout.drawLine(transform, diurnal0, GuiAlyzer.COLUMN_1, guiAlyzer.getColorCoding(false));
			textLayout.drawLine(transform, diurnal1, GuiAlyzer.COLUMN_2, guiAlyzer.getColorCoding(false));
			textLayout.newLineCompressed();

			textLayout.drawLine(transform, Component.translatable("for.gui.nocturnal"), GuiAlyzer.COLUMN_0);
			textLayout.drawLine(transform, nocturnal0, GuiAlyzer.COLUMN_1, guiAlyzer.getColorCoding(false));
			textLayout.drawLine(transform, nocturnal1, GuiAlyzer.COLUMN_2, guiAlyzer.getColorCoding(false));
			textLayout.newLineCompressed();

			Component primary = genome.getActiveValue(BeeChromosomes.TOLERATES_RAIN) ? yes : no;
			Component secondary = genome.getInactiveValue(BeeChromosomes.TOLERATES_RAIN) ? yes : no;

			guiAlyzer.drawRow(transform, Component.translatable("for.gui.flyer"), primary, secondary, bee, BeeChromosomes.TOLERATES_RAIN);

			textLayout.newLineCompressed();

			primary = genome.getActiveValue(BeeChromosomes.CAVE_DWELLING) ? yes : no;
			secondary = genome.getInactiveValue(BeeChromosomes.CAVE_DWELLING) ? yes : no;

			guiAlyzer.drawRow(transform, Component.translatable("for.gui.cave"), primary, secondary, bee, BeeChromosomes.CAVE_DWELLING);

			textLayout.newLine();

			if (type == EnumBeeType.PRINCESS || type == EnumBeeType.QUEEN) {
				String displayTextKey = "for.bees.stock.pristine";
				if (!bee.isNatural()) {
					displayTextKey = "for.bees.stock.ignoble";
				}
				Component displayText = Component.translatable(displayTextKey);
				textLayout.drawCenteredLine(transform, displayText, 8, 208, guiAlyzer.getFontColor().get("gui.beealyzer.binomial"));
			}

			if (bee.getGeneration() >= 0) {
				textLayout.newLineCompressed();
				Component displayText = Component.translatable("for.gui.beealyzer.generations", bee.getGeneration());
				textLayout.drawCenteredLine(transform, displayText, 8, 208, guiAlyzer.getFontColor().get("gui.beealyzer.binomial"));
			}

			textLayout.endPage(transform);
		}
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void drawAnalyticsPage3(PoseStack transform, ItemStack itemStack, Screen gui) {
		if (gui instanceof GuiAlyzer guiAlyzer) {
			IBee bee = BeeManager.beeRoot.create(itemStack);
			if (bee == null) {
				return;
			}

			TextLayoutHelper textLayout = guiAlyzer.getTextLayout();
			WidgetManager widgetManager = guiAlyzer.getWidgetManager();

			textLayout.startPage(transform, GuiAlyzer.COLUMN_0, GuiAlyzer.COLUMN_1, GuiAlyzer.COLUMN_2);

			textLayout.drawLine(transform, Component.translatable("for.gui.beealyzer.produce").append(":"), GuiAlyzer.COLUMN_0);

			textLayout.newLine();

			int x = GuiAlyzer.COLUMN_0;
			for (ItemStack stack : bee.getProduceList()) {
				widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), stack));

				x += 18;
				if (x > 148) {
					x = GuiAlyzer.COLUMN_0;
					textLayout.newLine();
				}
			}

			textLayout.newLine();
			textLayout.newLine();
			textLayout.newLine();
			textLayout.newLine();

			textLayout.drawLine(transform, Component.translatable("for.gui.beealyzer.specialty").append(":"), GuiAlyzer.COLUMN_0);
			textLayout.newLine();

			x = GuiAlyzer.COLUMN_0;
			for (ItemStack stack : bee.getSpecialtyList()) {
				widgetManager.add(new ItemStackWidget(widgetManager, x, textLayout.getLineY(), stack));

				x += 18;
				if (x > 148) {
					x = GuiAlyzer.COLUMN_0;
					textLayout.newLine();
				}
			}

			textLayout.endPage(transform);
		}
	}

	@Override
	public Map<ResourceLocation, ItemStack> getIconStacks() {
		return iconStacks;
	}

	@Override
	public List<String> getHints() {
		return Config.hints.get("beealyzer");
	}

}
