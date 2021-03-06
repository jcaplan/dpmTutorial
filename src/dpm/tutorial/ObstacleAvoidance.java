package dpm.tutorial;

public class ObstacleAvoidance extends Thread{

	Navigator nav;
	boolean safe;
	
	public ObstacleAvoidance(Navigator nav){
		this.nav = nav;
		safe = false;
	}
	
	
	public void run(){
		
		/*
		 * The "avoidance" just stops and turns to heading 0
		 * to make sure that the threads are working properly.
		 * 
		 * If you want to call travelTo from this class you
		 * MUST call travelTo(x,y,false) to go around the
		 * state machine
		 * 
		 * This means that you can't detect a new obstacle
		 * while avoiding the first one. That's probably not something
		 * you were going to do anyway.
		 * 
		 * Otherwise things get complicated and a lot of 
		 * new states will be necessary.
		 * 
		 */
		log("avoiding obstacle!");
		
		nav.setSpeeds(0, 0);
		nav.turnTo(0,true);
		nav.goForward(5, false); //using false means the Navigation method is used
		log("obstacle avoided!");
		safe = true;
	}


	public boolean resolved() {
		return safe;
	}
	
	private void log(String message){
		Log.log(ObstacleAvoidance.class,message);
		
	}
}
