package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;
import com.credorax.utils.EditFileUtils;

import java.io.IOException;
import java.io.UncheckedIOException;

public class TextFileStrategy implements CacheStrategyInterface {
    @Override
    public void incrementFinishTimeCounter(long targetMillisecond, Interval currentInterval) {
        try {
            EditFileUtils.incrementFinishTimeCounter(currentInterval.getEnd());
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public long processAllFinishedCallsTillStartOfInterval(Interval interval, Overlap currentOverlap, long currentMillisecond) {
        try {
            for(long i = currentMillisecond; i<=interval.getStart(); i++) {
                currentOverlap.decrementByValue(EditFileUtils.getFinishedCallsCounterAndRemoveFromFile(i));
            }

            for(long j = interval.getStart()+1; j<=interval.getEnd(); j++) {
                if ( EditFileUtils.getFinishedCallsCounter(j)!= 0) {
                    return j;
                }
            }
            return interval.getEnd();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }
}
