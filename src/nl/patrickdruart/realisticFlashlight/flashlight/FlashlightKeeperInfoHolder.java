package nl.patrickdruart.realisticFlashlight.flashlight;

import java.lang.ref.WeakReference;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * An InfoHolder for flashlight keepers (entities holding a flashlight)
 * containing the FlashlightRunnables of that flashlight keeper
 */
public class FlashlightKeeperInfoHolder {
	/*
	 * Left and Right don't actually represent the left and right hand for Players
	 * but Main hand (right) and Off hand (left) even if a player's Main hand is set
	 * to be their Left hand
	 */
	private FlashlightRunnable leftHandFlashlightRunnable, rightHandFlashlightRunnable;
	private WeakReference<LivingEntity> flashlightHolder;
	private UUID holderID;

	public FlashlightKeeperInfoHolder(LivingEntity flashlightHolder) {
		this(flashlightHolder, FlashlightUtils.getOrigin(flashlightHolder, false),
				FlashlightUtils.getDirection(flashlightHolder, false),
				(flashlightHolder.getEquipment().getItemInOffHand() != null && FlashlightsManager.getInstance()
						.isFlashlight(flashlightHolder.getEquipment().getItemInOffHand()))
								? new Flashlight(flashlightHolder.getEquipment().getItemInOffHand())
								: null,
				FlashlightUtils.getOrigin(flashlightHolder, true), FlashlightUtils.getDirection(flashlightHolder, true),
				(flashlightHolder.getEquipment().getItemInOffHand() != null && FlashlightsManager.getInstance()
						.isFlashlight(flashlightHolder.getEquipment().getItemInMainHand()))
								? new Flashlight(flashlightHolder.getEquipment().getItemInMainHand())
								: null);
	}

	public FlashlightKeeperInfoHolder(LivingEntity flashlightHolder, Location leftLightOrigin,
			Vector leftLightDirection, Flashlight leftHandFlashlight, Location rightLightOrigin,
			Vector rightLightDirection, Flashlight rightHandFlashlight) {
		if (flashlightHolder == null)
			throw new NullPointerException("Flashlight Holder can't be null!");
		if (flashlightHolder.isDead())
			throw new IllegalArgumentException("Flashlight Holder can't be dead!");
		this.flashlightHolder = new WeakReference<LivingEntity>(flashlightHolder);
		this.leftHandFlashlightRunnable = (leftHandFlashlight != null && leftLightDirection != null
				&& leftLightOrigin != null)
						? new FlashlightRunnable(leftHandFlashlight, leftLightOrigin, leftLightDirection, false, this)
						: null;
		this.rightHandFlashlightRunnable = (rightHandFlashlight != null && rightLightDirection != null
				&& rightLightOrigin != null)
						? new FlashlightRunnable(rightHandFlashlight, rightLightOrigin, rightLightDirection, true, this)
						: null;
		this.holderID = flashlightHolder.getUniqueId();
	}

	public void setRightHand(Location rightLightOrigin, Vector rightLightDirection, Flashlight rightHandFlashlight) {
		if (rightHandFlashlightRunnable != null && rightHandFlashlightRunnable.isRunning())
			rightHandFlashlightRunnable.cancel();
		this.rightHandFlashlightRunnable = (rightHandFlashlight != null && rightLightDirection != null
				&& rightLightOrigin != null)
						? new FlashlightRunnable(rightHandFlashlight, rightLightOrigin, rightLightDirection, true, this)
						: null;
		if (this.rightHandFlashlightRunnable == null) {
			rightLightOrigin = null;
			rightLightDirection = null;
		}
	}

	public void setLeftHand(Location leftLightOrigin, Vector leftLightDirection, Flashlight leftHandFlashlight) {
		if (leftHandFlashlightRunnable != null && leftHandFlashlightRunnable.isRunning())
			leftHandFlashlightRunnable.cancel();
		this.leftHandFlashlightRunnable = (leftHandFlashlight != null && leftLightDirection != null
				&& leftLightOrigin != null)
						? new FlashlightRunnable(leftHandFlashlight, leftLightOrigin, leftLightDirection, false, this)
						: null;
		if (this.leftHandFlashlightRunnable == null) {
			leftLightOrigin = null;
			leftLightDirection = null;
		}
	}

	public FlashlightRunnable getLeftHandFlashlightRunnable() {
		return leftHandFlashlightRunnable;
	}

	public FlashlightRunnable getRightHandFlashlightRunnable() {
		return rightHandFlashlightRunnable;
	}

	public WeakReference<LivingEntity> getHolder() {
		return flashlightHolder;
	}

	public UUID getHolderUniqueID() {
		return holderID;
	}
}
