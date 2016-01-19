package dpm.tutorial;


import java.io.FileNotFoundException;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.SensorPort;
import lejos.hardware.sensor.EV3UltrasonicSensor;

public class Main {
	
	// Static Resources:
	// Left motor connected to output A
	// Right motor connected to output D
	public static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	public static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	public static final EV3UltrasonicSensor usSensor = new EV3UltrasonicSensor(SensorPort.S1);
	
	
	// Constants
	public static final double WHEEL_RADIUS = 2.130;
	public static final double TRACK = 15;
	private static Navigator nav;
	private static Odometer odometer;
	
	public static void main(String[] args) throws FileNotFoundException {
	
		Log.addClass(Navigator.class, "NAV");
		Log.addClass(NavRunnable.class,"NAVR");
//		Log.addClass(ObstacleAvoidance.class, "OA");
//		Log.addClass(Odometer.class, "ODO");
//		Log.addClass(UltrasonicPoller.class, "US");
//		Log.setLogWriter(System.currentTimeMillis() + ".log");
		
		// some objects that need to be instantiated	
		odometer = new Odometer(leftMotor, rightMotor);
		odometer.start();
		UltrasonicPoller usPoller = new UltrasonicPoller(usSensor);
		usPoller.start();
		
		nav = new Navigator(odometer,usPoller);
		
		
		completeCourse();
		
		System.exit(0);
	}

	private static void completeCourse() {
		
		int[][] waypoints = {{60,30},{30,30},{30,60},{60,0}};
		
		for(int[] point : waypoints){
			nav.travelTo(point[0],point[1],true);
		}
	}
	
}