package forestry.modules;

import javax.annotation.Nullable;
import java.util.Set;

import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.fml.InterModComms;

import forestry.api.modules.ForestryModule;
import forestry.api.modules.IForestryModule;
import forestry.core.IPickupHandler;
import forestry.core.IResupplyHandler;
import forestry.core.ISaveEventHandler;
import forestry.core.config.Constants;

public class BlankForestryModule implements IForestryModule {
	/**
	 * The ForestryModule.moduleID()s of any other modules this module depends on.
	 */
	@Override
	public Set<ResourceLocation> getDependencyUids() {
		return Set.of(new ResourceLocation(Constants.MOD_ID, ForestryModuleUids.CORE));
	}

	@Override
	public String toString() {
		ForestryModule forestryModule = getClass().getAnnotation(ForestryModule.class);
		if (forestryModule == null) {
			return getClass().getSimpleName();
		}
		return forestryModule.name() + " Module";
	}

	public boolean processIMCMessage(InterModComms.IMCMessage message) {
		return false;
	}

	@Nullable
	public ISaveEventHandler getSaveEventHandler() {
		return null;
	}

	@Nullable
	public IPickupHandler getPickupHandler() {
		return null;
	}

	@Nullable
	public IResupplyHandler getResupplyHandler() {
		return null;
	}

	@Nullable
	public ISidedModuleHandler getModuleHandler() {
		return null;
	}
}
