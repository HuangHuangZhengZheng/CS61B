package lab14;

import edu.princeton.cs.algs4.StdAudio;
import lab14lib.Generator;

public class SawToothGenerator implements Generator {
	private int period;
	private int state;

    private double normalize(double value){
        double factor = value - (-1);
        double normalized = -1.0 + factor * 2;
        return normalized;
    }

	public SawToothGenerator(int period) {
		state = 0;
		this.period = period;
	}

	public double next() {
		state = (state + 1);
        double st = -1 + (state % period)*1.0 / (period - 1);
        return normalize(st);
	}
}
