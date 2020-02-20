package nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.V1_12_R1;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlock;
import org.bukkit.entity.Entity;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.cryptomorin.xseries.XMaterial;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.MovingObjectPosition;
import net.minecraft.server.v1_12_R1.MovingObjectPosition.EnumMovingObjectType;
import net.minecraft.server.v1_12_R1.Vec3D;
import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.BoundingBox;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.FluidCollisionModeFlash;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.IRayTrace;

/**
 * Copied from
 * https://hub.spigotmc.org/stash/users/blablubbabc/repos/bukkit/browse/src/main/java/org/bukkit/util/RayTraceResult.java?at=3bf5c1b38c03098131f47a89625d39abc19ceea0
 * to make raytracing work for older versions of mc (pre-1.13) with the same
 * source as newer versions (1.13+) without having to manually do it with nms
 */
public class RayTraceResult_V1_12_R1 implements IRayTrace {
	Vector hitPosition;
	BlockFace hitBlockFace;
	Block hitBlock;
	Entity hitEntity;

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition the hit position
	 */
	public RayTraceResult_V1_12_R1(Vector hitPosition) {
		this(hitPosition, (BlockFace) null);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition  the hit position
	 * @param hitBlockFace the hit block face
	 */
	public RayTraceResult_V1_12_R1(Vector hitPosition, BlockFace hitBlockFace) {
		this(hitPosition, (Block) null, hitBlockFace);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition  the hit position
	 * @param hitBlock     the hit block
	 * @param hitBlockFace the hit block face
	 */
	public RayTraceResult_V1_12_R1(Vector hitPosition, Block hitBlock, BlockFace hitBlockFace) {
		this.hitPosition = hitPosition;
		this.hitBlock = hitBlock;
		this.hitBlockFace = hitBlockFace;
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition the hit position
	 * @param hitEntity   the hit entity
	 */
	public RayTraceResult_V1_12_R1(Vector hitPosition, Entity hitEntity) {
		this(hitPosition, hitEntity, null);
	}

	/**
	 * Creates a RayTraceResult.
	 *
	 * @param hitPosition  the hit position
	 * @param hitEntity    the hit entity
	 * @param hitBlockFace the hit block face
	 */
	public RayTraceResult_V1_12_R1(Vector hitPosition, Entity hitEntity, BlockFace hitBlockFace) {
		this.hitPosition = hitPosition;
		this.hitEntity = hitEntity;
		this.hitBlockFace = hitBlockFace;
	}

	public static RayTraceResult_V1_12_R1 rayTraceEntities(Location start, Vector direction, double maxDistance) {
		return rayTraceEntities(start, direction, maxDistance, null);
	}

	public static RayTraceResult_V1_12_R1 rayTraceEntities(Location start, Vector direction, double maxDistance,
			double raySize) {
		return rayTraceEntities(start, direction, maxDistance, raySize, null);
	}

	public static RayTraceResult_V1_12_R1 rayTraceEntities(Location start, Vector direction, double maxDistance,
			Predicate<Entity> filter) {
		return rayTraceEntities(start, direction, maxDistance, 0.0D, filter);
	}

	public static RayTraceResult_V1_12_R1 rayTraceEntities(Location start, Vector direction, double maxDistance,
			double raySize, Predicate<Entity> filter) {
		Validate.notNull(start, "Start location is null!");
		CraftWorld world = (CraftWorld) start.getWorld();
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
		Collection<Entity> entities = world.getNearbyEntities(start, dir.getX(), dir.getY(), dir.getZ()).stream()
				.filter(filter).collect(Collectors.toList());

		Entity nearestHitEntity = null;
		RayTraceResult_V1_12_R1 nearestHitResult = null;
		double nearestDistanceSq = Double.MAX_VALUE;

		for (Entity entity : entities) {
			BoundingBox boundingBox = getBoundingBox(entity).expand(raySize);
			RayTraceResult_V1_12_R1 hitResult = (RayTraceResult_V1_12_R1) boundingBox.rayTrace(startPos, direction,
					maxDistance);

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
				: new RayTraceResult_V1_12_R1(nearestHitResult.getHitPosition(), nearestHitEntity,
						nearestHitResult.getHitBlockFace());
	}

	public static RayTraceResult_V1_12_R1 rayTraceBlocks(Location start, Vector direction, double maxDistance) {
		return rayTraceBlocks(start, direction, maxDistance, FluidCollisionModeFlash.NEVER, false);
	}

	public static RayTraceResult_V1_12_R1 rayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode) {
		return rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, false);
	}

	public static RayTraceResult_V1_12_R1 rayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks) {
		Validate.notNull(start, "Start location is null!");
		CraftWorld world = (CraftWorld) start.getWorld();
		Validate.isTrue(world.equals(start.getWorld()), "Start location is from different world!");
		start.checkFinite();

		Validate.notNull(direction, "Direction is null!");
		direction.checkFinite();

		Validate.isTrue(direction.lengthSquared() > 0, "Direction's magnitude is 0!");
		Validate.notNull(fluidCollisionMode, "Fluid collision mode is null!");

		if (maxDistance < 0.0D) {
			return null;
		}

		Vector dir = direction.clone().normalize().multiply(maxDistance);
		Vec3D startPos = new Vec3D(start.getX(), start.getY(), start.getZ());
		Vec3D endPos = new Vec3D(start.getX() + dir.getX(), start.getY() + dir.getY(), start.getZ() + dir.getZ());
		MovingObjectPosition nmsHitResult = world.getHandle().rayTrace(startPos, endPos,
				fluidCollisionMode != FluidCollisionModeFlash.NEVER, ignorePassableBlocks, false);
		RayTraceResult_V1_12_R1 result = fromNMS(world, nmsHitResult);
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
					Vector vecX = dir.clone().multiply((tempX - hitPos.getX()) / (tempX - hitPos.getX()));
					vecX.checkFinite();
					add = vecX;
				} catch (IllegalArgumentException e) {
				}
				try {
					Vector vecY = dir.clone().multiply((tempY - hitPos.getY()) / (tempY - hitPos.getY()));
					vecY.checkFinite();
					if (vecY.length() < add.length())
						add = vecY;
				} catch (IllegalArgumentException e) {
				}
				try {
					Vector vecZ = dir.clone().multiply((tempZ - hitPos.getZ()) / (tempZ - hitPos.getZ()));
					vecZ.checkFinite();
					if (vecZ.length() < add.length())
						add = vecZ;
				} catch (IllegalArgumentException e) {
				}
				hitPos.add(add);
				startPos = new Vec3D(hitPos.getX(), hitPos.getY(), hitPos.getZ());
				nmsHitResult = world.getHandle().rayTrace(startPos, endPos,
						fluidCollisionMode != FluidCollisionModeFlash.NEVER, ignorePassableBlocks, true);
				result = fromNMS(world, nmsHitResult);
				blockMat = (result == null || result.getHitBlock() == null) ? null : result.getHitBlock().getType();
			}
		}
		return result;
	}

	private static RayTraceResult_V1_12_R1 fromNMS(CraftWorld world, MovingObjectPosition nmsHitResult) {
		if (nmsHitResult != null && nmsHitResult.type != EnumMovingObjectType.MISS) {
			Vec3D nmsHitPos = nmsHitResult.pos;
			Vector hitPosition = new Vector(nmsHitPos.x, nmsHitPos.y, nmsHitPos.z);
			BlockFace hitBlockFace = null;
			if (nmsHitResult.direction != null) {
				hitBlockFace = CraftBlock.notchToBlockFace(nmsHitResult.direction);
			}

			if (nmsHitResult.entity != null) {
				Entity hitEntity = nmsHitResult.entity.getBukkitEntity();
				return new RayTraceResult_V1_12_R1(hitPosition, hitEntity, hitBlockFace);
			} else {
				Block hitBlock = null;
				BlockPosition nmsBlockPos = nmsHitResult.a();
				if (nmsBlockPos != null && world != null) {
					hitBlock = world.getBlockAt(nmsBlockPos.getX(), nmsBlockPos.getY(), nmsBlockPos.getZ());
				}

				return new RayTraceResult_V1_12_R1(hitPosition, hitBlock, hitBlockFace);
			}
		} else {
			return null;
		}
	}

	public static RayTraceResult_V1_12_R1 iteratorBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks) {
		BlockIterator blIt = new BlockIterator(start.getWorld(), start.toVector(), direction, 0d, (int) maxDistance);
		RayTraceResult_V1_12_R1 result = null;
		while (blIt.hasNext()) {
			Block block = blIt.next();
			if ((block.isLiquid() && fluidCollisionMode != FluidCollisionModeFlash.NEVER)
					|| (IRayTrace.isPassable(block.getType()) && !block.isLiquid() && !ignorePassableBlocks
							&& block.getType() != XMaterial.AIR.parseMaterial())
					|| (!IRayTrace.isSeeThrough(block.getType()) && !block.isPassable()) || (!blIt.hasNext())) {
				result = new RayTraceResult_V1_12_R1(block.getLocation().toVector());
				break;
			}
		}
		return result;
	}

	@Override
	public Location getHitLocation(World world) {
		Vector vector = this.hitPosition;
		if (vector == null)
			return null;
		return vector.toLocation(world);
	}

	@Override
	public Vector getHitPosition() {
		return hitPosition;
	}

	@Override
	public BlockFace getHitBlockFace() {
		return hitBlockFace;
	}

	@Override
	public Block getHitBlock() {
		return hitBlock;
	}

	@Override
	public Entity getHitEntity() {
		return hitEntity;
	}

	/**
	 * Creates a bounding box based on the entity's width and height.
	 *
	 * @return the entity's bounding box
	 */
	private static BoundingBox getBoundingBox(Entity entity) {
		Location loc = entity.getLocation();
		Location corner1 = loc.clone().add(-entity.getWidth() / 2d, 0, -entity.getWidth() / 2d);
		Location corner2 = loc.clone().add(entity.getWidth() / 2d, entity.getHeight(), entity.getWidth() / 2d);
		return new BoundingBox(corner1.getX(), corner1.getY(), corner1.getZ(), corner2.getX(), corner2.getY(),
				corner2.getZ());
	}
}