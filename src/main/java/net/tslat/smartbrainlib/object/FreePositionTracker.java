package net.tslat.smartbrainlib.object;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IPosWrapper;
import net.minecraft.util.math.vector.Vector3d;

public class FreePositionTracker implements IPosWrapper {
	private final Vector3d pos;

	public FreePositionTracker(Vector3d pos) {
		this.pos = pos;
	}

	@Override
	public Vector3d currentPosition() {
		return pos;
	}

	@Override
	public BlockPos currentBlockPosition() {
		return new BlockPos(pos);
	}

	@Override
	public boolean isVisibleBy(LivingEntity entity) {
		return true;
	}
}
