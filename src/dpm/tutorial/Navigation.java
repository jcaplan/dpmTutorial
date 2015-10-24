package dpm.tutorial;

/*
 * File: Navigation.java
 * Written by: Sean Lawlor
 * ECSE 211 - Design Principles and Methods, Head TA
 * Fall 2011
 * Ported to EV3 by: Francois Ouellet Delorme
 * Fall 2015
 * 
 * Movement control class (turnTo, travelTo, flt, localize)
 */
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {

	enum State {
		INIT, TURNING, TRAVELLING, EMERGENCY
	};
	State state;

	private boolean isNavigating = false;

	private double destx, desty, destAngle;

	final static int FAST = 200, SLOW = 100, ACCELERATION = 4000;
	final static double DEG_ERR = 3.0, CM_ERR = 1.0;
	final static int SLEEP_TIME = 50;
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	UltrasonicPoller usSensor;

	public Navigation(Odometer odo, UltrasonicPoller usSensor) {
		this.odometer = odo;
		this.usSensor = usSensor;
		EV3LargeRegulatedMotor[] motors = this.odometer.getMotors();
		this.leftMotor = motors[0];
		this.rightMotor = motors[1];

		// set acceleration
		this.leftMotor.setAcceleration(ACCELERATION);
		this.rightMotor.setAcceleration(ACCELERATION);
	}

	/*
	 * Functions to set the motor speeds jointly
	 */
	public void setSpeeds(float lSpd, float rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	public void setSpeeds(int lSpd, int rSpd) {
		this.leftMotor.setSpeed(lSpd);
		this.rightMotor.setSpeed(rSpd);
		if (lSpd < 0)
			this.leftMotor.backward();
		else
			this.leftMotor.forward();
		if (rSpd < 0)
			this.rightMotor.backward();
		else
			this.rightMotor.forward();
	}

	/*
	 * Float the two motors jointly
	 */
	public void setFloat() {
		this.leftMotor.stop();
		this.rightMotor.stop();
		this.leftMotor.flt(true);
		this.rightMotor.flt(true);
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm
	 * Will travel to designated position, while constantly updating it's
	 * heading
	 * 
	 * Should almost always use avoid = true
	 * 
	 * If necessary in the ObstacleAvoidance class, call travelTo(x,y,false) 
	 * for small distances known to have no obstacles.
	 */

	public void travelTo(double x, double y, boolean avoid) throws Exception {
		destx = x;
		desty = y;
		if (avoid) {
			travelTo(x, y);
		} else {
			if(state != State.EMERGENCY){
				throw new Exception("ERROR: Only allowed to call travelTo(x,y,false) when in"
						+ "EMERGENCY state!");
			}
			double minAng;
			while (!checkIfDone()) {
				minAng = getDestAngle();
				turnTo(minAng, false);
				setSpeeds(FAST, FAST);
			}
			setSpeeds(0, 0);
		}

	}

	private void travelTo(double x, double y) {
		destAngle = getDestAngle();
		isNavigating = true;
	}

	public void updateTravel() {
		double minAng;

		minAng = getDestAngle();
		turnTo(minAng);
		this.setSpeeds(FAST, FAST);
	}

	public void run() {
		ObstacleAvoidance avoidance = null;
		state = State.INIT;
		while (true) {
			switch (state) {
			case INIT:
				if (isNavigating) {
					state = State.TURNING;
				}
				break;
			case TURNING:
				turnTo(destAngle);
				if (facingDest(destAngle)) {
					setSpeeds(0, 0);
					state = State.TRAVELLING;
				}
				break;
			case TRAVELLING:
				if (checkEmergency()) { // order matters!
					state = State.EMERGENCY;
					avoidance = new ObstacleAvoidance(this);
					avoidance.start();
				} else if (!checkIfDone()) {
					updateTravel();
				} else { // Arrived!
					setSpeeds(0, 0);
					isNavigating = false;
					state = State.INIT;
				}
				break;
			case EMERGENCY:
				if (avoidance.resolved()) {
					state = State.TURNING;
				}
				break;
			}
			Log.log(Log.Sender.Navigator, "state: " + state);
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean checkEmergency() {
		return usSensor.getDistance() < 10;
	}

	private boolean checkIfDone() {
		return Math.abs(destx - odometer.getX()) < CM_ERR
				&& Math.abs(desty - odometer.getY()) < CM_ERR;
	}

	private boolean facingDest(double angle) {
		return Math.abs(angle - odometer.getAng()) < DEG_ERR;
	}

	private double getDestAngle() {
		double minAng = (Math.atan2(desty - odometer.getY(),
				destx - odometer.getX()))
				* (180.0 / Math.PI);
		if (minAng < 0) {
			minAng += 360.0;
		}
		return minAng;
	}

	/*
	 * this method returns even if turn is not complete. Only use from
	 * travelTo().
	 */
	private void turnTo(double angle) {

		double error = angle - this.odometer.getAng();

		error = angle - this.odometer.getAng();

		if (error < -180.0) {
			this.setSpeeds(-SLOW, SLOW);
		} else if (error < 0.0) {
			this.setSpeeds(SLOW, -SLOW);
		} else if (error > 180.0) {
			this.setSpeeds(SLOW, -SLOW);
		} else {
			this.setSpeeds(-SLOW, SLOW);
		}

	}

	/*
	 * This method only returns after turn is complete
	 */
	public void turnTo(double angle, boolean stop) {

		double error = angle - this.odometer.getAng();

		while (Math.abs(error) > DEG_ERR) {

			error = angle - this.odometer.getAng();

			if (error < -180.0) {
				this.setSpeeds(-SLOW, SLOW);
			} else if (error < 0.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else if (error > 180.0) {
				this.setSpeeds(SLOW, -SLOW);
			} else {
				this.setSpeeds(-SLOW, SLOW);
			}
		}

		if (stop) {
			this.setSpeeds(0, 0);
		}
	}

	/*
	 * Go foward a set distance in cm
	 */
	public void goForward(double distance) {
		this.travelTo(
				odometer.getX()
						+ Math.cos(Math.toRadians(this.odometer.getAng()))
						* distance,
				odometer.getY()
						+ Math.sin(Math.toRadians(this.odometer.getAng()))
						* distance);

	}

	public boolean isTravelling() {
		return isNavigating;
	}

}
