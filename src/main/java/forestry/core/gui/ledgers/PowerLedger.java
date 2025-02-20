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
package forestry.core.gui.ledgers;

import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.PoseStack;

import forestry.core.config.Config;
import forestry.core.render.TextureManagerForestry;
import forestry.energy.ForestryEnergyStorage;

public class PowerLedger extends Ledger {
	private final ForestryEnergyStorage energyStorage;

	public PowerLedger(LedgerManager manager, ForestryEnergyStorage energyStorage) {
		super(manager, "power");
		this.energyStorage = energyStorage;
		maxHeight = 94;
	}

	@Override
	public void draw(PoseStack transform, int y, int x) {
		// Draw background
		drawBackground(transform, y, x);

		// Draw icon
		drawSprite(transform, TextureManagerForestry.INSTANCE.getDefault("misc/energy"), x + 3, y + 4);

		if (!isFullyOpened()) {
			return;
		}

		int xHeader = x + 22;
		int xBody = x + 12;

		drawHeader(transform, Component.translatable("for.gui.energy"), xHeader, y + 8);

		drawSubheader(transform, Component.translatable("for.gui.stored").append(":"), xBody, y + 20);
		drawText(transform, Config.energyDisplayMode.formatEnergyValue(energyStorage.getEnergyStored()), xBody, y + 32);

		drawSubheader(transform, Component.translatable("for.gui.maxenergy").append(":"), xBody, y + 44);
		drawText(transform, Config.energyDisplayMode.formatEnergyValue(energyStorage.getMaxEnergyStored()), xBody, y + 56);

		drawSubheader(transform, Component.translatable("for.gui.maxenergyreceive").append(":"), xBody, y + 68);
		drawText(transform, Config.energyDisplayMode.formatEnergyValue(energyStorage.getMaxEnergyReceived()), xBody, y + 80);
	}

	@Override
	public Component getTooltip() {
		return Component.literal(Config.energyDisplayMode.formatEnergyValue(energyStorage.getEnergyStored()));
	}

}
