package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;

import java.io.IOException;

public interface CashStrategyInterface {
    void incrementFinishTimeCounter(long targetMillisecond, Interval currentInterval) throws IOException;
    Long processAllFinishedCallsTillStartOfInterval(Interval interval, Overlap currentOverlap, long currentMillisecond) throws IOException;
}
