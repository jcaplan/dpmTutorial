package dpm.tutorial;


public class NavRunnable implements Runnable{

	final static int SLEEP_TIME = 50;
	
	enum State {
		INIT, TURNING, TRAVELLING, EMERGENCY, DONE
	};

	State state;

	
	double destx, desty;
	Navigator nav;
	public NavRunnable(double x, double y, Navigator nav) {
		this.nav = nav;
		this.destx = x;
		this.desty = y;
	}

	public void run() {
		ObstacleAvoidance avoidance = null;
		boolean done = false;
		changeState(State.INIT);
		while (true) {
			switch (state) {
			case INIT:
				if (nav.isTravelling()) {
					log(String.format("Destination [%f,%f] requested",
									destx,desty));
					changeState(State.TURNING);
				}
				break;
			case TURNING:
				/*
				 * Note: you could probably use the original turnTo()
				 * from BasicNavigator here without doing any damage.
				 * It's cheating the idea of "regular and periodic" a bit
				 * but if you're sure you never need to interrupt a turn there's
				 * no harm.
				 * 
				 * However, this implementation would be necessary if you would like
				 * to stop a turn in the middle (e.g. if you were travelling but also
				 * scanning with a sensor for something...)
				 * 
				 */
				double destAngle = nav.getDestAngle(destx, desty);
				nav.turnTo(destAngle);
				if(nav.facingDest(destAngle)){
					nav.setSpeeds(0,0);
					changeState(State.TRAVELLING, nav.getOdometer().getPositionString());
				}
				break;
			case TRAVELLING:
				if (checkEmergency()) { // order matters!
					changeState(State.EMERGENCY);
					avoidance = new ObstacleAvoidance(nav);
					avoidance.start();
				} else if (!nav.checkIfDone(destx, desty)) {
					nav.updateTravel();
				} else { // Arrived!
					nav.setSpeeds(0, 0);
					log(String.format("Distance from destination: [%f,%f]",
							Math.abs(destx - nav.getOdometer().getX()),
							Math.abs(desty - nav.getOdometer().getY())));
					changeState(State.DONE, 
							nav.getOdometer().getPositionString());
				}
				break;
			case EMERGENCY:
				if (avoidance.resolved()) {
					changeState(State.TURNING);
				}
				break;
			case DONE:
				done = true;
				break;
			}
			if(done){
				break;
			} else {
				try {
					Thread.sleep(SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void changeState(State state) {
		changeState(state, "");
	}

	
	private void changeState(State state, String message) {

		this.state = state;
		log("state: " + state + ", " + message);		
	}

	private boolean checkEmergency() {
		return nav.getUsSensor().getDistance() < 10;
	}
	
	private void log(String message) {
		Log.log(NavRunnable.class, message);
		
	}
}
