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
package forestry.lepidopterology.proxy;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.resources.model.BakedModel;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;

import forestry.api.lepidopterology.genetics.ButterflyChromosomes;
import forestry.lepidopterology.features.LepidopterologyEntities;
import forestry.lepidopterology.items.ItemButterflyGE;
import forestry.lepidopterology.render.ButterflyEntityRenderer;
import forestry.lepidopterology.render.ButterflyItemModel;
import forestry.lepidopterology.render.ButterflyModel;
import forestry.lepidopterology.render.CocoonItemModel;
import forestry.modules.IClientModuleHandler;

import genetics.utils.AlleleUtils;

@SuppressWarnings("unused")
@OnlyIn(Dist.CLIENT)
public class ProxyLepidopterologyClient extends ProxyLepidopterology implements IClientModuleHandler {

	@Override
	public void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
		AlleleUtils.forEach(ButterflyChromosomes.COCOON, (allele) -> {
			ImmutableList.Builder<BakedModel> models = new ImmutableList.Builder<>();
			for (int age = 0; age < ItemButterflyGE.MAX_AGE; age++) {
				event.register(allele.getCocoonItemModel(age));
			}
		});
	}

	@Override
	public void setupRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(LepidopterologyEntities.BUTTERFLY.entityType(), ButterflyEntityRenderer::new);
	}

	@Override
	public void setupLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(ButterflyModel.LAYER, ButterflyModel::createLayer);
	}

	@Override
	public void registerModelLoaders(ModelEvent.RegisterGeometryLoaders event) {
		event.register("butterfly_ge", new ButterflyItemModel.Loader());
		event.register("butterfly_cocoon", new CocoonItemModel.Loader());
	}
}
