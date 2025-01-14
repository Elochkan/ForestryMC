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
package forestry.climatology.blocks;

import java.util.function.Supplier;

import forestry.climatology.features.ClimatologyTiles;
import forestry.climatology.tiles.TileHabitatFormer;
import forestry.core.blocks.IBlockTypeCustom;
import forestry.core.blocks.IMachineProperties;
import forestry.core.blocks.MachineProperties;
import forestry.core.tiles.TileForestry;
import forestry.modules.features.FeatureTileType;

public enum BlockTypeClimatology implements IBlockTypeCustom {
	HABITAT_FORMER(() -> ClimatologyTiles.HABITAT_FORMER, "habitat_former");

	private final IMachineProperties machineProperties;

	<T extends TileHabitatFormer> BlockTypeClimatology(Supplier<FeatureTileType<? extends T>> feature, String name) {
		this.machineProperties = new MachineProperties.Builder<>(feature, name).setServerTicker(TileHabitatFormer::serverTick).create();
	}

	@Override
	public IMachineProperties<?> getMachineProperties() {
		return machineProperties;
	}

	@Override
	public String getSerializedName() {
		return getMachineProperties().getSerializedName();
	}
}
