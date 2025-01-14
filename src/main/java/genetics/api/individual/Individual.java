package genetics.api.individual;

import com.google.common.base.MoreObjects;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;

import forestry.Forestry;

import genetics.api.GeneticsAPI;

/**
 * A simple abstract default implementation of {@link IIndividual}.
 */
public abstract class Individual implements IIndividual {
	private static final String NBT_GENOME = "Genome";
	private static final String NBT_MATE = "Mate";
	private static final String NBT_ANALYZED = "IsAnalyzed";

	protected final IGenome genome;
	protected boolean isAnalyzed = false;
	@Nullable
	protected IGenome mate;

	public Individual(IGenome genome) {
		this.genome = genome;
	}

	public Individual(IGenome genome, @Nullable IGenome mate) {
		this.genome = genome;
		this.mate = mate;
	}

	public Individual(CompoundTag compound) {
		IKaryotype karyotype = getRoot().getKaryotype();
		IGenome genome = null;
		if (compound.contains(NBT_GENOME)) {
			genome = GeneticsAPI.apiInstance.getGeneticFactory().createGenome(karyotype, compound.getCompound(NBT_GENOME));
		}
		if (genome == null) {
			Forestry.LOGGER.warn("Could not read genome from individual NBT: {}", compound);
		}
		this.genome = MoreObjects.firstNonNull(genome, karyotype.getDefaultGenome());

		if (compound.contains(NBT_MATE)) {
			mate = GeneticsAPI.apiInstance.getGeneticFactory().createGenome(karyotype, compound.getCompound(NBT_MATE));
		}

		isAnalyzed = compound.getBoolean(NBT_ANALYZED);
	}

	@Override
	public String getIdentifier() {
		return genome.getActiveAllele(getRoot().getKaryotype().getSpeciesType()).getRegistryName().toString();
	}

	@Override
	public IGenome getGenome() {
		return genome;
	}

	@Override
	public boolean mate(@Nullable IGenome mate) {
		if (mate != null && mate.getKaryotype() != genome.getKaryotype()) {
			return false;
		}
		this.mate = mate;
		return true;
	}

	@Nullable
	@Override
	public IGenome getMate() {
		return this.mate;
	}

	@Override
	public boolean isPureBred(IChromosomeType geneType) {
		return genome.isPureBred(geneType);
	}

	@Override
	public boolean isGeneticEqual(IIndividual other) {
		return genome.isGeneticEqual(other.getGenome());
	}

	@Override
	public void onBuild(IIndividual otherIndividual) {
		if (otherIndividual.isAnalyzed()) {
			analyze();
		}
		IGenome otherMate = otherIndividual.getMate();
		if (otherMate != null) {
			mate(otherMate);
		}
	}

	@Override
	public IIndividualBuilder toBuilder() {
		return GeneticsAPI.apiInstance.getGeneticFactory().createIndividualBuilder(this);
	}

	@Override
	public CompoundTag write(CompoundTag compound) {
		compound.put(NBT_GENOME, genome.writeToNBT(new CompoundTag()));
		if (mate != null) {
			compound.put(NBT_MATE, mate.writeToNBT(new CompoundTag()));
		}
		compound.putBoolean(NBT_ANALYZED, isAnalyzed);
		return compound;
	}

	@Override
	public boolean analyze() {
		if (isAnalyzed) {
			return false;
		}

		isAnalyzed = true;
		return true;
	}

	@Override
	public boolean isAnalyzed() {
		return isAnalyzed;
	}
}
