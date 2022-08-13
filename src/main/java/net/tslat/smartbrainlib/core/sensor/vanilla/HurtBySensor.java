package net.tslat.smartbrainlib.core.sensor.vanilla;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;

import java.util.List;

/**
 * A sensor that sets the memory state for the last damage source and attacker.
 *
 * @param <E> The entity
 */
public class HurtBySensor<E extends Mob> extends PredicateSensor<DamageSource, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.of(MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY);

	public HurtBySensor() {
		super((damageSource, entity) -> true);
	}

	@Override
	protected List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	protected void doTick(ServerLevel level, E entity) {
		Brain<?> brain = entity.getBrain();
		DamageSource damageSource = entity.getLastDamageSource();

		if (damageSource == null) {
			BrainUtils.clearMemory(brain, MemoryModuleType.HURT_BY);
		}
		else if (predicate().test(damageSource, entity)) {
			BrainUtils.setMemory(brain, MemoryModuleType.HURT_BY, damageSource);

			if (damageSource.getEntity() instanceof LivingEntity attacker && attacker.isAlive() && attacker.level == entity.level)
				BrainUtils.setMemory(brain, MemoryModuleType.HURT_BY_ENTITY, attacker);
		}
		else {
			BrainUtils.withMemory(brain, MemoryModuleType.HURT_BY_ENTITY, attacker -> {
				if (!attacker.isAlive() || attacker.level != entity.level)
					BrainUtils.clearMemory(brain, MemoryModuleType.HURT_BY_ENTITY);
			});
		}
	}
}