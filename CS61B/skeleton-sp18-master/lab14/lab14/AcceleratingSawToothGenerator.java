package lab14;

import lab14lib.Generator;

public class AcceleratingSawToothGenerator implements Generator {
    private int period;
	private int state;
    private double multiplier;

    private double normalize(double value){
        double factor = value - (-1);
        double normalized = -1.0 + factor * 2;
        return normalized;
    }

	public AcceleratingSawToothGenerator(int period, double multiplier) {
		state = 0;
		this.period = period;
        this.multiplier = multiplier;
	}

	public double next() {
		state = (state + 1);
        double st = -1 + (state % period)*1.0 / (period - 1);
        if (Double.compare(st, 0.0) == 0) {
            period = (int) Math.floor(period * multiplier);
            state = 0; // difference between original!
        }
        return normalize(st);
	}
}
