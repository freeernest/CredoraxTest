package com.credorax;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

import com.credorax.model.Overlap;
import com.credorax.model.Interval;

/**
* Created by Erik Feigin on 12/12/18.
 * @author Erik Feigin
*/
public class CallsLogLoadAnalyser {

	private static String SEPARATOR = "-";
	private static String NUMBER_DELIMITER = ",";
	private static String INTERVAL_DELIMTER_START = "[";
	private static String INTERVAL_DELIMTER_END = "]";

	private long counterOverlaps = 0;
	private int maxOverlaps = 0;
	private String sourceFilePath;
	private String resultFilePath;
	private String callsFinishTimesFilePath;
	private Stack<Overlap> intervalsStack = new Stack<>();
	HashMap<Long, Long> finishesCountMap = new HashMap<>();
	Overlap maxOverlap = new Overlap(0,0,0);
	Overlap currentOverlap = new Overlap(0,0,0);
	long currentLineNumber = 0;
	long currentMillisecond = 0;


	public CallsLogLoadAnalyser(String sourceFilePath, String resultFilePath, String callsFinishTimesFilePath) {
		this.sourceFilePath = sourceFilePath;
		this.resultFilePath = resultFilePath;
		this.callsFinishTimesFilePath = callsFinishTimesFilePath;

	}
	
	//Retrieve next interval

	public Interval getNextInterval(String line)  {
		Interval nextInterval = new Interval(line.substring(0, line.indexOf(SEPARATOR)), line.substring(line.indexOf(SEPARATOR)+1));
		return nextInterval;
	}
	//Process calls log file and finding interval with maximum number of simultaneous calls
	public void processCalls()  {

		try(BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFilePath));
			BufferedReader finishTimesReader = new BufferedReader(new FileReader(callsFinishTimesFilePath));
			PrintWriter finishTimesWriter = new PrintWriter(callsFinishTimesFilePath);
			PrintWriter ResultWriter = new PrintWriter(resultFilePath)) {

			long nextFinishMillisecond = 0;
			Interval currentInterval = new Interval(0, 0);
			String nextLine;
			String finishTimesNextLine;
			Long finishCount;

			if ((nextLine = sourceReader.readLine()) != null) {
				currentInterval = getNextInterval(nextLine);
				currentMillisecond = currentInterval.getStart();
				//EditFileUtils.findRightOffsetAndInsert(callsFinishTimesFilePath, nextFinishMillisecond, (nextFinishMillisecond + "\n").getBytes());
				incrementFinishTimeCounter(currentInterval.getEnd());
				currentLineNumber++;
				maxOverlap = new Overlap(currentInterval);
				maxOverlap.incrementStrength();
				currentOverlap = new Overlap(maxOverlap);
			}

			while ((nextLine = sourceReader.readLine()) != null) {

				//processAllFinishedCallsTillSomeTime(currentMillisecond);

				Interval nextInterval = getNextInterval(nextLine);
				currentLineNumber++;
				//EditFileUtils.findRightOffsetAndInsert(callsFinishTimesFilePath, currentInterval.getEnd(), (currentInterval.getEnd()+"\n").getBytes());
				incrementFinishTimeCounter(currentInterval.getEnd());

				if (nextInterval.getStart() == currentOverlap.getStart()) {
					if(nextInterval.getEnd() < currentOverlap.getEnd()){
						currentOverlap.setEnd(nextInterval.getEnd());
					}
					incrementCurrentOverlapStrength();
				} else {// can't be less only bigger or equal
					processAllFinishedCallsTillSomeTime(nextInterval.getStart());

					currentOverlap.setStart(nextInterval.getStart());

					if(nextInterval.getEnd() < currentOverlap.getEnd()) {
						currentOverlap.setEnd(nextInterval.getEnd());
					}
					if(nextInterval.getStart() >= currentOverlap.getEnd()){
						currentOverlap.setEnd(nextInterval.getEnd());
					}else {
						incrementCurrentOverlapStrength();
					}

					currentMillisecond = nextInterval.getStart();
				}

				//if((finishTimesNextLine = finishTimesReader.readLine()) != null){
				//	nextFinishMillisecond = Long.valueOf(finishTimesNextLine);
				//}
			}

			System.out.println("Maximum " + maxOverlap);

		} catch (IOException e) {
			System.out.println("Input / Output error!!!");
			e.printStackTrace();
		}
	}

	private void incrementCurrentOverlapStrength() {
		currentOverlap.incrementStrength();
		if(maxOverlap.getStrength() < currentOverlap.getStrength()) {
            maxOverlap.setAs(currentOverlap);
        }
	}

	private void processAllFinishedCallsTillSomeTime(long someTime) {
		for(long i = currentMillisecond; i<=someTime; i++) {
			if (finishesCountMap.get(i) != null) {
				currentOverlap.decrementByValue(finishesCountMap.get(i));
				finishesCountMap.remove(i);
			}
		}
	}

	private void incrementFinishTimeCounter(Long finishTime) {
		finishesCountMap.put(finishTime, finishesCountMap.getOrDefault(finishTime, 0L)+1);
	}
}
