package dpm.tutorial;

import lejos.hardware.Button;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class PauseThread extends Thread{
	
	int lSpeed;
	int rSpeed;
	EV3LargeRegulatedMotor leftMotor;
	EV3LargeRegulatedMotor rightMotor;
	public PauseThread(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor, int speed, int speed2) {
		lSpeed = speed;
		rSpeed = speed2;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}

	public void run(){
		System.out.println("pause thread");
		leftMotor.stop(); 
		rightMotor.stop();
		while (Button.readButtons() == 0);
		leftMotor.setSpeed(lSpeed);
		rightMotor.setSpeed(rSpeed);
	}

}
