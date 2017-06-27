

import java.awt.Color;
import com.leapmotion.leap.*;
import se.bitcraze.crazyflie.lib.crtp.CommanderPacket;
import se.bitcraze.crazyflie.lib.crazyflie.ConnectionAdapter;
import se.bitcraze.crazyflie.lib.crazyflie.Crazyflie;
import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;

/**
 * This is the class were the processing of the Leap data occurs 
 * and where this processed data is sent to the Crazyflie.
 * @author Achilleas
 *
 */
public class LeapControl extends Listener {

	private final int SENSITIVITY = 50;
	private Crazyflie crazyflie;
	private Controller controller;
	private LeapFlieUI ui;
	public RadioDriver radioDriver;
	final int HANDPOSITION_MIN = 60; // the minimum distance from the Leap
	final int HANDPOSITION_MAX = 500; // the maximum distance from the Leap
	final int THRUST_MIN = 15000; // the minimum thrust of the Crazyflie
	final int THRUST_MAX = 50000; // the maximum thrust of the Crazyflie //try
									// setting this to 30000 for testing
	public long thrust = 0;
	public double pitch = 0;
	public float roll = 0;
	public float yawrate = 0;
	public float prevRoll = 0;
	public double prevPitch = 0;
	public float smoothedRoll = 0;
	public double smoothedPitch = 0;
	public boolean isConnected = false;

	/**
	 * The consructor of the LeapControl class. This is where the Crazylie object is created
	 * and a connection with the selected channel is established via the Crazyradio.
	 * @param connectiondata
	 * @param radio
	 */
	public LeapControl(ConnectionData connectiondata, RadioDriver radio) {

		radioDriver = radio;
		crazyflie = new Crazyflie(radio);
		
		radioDriver.addConnectionListener(new ConnectionAdapter() {

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
	}
	/**
	 * Gets an instance of the LeapFlieUi class so that the manipulation of certain elements in that class becomes possible.
	 * @param ui
	 */
	public void setUI(LeapFlieUI ui) {
		this.ui = ui;
	}

	/**
	 * Returns the current instance of the Crazyflie.
	 * @return
	 */
	public Crazyflie getCrazyFlie() {
		return this.crazyflie;
	}
	
	/**
	 * Connects to the Crazyflie and to the Leap Motion controller.
	 * @param data
	 */
	public void connectToCrazyflie(ConnectionData data) {
		this.crazyflie.connect(data);
		controller = new Controller();
		controller.addListener(this);
	}
	
	/**
	 * Disconnects from the Crazyflie and from the Leap Motion controller.
	 */
	public void disconnectFromCrazyflie() {
		this.crazyflie.sendPacket(new CommanderPacket(0, (float) 0, 0, (char) 0));
		this.crazyflie.disconnect();
		controller.removeListener(this);
	}

	/**
	 * This method is called when the Leap Motion is connected and it sets the led in the LeapFlieUI class to green.
	 * @param controller
	 */
	public void onConnect(Controller controller) {
		System.out.println("Leap Connected");
		ui.lblLed.setForeground(Color.green);
	}

	/**
	 * This method is called when the Leap Motion is disconnected and it sets the led in the LeapFlieUI class to red.
	 * @param controller
	 */
	public void onExit(Controller controller) {
		System.out.println("Exited");
		ui.lblLed.setForeground(Color.red);
	}

	/**
	 * This method is called every time there is a new frame of hand tracking data available from the Leap Motion.
	 * This is were the Leap data is processed and then sent to the Crazyflie.
	 * @param controller
	 */
	public void onFrame(Controller controller) {		
		thrust = 0;
		roll = 0;
		pitch = 0;
		yawrate = 0;
		
		Frame frame = controller.frame();	//get current frame
		Hand hand = frame.hands().frontmost();//get the first hand detected by the Leap
		FingerList fingers = frame.fingers();//get the list of fingers detected
		this.crazyflie.sendPacket(new CommanderPacket(0, (float) 0, 0, (char) 0));//send 0 values to the Crazyflie to arm the copter.
		ui.updateTextField(thrust, roll, pitch, yawrate);//update the text fields in the LeapFlie UI

		//If the Leap detects 5 fingers then it proceeds to get the Leap data, process it and send it to the Crazyflie
		if (fingers.extended().count() == 5) {
			
			// Gets the position, roll, pitch, yaw of the hand at each frame
			float handPosition = hand.stabilizedPalmPosition().get(1);
			float handRoll = hand.palmNormal().roll();
			float handPitch = hand.direction().pitch();
			float handYaw = hand.direction().yaw();

			//Converts the Leap data to appropriate Crazyflie data
			thrust = (long) ((handPosition - HANDPOSITION_MIN) / (HANDPOSITION_MAX - HANDPOSITION_MIN)
					* (THRUST_MAX - THRUST_MIN) + THRUST_MIN);
			roll = (float)((-1)*handRoll*SENSITIVITY);
			pitch = (float)((-1)*handPitch*SENSITIVITY);
			yawrate = (float)(handYaw*SENSITIVITY);
			
			//Sends the converted data to the Crazyflie
			this.crazyflie.sendPacket(new CommanderPacket(roll, (float) pitch, yawrate, (char) thrust));
			//Updates the appropriate text fiels in the LeapFlieUI class
			ui.updateTextField(thrust, roll, pitch, yawrate);
			try {
				Thread.sleep(200);
			} catch (InterruptedException i) {
				i.printStackTrace();
			}
		}
		//sends 0 values to the Crazyflie
		this.crazyflie.sendPacket(new CommanderPacket(0, 0, 0, (char) 0));
	}

}
