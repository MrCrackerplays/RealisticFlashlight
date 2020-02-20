package nl.patrickdruart.realisticFlashlight.flashlight;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

/**
 * Utility methods class
 */
public class FlashlightUtils {
	/**
	 * @param entity    The entity from whom the origin point is
	 * @param rightHand Whether the entity's origin comes from its right hand
	 * @return The origin of a flashlight's light
	 */
	public static Location getOrigin(LivingEntity entity, boolean rightHand) {
		Location origin = null;
		if (entity instanceof ArmorStand) {
			ArmorStand armorStand = (ArmorStand) entity;
			if (rightHand)
				origin = getRightArmTip(armorStand);
			else
				origin = getLeftArmTip(armorStand);
		} else
			origin = entity.getEyeLocation();
		return origin;
	}

	/**
	 * @param entity    The entity for whom the direction is
	 * @param rightHand Whether the entity's origin comes from its right hand
	 * @return The direction of a flashlight's light
	 */
	public static Vector getDirection(LivingEntity entity, boolean rightHand) {
		Vector direction = null;
		if (entity instanceof ArmorStand) {
			ArmorStand armorStand = (ArmorStand) entity;
			if (rightHand)
				direction = getRightHeldItemVector(armorStand);
			else
				direction = getLeftHeldItemVector(armorStand);
		} else
			direction = entity.getEyeLocation().getDirection();
		return direction;
	}

	/**
	 * @param armorStand The ArmorStand from whom the item vector is
	 * @return The directional vector of the ArmorStand's left arm's held item
	 */
	public static Vector getLeftHeldItemVector(ArmorStand armorStand) {
		Location innerShoulderTarget = getLeftArmShoulder(armorStand);
		Location a = getLeftArmTip(armorStand, innerShoulderTarget.clone());
		Location b = getLeftArmShoulder(armorStand, false);
		Vector c = getHeldItemVector(a.clone(), innerShoulderTarget.clone(), b.clone());
		return c;
	}

	/**
	 * @param armorStand The ArmorStand from whom the item vector is
	 * @return The directional vector of the ArmorStand's right arm's held item
	 */
	public static Vector getRightHeldItemVector(ArmorStand armorStand) {
		Location innerShoulderTarget = getRightArmShoulder(armorStand);
		Location a = getRightArmTip(armorStand, innerShoulderTarget.clone());
		Location b = getRightArmShoulder(armorStand, false);
		Vector c = getHeldItemVector(a.clone(), innerShoulderTarget.clone(), b.clone());
		return c;
	}

	/**
	 * @param handOrigin          The origin of the hand/item in the hand
	 * @param innerShoulderTarget The location of the inside of the shoulder of the
	 *                            arm
	 * @param outerShoulderTarget The location of the outside of the shoulder of the
	 *                            arm
	 * @return A vector representing the direction an item in a hand should pointing
	 *         (90 degrees along the side of the arm)
	 */
	public static Vector getHeldItemVector(Location handOrigin, Location innerShoulderTarget,
			Location outerShoulderTarget) {
		Vector q = innerShoulderTarget.toVector();
		Vector z = handOrigin.toVector();
		Vector a = q.clone().subtract(z.clone());
		Vector b = outerShoulderTarget.toVector().clone().subtract(handOrigin.toVector().clone());
		Vector c = getHeldItemVector(a, b);
		return c;
	}

	/**
	 * @param innerArmVector The vector representing the direction from the inside
	 *                       of the shoulder to the hand
	 * @param outerArmVector The vector representing the direction from the inside
	 *                       of the shoulder to the outside of the shoulder
	 * @return A vector representing the direction an item in a hand should pointing
	 *         (90 degrees along the side of the arm)
	 */
	public static Vector getHeldItemVector(Vector innerArmVector, Vector outerArmVector) {
		/*
		 * ===============The theory behind getting the held item vector===============
		 * if there's a vector from location a to b (where a is the hand location and b
		 * is the shoulder location) and you copy that vector but you start at a
		 * location c (to d) that lies on the same "plane" as a-b then when you create a
		 * vector from a to c (or d) and you take the cross product of that new vector
		 * and the original vector then you get a 4th vector that's perpendicular to
		 * both the 1st and the 3rd vector and thus is the vector that points into the
		 * direction that an item the arm is holding is pointing to. and a very easy and
		 * reliable way of finding a place on the same "plane" of the original vector is
		 * using the outer side of the arm. ===========================================
		 * ====================The theory behind the cross product====================
		 * the reason behind why we want a vector that's NOT perpendicular with the
		 * original vector but IS on the sane "plane" is the theory behind the cross
		 * product which is for any 2 vectors that are on the same plane that are not
		 * parallel to eachother when taking the cross product will result in a third
		 * vector that stands perpendicular (has an angle of 90 degrees) on first and
		 * second vector and has a length the size of the area of the parallellogram
		 * that's created by the first and second vector (see
		 * https://en.wikipedia.org/wiki/Cross_product#targetText=The%20cross%20product%
		 * 20a%20×,parallelogram%20that%20the%20vectors%20span.)
		 */
		Vector a = innerArmVector.getCrossProduct(outerArmVector);
		return a;
	}

	/*
	 * The following methods are built on the solution provided in this thread
	 * https://www.spigotmc.org/threads/how-to-calculate-armorstand-arm-tip-location
	 * .331825/
	 */

	/**
	 * @param armorStand The ArmorStand of whom we want its right arm's shoulder
	 * @return The location of the ArmorStand's right arm's shoulder
	 */
	public static Location getRightArmShoulder(ArmorStand armorStand) {
		return getRightArmShoulder(armorStand.getLocation().clone());
	}

	/**
	 * @param armorStandLocation The location of an ArmorStand
	 * @return The location of the ArmorStand's right arm's shoulder
	 */
	public static Location getRightArmShoulder(Location armorStandLocation) {
		return getRightArmShoulder(armorStandLocation, true);
	}

	/**
	 * @param armorStand The ArmorStand of whom we want its right arm's shoulder
	 * @param insideArm  Whether to get the inside of the arm or the outside
	 * @return The location of the ArmorStand's right arm's shoulder
	 */
	public static Location getRightArmShoulder(ArmorStand armorStand, boolean insideArm) {
		return getRightArmShoulder(armorStand.getLocation().clone(), insideArm);
	}

	/**
	 * @param armorStandLocation The location of an ArmorStand
	 * @param insideArm          Whether to get the inside of the arm or the outside
	 * @return The location of the ArmorStand's right arm's shoulder
	 */
	public static Location getRightArmShoulder(Location armorStandLocation, boolean insideArm) {
		// Gets shoulder location
		armorStandLocation.setYaw(armorStandLocation.getYaw() + 90f); // turning right 90 degrees
		Vector dir = armorStandLocation.getDirection();
		float pixels = 5f;
		if (!insideArm)
			pixels = 7f;
		armorStandLocation.setX(armorStandLocation.getX() + pixels / 16f * dir.getX());
		armorStandLocation.setY(armorStandLocation.getY() + 22f / 16f);
		armorStandLocation.setZ(armorStandLocation.getZ() + pixels / 16f * dir.getZ());
		return armorStandLocation;
	}

	/**
	 * @param armorStand The ArmorStand of whom we want its right arm's tip
	 * @return The location of the ArmorStand's right arm's tip
	 */
	public static Location getRightArmTip(ArmorStand armorStand) {
		return getRightArmTip(armorStand, getRightArmShoulder(armorStand));
	}

	/**
	 * @param armorStand    The ArmorStand of whom we want its right arm's tip
	 * @param rightShoulder The location of the ArmorStand's right shoulder
	 * @return The location of the ArmorStand's right arm's tip
	 */
	public static Location getRightArmTip(ArmorStand armorStand, Location rightShoulder) {
		return getRightArmTip(armorStand.getRightArmPose(), rightShoulder);
	}

	/**
	 * @param armEulerAngle The angle at which the ArmorStand's right arm is pointed
	 * @param rightShoulder The location of the ArmorStand's right shoulder
	 * @return The location of the ArmorStand's right arm's tip
	 */
	public static Location getRightArmTip(EulerAngle armEulerAngle, Location rightShoulder) {
		// Get Hand Location
		Vector armDir = getDirection(armEulerAngle.getY(), armEulerAngle.getX(), -armEulerAngle.getZ());
		// correcting for the yaw being turned originally?
		armDir = rotateAroundAxisY(armDir, Math.toRadians(rightShoulder.getYaw() - 90f));
		// going to the end of the arm (the arm being 10 pixels long)
		rightShoulder.setX(rightShoulder.getX() + 10f / 16f * armDir.getX());
		rightShoulder.setY(rightShoulder.getY() + 10f / 16f * armDir.getY());
		rightShoulder.setZ(rightShoulder.getZ() + 10f / 16f * armDir.getZ());

		return rightShoulder;
	}

	/**
	 * @param armorStand The ArmorStand of whom we want its left arm's shoulder
	 * @return The location of the ArmorStand's left arm's shoulder
	 */
	public static Location getLeftArmShoulder(ArmorStand armorStand) {
		return getLeftArmShoulder(armorStand.getLocation().clone());
	}

	/**
	 * @param armorStandLocation The location of an ArmorStand
	 * @return The location of the ArmorStand's left arm's shoulder
	 */
	public static Location getLeftArmShoulder(Location armorStandLocation) {
		return getLeftArmShoulder(armorStandLocation, true);
	}

	/**
	 * @param armorStand The ArmorStand of whom we want its left arm's shoulder
	 * @param insideArm  Whether to get the inside of the arm or the outside
	 * @return The location of the ArmorStand's left arm's shoulder
	 */
	public static Location getLeftArmShoulder(ArmorStand armorStand, boolean insideArm) {
		return getLeftArmShoulder(armorStand.getLocation().clone(), insideArm);
	}

	/**
	 * @param armorStandLocation The location of an ArmorStand
	 * @param insideArm          Whether to get the inside of the arm or the outside
	 * @return The location of the ArmorStand's left arm's shoulder
	 */
	public static Location getLeftArmShoulder(Location armorStandLocation, boolean insideArm) {
		// Gets shoulder location
		armorStandLocation.setYaw(armorStandLocation.getYaw() - 90f); // turning left 90 degrees
		Vector dir = armorStandLocation.getDirection();
		float pixels = 5f;
		if (!insideArm)
			pixels = 7f;
		armorStandLocation.setX(armorStandLocation.getX() + pixels / 16f * dir.getX());
		armorStandLocation.setY(armorStandLocation.getY() + 22f / 16f);
		armorStandLocation.setZ(armorStandLocation.getZ() + pixels / 16f * dir.getZ());
		return armorStandLocation;
	}

	/**
	 * @param armorStand The ArmorStand of whom we want its left arm's tip
	 * @return The location of the ArmorStand's left arm's tip
	 */
	public static Location getLeftArmTip(ArmorStand armorStand) {
		return getLeftArmTip(armorStand, getLeftArmShoulder(armorStand));
	}

	/**
	 * @param armorStand   The ArmorStand of whom we want its left arm's tip
	 * @param leftShoulder The location of the ArmorStand's left shoulder
	 * @return The location of the ArmorStand's left arm's tip
	 */
	public static Location getLeftArmTip(ArmorStand armorStand, Location leftShoulder) {
		return getLeftArmTip(armorStand.getLeftArmPose(), leftShoulder);
	}

	/**
	 * @param armEulerAngle The angle at which the ArmorStand's left arm is pointed
	 * @param leftShoulder  The location of the ArmorStand's left shoulder
	 * @return The location of the ArmorStand's left arm's tip
	 */
	public static Location getLeftArmTip(EulerAngle armEulerAngle, Location leftShoulder) {
		// Get Hand Location
		Vector armDir = getDirection(armEulerAngle.getY(), armEulerAngle.getX(), -armEulerAngle.getZ());
		// correcting for the yaw being turned originally?
		armDir = rotateAroundAxisY(armDir, Math.toRadians(leftShoulder.getYaw() + 90f));
		// going to the end of the arm (the arm being 10 pixels long)
		leftShoulder.setX(leftShoulder.getX() + 10f / 16f * armDir.getX());
		leftShoulder.setY(leftShoulder.getY() + 10f / 16f * armDir.getY());
		leftShoulder.setZ(leftShoulder.getZ() + 10f / 16f * armDir.getZ());

		return leftShoulder;
	}

	/**
	 * @param yaw
	 * @param pitch
	 * @param roll
	 * @return The vector made of the yaw, pitch, and roll parameters
	 */
	public static Vector getDirection(Double yaw, Double pitch, Double roll) {
		return getDirection(new Vector(0, -1, 0), yaw, pitch, roll);
	}

	/**
	 * @param v     The Vector around which to base the changes off
	 * @param yaw
	 * @param pitch
	 * @param roll
	 * @return The vector made of the v, yaw, pitch, and roll parameters
	 */
	public static Vector getDirection(Vector v, Double yaw, Double pitch, Double roll) {
		v = rotateAroundAxisX(v, pitch);
		v = rotateAroundAxisY(v, yaw);
		v = rotateAroundAxisZ(v, roll);
		return v;
	}

	/**
	 * @param v     The Vector around which to base the changes off
	 * @param angle The amount to rotate the X axis on
	 * @return The rotated Vector
	 */
	public static Vector rotateAroundAxisX(Vector v, double angle) {
		double y, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		y = v.getY() * cos - v.getZ() * sin;
		z = v.getY() * sin + v.getZ() * cos;
		return v.setY(y).setZ(z);
	}

	/**
	 * @param v     The Vector around which to base the changes off
	 * @param angle The amount to rotate the Y axis on
	 * @return The rotated Vector
	 */
	public static Vector rotateAroundAxisY(Vector v, double angle) {
		angle = -angle;
		double x, z, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos + v.getZ() * sin;
		z = v.getX() * -sin + v.getZ() * cos;
		return v.setX(x).setZ(z);
	}

	/**
	 * @param v     The Vector around which to base the changes off
	 * @param angle The amount to rotate the Z axis on
	 * @return The rotated Vector
	 */
	public static Vector rotateAroundAxisZ(Vector v, double angle) {
		double x, y, cos, sin;
		cos = Math.cos(angle);
		sin = Math.sin(angle);
		x = v.getX() * cos - v.getY() * sin;
		y = v.getX() * sin + v.getY() * cos;
		return v.setX(x).setY(y);
	}
}