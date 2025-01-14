package genetics.individual;

import javax.annotation.Nullable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import genetics.api.root.IIndividualRoot;
import genetics.api.root.IRootDefinition;

public class RootDefinition<R extends IIndividualRoot> implements IRootDefinition<R> {
	@Nullable
	private R root = null;

	public void setRoot(@Nullable R definition) {
		this.root = definition;
	}

	@Nullable
	@Override
	public R maybe() {
		return root;
	}

	@Override
	public R get() {
		if (root == null) {
			throw new NoSuchElementException("No value present");
		}
		return root;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends IIndividualRoot> T cast() {
		return (T) get();
	}

	@Override
	public R orElse(R other) {
		return root != null ? root : other;
	}

	@Override
	public boolean test(Predicate<? super R> predicate) {
		Objects.requireNonNull(predicate);
		return root != null && predicate.test(root);
	}

	@Nullable
	@Override
	public R filter(Predicate<? super R> predicate) {
		Objects.requireNonNull(predicate);
		if (root == null) {
			return null;
		} else {
			return predicate.test(root) ? root : null;
		}
	}

	@Override
	public boolean isPresent() {
		return root != null;
	}

	@Override
	public void ifPresent(Consumer<R> consumer) {
		if (root != null) {
			consumer.accept(root);
		}
	}

	@Override
	public <U> Optional<U> map(Function<? super R, ? extends U> mapper) {
		Objects.requireNonNull(mapper);
		if (!isPresent()) {
			return Optional.empty();
		} else {
			return Optional.ofNullable(mapper.apply(root));
		}
	}
}
