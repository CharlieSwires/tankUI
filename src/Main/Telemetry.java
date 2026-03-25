package Main;
import javax.swing.JFrame;

import Const.Constant;

public class Telemetry {
	private enum COMMS_STATUS {
		CPU_GOOD, CPU_DOWN, CPU_COMMS_DOWN, BATTERY_GOOD, BATTERY_DOWN, BATTERY_COMMS_DOWN
	}
	// Creating the JFrame for the application
	private static JFrame frame = new JFrame("Telemetry");
	// Creating the JSlider
	private static Double cpuTemp;
	private static double batteryVoltage;
	private static void updateStatus(COMMS_STATUS stat) {
		switch(stat) {
		case CPU_DOWN:
			break;
		case CPU_GOOD:
			break;
		case CPU_COMMS_DOWN:
			break;
		case BATTERY_DOWN:
			break;
		case BATTERY_GOOD:
			break;
		case BATTERY_COMMS_DOWN:
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
				frame.setTitle("CPU temp=" + result + "Celcius, "
						+ "Battery="+(result2*2.5)+"Volts");
				try {
					Thread.sleep(Constant.sensor_read_ms);
				} catch (InterruptedException e) {
				}
			}
		}
	}


	public static void main(String[] args) {
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(500, 550);


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
					cpuTemp = (double)result;
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
		return batteryVoltage;
	}

}
