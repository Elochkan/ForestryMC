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
package forestry.core.fluids;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.level.Level;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import forestry.api.core.INbtReadable;
import forestry.api.core.INbtWritable;
import forestry.core.network.IForestryPacketClient;
import forestry.core.network.IStreamable;
import forestry.core.network.packets.PacketTankLevelUpdate;
import forestry.core.tiles.ILiquidTankTile;
import forestry.core.tiles.IRenderableTile;
import forestry.core.utils.NetworkUtil;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public class TankManager implements ITankManager, ITankUpdateHandler, IStreamable, INbtWritable, INbtReadable {

	private final List<StandardTank> tanks = new ArrayList<>();

	// for container updates, keeps track of the fluids known to each client (container)
	private final Table<AbstractContainerMenu, Integer, FluidStack> prevFluidStacks = HashBasedTable.create();

	// tank tile updates, for blocks that show fluid levels on the outside
	@Nullable
	private final ILiquidTankTile tile;

	public TankManager() {
		this.tile = null;
	}

	public TankManager(ILiquidTankTile tile, StandardTank... tanks) {
		this.tile = tile;
		addAll(Arrays.asList(tanks));
	}

	public final boolean addAll(Collection<? extends StandardTank> collection) {
		boolean addedAll = true;
		for (StandardTank tank : collection) {
			addedAll &= add(tank);
		}
		return addedAll;
	}

	public boolean add(StandardTank tank) {
		//TODO added is always true, and things are always appended to the end of the list?
		boolean added = tanks.add(tank);
		int index = tanks.indexOf(tank);
		tank.setTankUpdateHandler(this);
		tank.setTankIndex(index);
		return added;
	}

	@Override
	public CompoundTag write(CompoundTag data) {
		ListTag tagList = new ListTag();
		for (byte slot = 0; slot < tanks.size(); slot++) {
			StandardTank tank = tanks.get(slot);
			if (!tank.getFluid().isEmpty()) {
				CompoundTag tag = new CompoundTag();
				tag.putByte("tank", slot);
				tank.writeToNBT(tag);
				tagList.add(tag);
			}
		}
		data.put("tanks", tagList);
		return data;
	}

	@Override
	public void read(CompoundTag data) {
		for (Tag tag : data.getList("tanks", Tag.TAG_COMPOUND)) {
			CompoundTag compound = (CompoundTag) tag;
			int slot = compound.getByte("tank");
			if (slot >= 0 && slot < tanks.size()) {
				StandardTank tank = tanks.get(slot);
				tank.readFromNBT(compound);
				updateTankLevels(tank);
			}
		}
	}

	@Override
	public void writeData(FriendlyByteBuf data) {
		for (StandardTank tank : tanks) {
			tank.writeData(data);
		}
	}

	@Override
	public void readData(FriendlyByteBuf data) {
		for (StandardTank tank : tanks) {
			tank.readData(data);
		}
	}

	@Override
	public void containerAdded(AbstractContainerMenu container, ContainerListener player) {
		if (!(player instanceof ServerPlayer)) {
			return;
		}

		List<ContainerListener> crafters = Collections.singletonList(player);

		for (StandardTank tank : tanks) {
			sendTankUpdate(container, crafters, tank);
		}
	}

	@Override
	public void containerRemoved(AbstractContainerMenu container) {
		for (StandardTank tank : tanks) {
			prevFluidStacks.remove(container, tank.getTankIndex());
		}
	}

	@Override
	public void sendTankUpdate(AbstractContainerMenu container, List<ContainerListener> crafters) {
		for (StandardTank tank : tanks) {
			sendTankUpdate(container, crafters, tank.getTankIndex());
		}
	}

	private void sendTankUpdate(AbstractContainerMenu container, List<ContainerListener> crafters, int tankIndex) {
		StandardTank tank = tanks.get(tankIndex);
		if (tank == null) {
			return;
		}

		FluidStack fluidStack = tank.getFluid();
		FluidStack prev = prevFluidStacks.get(container, tankIndex);
		if (prev == null) {
			prev = FluidStack.EMPTY;
		}
		if (FluidHelper.areFluidStacksEqual(fluidStack, prev)) {
			return;
		}

		sendTankUpdate(container, crafters, tank);
	}

	private void sendTankUpdate(AbstractContainerMenu container, Iterable<ContainerListener> crafters, StandardTank tank) {
		if (tile != null) {
			int tankIndex = tank.getTankIndex();
			FluidStack fluid = tank.getFluid();
			IForestryPacketClient packet = new PacketTankLevelUpdate(tile, tankIndex, fluid);
			for (ContainerListener crafter : crafters) {
				if (crafter instanceof ServerPlayer) {
					NetworkUtil.sendToPlayer(packet, (ServerPlayer) crafter);
				}
			}

			if (fluid.isEmpty()) {
				prevFluidStacks.remove(container, tankIndex);
			} else {
				prevFluidStacks.put(container, tankIndex, fluid.copy());
			}
		}
	}

	@Override
	public void processTankUpdate(int tankIndex, @Nullable FluidStack contents) {
		if (tankIndex < 0 || tankIndex > tanks.size()) {
			return;
		}
		StandardTank tank = tanks.get(tankIndex);
		tank.setFluid(contents);
	}

	@Override
	public IFluidTank getTank(int tankIndex) {
		return tanks.get(tankIndex);
	}

	@Override
	public int getTanks() {
		return tanks.size();
	}

	@Nonnull
	@Override
	public FluidStack getFluidInTank(int tankIndex) {
		IFluidTank tank = getTank(tankIndex);
		if (tank == null) {
			return FluidStack.EMPTY;
		}
		return tank.getFluid();
	}

	@Override
	public int getTankCapacity(int tankIndex) {
		IFluidTank tank = getTank(tankIndex);
		if (tank == null) {
			return 0;
		}
		return tank.getCapacity();
	}

	@Override
	public boolean isFluidValid(int tankIndex, @Nonnull FluidStack stack) {
		IFluidTank tank = getTank(tankIndex);
		if (tank == null) {
			return false;
		}
		return tank.isFluidValid(stack);
	}

	@Override
	public int fill(FluidStack resource, FluidAction action) {
		for (StandardTank tank : tanks) {
			if (tankAcceptsFluid(tank, resource)) {
				return fill(tank.getTankIndex(), resource, action);
			}
		}

		return EmptyFluidHandler.INSTANCE.fill(resource, action);
	}

	public int fill(int tankIndex, FluidStack resource, FluidAction action) {
		if (tankIndex < 0 || tankIndex >= tanks.size()) {
			return 0;
		}

		StandardTank tank = tanks.get(tankIndex);
		if (!tank.canFill()) {
			return 0;
		}

		return tank.fill(resource, action);
	}

	@Override
	public void updateTankLevels(StandardTank tank) {
		if (!(tile instanceof IRenderableTile)) {
			return;
		}


		Level world = tile.getWorldObj();
		if (world == null || world.isClientSide)
			return;

		int tankIndex = tank.getTankIndex();
		PacketTankLevelUpdate tankLevelUpdate = new PacketTankLevelUpdate(tile, tankIndex, tank.getFluid());
		NetworkUtil.sendNetworkPacket(tankLevelUpdate, tile.getCoordinates(), world);
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		for (StandardTank tank : tanks) {
			if (tankCanDrain(tank)) {
				return drain(tank.getTankIndex(), maxDrain, action);
			}
		}
		return EmptyFluidHandler.INSTANCE.drain(maxDrain, action);
	}

	public FluidStack drain(int tankIndex, int maxDrain, FluidAction action) {
		if (tankIndex < 0 || tankIndex >= tanks.size()) {
			return FluidStack.EMPTY;
		}

		StandardTank tank = tanks.get(tankIndex);
		if (!tank.canDrain()) {
			return FluidStack.EMPTY;
		}

		return tank.drain(maxDrain, action);
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		for (StandardTank tank : tanks) {
			if (tankCanDrainFluid(tank, resource)) {
				return drain(tank.getTankIndex(), resource.getAmount(), action);
			}
		}
		return FluidStack.EMPTY;
	}

	@Nullable
	public FluidStack getFluid(int tankIndex) {
		return tanks.get(tankIndex).getFluid();
	}

	@Override
	public boolean canFillFluidType(FluidStack fluidStack) {
		for (StandardTank tank : tanks) {
			if (tank.isFluidValid(fluidStack)) {
				return true;
			}
		}

		return false;
	}

	private static boolean tankAcceptsFluid(StandardTank tank, FluidStack fluidStack) {
		return tank.canFill() &&
				tank.fill(fluidStack, FluidAction.SIMULATE) > 0;
	}

	private static boolean tankCanDrain(StandardTank tank) {
		if (!tank.canDrain()) {
			return false;
		}
		FluidStack drained = tank.drain(1, FluidAction.SIMULATE);
		return !drained.isEmpty() && drained.getAmount() > 0;
	}

	private static boolean tankCanDrainFluid(StandardTank tank, FluidStack fluidStack) {
		return ForestryFluids.areEqual(tank.getFluidType(), fluidStack) &&
				tankCanDrain(tank);
	}
}
