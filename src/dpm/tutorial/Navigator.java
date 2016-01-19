package dpm.tutorial;

/*
 * 
 * The Navigator class extends the functionality of the Navigation class.
 * It offers an alternative travelTo() method which uses a state machine
 * to implement obstacle avoidance.
 * 
 * The Navigator class does not override any of the methods in Navigation.
 * All methods with the same name are overloaded i.e. the Navigator version
 * takes different parameters than the Navigation version.
 * 
 * This is useful if, for instance, you want to force travel without obstacle
 * detection over small distances. One place where you might want to do this
 * is in the ObstacleAvoidance class. Another place is methods that implement 
 * specific features for future milestones such as retrieving an object.
 * 
 * 
 */

public class Navigator extends BasicNavigator {


	private boolean isNavigating = false;

	private double destx, desty;


	UltrasonicPoller usSensor;

	public Navigator(Odometer odo, UltrasonicPoller usSensor) {
		super(odo);
		this.usSensor = usSensor;
	}

	/*
	 * TravelTo function which takes as arguments the x and y position in cm
	 * Will travel to designated position, while constantly updating it's
	 * heading
	 * 
	 * When avoid=true, the nav thread will handle traveling. If you want to
	 * travel without avoidance, this is also possible. In this case,
	 * the method in the Navigation class is used.
	 * 
	 */
	public void travelTo(double x, double y, boolean avoid) {
		
		if (avoid) {

			destx = x;
			desty = y;
			isNavigating = true;
			Thread thread = new Thread(new NavRunnable(x,y,this));
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			isNavigating = false;
		} else {
			isNavigating = true;
			super.travelTo(x, y);
			isNavigating = false;
		}
	}

	
	/*
	 * Updates the h
	 */
	void updateTravel() {
		double minAng;

		minAng = getDestAngle(destx, desty);
		/*
		 * Use the BasicNavigator turnTo here because 
		 * minAng is going to be very small so just complete
		 * the turn.
		 */
		super.turnTo(minAng,false);
		this.setSpeeds(FAST, FAST);
	}

	private void log(String message) {
		Log.log(Navigator.class, message);
		
	}

	void turnTo(double angle) {
		double error;
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
	 * Go foward a set distance in cm with or without avoidance
	 */
	public void goForward(double distance, boolean avoid) {
		double x = odometer.getX()
				+ Math.cos(Math.toRadians(this.odometer.getAng())) * distance;
		double y = odometer.getY()
				+ Math.sin(Math.toRadians(this.odometer.getAng())) * distance;

		this.travelTo(x, y, avoid);

	}

	public boolean isTravelling() {
		return isNavigating;
	}

	public UltrasonicPoller getUsSensor() {
		return usSensor;
	}

	public Odometer getOdometer() {
		return odometer;
	}

}
