/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.arboriculture.genetics.alleles;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.PlantType;

import forestry.api.arboriculture.IGermlingModelProvider;
import forestry.api.arboriculture.IGrowthProvider;
import forestry.api.arboriculture.ILeafProvider;
import forestry.api.arboriculture.ILeafSpriteProvider;
import forestry.api.arboriculture.ITreeGenerator;
import forestry.api.arboriculture.TreeManager;
import forestry.api.arboriculture.genetics.EnumGermlingType;
import forestry.api.arboriculture.genetics.IAlleleTreeSpecies;
import forestry.api.arboriculture.genetics.IAlleleTreeSpeciesBuilder;
import forestry.api.arboriculture.genetics.ITreeRoot;
import forestry.api.genetics.IFruitFamily;
import forestry.arboriculture.genetics.ClimateGrowthProvider;
import forestry.core.genetics.alleles.AlleleForestrySpecies;

import forestry.core.genetics.alleles.AlleleForestrySpecies.AbstractBuilder;

public class AlleleTreeSpecies extends AlleleForestrySpecies implements IAlleleTreeSpecies {
	private final ITreeGenerator generator;
	private final IGermlingModelProvider germlingModelProvider;
	private final ILeafSpriteProvider leafSpriteProvider;
	private final ImmutableList<IFruitFamily> fruits;
	private final PlantType nativeType;
	@Nullable
	private final ILeafProvider leafProvider;
	private final IGrowthProvider growthProvider;
	private final float rarity;

	public AlleleTreeSpecies(Builder builder) {
		super(builder);

		this.generator = builder.generator;
		this.germlingModelProvider = builder.germlingModelProvider;
		this.leafSpriteProvider = builder.leafSpriteProvider;
		this.nativeType = builder.nativeType;
		this.fruits = builder.fruits.build();
		this.rarity = builder.rarity;
		this.growthProvider = builder.growthProvider;
		this.leafProvider = builder.leafProvider != null ? builder.leafProvider.apply(this) : null;
	}

	@Override
	public ITreeRoot getRoot() {
		return TreeManager.treeRoot;
	}

	@Override
	public float getRarity() {
		return rarity;
	}

	@Override
	public IGrowthProvider getGrowthProvider() {
		return growthProvider;
	}

	/* OTHER */
	@Override
	public PlantType getPlantType() {
		return nativeType;
	}

	@Override
	public List<IFruitFamily> getSuitableFruit() {
		return fruits;
	}

	@Override
	public ITreeGenerator getGenerator() {
		return generator;
	}

	@Override
	public ILeafSpriteProvider getLeafSpriteProvider() {
		return leafSpriteProvider;
	}

	@Override
	public int getSpriteColour(int renderPass) {
		return leafSpriteProvider.getColor(false);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ModelResourceLocation getItemModel() {
		return new ModelResourceLocation(germlingModelProvider.getItemModel(), "inventory");
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ResourceLocation getBlockModel() {
		return germlingModelProvider.getBlockModel();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public int getGermlingColour(EnumGermlingType type, int renderPass) {
		return germlingModelProvider.getSpriteColor(type, renderPass);
	}

	@Nullable
	@Override
	public ILeafProvider getLeafProvider() {
		return leafProvider;
	}

	@Override
	public int compareTo(IAlleleTreeSpecies o) {
		return 0;
	}

	public static class Builder extends AbstractBuilder<IAlleleTreeSpeciesBuilder> implements IAlleleTreeSpeciesBuilder {

		private final ImmutableList.Builder<IFruitFamily> fruits = new ImmutableList.Builder<>();

		private ITreeGenerator generator;
		private IGermlingModelProvider germlingModelProvider;
		@Nullable
		private Function<IAlleleTreeSpecies, ILeafProvider> leafProvider;
		private ILeafSpriteProvider leafSpriteProvider;
		private PlantType nativeType = PlantType.PLAINS;
		private IGrowthProvider growthProvider = new ClimateGrowthProvider();
		private float rarity = 0.0F;

		public Builder(String modId, String uid, String speciesIdentifier) {
			super(modId, uid, speciesIdentifier);
		}

		protected static void checkBuilder(Builder builder) {
			AbstractBuilder.checkBuilder(builder);
			Preconditions.checkNotNull(builder.generator);
			Preconditions.checkNotNull(builder.germlingModelProvider);
			Preconditions.checkNotNull(builder.leafSpriteProvider);
		}

		@Override
		public IAlleleTreeSpeciesBuilder cast() {
			return this;
		}

		@Override
		public IAlleleTreeSpecies build() {
			checkBuilder(this);
			return new AlleleTreeSpecies(this);
		}

		@Override
		public IAlleleTreeSpeciesBuilder setGenerator(ITreeGenerator generator) {
			this.generator = generator;
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder setModel(IGermlingModelProvider germlingModelProvider) {
			this.germlingModelProvider = germlingModelProvider;
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder setLeaf(Function<IAlleleTreeSpecies, ILeafProvider> leafProvider) {
			this.leafProvider = leafProvider;
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder setLeafSprite(ILeafSpriteProvider leafSpriteProvider) {
			this.leafSpriteProvider = leafSpriteProvider;
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder addFruitFamily(IFruitFamily family) {
			this.fruits.add(family);
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder setPlantType(PlantType type) {
			this.nativeType = type;
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder setRarity(float rarity) {
			this.rarity = rarity;
			return this;
		}

		@Override
		public IAlleleTreeSpeciesBuilder setGrowthProvider(IGrowthProvider growthProvider) {
			this.growthProvider = growthProvider;
			return this;
		}
	}
}
