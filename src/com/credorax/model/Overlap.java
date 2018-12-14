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

	public Overlap(Overlap overlap) {
		super(overlap);
		this.strength = overlap.getStrength();
	}

	public void setAs(Overlap overlap) {
		this.start = overlap.start;
		this.end = overlap.end;
		this.strength = overlap.getStrength();
	}

	public long getStrength() {
		return strength;
	}


	public void setStrength(long strength) {
		this.strength = strength;
	}


	@Override
	public String toString() {
		return "Overlap ("
				+ "start = " + this.start
				+ ", end = " + this.end
				+ ", strength = " + this.strength
				+ ")";
	}

	public void incrementStrength() {
		strength++;
	}
	public void decrementByValue(Long value) {
		strength -= value;
	}

	public void decrement() {strength--;}
}