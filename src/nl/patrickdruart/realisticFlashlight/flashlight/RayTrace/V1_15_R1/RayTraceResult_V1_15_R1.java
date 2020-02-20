package nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.V1_15_R1;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.commons.lang.Validate;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.CraftFluidCollisionMode;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftRayTraceResult;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import net.minecraft.server.v1_15_R1.MovingObjectPosition;
import net.minecraft.server.v1_15_R1.RayTrace;
import net.minecraft.server.v1_15_R1.Vec3D;
import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.FluidCollisionModeFlash;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.IRayTrace;

/**
 * Copied from
 * https://hub.spigotmc.org/stash/users/blablubbabc/repos/bukkit/browse/src/main/java/org/bukkit/util/RayTraceResult.java?at=3bf5c1b38c03098131f47a89625d39abc19ceea0
 * to make raytracing work for older versions of mc (pre-1.13) with the same
 * source as newer versions (1.13+) without having to manually do it with nms
 */
public class RayTraceResult_V1_15_R1 implements IRayTrace {
	org.bukkit.util.RayTraceResult result;

	/**
	 * Dependancy injection
	 * 
	 * @param result
	 */
	public RayTraceResult_V1_15_R1(org.bukkit.util.RayTraceResult result) {
		this.result = result;
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition the hit position
	 */
	public RayTraceResult_V1_15_R1(Vector hitPosition) {
		result = new org.bukkit.util.RayTraceResult(hitPosition);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition  the hit position
	 * @param hitBlockFace the hit block face
	 */
	public RayTraceResult_V1_15_R1(Vector hitPosition, BlockFace hitBlockFace) {
		result = new org.bukkit.util.RayTraceResult(hitPosition, hitBlockFace);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition  the hit position
	 * @param hitBlock     the hit block
	 * @param hitBlockFace the hit block face
	 */
	public RayTraceResult_V1_15_R1(Vector hitPosition, Block hitBlock, BlockFace hitBlockFace) {
		result = new org.bukkit.util.RayTraceResult(hitPosition, hitBlock, hitBlockFace);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition the hit position
	 * @param hitEntity   the hit entity
	 */
	public RayTraceResult_V1_15_R1(Vector hitPosition, Entity hitEntity) {
		result = new org.bukkit.util.RayTraceResult(hitPosition, hitEntity);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition  the hit position
	 * @param hitEntity    the hit entity
	 * @param hitBlockFace the hit block face
	 */
	public RayTraceResult_V1_15_R1(Vector hitPosition, Entity hitEntity, BlockFace hitBlockFace) {
		result = new org.bukkit.util.RayTraceResult(hitPosition, hitEntity, hitBlockFace);
	}

	public static RayTraceResult_V1_15_R1 rayTraceEntities(Location start, Vector direction, double maxDistance) {
		return rayTraceEntities(start, direction, maxDistance, null);
	}

	public static RayTraceResult_V1_15_R1 rayTraceEntities(Location start, Vector direction, double maxDistance,
			double raySize) {
		return rayTraceEntities(start, direction, maxDistance, raySize, null);
	}

	public static RayTraceResult_V1_15_R1 rayTraceEntities(Location start, Vector direction, double maxDistance,
			Predicate<Entity> filter) {
		return rayTraceEntities(start, direction, maxDistance, 0.0D, filter);
	}

	public static RayTraceResult_V1_15_R1 rayTraceEntities(Location start, Vector direction, double maxDistance,
			double raySize, Predicate<Entity> filter) {
		Validate.notNull(start, "Start location is null!");
		World world = start.getWorld();
		Validate.isTrue(world.equals(start.getWorld()), "Start location is from different world!");
		start.checkFinite();

		Validate.notNull(direction, "Direction is null!");
		direction.checkFinite();

		Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");

		if (maxDistance < 0.0D) {
			return null;
		}

		Vector startPos = start.toVector();
		Vector dir = direction.clone().normalize().multiply(maxDistance);
		BoundingBox aabb = BoundingBox.of(startPos, startPos).expandDirectional(dir).expand(raySize);
		Collection<Entity> entities = world.getNearbyEntities(aabb, filter);

		Entity nearestHitEntity = null;
		org.bukkit.util.RayTraceResult nearestHitResult = null;
		double nearestDistanceSq = Double.MAX_VALUE;

		for (Entity entity : entities) {
			BoundingBox boundingBox = entity.getBoundingBox().expand(raySize);
			org.bukkit.util.RayTraceResult hitResult = boundingBox.rayTrace(startPos, direction, maxDistance);

			if (hitResult != null) {
				double distanceSq = startPos.distanceSquared(hitResult.getHitPosition());

				if (distanceSq < nearestDistanceSq) {
					nearestHitEntity = entity;
					nearestHitResult = hitResult;
					nearestDistanceSq = distanceSq;
				}
			}
		}

		return (nearestHitEntity == null) ? null
				: new RayTraceResult_V1_15_R1(nearestHitResult.getHitPosition(), nearestHitEntity,
						nearestHitResult.getHitBlockFace());
	}

	public static RayTraceResult_V1_15_R1 rayTraceBlocks(Location start, Vector direction, double maxDistance) {
		return rayTraceBlocks(start, direction, maxDistance, FluidCollisionModeFlash.NEVER, false);
	}

	public static RayTraceResult_V1_15_R1 rayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode) {
		return rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
	}

	public static RayTraceResult_V1_15_R1 rayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks) {
		Validate.notNull(start, "Start location is null!");
		CraftWorld world = (CraftWorld) start.getWorld();
		Validate.isTrue(world.equals(start.getWorld()), "Start location is from different world!");
		start.checkFinite();

		Validate.notNull(direction, "Direction is null!");
		direction.checkFinite();

		Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");
		Validate.notNull(fluidCollisionMode, "Fluid collision mode is null!");

		FluidCollisionMode mode = FluidCollisionMode.valueOf(fluidCollisionMode.name());
		if (maxDistance < 0.0D) {
			return null;
		}

		Vector dir = direction.clone().normalize().multiply(maxDistance);
		Vec3D startPos = new Vec3D(start.getX(), start.getY(), start.getZ());
		Vec3D endPos = new Vec3D(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
		MovingObjectPosition nmsHitResult = world.getHandle().rayTrace(new RayTrace(startPos, endPos,
				ignorePassableBlocks ? RayTrace.BlockCollisionOption.COLLIDER : RayTrace.BlockCollisionOption.OUTLINE,
				CraftFluidCollisionMode.toNMS(mode), null));
		org.bukkit.util.RayTraceResult result = CraftRayTraceResult.fromNMS(world, nmsHitResult);
		if (FlashlightPlugin.getConfigurationManager().getConfiguration("config")
				.getBoolean("gameplay.passSeeThroughBlocks") && result != null && result.getHitBlock() != null
				&& result.getHitPosition() != null) {
			Material blockMat = result.getHitBlock().getType();
			while (result != null && result.getHitBlock() != null && result.getHitPosition() != null
					&& start.clone().toVector().distance(result.getHitPosition()) < maxDistance
					&& (IRayTrace.isSeeThrough(blockMat) && !result.getHitBlock().isPassable())) {
				Vector hitPos = result.getHitPosition();
				int tempX = hitPos.getBlockX();
				int tempY = hitPos.getBlockY();
				int tempZ = hitPos.getBlockZ();
				dir = direction.clone().normalize();
				if (dir.getX() < 0)
					tempX -= 1;
				else if (dir.getX() > 0)
					tempX += 1;
				if (dir.getY() < 0)
					tempY -= 1;
				else if (dir.getY() > 0)
					tempY += 1;
				if (dir.getZ() < 0)
					tempZ -= 1;
				else if (dir.getZ() > 0)
					tempZ += 1;
				// will take the shortest valid vector to the next closest axis/location on the
				// direction vector
				Vector add = null;
				try {
					Vector vecX = dir.clone().multiply((tempX - hitPos.getX()) / dir.getX());
					vecX.checkFinite();
					add = vecX;
				} catch (IllegalArgumentException e) {
				}
				try {
					Vector vecY = dir.clone().multiply((tempY - hitPos.getY()) / dir.getY());
					vecY.checkFinite();
					if (vecY.length() < add.length())
						add = vecY;
				} catch (IllegalArgumentException e) {
				}
				try {
					Vector vecZ = dir.clone().multiply((tempZ - hitPos.getZ()) / dir.getZ());
					vecZ.checkFinite();
					if (vecZ.length() < add.length())
						add = vecZ;
				} catch (IllegalArgumentException e) {
				}
				hitPos.add(add);
				startPos = new Vec3D(hitPos.getX(), hitPos.getY(), hitPos.getZ());
				nmsHitResult = world.getHandle()
						.rayTrace(new RayTrace(startPos, endPos,
								ignorePassableBlocks ? RayTrace.BlockCollisionOption.COLLIDER
										: RayTrace.BlockCollisionOption.OUTLINE,
								CraftFluidCollisionMode.toNMS(mode), null));
				result = CraftRayTraceResult.fromNMS(world, nmsHitResult);
				blockMat = (result == null || result.getHitBlock() == null) ? null : result.getHitBlock().getType();
			}
		}
		return (result == null) ? null : new RayTraceResult_V1_15_R1(result);
	}

	public static RayTraceResult_V1_15_R1 iteratorBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks) {
		BlockIterator blIt = new BlockIterator(start.getWorld(), start.toVector(), direction, 0d, (int) maxDistance);
		RayTraceResult_V1_15_R1 result = null;
		while (blIt.hasNext()) {
			Block block = blIt.next();
			if ((block.isLiquid() && fluidCollisionMode != FluidCollisionModeFlash.NEVER)
					|| (block.isPassable() && !block.isLiquid() && !ignorePassableBlocks && !block.getType().isAir())
					|| (!IRayTrace.isSeeThrough(block.getType()) && !block.isPassable()) || (!blIt.hasNext())) {
				result = new RayTraceResult_V1_15_R1(block.getLocation().toVector());
				break;
			}
		}
		return result;
	}

	@Override
	public Location getHitLocation(World world) {
		if (result == null)
			return null;
		Vector vector = result.getHitPosition();
		if (vector == null)
			return null;
		return vector.toLocation(world);
	}

	@Override
	public Vector getHitPosition() {
		if (result == null)
			return null;
		return result.getHitPosition();
	}

	@Override
	public BlockFace getHitBlockFace() {
		if (result == null)
			return null;
		return result.getHitBlockFace();
	}

	@Override
	public Block getHitBlock() {
		if (result == null)
			return null;
		return result.getHitBlock();
	}

	@Override
	public Entity getHitEntity() {
		if (result == null)
			return null;
		return result.getHitEntity();
	}
}