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
		CPU_GOOD, CPU_DOWN, CPU_COMMS_DOWN, BATTERY_GOOD, BATTERY_DOWN, BATTERY_COMMS_DOWN, CPU_COMMS_TIMEOUT, BATTERY_COMMS_TIMEOUT, BATTERY_COMMS_FINE, CPU_COMMS_FINE
	}
	// Creating the JFrame for the application
	private static JFrame frame = new JFrame("Telemetry");
	// Creating the JSlider
	private static Double cpuTemp = (double) Constant.ERROR;
	private static Double batteryVoltage = (double) Constant.ERROR;
	private static JLabel cpuTitle = new JLabel("CPU OK", SwingConstants.LEFT);
	private static Color originalColour = cpuTitle.getForeground();
	private static JLabel batteryTitle= new JLabel("BATTERY OK", SwingConstants.RIGHT);
	private static JLabel batteryTitle2= new JLabel("BATTERY OK", SwingConstants.RIGHT);
	private static JLabel cpuTitle2= new JLabel("CPU OK", SwingConstants.LEFT);
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
			cpuTitle2.setForeground(Color.RED);
			cpuTitle2.setText("CPU COMMS UP TIMEOUT");
			break;
		case CPU_COMMS_FINE:
			cpuTitle2.setForeground(originalColour);
			cpuTitle2.setText("CPU COMMS UP");
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
			frame.setForeground(Color.RED);
			batteryTitle.setForeground(Color.RED);
			batteryTitle.setText("BATTERY COMMS DOWN");
			break;
		case BATTERY_COMMS_TIMEOUT:
			frame.setForeground(Color.RED);
			batteryTitle2.setForeground(Color.RED);
			batteryTitle2.setText("BATTERY COMMS UP TIMEOUT");
			break;
		case BATTERY_COMMS_FINE:
			frame.setForeground(Color.BLACK);
			batteryTitle2.setForeground(originalColour);
			batteryTitle2.setText("BATTERY COMMS UP");
			break;
		default:
			throw new RuntimeException("Not a valid status");
		}
		frame.repaint();
	}



	private class MyThreadTelemetry extends Thread {
		@Override
		public void run() {
			while (true) {
				Double result = getCPUTemp();
				Double result2 = getBatteryVoltage();
				if ((""+result2).equals(""+Constant.ERROR+".0")){
				}
				else {
					result2 *= 4.9;
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
	private class MyThreadTimeout extends Thread {
		@Override
		public void run() {
			while (true) {
				getBatteryTimeout();
				getCPUTimeout();
				try {
					Thread.sleep(Constant.sensor_read_ms);
				} catch (InterruptedException e) {
				}

			}
		}
	}


	public static void main(String[] args) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 200);

		JPanel toppanel = new JPanel();
		toppanel.add(cpuTitle);
		toppanel.add(batteryTitle= new JLabel("BATTERY OK", SwingConstants.RIGHT));
		frame.add(toppanel, BorderLayout.NORTH);
		JPanel middlepanel = new JPanel();
		middlepanel.add(cpuTitle2);
		middlepanel.add(batteryTitle2);
		frame.add(middlepanel, BorderLayout.SOUTH);

		// Making the frame visible
		frame.setVisible(true);
		Telemetry er = new Telemetry();
		MyThreadTelemetry t2 = er.new MyThreadTelemetry();
		t2.start();
		MyThreadTimeout t = er.new MyThreadTimeout();
		t.start();

	}
	private double batteryVoltage2;
	private Double timeout;


	public Double getCPUTemp() {
		Constant.gg.getGenericAsync(
				"/telemetry/cpu-temp",
				result -> {
					cpuTemp = result;
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
					batteryVoltage = result;
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
		return batteryVoltage;
	}
	public Double getCPUTimeout() {
		Constant.gg.getGenericAsync(
				"/telemetry/timeout-temp",
				result -> {
					timeout = result;
					if ((""+Constant.ERROR+".0").equals(""+timeout)) {
						updateStatus(COMMS_STATUS.CPU_COMMS_TIMEOUT);
					} else {
						updateStatus(COMMS_STATUS.CPU_COMMS_FINE);

					}
				},
				errorMessage -> {
					updateStatus(COMMS_STATUS.CPU_COMMS_TIMEOUT);
				}
				);
		return timeout;
	}
	public Double getBatteryTimeout() {
		Constant.gg.getGenericAsync(
				"/telemetry/timeout-battery",
				result -> {
					batteryVoltage2 = result;
					if ((""+Constant.ERROR+".0").equals(""+batteryVoltage2)) {
						updateStatus(COMMS_STATUS.BATTERY_COMMS_TIMEOUT);
					} else {
						updateStatus(COMMS_STATUS.BATTERY_COMMS_FINE);

					}
				},
				errorMessage -> {
					updateStatus(COMMS_STATUS.BATTERY_COMMS_TIMEOUT);
				}
				);
		return batteryVoltage2;
	}
}
