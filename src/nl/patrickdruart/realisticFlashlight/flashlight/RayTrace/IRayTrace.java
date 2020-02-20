package nl.patrickdruart.realisticFlashlight.flashlight.RayTrace;

import java.util.Arrays;
import java.util.function.Predicate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

import com.cryptomorin.xseries.XMaterial;

import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.V1_12_R1.RayTraceResult_V1_12_R1;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.V1_13_R2.RayTraceResult_V1_13_R2;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.V1_14_R1.RayTraceResult_V1_14_R1;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.V1_15_R1.RayTraceResult_V1_15_R1;
import nl.tabuu.tabuucore.nms.NMSUtil;

public interface IRayTrace {
	/**
	 * Contains blocks through which players can pass (and thus light as well)
	 */
	public static final XMaterial[] passable = { XMaterial.AIR, XMaterial.WATER };
	/**
	 * Contains all blocks through which light could reasonably be expected to pass
	 * through its hitbox
	 */
	public static final XMaterial[] seeThrough = { XMaterial.BARRIER, XMaterial.IRON_BARS,
			XMaterial.BLACK_STAINED_GLASS, XMaterial.BLACK_STAINED_GLASS_PANE, XMaterial.BLUE_STAINED_GLASS,
			XMaterial.BLUE_STAINED_GLASS_PANE, XMaterial.GLASS, XMaterial.GLASS_PANE, XMaterial.BROWN_STAINED_GLASS,
			XMaterial.BROWN_STAINED_GLASS_PANE, XMaterial.CYAN_STAINED_GLASS, XMaterial.CYAN_STAINED_GLASS_PANE,
			XMaterial.GRAY_STAINED_GLASS, XMaterial.GRAY_STAINED_GLASS_PANE, XMaterial.GREEN_STAINED_GLASS,
			XMaterial.GREEN_STAINED_GLASS_PANE, XMaterial.LIGHT_BLUE_STAINED_GLASS,
			XMaterial.LIGHT_BLUE_STAINED_GLASS_PANE, XMaterial.LIGHT_GRAY_STAINED_GLASS,
			XMaterial.LIGHT_GRAY_STAINED_GLASS_PANE, XMaterial.LIME_STAINED_GLASS, XMaterial.LIME_STAINED_GLASS_PANE,
			XMaterial.MAGENTA_STAINED_GLASS, XMaterial.MAGENTA_STAINED_GLASS_PANE, XMaterial.ORANGE_STAINED_GLASS,
			XMaterial.ORANGE_STAINED_GLASS_PANE, XMaterial.PINK_STAINED_GLASS, XMaterial.PINK_STAINED_GLASS_PANE,
			XMaterial.PURPLE_STAINED_GLASS, XMaterial.PURPLE_STAINED_GLASS_PANE, XMaterial.RED_STAINED_GLASS,
			XMaterial.RED_STAINED_GLASS_PANE, XMaterial.WHITE_STAINED_GLASS, XMaterial.WHITE_STAINED_GLASS_PANE,
			XMaterial.YELLOW_STAINED_GLASS, XMaterial.YELLOW_STAINED_GLASS_PANE };

	public Location getHitLocation(World world);

	public Vector getHitPosition();

	public BlockFace getHitBlockFace();

	public Block getHitBlock();

	public Entity getHitEntity();

	public static IRayTrace getRayTrace(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks, double raySize,
			Predicate<Entity> filter) {
		IRayTrace blockHit;
		if (FlashlightPlugin.getConfigurationManager().getConfiguration("config")
				.getBoolean("gameplay.detailedLightCheck")) {
			blockHit = getRayTraceBlocks(start, direction, maxDistance, fluidCollisionMode, ignorePassableBlocks);
		} else {
			blockHit = getIteratorBlocks(start, direction, maxDistance, fluidCollisionMode, ignorePassableBlocks);
		}
		Vector startVec = null;
		double blockHitDistance = maxDistance;

		// limiting the entity search range if we found a block hit:
		if (blockHit != null && blockHit.getHitLocation(start.getWorld()) != null) {
			startVec = start.toVector();
			blockHitDistance = startVec.distance(blockHit.getHitLocation(start.getWorld()).toVector());
		}

		IRayTrace entityHit = getRayTraceEntities(start, direction, blockHitDistance, raySize, filter);
		if (blockHit == null || blockHit.getHitLocation(start.getWorld()) == null) {
			return entityHit;
		}

		if (entityHit == null) {
			return blockHit;
		}

		// Cannot be null as blockHit == null returns above
		double entityHitDistanceSquared = startVec
				.distanceSquared(entityHit.getHitLocation(start.getWorld()).toVector());
		if (entityHitDistanceSquared < (blockHitDistance * blockHitDistance)) {
			return entityHit;
		}

		return blockHit;
	}

	public static IRayTrace getIteratorBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks) {
		switch (NMSUtil.getVersion()) {
		case v1_12_R1:
			return RayTraceResult_V1_12_R1.iteratorBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		case v1_13_R2:
			return RayTraceResult_V1_13_R2.iteratorBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		case v1_14_R1:
			return RayTraceResult_V1_14_R1.iteratorBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		case v1_15_R1:
			return RayTraceResult_V1_15_R1.iteratorBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		default:
			break;
		}
		return null;
	}

	public static IRayTrace getRayTraceBlocks(Location start, Vector direction, double maxDistance,
			FluidCollisionModeFlash fluidCollisionMode, boolean ignorePassableBlocks) {
		switch (NMSUtil.getVersion()) {
		case v1_12_R1:
			return RayTraceResult_V1_12_R1.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		case v1_13_R2:
			return RayTraceResult_V1_13_R2.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		case v1_14_R1:
			return RayTraceResult_V1_14_R1.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		case v1_15_R1:
			return RayTraceResult_V1_15_R1.rayTraceBlocks(start, direction, maxDistance, fluidCollisionMode,
					ignorePassableBlocks);
		default:
			break;
		}
		return null;
	}

	public static IRayTrace getRayTraceEntities(Location start, Vector direction, double maxDistance, double raySize,
			Predicate<Entity> filter) {
		switch (NMSUtil.getVersion()) {
		case v1_12_R1:
			return RayTraceResult_V1_12_R1.rayTraceEntities(start, direction, maxDistance, raySize, filter);
		case v1_13_R2:
			return RayTraceResult_V1_13_R2.rayTraceEntities(start, direction, maxDistance, raySize, filter);
		case v1_14_R1:
			return RayTraceResult_V1_14_R1.rayTraceEntities(start, direction, maxDistance, raySize, filter);
		case v1_15_R1:
			return RayTraceResult_V1_15_R1.rayTraceEntities(start, direction, maxDistance, raySize, filter);
		default:
			break;
		}
		return null;
	}

	public static boolean isPassable(Material m) {
		return Arrays.stream(seeThrough).map(xMat -> xMat.parseMaterial()).anyMatch(mat -> mat == m);
	}

	public static boolean isSeeThrough(Material m) {
		return isPassable(m) || Arrays.stream(seeThrough).map(xMat -> xMat.parseMaterial()).anyMatch(mat -> mat == m);
	}

	public static IRayTrace getRayTraceResult(Vector hitPosition, BlockFace hitBlockFace) {
		switch (NMSUtil.getVersion()) {
		case v1_12_R1:
			return new RayTraceResult_V1_12_R1(hitPosition, hitBlockFace);
		case v1_13_R2:
			return new RayTraceResult_V1_13_R2(hitPosition, hitBlockFace);
		case v1_14_R1:
			return new RayTraceResult_V1_14_R1(hitPosition, hitBlockFace);
		case v1_15_R1:
			return new RayTraceResult_V1_15_R1(hitPosition, hitBlockFace);
		default:
			break;
		}
		return null;
	}
}
