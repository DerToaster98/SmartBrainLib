package net.tslat.smartbrainlib.core.behaviour.custom.attack;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.behaviour.DelayedBehaviour;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Attack behaviour that doesn't require line of sight or proximity to target, or to even have a target at all. This is useful for special attacks. <br>
 * Set the actual condition for activation via {@link net.tslat.smartbrainlib.core.behaviour.ExtendedBehaviour#startCondition(Predicate)}
 * @param <E> The entity
 */
public class ConditionlessAttack<E extends LivingEntity> extends DelayedBehaviour<E> {
	private static final List<Pair<MemoryModuleType<?>, MemoryStatus>> MEMORY_REQUIREMENTS = ObjectArrayList.of(Pair.of(MemoryModuleType.ATTACK_COOLING_DOWN, MemoryStatus.VALUE_ABSENT));

	private Function<E, Integer> attackIntervalSupplier = entity -> 20;
	private boolean requireTarget = false;
	private Consumer<E> effect = entity -> {};

	@Nullable
	protected LivingEntity target = null;

	public ConditionlessAttack(int delayTicks) {
		super(delayTicks);
	}

	/**
	 * Set the time between attacks.
	 * @param supplier The tick value provider
	 * @return this
	 */
	public ConditionlessAttack<E> attackInterval(Function<E, Integer> supplier) {
		this.attackIntervalSupplier = supplier;

		return this;
	}

	/**
	 * Set that the attack requires that the entity have an attack target set to activate.
	 * @return this
	 */
	public ConditionlessAttack<E> requiresTarget() {
		this.requireTarget = true;

		return this;
	}

	/**
	 * Set the callback for the actual attack when the delay time has elapsed
	 * @param consumer The callback
	 * @return this
	 */
	public ConditionlessAttack<E> attack(Consumer<E> consumer) {
		this.effect = consumer;

		return this;
	}

	@Override
	protected List<Pair<MemoryModuleType<?>, MemoryStatus>> getMemoryRequirements() {
		return MEMORY_REQUIREMENTS;
	}

	@Override
	protected boolean checkExtraStartConditions(ServerLevel level, E entity) {
		this.target = BrainUtils.getTargetOfEntity(entity);

		return !requireTarget || BrainUtils.hasMemory(entity, MemoryModuleType.ATTACK_TARGET);
	}

	@Override
	protected void doDelayedAction(E entity) {
		if (this.requireTarget && this.target == null)
			return;

		this.effect.accept(entity);
		BrainUtils.setForgettableMemory(entity, MemoryModuleType.ATTACK_COOLING_DOWN, true, this.attackIntervalSupplier.apply(entity));
	}
}
