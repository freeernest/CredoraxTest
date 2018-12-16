package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;
import com.credorax.utils.EditFileUtils;

import java.io.IOException;

public class TextFileStrategy implements CashStrategyInterface {
    @Override
    public void incrementFinishTimeCounter(long targetMillisecond, Interval currentInterval) throws IOException {
        EditFileUtils.incrementFinishTimeCounter(currentInterval.getEnd());
    }

    @Override
    public Long processAllFinishedCallsTillStartOfInterval(Interval interval, Overlap currentOverlap, long currentMillisecond) throws IOException {
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
}
