package com.credorax;
import java.io.*;
import java.util.Properties;

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
	Overlap maxOverlap = new Overlap(0,0,0);
	Overlap currentOverlap = new Overlap(0,0,0);
	long currentLineNumber = 0;
	long currentMillisecond = 0;
	Properties props = new Properties();
	CashStrategyInterface cashStrategy;


	public CallsLogLoadAnalyser(String sourceFilePath) throws IOException {
		this.sourceFilePath = sourceFilePath;
		initCashStrategy();
	}

	private void initCashStrategy() throws IOException {
		try {
			String path = getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
			FileInputStream fis = new FileInputStream(new File( path + "/application.properties"));
			props.load(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Properties file was not found!!!");
		}
		boolean useHashMap = Boolean.valueOf(props.get("useHashMap").toString());
		cashStrategy = useHashMap ? new HashMapStrategy() : new TextFileStrategy();
		if(!useHashMap){
			EditFileUtils.cleanFile();
		}
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

				cashStrategy.incrementFinishTimeCounter(currentInterval.getEnd(), currentInterval);

				currentLineNumber++;
				maxOverlap = new Overlap(currentInterval);
				maxOverlap.incrementStrength();
				currentOverlap = new Overlap(maxOverlap);
			}

			while ((nextLine = sourceReader.readLine()) != null) {

				Interval nextInterval = getNextInterval(nextLine);
				currentLineNumber++;

				cashStrategy.incrementFinishTimeCounter(nextInterval.getEnd(), nextInterval);

				if (nextInterval.getStart() == currentOverlap.getStart()) {
					if(nextInterval.getEnd() < currentOverlap.getEnd()){
						currentOverlap.setEnd(nextInterval.getEnd());
					}
				} else {// can't be less only bigger or equal

					Long nextFinish = cashStrategy.processAllFinishedCallsTillStartOfInterval(nextInterval, currentOverlap, currentMillisecond);

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



}
