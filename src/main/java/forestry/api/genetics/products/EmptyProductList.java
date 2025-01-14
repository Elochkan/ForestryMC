package forestry.api.genetics.products;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.NonNullList;

/**
 * Empty implementation of the product list
 */
public final class EmptyProductList implements IDynamicProductList {
	public static final EmptyProductList INSTANCE = new EmptyProductList();

	private EmptyProductList() {
	}

	@Override
	public Collection<Product> getPossibleProducts() {
		return Collections.emptyList();
	}

	@Override
	public NonNullList<ItemStack> getPossibleStacks() {
		return NonNullList.create();
	}

	@Override
	public Collection<Product> getConstantProducts() {
		return Collections.emptyList();
	}

	@Override
	public void addProducts(NonNullList<ItemStack> stacks, Function<Product, Float> modifier, RandomSource rand) {
		//Empty implementation, nothing to add here
	}
}
