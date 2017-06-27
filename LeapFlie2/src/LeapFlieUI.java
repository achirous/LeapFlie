
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import se.bitcraze.crazyflie.lib.crazyradio.ConnectionData;
import se.bitcraze.crazyflie.lib.crazyradio.RadioDriver;
import se.bitcraze.crazyflie.lib.usb.UsbLinkJava;

import java.awt.Color;
import javax.swing.JComboBox;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.FormSpecs;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.Font;
import javax.swing.JRadioButton;

/**
 * The main class. Contains the GUI for the application.
 * @author Achilleas
 *
 */

public class LeapFlieUI {

	private JFrame frame;//the frame of the GUI
	private JList list;//the list where the channels are stored
	private JScrollPane listScrollPane;
	private JPanel listPanel;
	private DefaultListModel<ConnectionData> model;
	private JComboBox<String> comboBox;//the comboBox where the user can select a data rate
	private int dataRate;//the selected data rate
	public JButton btnConnect;//the Connect button
	private int counter = 0;//counts how many clicks have been performed on the Connect button
	RadioDriver radio;
	UsbLinkJava usbLink;
	ConnectionData data;
	LeapControl leapControl;
	public JTextField cfThrust;//displays the Crazyflies thrust
	public JTextField cfRoll;//displays the Crazyflies roll
	public JTextField cfPitch;//displays the Crazyflies pitch
	public JTextField cfYaw;//diplays the Crazyflies yaw
	public JLabel lblMode;//displays the mode that is selected
	//public boolean isLeapSelected = true;
	public JLabel lblLed;//the led that represents if the current input device is on or off
	public ButtonGroup btnGroup;
	public JRadioButton rdbtnLeapMotion;//the Leap Motion radio button
	public Thread gamepadCtrl;//the GamepadControl thread
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LeapFlieUI window = new LeapFlieUI();
					window.frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public LeapFlieUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame and add Listeners.
	 */
	private void initialize() {
		
		//create the frame for the GUI
		frame = new JFrame();
		frame.setBounds(100, 100, 449, 727);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle("LeapFlie");
		
		//Create scan button
		JButton btnScan = new JButton("Scan");
		btnScan.setBounds(136, 315, 107, 35);
		
		//Create label to display system status
		JLabel lblStatus = new JLabel("");
		lblStatus.setBounds(75, 90, 293, 16);
		frame.getContentPane().add(lblStatus);
		
		frame.getContentPane().add(btnScan);
		
		//create a DefaultListModel to add the ConnectionData objects
		model = new DefaultListModel<ConnectionData>();
		
		//Create the Leap motion radio button and make it the default selection
		rdbtnLeapMotion = new JRadioButton("Leap Motion");
		rdbtnLeapMotion.setBounds(54, 403, 115, 18);
		rdbtnLeapMotion.setSelected(true);
		rdbtnLeapMotion.setEnabled(false);
		frame.getContentPane().add(rdbtnLeapMotion);
		
		//Create the Gamepad radio button
		JRadioButton rdbtnGamepad = new JRadioButton("Gamepad");
		rdbtnGamepad.setBounds(54, 433, 115, 18);
		rdbtnGamepad.setEnabled(false);
		frame.getContentPane().add(rdbtnGamepad);
		
		//Add both radio buttons to a button group
		btnGroup = new ButtonGroup();
		btnGroup.add(rdbtnLeapMotion);
		btnGroup.add(rdbtnGamepad);
		
		//Create label to display "Select an input device"
		JLabel lblSelectAnInput = new JLabel("Select an input device");
		lblSelectAnInput.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblSelectAnInput.setBounds(46, 362, 197, 29);
		frame.getContentPane().add(lblSelectAnInput);
		
		//Create the combo box consisting of three items representing the available data rates
		comboBox = new JComboBox<String>();
		comboBox.setBounds(54, 321, 70, 22);
		frame.getContentPane().add(comboBox);
		comboBox.addItem("250kbs");
		comboBox.addItem("1mbs");
		comboBox.addItem("2mbs");
		comboBox.setSelectedIndex(0);
		
		//Create the Connect button
		btnConnect = new JButton("Connect");
		btnConnect.setBounds(255, 315, 107, 35);
		btnConnect.setEnabled(false);
		
		//Create list to add the channels
		list = new JList<ConnectionData>(model);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setFixedCellWidth(300);
		list.setSelectedIndex(0);
		list.setVisibleRowCount(10);
		listScrollPane = new JScrollPane(list);
		listPanel = new JPanel();
		listPanel.setBounds(6,118,432,185);
		listPanel.setLayout(new FlowLayout());
		listPanel.add(listScrollPane);
		frame.getContentPane().add(listPanel);
		frame.getContentPane().add(btnConnect);
		
		//Create the cfPanel
		JPanel cfPanel = new JPanel();
		cfPanel.setBounds(54, 504, 308, 170);
		frame.getContentPane().add(cfPanel);
		cfPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,}));
		
		//Create the Thrust, Roll, Pitch and Yaw labels and add them to the cfPanel
		JLabel lblThrust = new JLabel("Thrust:");
		cfPanel.add(lblThrust, "2, 2, right, default");
		
		JLabel lblRoll = new JLabel("Roll:");
		cfPanel.add(lblRoll, "2, 4, right, default");
		
		JLabel lblPitch = new JLabel("Pitch:");
		cfPanel.add(lblPitch, "2, 6, right, default");
		
		JLabel lblYaw = new JLabel("Yaw:");
		cfPanel.add(lblYaw, "2, 8, right, default");

		//Create the text fields to display the Thrust, Roll, Pitch and Yaw values of the Crazyflie.
		cfThrust = new JTextField("0");
		cfPanel.add(cfThrust, "4, 2, fill, default");
		cfThrust.setColumns(10);
		
		cfRoll = new JTextField("0");
		cfPanel.add(cfRoll, "4, 4, fill, default");
		cfRoll.setColumns(10);
		
		cfPitch = new JTextField("0");
		cfPanel.add(cfPitch, "4, 6, fill, default");
		cfPitch.setColumns(10);
		
		cfYaw = new JTextField("0");
		cfPanel.add(cfYaw, "4, 8, fill, default");
		cfYaw.setColumns(10);
		
		//Add label to display "Flight Data" above the cfPanel
		JLabel lblFlightData = new JLabel("Flight Data");
		lblFlightData.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblFlightData.setBounds(57, 463, 138, 29);
		frame.getContentPane().add(lblFlightData);

		//Add the label to display the selected mode and set default to "Leap Motion"
		lblMode = new JLabel("Leap Motion");
		lblMode.setBounds(285, 14, 77, 16);
		frame.getContentPane().add(lblMode);
		
		//Add led icon next to the mode label 
		lblLed = new JLabel("\u2022");
		lblLed.setBounds(272, 14, 12, 16);
		frame.getContentPane().add(lblLed);
		
		//Establish connection with the Crazyradio.
		try {
			usbLink = new UsbLinkJava();
			radio = new RadioDriver(usbLink);
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		/*Check if the Leap Motion radio button is initially selected in order to
		  create a LeapControl object by sending the selected data rate and RadioDriver object to said class
		  and send the current LeapFlieUI instance to it.
		*/
		if(rdbtnLeapMotion.isSelected()){
			leapControl = new LeapControl(data,radio);
			leapControl.setUI(LeapFlieUI.this);
		}
		
		//Add listener to Scan button
		btnScan.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e){
				lblStatus.setText("Scanning for Crazyflie...");
		    	try {
		    		//Scan for available Crazyflies
			    	List<ConnectionData> available = radio.scanInterface();
			    	if(!available.isEmpty()){
			    		//If channels are found it adds them to the list panel by calling the setJListItems method
						lblStatus.setText("Crazyflies found:");
						lblStatus.setForeground(Color.GREEN);
						model.clear();
						setJListItems(available);	
						
					}else{
						//If no channels are found display appropriate text in red
						lblStatus.setText("No Crazyflies found!!");
						lblStatus.setForeground(Color.RED);
					}
		    	} catch (IllegalArgumentException ex) {
		    		System.out.println(ex.getMessage());
		    	}
			}

			public void mouseEntered(MouseEvent arg0) {}

			public void mouseExited(MouseEvent arg0) {}

			public void mousePressed(MouseEvent arg0) {}

			public void mouseReleased(MouseEvent arg0) {}
			
		});
		
		//Add a listener to the Leap Motion radio button
		rdbtnLeapMotion.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				//if this is selected then set the model label to "Leap Motion"
				lblMode.setText("Leap Motion");
			}
			
		});
		
		//Add listener to the Gamepad radio button
		rdbtnGamepad.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				lblMode.setText("Gamepad");
				//Disconnect from the Leap Control mode if there's an open connection
				if(leapControl.getCrazyFlie().isConnected()){
					leapControl.disconnectFromCrazyflie();
				}
				//Create a GamepadControl object by sending the currently selected data rate and RadioDriver object as parameters
				GamepadControl gpdCtrl = new GamepadControl(data,radio);
				//Send the current LeapFlieUI instance to the GamepadControl class
				gpdCtrl.setUI(LeapFlieUI.this);
				//Creat a new GamepadControl thread
				gamepadCtrl = new Thread(gpdCtrl);
			}
			
		});
		
		//Add a listener to the Connect button
		btnConnect.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent arg0) {
				// Increase the counter by 1
				counter++;
				if(rdbtnLeapMotion.isSelected()){
					if(counter == 1){
						/*If the Leap Motion radio button is selected and the click count is 1 
						then set button text to "Disconnect", connect to the Leap Motion 
						and to the Crazyflie throught the LeapControl class
						*/
						btnConnect.setText("Disconnect");
						leapControl.connectToCrazyflie(data);
						
					}else{
						/*If it's the second click then set button text back to "Connect", 
						disconnect from the LeapControl class and reset counter to 0
						*/
						btnConnect.setText("Connect");
						leapControl.disconnectFromCrazyflie();
						counter = 0;
					}
				}else{
					
					if(counter == 1){
						/*If the Gamepad radio button is selected and the counter is 1
						 * then set button text to "Disconnect" 
						 */
						btnConnect.setText("Disconnect");
						if(!gamepadCtrl.isAlive()){
							//if the GamepadCotnrol thread is not alive then create GamepadControl object
							GamepadControl gpdCtrl = new GamepadControl(data,radio);
							gpdCtrl.setUI(LeapFlieUI.this);
							gamepadCtrl = new Thread(gpdCtrl);
						}
						//start the GamepadControl thread
						gamepadCtrl.start();
					}else{
						//if it's the second click then reset counter to 0 and interrupt the thread
						counter = 0;
						btnConnect.setText("Connect");
						if(gamepadCtrl.isAlive()){
							gamepadCtrl.interrupt();
						}
					}
				}
			}

			public void mouseEntered(MouseEvent arg0) {}

			public void mouseExited(MouseEvent arg0) {}

			public void mousePressed(MouseEvent arg0) {}

			public void mouseReleased(MouseEvent arg0) {}
		});
		
		//Add listener to the list items
		list.addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent event) {
				// Gets selected item and converts it to ConnectionData object
				data = (ConnectionData)((JList)event.getSource()).getSelectedValue();
				//Enables the Connect button
				btnConnect.setEnabled(true);
				//Enables the Leap Motion and Gamepad radio buttons
				rdbtnLeapMotion.setEnabled(true);
				rdbtnGamepad.setEnabled(true);
			}
			
		});
		
		
	}
	/**
	 * Dynamically updates the Thrust, Roll, Pitch and Yaw textfields in the GUI.
	 * @param thrust
	 * @param roll
	 * @param pitch
	 * @param yaw
	 */
	public void updateTextField(long thrust, float roll, double pitch, float yaw){
		cfThrust.setText(String.valueOf(thrust));
		cfRoll.setText(String.valueOf(roll));
		cfPitch.setText(String.valueOf(pitch));
		cfYaw.setText(String.valueOf(yaw));
	}
	
	/**
	 * Sets the channels that are to be displayed in the list panel.
	 * @param channels
	 */
	public void setJListItems(List<ConnectionData> channels){
		for(int i =0; i<=channels.size()-1; i++){
			getThisDataRate();
			if(channels.get(i).getDataRate()==dataRate){
				model.addElement(channels.get(i));
			}
		}
	}
	
	/**
	 * Returns the channels for the specific data rate that was selected from the combo box. 
	 * @return datarate
	 */
	public int getThisDataRate(){
		String option = (String) comboBox.getSelectedItem();
		if (option.equals("250kbs")){
			dataRate = 0;
		}else if(option.equals("1mbs")){
			dataRate = 1;
		}else{
			dataRate = 2;
		}
		return dataRate;
	}
}
