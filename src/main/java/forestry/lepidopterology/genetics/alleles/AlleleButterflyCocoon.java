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
package forestry.lepidopterology.genetics.alleles;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

import genetics.api.alleles.AlleleCategorized;

import forestry.api.core.ISetupListener;
import forestry.api.genetics.products.IDynamicProductList;
import forestry.api.lepidopterology.genetics.IAlleleButterflyCocoon;
import forestry.core.config.Constants;
import forestry.core.genetics.ProductListWrapper;
import forestry.lepidopterology.blocks.PropertyCocoon;

// this was never implemented in Forestry. what do we do about this?
public class AlleleButterflyCocoon extends AlleleCategorized implements IAlleleButterflyCocoon, ISetupListener {
	public static final PropertyCocoon COCOON = new PropertyCocoon("cocoon");

	private ProductListWrapper loot = ProductListWrapper.create();
	private final String name;

	public AlleleButterflyCocoon(String name, boolean isDominant) {
		super(Constants.MOD_ID, "cocoon", name, isDominant);
		this.name = name;
	}

	@Override
	public void onFinishSetup() {
		loot = loot.bake();
	}

	private static String getAgeKey(int age) {
		if (age == 0) {
			return "early";
		} else if (age == 1) {
			return "middle";
		} else {
			return "late";
		}
	}

	@Override
	public String getCocoonName() {
		return name;
	}

	@Override
	public ModelResourceLocation getCocoonItemModel(int age) {
		return new ModelResourceLocation(
			Constants.MOD_ID + ":lepidopterology/cocoons/cocoon_" + name + "_" + getAgeKey(age), "inventory");
	}

	@Override
	public void clearLoot() {
		loot = ProductListWrapper.create();
	}

	@Override
	public void bakeLoot() {
		loot = loot.bake();
	}

	@Override
	public void addLoot(ItemStack loot, float chance) {
		this.loot.addProduct(loot, chance);
	}


	@Override
	public IDynamicProductList getCocoonLoot() {
		return loot;
	}

	@Override
	public int compareTo(IAlleleButterflyCocoon o) {
		return 0;
	}
}
