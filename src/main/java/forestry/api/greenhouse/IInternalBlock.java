/*******************************************************************************
 * Copyright 2011-2014 SirSengir
 *
 * This work (the API) is licensed under the "MIT" License, see LICENSE.txt for details.
 ******************************************************************************/
package forestry.api.greenhouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import forestry.api.core.EnumHumidity;

import java.util.Collection;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IInternalBlock {
	
	@Nonnull
	BlockPos getPos();
	
	@Nullable
	IInternalBlock getRoot();
	
	@Nonnull
	Collection<IInternalBlockFace> getFaces();

}
