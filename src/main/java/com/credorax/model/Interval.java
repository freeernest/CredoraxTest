package com.credorax.model;

public class Interval {
	protected long start;
	protected long end;

	public Interval(String start, String duration) {
		this(Long.valueOf(start), Long.valueOf(duration));
	}
	
	public Interval(long start, long duration) {
		this.start = start;
		this.end = start + duration;
	}

	public Interval(Interval interval) {
		this.start = interval.start;
		this.end = interval.end;
	}

	public long length() {
		return this.end - this.start;
	}
	public void setStart(long start) {
		this.start = start;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	@Override
	public String toString() {
		return "Interval("
				+ "start=" + this.start 
				+ ", end=" + this.end 
				+ ")";
	}
}