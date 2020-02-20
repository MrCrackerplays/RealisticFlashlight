package nl.patrickdruart.realisticFlashlight.flashlight;

import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import nl.patrickdruart.realisticFlashlight.FlashlightPlugin;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.FluidCollisionModeFlash;
import nl.patrickdruart.realisticFlashlight.flashlight.RayTrace.IRayTrace;
import nl.tabuu.tabuucore.configuration.IConfiguration;
import ru.beykerykt.lightapi.LightAPI;
import ru.beykerykt.lightapi.LightType;

/**
 * A runnable representing a flashlight in a holder's hand
 */
public class FlashlightRunnable extends BukkitRunnable {
	protected Flashlight flashlight;
	protected Location origin, oldTarget;
	protected Vector direction;
	protected Predicate<Entity> filter;
	protected boolean async, running;
	protected final boolean rightHand;
	protected FlashlightKeeperInfoHolder infoholder;
	protected IConfiguration config;
	protected FluidCollisionModeFlash fluidCollisionMode;

	public FlashlightRunnable(Flashlight flashlight, Location origin, Vector direction, boolean rightHand,
			FlashlightKeeperInfoHolder infoholder) {
		this.flashlight = flashlight;
		this.origin = origin;
		this.direction = direction;
		this.config = FlashlightPlugin.getConfigurationManager().getConfiguration("config");
		if (config.getBoolean("gameplay.passThroughInvisibility"))
			this.filter = e -> e != infoholder.getHolder().get()
					&& !(e instanceof LivingEntity && ((LivingEntity) e).hasPotionEffect(PotionEffectType.INVISIBILITY))
					&& !(e instanceof Player && ((Player) e).isPermissionSet("flashlight.passthrough")
							&& ((Player) e).hasPermission("flashlight.passthrough"));
		else
			this.filter = e -> e != infoholder.getHolder().get()
					&& !(e instanceof Player && ((Player) e).isPermissionSet("flashlight.passthrough")
							&& ((Player) e).hasPermission("flashlight.passthrough"));
		this.rightHand = rightHand;
		this.infoholder = infoholder;
		this.async = config.getBoolean("gameplay.asyncLighting");
		this.running = false;
		this.fluidCollisionMode = config.getBoolean("gameplay.fluidCollide") ? FluidCollisionModeFlash.ALWAYS
				: FluidCollisionModeFlash.NEVER;
	}

	@Override
	public void run() {
		if (this.infoholder.getHolder().get() == null || this.infoholder.getHolder().get().isDead()) {
			FlashlightPlugin.getFlashlightsManager().removeFlashlightKeeperInfoholder(infoholder.getHolderUniqueID());
			return;
			// if there's no entity alive that's holding this runnable's flashlight this
			// runnable stops
		}
		LivingEntity flashlightHolder = this.infoholder.getHolder().get();
		if ((rightHand ? flashlightHolder.getEquipment().getItemInMainHand()
				: flashlightHolder.getEquipment().getItemInOffHand()) == null
				|| !(rightHand ? flashlightHolder.getEquipment().getItemInMainHand()
						: flashlightHolder.getEquipment().getItemInOffHand()).equals(flashlight.getItem())) {
			FlashlightPlugin.getFlashlightsManager().forceReloadFlashlightKeeper(flashlightHolder);
			return;
			// if the item in the holder's hand that this runnable represents is no longer
			// the flashlight this runnable is supposed to represent this runnable force
			// reloads the FlashlightKeeperInfoHolder which in turn will lead to this
			// runnable stopping
		}
		if (!flashlight.isPowered() || flashlight.getBatteryCharge() < flashlight.getBatteryDrainRate()
				|| flashlight.getBatteryCharge() <= 0) {
			if (rightHand)
				infoholder.setRightHand(null, null, null);
			else
				infoholder.setLeftHand(null, null, null);
			return;
			// if the flashlight is no longer powered or its charge is below the drain rate
			// or its charge is at/below 0 this runnable stops
		}

		origin = FlashlightUtils.getOrigin(flashlightHolder, rightHand);
		direction = FlashlightUtils.getDirection(flashlightHolder, rightHand);
		IRayTrace trace;
		// the IRayTrace is made depending on collision setting in the config
		if (config.getBoolean("gameplay.entityCollide"))
			trace = IRayTrace.getRayTrace(origin, direction, flashlight.getDistance(), this.fluidCollisionMode, true,
					0d, filter);
		else if (config.getBoolean("gameplay.detailedLightCheck"))
			trace = IRayTrace.getRayTraceBlocks(origin, direction, flashlight.getDistance(), this.fluidCollisionMode,
					true);
		else
			trace = IRayTrace.getIteratorBlocks(origin, direction, flashlight.getDistance(), this.fluidCollisionMode,
					true);

		Location hit;
		if (trace != null)
			hit = trace.getHitLocation(origin.getWorld());
		else
			hit = origin.add(direction.clone().normalize().multiply(flashlight.getDistance()));

		if (oldTarget == null) {
			LightAPI.createLight(hit, LightType.BLOCK, flashlight.getLuminocity(), async);
			LightAPI.collectChunks(hit, LightType.BLOCK, 15)
					.forEach(chunk -> LightAPI.updateChunk(chunk, LightType.BLOCK));
			oldTarget = hit;
		} else if (!(hit.getWorld().equals(oldTarget.getWorld()) && hit.getBlockX() == oldTarget.getBlockX()
				&& hit.getBlockY() == oldTarget.getBlockY() && hit.getBlockZ() == oldTarget.getBlockZ())) {
			LightAPI.deleteLight(oldTarget, LightType.BLOCK, async);
			LightAPI.createLight(hit, LightType.BLOCK, flashlight.getLuminocity(), async);
			LightAPI.collectChunks(hit, LightType.BLOCK, 15)
					.forEach(chunk -> LightAPI.updateChunk(chunk, LightType.BLOCK));
			oldTarget = hit;
		}

		if (config.getBoolean("gameplay.useBattery") && flashlight.getBatteryDrainRate() != 0) {
			flashlight.setBatteryCharge(flashlight.getBatteryCharge() - flashlight.getBatteryDrainRate());
			if (flashlight.getBatteryCharge() <= 0)
				flashlight.setPowered(false);
			if (rightHand)
				flashlightHolder.getEquipment().setItemInMainHand(flashlight.getItem());
			else
				flashlightHolder.getEquipment().setItemInOffHand(flashlight.getItem());
			// drains the battery charge of the flashlight by its drain rate when this
			// feature is enabled and the drain rate isn't 0
		}
	}

	@Override
	public synchronized void cancel() throws IllegalStateException {
		if (oldTarget != null) {
			LightAPI.deleteLight(oldTarget, LightType.BLOCK, false);
			LightAPI.collectChunks(oldTarget, LightType.BLOCK, 15)
					.forEach(chunk -> LightAPI.updateChunk(chunk, LightType.BLOCK));
		}
		super.cancel();
		running = false;
	}

	@Override
	public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period)
			throws IllegalArgumentException, IllegalStateException {
		this.running = true;
		return super.runTaskTimer(plugin, delay, period);
	}

	@Override
	public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period)
			throws IllegalArgumentException, IllegalStateException {
		this.running = true;
		return super.runTaskTimerAsynchronously(plugin, delay, period);
	}

	public Flashlight getFlashlight() {
		return flashlight;
	}

	public void setFlashlight(Flashlight flashlight) {
		this.flashlight = flashlight;
	}

	public Location getOrigin() {
		return origin.clone();
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction.clone();
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * @return whether the runnable has been started
	 */
	public boolean start() {
		if (!running && flashlight.isPowered()) {
			this.runTaskTimer(FlashlightPlugin.getPlugin(), 0, FlashlightPlugin.getConfigurationManager()
					.getConfiguration("config").getLong("gameplay.updateDelay"));
			return true;
		} else
			return false;
	}
}
