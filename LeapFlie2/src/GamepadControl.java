

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.java.games.input.Component;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

/**
 * This is the GamepadControl thread where the processing of the controller's data takes place.
 * That data is then sent to the Crazyflie.
 * @author Achilleas
 *
 */
public class GamepadControl implements Runnable {
	private Crazyflie crazyflie;
	private float value;
	private long thrust;
	private float yaw;
	private double pitch;
	private float roll;
	private float maxThrust = 40000;
	private float maxYaw = 200;
	private float maxPitch = 30;
	private float maxRoll = 30;
	private LeapFlieUI ui;
	private Controller controller;
	protected Thread poll;
	public volatile boolean isConnected = true;

	/**
	 * The GamepadControl constructor. Sets up a connection with the Crazyflie and searches for controllers
	 * @param data
	 * @param driver
	 */
	public GamepadControl(ConnectionData data, RadioDriver driver) {
		//Create Crazyflie object with the current RadioDriver instance
		crazyflie = new Crazyflie(driver);
		//listen for connections with the Crazyflie
		driver.addConnectionListener(new ConnectionAdapter() {

			public void connected(String connectionInfo) {

				System.out.println("Connected to:" + connectionInfo);

			}

			public void disconnected(String connectionInfo) {

				System.out.println("Disconnected from:" + connectionInfo);
			}

			public void connectionFailed(String connectionInfo, String msg) {

				System.out.println("Connection Failed: " + connectionInfo + " Message: " + msg);
			}

			public void ConnectionLost(String connectionInfo) {

				System.out.println("Connection lost:" + connectionInfo);
			}
		});
		//Connect to the crazyflie with the currently selected ConnectionData
		crazyflie.connect(data);

		//Create a list to store the controllers that are connected
		ArrayList<Controller> gamepads = new ArrayList<Controller>();
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		
		//Loop through all controllers and only add the ones of type STICK to the gamepads list
		for (int i = 0; i < controllers.length; i++) {
			if (controllers[i].getType().equals(Controller.Type.STICK)) {
				gamepads.add(controllers[i]);
			}
		}
		//get the first controller from the list
		controller = gamepads.get(0);
	}

	//This method is called when the thread is started
	public void run() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				//Poll the current controller and get the event that occured
				controller.poll();
				EventQueue queue = controller.getEventQueue();
				Event event = new Event();
				crazyflie.sendPacket(new CommanderPacket(roll, (float) pitch, yaw, (char) thrust));
				ui.updateTextField(thrust, roll, pitch, yaw);
				while (queue.getNextEvent(event)) {
					//get the component of the current event (e.g. analog sticks, buttons etc)
					Component comp = event.getComponent();
					//Get the value of the event
					value = event.getValue();

					if (comp.isAnalog()) {
						
						if ((comp.getName().equals("Y Axis")) && (value <= 0)) {
							//if left analog stick is moved along the y axis gives thrust to the Crazyflie
							thrust = (long) ((-1) * value * maxThrust);
						} else if (comp.getName().equals("X Axis")) {
							//if left analog stick is moved along the x axis it gives yaw to the Crazyflie
							yaw = value * maxYaw;
						} else if (comp.getName().equals("Z Axis")) {
							//if the right analog stick is moved along the x axis it gives roll to the Crazyflie
							roll = value * maxRoll;
						} else if (comp.getName().equals("Z Rotation")) {
							//if the right analog stick is moved along the y axis it gives pitch to the Crazyflie
							pitch = (-1) * value * maxPitch;
						}
						//Send values to Crazyflie
						crazyflie.sendPacket(new CommanderPacket(roll, (float) pitch, yaw, (char) thrust));
						//update the appropriate text fields in the LeapFlieUI class
						ui.updateTextField(thrust, roll, pitch, yaw);

					} else {
						if (value == 1.0f) {
							crazyflie.disconnect();
							break;
						} else {
						}
					}
				}
				Thread.sleep(200);

			} catch (InterruptedException e) {
				// e.printStackTrace();
				System.out.println("Exiting....");
				Thread.currentThread().interrupt();
			}
		}

	}
	/**
	 * Gets the LeapFlieUI instance in order to change the text fields
	 * and sets the led label to green if a controller is found
	 * @param ui
	 */
	public void setUI(LeapFlieUI ui) {
		this.ui = ui;
		if(controller!=null){
			ui.lblLed.setForeground(Color.green);
		}
	}
}
