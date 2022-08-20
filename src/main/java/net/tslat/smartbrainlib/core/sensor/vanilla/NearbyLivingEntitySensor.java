package net.tslat.smartbrainlib.core.sensor.vanilla;

import java.util.Comparator;
import java.util.List;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.tslat.smartbrainlib.api.util.BrainUtils;
import net.tslat.smartbrainlib.api.util.EntityRetrievalUtil;
import net.tslat.smartbrainlib.core.sensor.ExtendedSensor;
import net.tslat.smartbrainlib.core.sensor.PredicateSensor;
import net.tslat.smartbrainlib.object.NearestVisibleLivingEntities;
import net.tslat.smartbrainlib.registry.SBLMemoryTypes;
import net.tslat.smartbrainlib.registry.SBLSensors;

/**
 * A sensor that looks for nearby living entities in the surrounding area, sorted by proximity to the brain owner.<br>
 * Defaults:
 * <ul>
 *     <li>Radius is equivalent to the entity's {@link net.minecraft.world.entity.ai.attributes.Attributes#FOLLOW_RANGE} attribute</li>
 *     <li>Only alive entities</li>
 * </ul>
 * @param <E> The entity
 */
public class NearbyLivingEntitySensor<E extends LivingEntity> extends PredicateSensor<LivingEntity, E> {
	private static final List<MemoryModuleType<?>> MEMORIES = ObjectArrayList.wrap(new MemoryModuleType[] {MemoryModuleType.LIVING_ENTITIES, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get()});

	@Nullable
	protected Vector3d radius = null;

	public NearbyLivingEntitySensor() {
		super((target, entity) -> target != entity && target.isAlive());
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius
	 * @return this
	 */
	public NearbyLivingEntitySensor<E> setRadius(double radius) {
		return setRadius(new Vector3d(radius, radius, radius));
	}

	/**
	 * Set the radius for the item sensor to scan
	 *
	 * @param radius The radius triplet
	 * @return this
	 */
	public NearbyLivingEntitySensor<E> setRadius(Vector3d radius) {
		this.radius = radius;

		return this;
	}

	@Override
	public List<MemoryModuleType<?>> memoriesUsed() {
		return MEMORIES;
	}

	@Override
	public SensorType<? extends ExtendedSensor<?>> type() {
		return SBLSensors.NEARBY_LIVING_ENTITY.get();
	}

	@Override
	protected void doTick(ServerWorld level, E entity) {
		Vector3d radius = this.radius;

		if (radius == null) {
			double dist = entity.getAttributeValue(Attributes.FOLLOW_RANGE);

			radius = new Vector3d(dist, dist, dist);
		}

		List<LivingEntity> entities = EntityRetrievalUtil.getEntities(level, entity.getBoundingBox().inflate(radius.x(), radius.y(), radius.z()), obj -> obj instanceof LivingEntity && predicate().test((LivingEntity)obj, entity));

		entities.sort(Comparator.comparingDouble(entity::distanceToSqr));

		BrainUtils.setMemory(entity, MemoryModuleType.LIVING_ENTITIES, entities);
		BrainUtils.setMemory(entity, SBLMemoryTypes.NEAREST_VISIBLE_LIVING_ENTITIES.get(), new NearestVisibleLivingEntities(entity, entities));
	}
}