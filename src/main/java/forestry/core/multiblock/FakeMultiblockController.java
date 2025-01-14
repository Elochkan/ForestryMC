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
package forestry.core.multiblock;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;

import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.IErrorLogic;
import forestry.api.core.ILocatable;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.core.errors.FakeErrorLogic;
import forestry.core.owner.FakeOwnerHandler;
import forestry.core.owner.IOwnerHandler;

public interface FakeMultiblockController extends IMultiblockControllerInternal, ILocatable {
	@Override
	default void attachBlock(IMultiblockComponent part) {
	}

	@Override
	default void detachBlock(IMultiblockComponent part, boolean chunkUnloading) {
	}

	@Override
	default void checkIfMachineIsWhole() {
	}

	@Override
	default void assimilate(IMultiblockControllerInternal other) {
	}

	@Override
	default void _onAssimilated(IMultiblockControllerInternal otherController) {
	}

	@Override
	default void onAssimilated(IMultiblockControllerInternal assimilator) {
	}

	@Override
	default void updateMultiblockEntity() {
	}

	@Override
	default BlockPos getReferenceCoord() {
		return BlockPos.ZERO;
	}

	@Override
	default void recalculateMinMaxCoords() {
	}

	@Override
	default void formatDescriptionPacket(CompoundTag data) {
	}

	@Override
	default void decodeDescriptionPacket(CompoundTag data) {
	}

	@Override
	@Nullable
	default Level getWorldObj() {
		return null;
	}

	@Override
	default boolean isEmpty() {
		return true;
	}

	@Override
	default boolean shouldConsume(IMultiblockControllerInternal otherController) {
		return false;
	}

	@Override
	default String getPartsListString() {
		return "";
	}

	@Override
	default void auditParts() {
	}

	@Override
	default Set<IMultiblockComponent> checkForDisconnections() {
		return Collections.emptySet();
	}

	@Override
	default Set<IMultiblockComponent> detachAllBlocks() {
		return Collections.emptySet();
	}

	@Override
	default boolean isAssembled() {
		return false;
	}

	@Override
	default void reassemble() {
	}

	@Override
	default String getLastValidationError() {
		return null;
	}

	@Nullable
	@Override
	default BlockPos getLastValidationErrorPosition() {
		return null;
	}

	@Override
	default Collection<IMultiblockComponent> getComponents() {
		return Collections.emptyList();
	}

	@Override
	default void read(CompoundTag CompoundNBT) {
	}

	@Override
	default CompoundTag write(CompoundTag CompoundNBT) {
		return CompoundNBT;
	}

	@Override
	default IOwnerHandler getOwnerHandler() {
		return FakeOwnerHandler.INSTANCE;
	}

	@Override
	default EnumTemperature getTemperature() {
		return EnumTemperature.NORMAL;
	}

	@Override
	default EnumHumidity getHumidity() {
		return EnumHumidity.NORMAL;
	}

	@Override
	default float getExactTemperature() {
		return 0.5f;
	}

	@Override
	default float getExactHumidity() {
		return 0.5f;
	}

	@Override
	default IErrorLogic getErrorLogic() {
		return FakeErrorLogic.INSTANCE;
	}

	@Override
	default void writeGuiData(FriendlyByteBuf data) {
	}

	@Override
	default void readGuiData(FriendlyByteBuf data) {
	}
}
