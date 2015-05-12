package yttrium;

import robocode.AdvancedRobot;
import robocode.DeathEvent;
import robocode.ScannedRobotEvent;
import robocode.util.Utils;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.lang.Math;

public class Deconstructor extends AdvancedRobot
{
    private static final double RADIANS_5 = Math.toRadians(5);
    private final Enemy enemy = new Enemy();
    private boolean isAlive = true;
    private boolean shouldFire = false;

    public void run() {
        setColors(Color.black, Color.black, Color.black);
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

        while (isAlive) {
            if (enemy.spotted) {
                if (shouldFire) {
                    setFire(2);
                    shouldFire = false;
                }
                final double radarTurn = getRadarTurn();
                setTurnRadarRightRadians(radarTurn);

                final double bodyTurn = getBodyTurn();
                setTurnRightRadians(bodyTurn);
                if (getDistanceRemaining() == 0) {
                    final double distance = getDistance();
                    setAhead(distance);
                }

                final double gunTurn = getGunTurn(2);
                setTurnGunRightRadians(gunTurn);
                shouldFire = true;
            }
            execute();
        }
    }

    private double getRadarTurn() {
        final double alphaToEnemy = angleTo(getX(), getY(), enemy.x, enemy.y);
        final double sign = (alphaToEnemy != getRadarHeadingRadians()) ?
                Math.signum(Utils.normalRelativeAngle(alphaToEnemy - getRadarHeadingRadians())) : 1;
        return Utils.normalRelativeAngle(alphaToEnemy - getRadarHeadingRadians() + RADIANS_5 * sign);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        enemy.spotted = true;
        enemy.distance = e.getDistance();
        enemy.energy = e.getEnergy();
        enemy.name = e.getName();
        enemy.velocity = e.getVelocity();
        enemy.bearing = e.getBearingRadians();
        enemy.heading = e.getHeadingRadians();
        final double alphaToEnemy = getHeadingRadians() + enemy.bearing;
        enemy.x = getX() + Math.sin(alphaToEnemy) * enemy.distance;
        enemy.y = getY() + Math.cos(alphaToEnemy) * enemy.distance;
    }

    public void onDeath(DeathEvent e) {
        isAlive = false;
    }

    private static double angleTo(double baseX, double baseY, double x, double y) {
        double theta = Math.asin((y - baseY) / Point2D.distance(x, y, baseX, baseY)) - Math.PI / 2;
        if (x >= baseX && theta < 0) {
            theta = -theta;
        }
        return (theta %= Math.PI * 2) >= 0 ? theta : (theta + Math.PI * 2);
    }

    private double getDistance() {
        return 200 - 400 * Math.random();
    }

    private double getBodyTurn() {
        final double alphaToMe = angleTo(enemy.x, enemy.y, getX(), getY());
        final double lateralDirection = Math.signum((getVelocity() != 0 ? getVelocity() : 1) *
                Math.sin(Utils.normalRelativeAngle(getHeadingRadians() - alphaToMe)));
        final double desiredHeading = Utils.normalAbsoluteAngle(alphaToMe + Math.PI / 2 * lateralDirection);
        final double normalHeading = getVelocity() >= 0 ?
                getHeadingRadians() : Utils.normalAbsoluteAngle(getHeadingRadians() + Math.PI);
        return Utils.normalRelativeAngle(desiredHeading - normalHeading);
    }

    private double getGunTurn(double firePower) {
        final double ticks = enemy.distance / (20 - (3 * firePower));
        double futureX = enemy.x + Math.sin(enemy.heading) * enemy.velocity * ticks;
        double futureY = enemy.y + Math.cos(enemy.heading) * enemy.velocity * ticks;
        return Utils.normalRelativeAngle(angleTo(getX(), getY(), futureX, futureY) - getGunHeadingRadians());
    }
}
