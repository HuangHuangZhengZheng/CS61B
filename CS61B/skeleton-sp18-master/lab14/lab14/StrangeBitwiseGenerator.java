package lab14;

import lab14lib.Generator;

public class StrangeBitwiseGenerator implements Generator {
	private int period;
	private int state;

    private double normalize(double value){
        double factor = value - (-1);
        double normalized = -1.0 + factor * 2;
        return normalized;
    }

	public StrangeBitwiseGenerator(int period) {
		state = 0;
		this.period = period;
	}

	public double next() {
		state = (state + 1);
        int weirdState = state & (state >>> 3) % period;
        weirdState = state & (state >> 3) & (state >> 8) % period; // cool
        weirdState = state & (state >> 7) % period; // cooler
        double st = -1 + (weirdState % period)*1.0 / (period - 1);
        return normalize(st);
	}

}
