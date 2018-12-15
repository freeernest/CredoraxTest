package com.credorax;
import java.io.*;
import java.util.HashMap;

import com.credorax.model.Overlap;
import com.credorax.model.Interval;
import com.credorax.utils.EditFileUtils;

/**
* Created by Erik Feigin on 12/12/18.
 * @author Erik Feigin
*/
public class CallsLogLoadAnalyser {

	private static String START_DURATION_SEPARATOR = "-";

	private String sourceFilePath;
	HashMap<Long, Long> finishesCountMap = new HashMap<>();
	Overlap maxOverlap = new Overlap(0,0,0);
	Overlap currentOverlap = new Overlap(0,0,0);
	long currentLineNumber = 0;
	long currentMillisecond = 0;


	public CallsLogLoadAnalyser(String sourceFilePath) {
		this.sourceFilePath = sourceFilePath;
	}
	
	//Retrieve next interval

	public Interval getNextInterval(String line)  {
		Interval nextInterval = new Interval(line.substring(0, line.indexOf(START_DURATION_SEPARATOR)), line.substring(line.indexOf(START_DURATION_SEPARATOR)+1));
		return nextInterval;
	}
	//Process calls log file and finding interval with maximum number of simultaneous calls
	public void processCalls()  {

		try(BufferedReader sourceReader = new BufferedReader(new FileReader(sourceFilePath))) {

			Interval currentInterval = new Interval(0, 0);
			String nextLine;

			if ((nextLine = sourceReader.readLine()) != null) {
				currentInterval = getNextInterval(nextLine);
				currentMillisecond = currentInterval.getStart();


				//EditFileUtils.findRightOffsetAndInsert(currentInterval.getEnd());
				incrementFinishTimeCounter(currentInterval.getEnd());


				currentLineNumber++;
				maxOverlap = new Overlap(currentInterval);
				maxOverlap.incrementStrength();
				currentOverlap = new Overlap(maxOverlap);
			}

			while ((nextLine = sourceReader.readLine()) != null) {

				Interval nextInterval = getNextInterval(nextLine);
				currentLineNumber++;


				//EditFileUtils.findRightOffsetAndInsert(nextInterval.getEnd());
				incrementFinishTimeCounter(nextInterval.getEnd());


				if (nextInterval.getStart() == currentOverlap.getStart()) {
					if(nextInterval.getEnd() < currentOverlap.getEnd()){
						currentOverlap.setEnd(nextInterval.getEnd());
					}
					incrementCurrentOverlapStrength();
				} else {// can't be less only bigger or equal


					processAllFinishedCallsTillSomeTime(nextInterval.getStart());
					//processAllFinishedCallsTillSomeTimeAtTextFile(nextInterval.getStart());


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

	private void processAllFinishedCallsTillSomeTimeAtTextFile(long someTime) throws IOException {
		Long countToReduce;
		for(long i = currentMillisecond; i<=someTime; i++) {
			if ((countToReduce = EditFileUtils.getFinishedCallsCounterAndRemoveFromFile(someTime)) != null) {
				currentOverlap.decrementByValue(countToReduce);
			}
		}
	}

	private void incrementFinishTimeCounter(Long finishTime) {
		finishesCountMap.put(finishTime, finishesCountMap.getOrDefault(finishTime, 0L)+1);
	}
}
