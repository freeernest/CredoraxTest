package com.credorax.model;

public class Overlap extends Interval {
	
	private long strength;

	public Overlap(long start, long end, long strength) {
		super(start, end);
		
		this.strength = strength;
	}

	public Overlap(Interval interval) {
		super(interval);
	}

	public long getStrength() {
		return strength;
	}


	public void setStrength(long strength) {
		this.strength = strength;
	}


	@Override
	public String toString() {
		return "Overlap("
				+ "start=" + this.start
				+ ", end=" + this.end
				+ ", strength=" + this.strength
				+ ")";
	}

	public void incrementStrength() {
		strength++;
	}
}