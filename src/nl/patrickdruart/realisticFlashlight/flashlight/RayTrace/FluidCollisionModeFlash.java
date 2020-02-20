package nl.patrickdruart.realisticFlashlight.flashlight.RayTrace;

// basically a wrapper for FluidCollisionMode due to it not existing in older spigot versions
/**
 * Determines the collision behavior when fluids get hit during ray tracing.
 */
public enum FluidCollisionModeFlash {

	/**
	 * Ignore fluids.
	 */
	NEVER,
	/**
	 * Only collide with source fluid blocks.
	 */
	SOURCE_ONLY,
	/**
	 * Collide with all fluids.
	 */
	ALWAYS;
}
