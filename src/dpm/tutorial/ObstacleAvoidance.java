package dpm.tutorial;

public class ObstacleAvoidance extends Thread{

	Navigation nav;
	boolean safe;
	
	public ObstacleAvoidance(Navigation nav){
		this.nav = nav;
		safe = false;
	}
	
	
	public void run(){
		
		/*
		 * The "avoidance" just stops and turns to heading 0
		 * to make sure that the threads are working properly.
		 * 
		 * If you want to call travelTo from this class you're going to
		 * probably want to call travelTo(x,y,false)
		 * 
		 * Otherwise nav.goForward() may be easier.
		 */
		
		Log.log(Log.Sender.avoidance,"avoiding obstacle!");
		nav.setSpeeds(0, 0);
		nav.turnTo(0,true);
		
		Log.log(Log.Sender.avoidance,"obstacle avoided!");
		safe = true;
	}


	public boolean resolved() {
		return safe;
	}
}
