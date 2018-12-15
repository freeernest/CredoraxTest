package com.credorax.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 * Created by Erik Feigin on 14/12/2018.
 */
public class EditFileUtils {

    private static String FINISH_COUNT_SEPARATOR = ":";
    private static String CALLS_FINISH_TIMES_FILE_PATH = "resources/calls_finish_times.txt";

    static public void findRightOffsetAndInsert(long targetMillisecond) throws IOException {
        insert(findOffset(targetMillisecond), targetMillisecond);
    }


    static public long findOffset(long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH), "r");
        long fileSize = r.length();
        r.seek(0);
        String finishTime;
        long currentLineStartPointer;

        do {
            currentLineStartPointer = r.getFilePointer();
            if((finishTime = r.readLine()) == null
                    || Long.valueOf(finishTime.substring(0, finishTime.indexOf(FINISH_COUNT_SEPARATOR))) >= targetMillisecond) {
                return currentLineStartPointer;
            }
        } while(r.getFilePointer() < fileSize);

        return r.getFilePointer();
    }

    static public long getFinishedCallsCounterAndRemoveFromFile(long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH), "rw");
        RandomAccessFile rtemp = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH + "~"), "rw");
        long fileSize = r.length();
        FileChannel sourceChannel = r.getChannel();
        FileChannel targetChannel = rtemp.getChannel();

        r.seek(0);
        String finishTimeLine;
        long currentLineStartPointer;

        do {
            currentLineStartPointer = r.getFilePointer();
            if((finishTimeLine = r.readLine()) == null
                    || Long.valueOf(finishTimeLine.substring(0, finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR))) == targetMillisecond){


                sourceChannel.transferTo(currentLineStartPointer, (fileSize - currentLineStartPointer), targetChannel);
                sourceChannel.truncate(currentLineStartPointer);
                r.seek(currentLineStartPointer);
                long newOffset = r.getFilePointer();
                targetChannel.position(0L);
                sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - currentLineStartPointer));
                sourceChannel.close();
                targetChannel.close();

                return Long.valueOf(finishTimeLine.substring(finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR) + 1));
            }
        } while(r.getFilePointer() < fileSize);

        return 0;
    }

    static public long getFinishedCallsCounter(long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH), "rw");
        RandomAccessFile rtemp = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH + "~"), "rw");
        long fileSize = r.length();

        r.seek(0);
        String finishTimeLine;

        do {
            if((finishTimeLine = r.readLine()) == null
                    || Long.valueOf(finishTimeLine.substring(0, finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR))) == targetMillisecond){
                return Long.valueOf(finishTimeLine.substring(finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR) + 1));
            }
        } while(r.getFilePointer() < fileSize);

        return 0;
    }

    static public void insert(long offset, long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH), "rw");
        RandomAccessFile rtemp = new RandomAccessFile(new File(CALLS_FINISH_TIMES_FILE_PATH + "~"), "rw");
        long fileSize = r.length();
        FileChannel sourceChannel = r.getChannel();
        FileChannel targetChannel = rtemp.getChannel();
        sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
        sourceChannel.truncate(offset);
        r.seek(offset);

        byte[] newContent;
        String finishTime;
        if((finishTime = r.readLine()) != null){
            String finishTimeCounter = finishTime.substring(finishTime.indexOf(FINISH_COUNT_SEPARATOR)+1);
            newContent = new StringBuilder().append(finishTime.substring(0, finishTime.indexOf(FINISH_COUNT_SEPARATOR)+1)).append((Long.valueOf(finishTimeCounter) + 1)).append("\n").toString().getBytes();
        } else {
            newContent = new StringBuilder().append(targetMillisecond).append(FINISH_COUNT_SEPARATOR).append("1\n").toString().getBytes();
        }

        r.write(newContent);
        long newOffset = r.getFilePointer();
        targetChannel.position(0L);
        sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
        sourceChannel.close();
        targetChannel.close();
    }

}
