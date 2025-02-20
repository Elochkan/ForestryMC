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
package forestry.arboriculture.worldgen;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

import forestry.api.arboriculture.ITreeGenData;
import forestry.core.worldgen.FeatureHelper;

public class FeatureKapok extends FeatureTree {

	public FeatureKapok(ITreeGenData tree) {
		super(tree, 10, 8);
	}

	@Override
	public Set<BlockPos> generateTrunk(LevelAccessor world, RandomSource rand, TreeBlockTypeLog wood, BlockPos startPos) {
		FeatureHelper.generateTreeTrunk(world, rand, wood, startPos, height, girth, 0, 0.6f, null, 0);
		FeatureHelper.generateSupportStems(wood, world, rand, startPos, height, girth, 0.8f, 0.4f);

		Set<BlockPos> branchCoords = new HashSet<>();
		int leafSpawn = height + 1;
		while (leafSpawn > height - 4) {
			int radius = Math.round(girth * (height - leafSpawn) / 1.5f) + 6;
			branchCoords.addAll(FeatureHelper.generateBranches(world, rand, wood, startPos.offset(0, leafSpawn, 0), girth, 0.3f, 0.25f, radius, 6, 1.0f));
			leafSpawn -= 2;
		}

		return branchCoords;
	}

	@Override
	protected void generateLeaves(LevelAccessor world, RandomSource rand, TreeBlockTypeLeaf leaf, TreeContour contour, BlockPos startPos) {
		int leafSpawn = height + 1;

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);
		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn--, 0), girth, 0.5f + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		FeatureHelper.generateCylinderFromTreeStartPos(world, leaf, startPos.offset(0, leafSpawn, 0), girth, 1.9f + girth, 1, FeatureHelper.EnumReplaceMode.SOFT, contour);

		for (BlockPos branchEnd : contour.getBranchEnds()) {
			FeatureHelper.generateCylinderFromPos(world, leaf, branchEnd.above(), 2.0f + girth, 2, FeatureHelper.EnumReplaceMode.AIR, contour);
		}

		// Add some smaller twigs below for flavour
		for (int times = 0; times < height / 4; times++) {
			int h = 10 + rand.nextInt(Math.max(1, height - 10));
			if (rand.nextBoolean() && h < height / 2) {
				h = height / 2 + rand.nextInt(height / 2);
			}
			int x_off = -1 + rand.nextInt(3);
			int y_off = -1 + rand.nextInt(3);
			FeatureHelper.generateSphere(world, startPos.offset(x_off, h, y_off), 1 + rand.nextInt(1), leaf, FeatureHelper.EnumReplaceMode.AIR, contour);
		}
	}
}
