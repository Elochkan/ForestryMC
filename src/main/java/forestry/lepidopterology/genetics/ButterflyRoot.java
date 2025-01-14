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
package forestry.lepidopterology.genetics;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IBreedingTrackerHandler;
import forestry.api.genetics.gatgets.IDatabasePlugin;
import forestry.api.lepidopterology.IButterflyNursery;
import forestry.api.lepidopterology.ILepidopteristTracker;
import forestry.api.lepidopterology.genetics.ButterflyChromosomes;
import forestry.api.lepidopterology.genetics.EnumFlutterType;
import forestry.api.lepidopterology.genetics.IAlleleButterflySpecies;
import forestry.api.lepidopterology.genetics.IButterfly;
import forestry.api.lepidopterology.genetics.IButterflyRoot;
import forestry.core.genetics.root.BreedingTrackerManager;
import forestry.core.tiles.TileUtil;
import forestry.core.utils.BlockUtil;
import forestry.core.utils.EntityUtil;
import forestry.core.utils.GeneticsUtil;
import forestry.lepidopterology.blocks.BlockCocoon;
import forestry.lepidopterology.entities.EntityButterfly;
import forestry.lepidopterology.features.LepidopterologyBlocks;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.tiles.TileCocoon;

import genetics.api.individual.IGenome;
import genetics.api.individual.IGenomeWrapper;
import genetics.api.individual.IIndividual;
import genetics.api.root.IRootContext;
import genetics.api.root.IndividualRoot;
import genetics.utils.AlleleUtils;

public class ButterflyRoot extends IndividualRoot<IButterfly> implements IButterflyRoot, IBreedingTrackerHandler {

	private int butterflySpeciesCount = -1;
	public static final String UID = "rootButterflies";
	private static final List<IButterfly> butterflyTemplates = new ArrayList<>();

	public ButterflyRoot(IRootContext<IButterfly> context) {
		super(context);
		BreedingTrackerManager.INSTANCE.registerTracker(UID, this);
	}

	@Override
	public IButterfly create(CompoundTag compound) {
		return new Butterfly(compound);
	}

	@Override
	public IButterfly create(IGenome genome) {
		return new Butterfly(genome);
	}

	@Override
	public IButterfly create(IGenome genome, IGenome mate) {
		return new Butterfly(genome, mate);
	}

	@Override
	public IGenomeWrapper createWrapper(IGenome genome) {
		return () -> genome;
	}

	@Override
	public Class<? extends IButterfly> getMemberClass() {
		return IButterfly.class;
	}

	@Override
	public int getSpeciesCount() {
		if (butterflySpeciesCount < 0) {
			butterflySpeciesCount = (int) AlleleUtils.filteredStream(ButterflyChromosomes.SPECIES)
				.filter(IAlleleButterflySpecies::isCounted).count();
		}

		return butterflySpeciesCount;
	}

	@Override
	public EnumFlutterType getIconType() {
		return EnumFlutterType.BUTTERFLY;
	}

	@Override
	public boolean isMember(IIndividual individual) {
		return individual instanceof IButterfly;
	}

	@Override
	public EntityButterfly spawnButterflyInWorld(Level world, IButterfly butterfly, double x, double y, double z) {
		return EntityUtil.spawnEntity(world, EntityButterfly.create(LepidopterologyEntities.BUTTERFLY.entityType(), world, butterfly, new BlockPos(x, y, z)), x, y, z);
	}

	@Override
	public BlockPos plantCocoon(LevelAccessor world, BlockPos coordinates, @Nullable IButterfly caterpillar, GameProfile owner, int age, boolean createNursery) {
		if (caterpillar == null) {
			return BlockPos.ZERO;
		}

		BlockPos pos = getValidCocoonPos(world, coordinates, caterpillar, owner, createNursery);
		if (pos == BlockPos.ZERO) {
			return pos;
		}
		BlockState state = LepidopterologyBlocks.COCOON.defaultState().setValue(BlockCocoon.AGE, age);
		boolean placed = world.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		if (!placed) {
			return BlockPos.ZERO;
		}

		Block block = world.getBlockState(pos).getBlock();
		if (!LepidopterologyBlocks.COCOON.blockEqual(block)) {
			return BlockPos.ZERO;
		}

		TileCocoon cocoon = TileUtil.getTile(world, pos, TileCocoon.class);
		if (cocoon == null) {
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), 18);
			return BlockPos.ZERO;
		}

		cocoon.setCaterpillar(caterpillar);
		cocoon.getOwnerHandler().setOwner(owner);

		return pos;
	}

	private BlockPos getValidCocoonPos(LevelAccessor world, BlockPos pos, IButterfly caterpillar, GameProfile gameProfile, boolean createNursery) {
		if (isPositionValid(world, pos.below(), caterpillar, gameProfile, createNursery)) {
			return pos.below();
		}
		for (int tries = 0; tries < 3; tries++) {
			for (int y = 1; y < world.getRandom().nextInt(5); y++) {
				BlockPos coordinate = pos.offset(world.getRandom().nextInt(6) - 3, -y, world.getRandom().nextInt(6) - 3);
				if (isPositionValid(world, coordinate, caterpillar, gameProfile, createNursery)) {
					return coordinate;
				}
			}
		}

		return BlockPos.ZERO;
	}

	public boolean isPositionValid(LevelAccessor world, BlockPos pos, IButterfly caterpillar, GameProfile gameProfile, boolean createNursery) {
		BlockState blockState = world.getBlockState(pos);
		if (BlockUtil.canReplace(blockState, world, pos)) {
			BlockPos nurseryPos = pos.above();
			IButterflyNursery nursery = GeneticsUtil.getNursery(world, nurseryPos);
			if (isNurseryValid(nursery, caterpillar, gameProfile)) {
				return true;
			} else if (createNursery && GeneticsUtil.canCreateNursery(world, nurseryPos)) {
				nursery = GeneticsUtil.getOrCreateNursery(gameProfile, world, nurseryPos, false);
				return isNurseryValid(nursery, caterpillar, gameProfile);
			}
		}
		return false;
	}

	private boolean isNurseryValid(@Nullable IButterflyNursery nursery, IButterfly caterpillar, GameProfile gameProfile) {
		return nursery != null && nursery.canNurse(caterpillar);
	}

	@Override
	public boolean isMated(ItemStack stack) {
		IButterfly butterfly = getTypes().createIndividual(stack);
		return butterfly != null && butterfly.getMate() == null;
	}

	/* BREEDING TRACKER */
	@Override
	public ILepidopteristTracker getBreedingTracker(LevelAccessor world, @Nullable GameProfile player) {
		return BreedingTrackerManager.INSTANCE.getTracker(getUID(), world, player);
	}

	@Override
	public String getFileName(@Nullable GameProfile profile) {
		return "LepidopteristTracker." + (profile == null ? "common" : profile.getId());
	}

	@Override
	public IBreedingTracker createTracker() {
		return new LepidopteristTracker();
	}

	@Override
	public IBreedingTracker createTracker(CompoundTag tag) {
		return new LepidopteristTracker(tag);
	}

	@Override
	public void populateTracker(IBreedingTracker tracker, @Nullable Level world, @Nullable GameProfile profile) {
		if (!(tracker instanceof LepidopteristTracker arboristTracker)) {
			return;
		}
		arboristTracker.setLevel(world);
		arboristTracker.setUsername(profile);
	}

	@Override
	public IAlyzerPlugin getAlyzerPlugin() {
		return ButterflyAlyzerPlugin.INSTANCE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IDatabasePlugin getSpeciesPlugin() {
		return ButterflyPlugin.INSTANCE;
	}
}
