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

				EditFileUtils.cleanFile();
				EditFileUtils.findRightOffsetAndIncrement(currentInterval.getEnd());
				//incrementFinishTimeCounter(currentInterval.getEnd());

				currentLineNumber++;
				maxOverlap = new Overlap(currentInterval);
				maxOverlap.incrementStrength();
				currentOverlap = new Overlap(maxOverlap);
			}

			while ((nextLine = sourceReader.readLine()) != null) {

				Interval nextInterval = getNextInterval(nextLine);
				currentLineNumber++;

				EditFileUtils.findRightOffsetAndIncrement(nextInterval.getEnd());
				//incrementFinishTimeCounter(nextInterval.getEnd());

				if (nextInterval.getStart() == currentOverlap.getStart()) {
					if(nextInterval.getEnd() < currentOverlap.getEnd()){
						currentOverlap.setEnd(nextInterval.getEnd());
					}
				} else {// can't be less only bigger or equal

					//Long nextFinish = processAllFinishedCallsTillStartOfInterval(nextInterval);
					Long nextFinish = processAllFinishedCallsTillSomeTimeAtTextFile(nextInterval);

					currentOverlap.setStart(nextInterval.getStart());
					currentOverlap.setEnd(nextFinish);
					currentMillisecond = nextInterval.getStart();
				}
				incrementCurrentOverlapStrength();
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

	private long processAllFinishedCallsTillStartOfInterval(Interval interval) {
		for(long i = currentMillisecond; i<=interval.getStart(); i++) {
			if (finishesCountMap.get(i) != null) {
				currentOverlap.decrementByValue(finishesCountMap.get(i));
				finishesCountMap.remove(i);
			}
		}

		for(long j = interval.getStart()+1; j<=interval.getEnd(); j++) {
			if (finishesCountMap.get(j) != null) {
				return j;
			}
		}
		return interval.getEnd();
	}

	private Long processAllFinishedCallsTillSomeTimeAtTextFile(Interval interval) throws IOException {
		for(long i = currentMillisecond; i<=interval.getStart(); i++) {
			currentOverlap.decrementByValue(EditFileUtils.getFinishedCallsCounterAndRemoveFromFile(i));
		}

		for(long j = interval.getStart()+1; j<=interval.getEnd(); j++) {
			if ( EditFileUtils.getFinishedCallsCounter(j)!= 0) {
				return j;
			}
		}
		return interval.getEnd();
	}

	private void incrementFinishTimeCounter(Long finishTime) {
		finishesCountMap.put(finishTime, finishesCountMap.getOrDefault(finishTime, 0L)+1);
	}
}
