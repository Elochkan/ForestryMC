package genetics.api.root;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import net.minecraft.world.item.ItemStack;

import genetics.api.GeneticsAPI;
import genetics.api.IGeneticFactory;
import genetics.api.alleles.IAllele;
import genetics.api.individual.IIndividual;
import genetics.api.individual.IKaryotype;
import genetics.api.organism.IOrganismType;
import genetics.api.organism.IOrganismTypes;
import genetics.api.root.components.ComponentKey;
import genetics.api.root.components.ComponentKeys;
import genetics.api.root.components.IRootComponent;
import genetics.api.root.components.IRootComponentContainer;
import genetics.api.root.translator.IIndividualTranslator;
import genetics.individual.RootDefinition;
import genetics.root.RootComponentContainer;

/**
 * Abstract implementation of the {@link IIndividualRoot} interface.
 *
 * @param <I> The type of the individual that this root provides.
 */
public abstract class IndividualRoot<I extends IIndividual> implements IIndividualRoot<I> {
	protected final IRootDefinition<? extends IIndividualRoot<I>> definition;
	protected final IOrganismTypes<I> types;
	protected final ITemplateContainer<I> templates;
	protected final IKaryotype karyotype;
	protected final String uid;
	private ImmutableList<I> individualTemplates;
	private I defaultMember;
	private final IRootComponentContainer<I> components;
	@Nullable
	private IDisplayHelper<I> displayHelper;

	public IndividualRoot(IRootContext<I> context) {
		this.uid = context.getKaryotype().getUID();
		this.definition = (IRootDefinition<? extends IIndividualRoot<I>>) context.getDefinition();
		//noinspection unchecked
		((RootDefinition<IIndividualRoot<I>>) this.definition).setRoot(this);
		this.karyotype = context.getKaryotype();
		this.components = new RootComponentContainer<>(context.createComponents(this), context.getComponentListeners(), context.getListeners());
		this.types = components.get(ComponentKeys.TYPES);
		this.templates = components.get(ComponentKeys.TEMPLATES);
		createDefault();
	}

	protected void createDefault() {
		this.defaultMember = create(karyotype.getDefaultGenome());
		ImmutableList.Builder<I> templateBuilder = new ImmutableList.Builder<>();
		for (IAllele[] template : templates.getTemplates()) {
			templateBuilder.add(templateAsIndividual(template));
		}
		this.individualTemplates = templateBuilder.build();
	}

	@Override
	public final String getUID() {
		return uid;
	}

	@Override
	public I getDefaultMember() {
		return defaultMember;
	}

	@Override
	public List<I> getIndividualTemplates() {
		return individualTemplates;
	}

	@Override
	public I create(String templateIdentifier) {
		IAllele[] template = templates.getTemplate(templateIdentifier);
		return template.length == 0 ? null : create(karyotype.templateAsGenome(template));
	}

	@Override
	public ItemStack createStack(IAllele allele, IOrganismType type) {
		I individual = create(allele.getRegistryName().toString());
		return individual == null ? ItemStack.EMPTY : types.createStack(individual, type);
	}

	@Override
	public boolean isMember(ItemStack stack) {
		return types.getType(stack) != null;
	}

	@Override
	public ITemplateContainer<I> getTemplates() {
		return templates;
	}

	@Override
	public IKaryotype getKaryotype() {
		return karyotype;
	}

	@Override
	public IIndividualTranslator<I> getTranslator() {
		IIndividualTranslator<I> translator = getComponentSafe(ComponentKeys.TRANSLATORS);
		if (translator == null) {
			throw new IllegalStateException(String.format("No translator component was added to the root with the uid '%s'.", getUID()));
		}
		return translator;
	}

	@Override
	public IOrganismTypes<I> getTypes() {
		return types;
	}

	@Override
	public IRootDefinition<? extends IIndividualRoot<I>> getDefinition() {
		return definition;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T extends IIndividualRoot<?>> T cast() {
		return (T) this;
	}

	@Override
	public boolean hasComponent(ComponentKey<?> key) {
		return components.has(key);
	}

	@Nullable
	@Override
	public <C extends IRootComponent<I>> C getComponentSafe(ComponentKey<?> key) {
		return components.getSafe(key);
	}

	@Override
	public <C extends IRootComponent<I>> C getComponent(ComponentKey<?> key) {
		return components.get(key);
	}

	@Override
	public IRootComponentContainer<I> getComponentContainer() {
		return components;
	}

	@Override
	public IDisplayHelper<I> getDisplayHelper() {
		if (displayHelper == null) {
			IGeneticFactory geneticFactory = GeneticsAPI.apiInstance.getGeneticFactory();
			displayHelper = geneticFactory.createDisplayHelper(this);
		}
		return displayHelper;
	}
}
