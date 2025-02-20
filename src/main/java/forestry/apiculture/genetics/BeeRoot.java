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
package forestry.apiculture.genetics;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import com.mojang.authlib.GameProfile;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import forestry.Forestry;
import forestry.api.apiculture.IApiaristTracker;
import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.apiculture.IBeekeepingMode;
import forestry.api.apiculture.genetics.BeeChromosomes;
import forestry.api.apiculture.genetics.EnumBeeType;
import forestry.api.apiculture.genetics.IAlleleBeeSpecies;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeRoot;
import forestry.api.genetics.IAlyzerPlugin;
import forestry.api.genetics.IBreedingTracker;
import forestry.api.genetics.IBreedingTrackerHandler;
import forestry.api.genetics.gatgets.IDatabasePlugin;
import forestry.apiculture.BeeHousingListener;
import forestry.apiculture.BeeHousingModifier;
import forestry.apiculture.BeekeepingLogic;
import forestry.core.genetics.root.BreedingTrackerManager;

import genetics.api.individual.IGenome;
import genetics.api.individual.IGenomeWrapper;
import genetics.api.individual.IIndividual;
import genetics.api.organism.IOrganismType;
import genetics.api.root.IRootContext;
import genetics.api.root.IndividualRoot;
import genetics.utils.AlleleUtils;

public class BeeRoot extends IndividualRoot<IBee> implements IBeeRoot, IBreedingTrackerHandler {

	private int beeSpeciesCount = -1;
	public static final String UID = "rootBees";

	private final List<IBeekeepingMode> beekeepingModes = new ArrayList<>();

	@Nullable
	private static IBeekeepingMode activeBeekeepingMode;

	public BeeRoot(IRootContext<IBee> context) {
		super(context);
		BreedingTrackerManager.INSTANCE.registerTracker(UID, this);
	}

	@Override
	public Class<? extends IBee> getMemberClass() {
		return IBee.class;
	}

	@Override
	public int getSpeciesCount() {
		if (beeSpeciesCount < 0) {
			beeSpeciesCount = (int) AlleleUtils.filteredStream(BeeChromosomes.SPECIES)
					.filter(IAlleleBeeSpecies::isCounted).count();
		}

		return beeSpeciesCount;
	}


	@Override
	public EnumBeeType getIconType() {
		return EnumBeeType.DRONE;
	}

	@Override
	public IOrganismType getTypeForMutation(int position) {
		return switch (position) {
			case 0 -> EnumBeeType.PRINCESS;
			case 1 -> EnumBeeType.DRONE;
			case 2 -> EnumBeeType.QUEEN;
			default -> getIconType();
		};
	}

	@Override
	public boolean isDrone(ItemStack stack) {
		return getType(stack) == EnumBeeType.DRONE;
	}

	@Override
	public boolean isMated(ItemStack stack) {
		if (getType(stack) != EnumBeeType.QUEEN) {
			return false;
		}

		CompoundTag nbt = stack.getTag();
		return nbt != null && nbt.contains("Mate");
	}

	@Override
	public IBee create(IGenome genome) {
		return new Bee(genome);
	}

	@Override
	public IBee create(IGenome genome, IGenome mate) {
		return new Bee(genome, mate);
	}

	@Override
	public IGenomeWrapper createWrapper(IGenome genome) {
		return () -> genome;
	}

	@Override
	public IBee create(CompoundTag compound) {
		return new Bee(compound);
	}

	@Override
	public IBee getBee(Level world, IGenome genome, IBee mate) {
		return new Bee(genome, mate);
	}

	/* BREEDING MODES */
	@Override
	public void resetBeekeepingMode() {
		activeBeekeepingMode = null;
	}

	@Override
	public List<IBeekeepingMode> getBeekeepingModes() {
		return this.beekeepingModes;
	}

	@Override
	public IBeekeepingMode getBeekeepingMode(Level world) {
		if (activeBeekeepingMode != null) {
			return activeBeekeepingMode;
		}

		// No beekeeping mode yet, get it.
		IApiaristTracker tracker = getBreedingTracker(world, null);
		String modeName = tracker.getModeName();
		IBeekeepingMode mode = getBeekeepingMode(modeName);
		Preconditions.checkNotNull(mode);

		setBeekeepingMode(world, mode);
		Forestry.LOGGER.debug("Set beekeeping mode for a world to {}", mode);

		return activeBeekeepingMode;
	}

	@Override
	public void registerBeekeepingMode(IBeekeepingMode mode) {
		beekeepingModes.add(mode);
	}

	@Override
	public void setBeekeepingMode(Level world, IBeekeepingMode mode) {
		Preconditions.checkNotNull(world);
		Preconditions.checkNotNull(mode);
		activeBeekeepingMode = mode;
		getBreedingTracker(world, null).setModeName(mode.getName());
	}

	@Override
	public IBeekeepingMode getBeekeepingMode(String name) {
		for (IBeekeepingMode mode : beekeepingModes) {
			if (mode.getName().equals(name) || mode.getName().equals(name.toLowerCase(Locale.ENGLISH))) {
				return mode;
			}
		}

		Forestry.LOGGER.debug("Failed to find a beekeeping mode called '{}', reverting to fallback.", name);
		return beekeepingModes.get(0);
	}

	@Override
	public IApiaristTracker getBreedingTracker(LevelAccessor world, @Nullable GameProfile player) {
		return BreedingTrackerManager.INSTANCE.getTracker(getUID(), world, player);
	}

	@Override
	public String getFileName(@Nullable GameProfile profile) {
		return "ApiaristTracker." + (profile == null ? "common" : profile.getId());
	}

	@Override
	public IBreedingTracker createTracker() {
		return new ApiaristTracker();
	}

	@Override
	public IBreedingTracker createTracker(CompoundTag tag) {
		return new ApiaristTracker(tag);
	}

	@Override
	public void populateTracker(IBreedingTracker tracker, @Nullable Level world, @Nullable GameProfile profile) {
		if (!(tracker instanceof ApiaristTracker apiaristTracker)) {
			return;
		}
		apiaristTracker.setLevel(world);
		apiaristTracker.setUsername(profile);
	}

	@Override
	public boolean isMember(IIndividual individual) {
		return individual instanceof IBee;
	}

	@Override
	public IBeekeepingLogic createBeekeepingLogic(IBeeHousing housing) {
		return new BeekeepingLogic(housing);
	}

	@Override
	public IBeeModifier createBeeHousingModifier(IBeeHousing housing) {
		return new BeeHousingModifier(housing);
	}

	@Override
	public IBeeListener createBeeHousingListener(IBeeHousing housing) {
		return new BeeHousingListener(housing);
	}

	@Override
	public IAlyzerPlugin getAlyzerPlugin() {
		return BeeAlyzerPlugin.INSTANCE;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public IDatabasePlugin getSpeciesPlugin() {
		return BeePlugin.INSTANCE;
	}
}
