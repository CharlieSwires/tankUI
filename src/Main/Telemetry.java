package Main;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import Const.Constant;

public class Telemetry {
	private enum COMMS_STATUS {
		CPU_GOOD, CPU_DOWN, CPU_COMMS_DOWN, BATTERY_GOOD, BATTERY_DOWN, BATTERY_COMMS_DOWN, CPU_COMMS_TIMEOUT, BATTERY_COMMS_TIMEOUT
	}
	// Creating the JFrame for the application
	private static JFrame frame = new JFrame("Telemetry");
	// Creating the JSlider
	private static Double cpuTemp = (double) Constant.ERROR;
	private static Double batteryVoltage = (double) Constant.ERROR;
	private static JLabel cpuTitle = new JLabel("CPU OK", SwingConstants.LEFT);
	private static Color originalColour = cpuTitle.getForeground();
	private static JLabel batteryTitle= new JLabel("BATTERY OK", SwingConstants.RIGHT);
	private static void updateStatus(COMMS_STATUS stat) {
		switch(stat) {
		case CPU_DOWN:
			frame.setForeground(Color.RED);
			cpuTitle.setForeground(Color.RED);
			cpuTitle.setText("CPU ERROR READING VALUE");
			break;
		case CPU_GOOD:
			frame.setForeground(Color.BLACK);
			cpuTitle.setForeground(originalColour);
			cpuTitle.setText("CPU OK");
			break;
		case CPU_COMMS_DOWN:
			cpuTitle.setForeground(Color.RED);
			cpuTitle.setText("CPU COMMS DOWN");
			break;
		case CPU_COMMS_TIMEOUT:
			cpuTitle.setForeground(Color.RED);
			cpuTitle.setText("CPU COMMS UP TIMEOUT");
			break;
		case BATTERY_DOWN:
			frame.setForeground(Color.RED);
			batteryTitle.setForeground(Color.RED);
			batteryTitle.setText("BATTERY ERROR READING VALUE");
			break;
		case BATTERY_GOOD:
			frame.setForeground(Color.BLACK);
			batteryTitle.setForeground(originalColour);
			batteryTitle.setText("BATTERY OK");
			break;
		case BATTERY_COMMS_DOWN:
			batteryTitle.setForeground(Color.RED);
			batteryTitle.setText("BATTERY COMMS DOWN");
			break;
		case BATTERY_COMMS_TIMEOUT:
			batteryTitle.setForeground(Color.RED);
			batteryTitle.setText("BATTERY COMMS UP TIMEOUT");
			break;
		default:
			throw new RuntimeException("Not a valid status");
		}
	}



	private class MyThreadTelemetry extends Thread {
		@Override
		public void run() {
			while (true) {
				Double result = getCPUTemp();
				Double result2 = getBatteryVoltage();
				getBatteryTimeout();
				getCPUTimeout();
				if ((""+result2).equals(""+Constant.ERROR+".0")){
				}
				else {
					result2 *= 2.5;
				}

				frame.setTitle("CPU temp=" + result + "Celcius, "
						+ "Battery="+(result2)+"Volts");
				try {
					Thread.sleep(Constant.sensor_read_ms);
				} catch (InterruptedException e) {
				}
			}
		}
	}


	public static void main(String[] args) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 100);

		JPanel toppanel = new JPanel();
		toppanel.add(cpuTitle);
		toppanel.add(batteryTitle= new JLabel("BATTERY OK", SwingConstants.RIGHT));
		frame.add(toppanel, BorderLayout.NORTH);

		// Making the frame visible
		frame.setVisible(true);
		Telemetry er = new Telemetry();
		MyThreadTelemetry t2 = er.new MyThreadTelemetry();
		t2.start();

	}


	public Double getCPUTemp() {
		Constant.gg.getGenericAsync(
				"/telemetry/cpu-temp",
				result -> {
					cpuTemp = result != null? result:null;
					if ((""+cpuTemp).equals(""+Constant.ERROR+".0")) {
						updateStatus(COMMS_STATUS.CPU_DOWN);
					} else {
						updateStatus(COMMS_STATUS.CPU_GOOD);

					}
				},
				errorMessage -> {
					updateStatus(COMMS_STATUS.CPU_COMMS_DOWN);
				}
				);
		return cpuTemp;
	}
	public Double getBatteryVoltage() {
		Constant.gg.getGenericAsync(
				"/telemetry/volts/0",
				result -> {
					batteryVoltage = (double)result;
					if ((""+batteryVoltage).equals(""+Constant.ERROR+".0")) {
						updateStatus(COMMS_STATUS.BATTERY_DOWN);
					} else {
						updateStatus(COMMS_STATUS.BATTERY_GOOD);

					}
				},
				errorMessage -> {
					updateStatus(COMMS_STATUS.BATTERY_COMMS_DOWN);
				}
				);
		System.out.println(batteryVoltage);
		return batteryVoltage;
	}
	public Double getCPUTimeout() {
		Constant.gg.getGenericAsync(
				"/telemetry/timeout-temp",
				result -> {
					Double timeout = result != null? result:null;
					if ((""+timeout).equals(""+Constant.ERROR+".0")) {
						updateStatus(COMMS_STATUS.CPU_COMMS_TIMEOUT);
					} else {
						updateStatus(COMMS_STATUS.CPU_GOOD);

					}
				},
				errorMessage -> {
					updateStatus(COMMS_STATUS.CPU_COMMS_DOWN);
				}
				);
		return cpuTemp;
	}
	public Double getBatteryTimeout() {
		Constant.gg.getGenericAsync(
				"/telemetry/timeout-battery",
				result -> {
					batteryVoltage = (double)result;
					if ((""+batteryVoltage).equals(""+Constant.ERROR+".0")) {
						updateStatus(COMMS_STATUS.BATTERY_COMMS_TIMEOUT);
					} else {
						updateStatus(COMMS_STATUS.BATTERY_GOOD);

					}
				},
				errorMessage -> {
					updateStatus(COMMS_STATUS.BATTERY_COMMS_DOWN);
				}
				);
		System.out.println(batteryVoltage);
		return batteryVoltage;
	}
}
