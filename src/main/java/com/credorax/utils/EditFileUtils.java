package com.credorax.utils;

import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by Erik Feigin on 14/12/2018.
 */
public class EditFileUtils {

    private static String FINISH_COUNT_SEPARATOR = ":";
    private static String CALLS_FINISH_TIMES_FILE_PATH = "calls_finish_times.txt";
    private static String TEMP_FILE_CALLS_FINISH_TIMES_FILE_PATH = "calls_finish_times~.txt";

    static public void cleanFile() throws IOException {
        new PrintWriter(EditFileUtils.class.getClassLoader().getResource(CALLS_FINISH_TIMES_FILE_PATH).getFile()).close();
    }

    static public void incrementFinishTimeCounter(long targetMillisecond) throws IOException {
        increment(findOffset(targetMillisecond), targetMillisecond);
    }


    static public long findOffset(long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(EditFileUtils.class.getClassLoader().getResource(CALLS_FINISH_TIMES_FILE_PATH).getFile(), "r");
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
        RandomAccessFile r = new RandomAccessFile(EditFileUtils.class.getClassLoader().getResource(CALLS_FINISH_TIMES_FILE_PATH).getFile(), "rw");
        //File tempFile = new File(EditFileUtils.class.getClassLoader().getResource(TEMP_FILE_CALLS_FINISH_TIMES_FILE_PATH).getFile());
        File tempFile = new File(TEMP_FILE_CALLS_FINISH_TIMES_FILE_PATH);
        RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw");
        long fileSize = r.length();

        try(FileChannel sourceChannel = r.getChannel(); FileChannel targetChannel = rtemp.getChannel()) {

            r.seek(0);
            String finishTimeLine;
            long currentLineStartPointer;

            do {
                currentLineStartPointer = r.getFilePointer();
                if ((finishTimeLine = r.readLine()) == null
                        || Long.valueOf(finishTimeLine.substring(0, finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR))) == targetMillisecond) {

                    sourceChannel.transferTo(r.getFilePointer(), (fileSize - r.getFilePointer()), targetChannel);
                    sourceChannel.truncate(currentLineStartPointer);
                    //r.seek(currentLineStartPointer);
                    targetChannel.position(0L);
                    sourceChannel.transferFrom(targetChannel, r.getFilePointer(), (fileSize - r.getFilePointer()));

                    return finishTimeLine == null ? 0 : Long.valueOf(finishTimeLine.substring(finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR) + 1));

                }
            } while (r.getFilePointer() < fileSize);
        }
        tempFile.delete();

        return 0;
    }

    static public long getFinishedCallsCounter(long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(EditFileUtils.class.getClassLoader().getResource(CALLS_FINISH_TIMES_FILE_PATH).getFile(), "rw");
        long fileSize = r.length();

        r.seek(0);
        String finishTimeLine;

        do {
            if((finishTimeLine = r.readLine()) == null){
                return 0;
            }else if(Long.valueOf(finishTimeLine.substring(0, finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR))) == targetMillisecond){
                return Long.valueOf(finishTimeLine.substring(finishTimeLine.indexOf(FINISH_COUNT_SEPARATOR) + 1));
            }
        } while(r.getFilePointer() < fileSize);

        return 0;
    }

    static public void increment(long offset, long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(EditFileUtils.class.getClassLoader().getResource(CALLS_FINISH_TIMES_FILE_PATH).getFile(), "rw");
        //File tempFile = new File(EditFileUtils.class.getClassLoader().getResource(TEMP_FILE_CALLS_FINISH_TIMES_FILE_PATH).getFile());
        File tempFile = new File(TEMP_FILE_CALLS_FINISH_TIMES_FILE_PATH);
        RandomAccessFile rtemp = new RandomAccessFile(tempFile, "rw");
        //new PrintWriter(tempFile).close();
        long fileSize = r.length();

        try(FileChannel sourceChannel = r.getChannel(); FileChannel targetChannel = rtemp.getChannel()) {

            r.seek(offset);

            byte[] newContent;
            String finishTime;
            if ((finishTime = r.readLine()) != null) {
                if(Long.valueOf(finishTime.substring(0, finishTime.indexOf(FINISH_COUNT_SEPARATOR))) == targetMillisecond) {
                    String finishTimeCounter = finishTime.substring(finishTime.indexOf(FINISH_COUNT_SEPARATOR) + 1);
                    newContent = new StringBuilder()
                            .append(finishTime.substring(0, finishTime.indexOf(FINISH_COUNT_SEPARATOR) + 1))
                            .append((Long.valueOf(finishTimeCounter) + 1))
                            .append("\n")
                            .toString()
                            .getBytes();

                    sourceChannel.transferTo(r.getFilePointer(), (fileSize - r.getFilePointer()), targetChannel);
                    targetChannel.position(0L);
                    sourceChannel.truncate(offset);
                    r.write(newContent);
                    sourceChannel.transferFrom(targetChannel, offset + newContent.length + finishTime.getBytes().length, (fileSize - offset - finishTime.getBytes().length));
                } else {
                    newContent = new StringBuilder()
                            .append(targetMillisecond)
                            .append(FINISH_COUNT_SEPARATOR)
                            .append("1\n")
                            .toString()
                            .getBytes();
                    sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
                    targetChannel.position(0L);
                    sourceChannel.truncate(offset);
                    r.write(newContent);
                    sourceChannel.transferFrom(targetChannel, r.getFilePointer(), (fileSize - offset));
                }
            } else {
                newContent = new StringBuilder()
                        .append(targetMillisecond)
                        .append(FINISH_COUNT_SEPARATOR)
                        .append("1\n")
                        .toString()
                        .getBytes();

                sourceChannel.transferTo(r.getFilePointer(), (fileSize - r.getFilePointer()), targetChannel);
                targetChannel.position(0L);
                sourceChannel.truncate(offset);
                r.write(newContent);
                sourceChannel.transferFrom(targetChannel, 0, fileSize);
            }

        }

        tempFile.delete();
    }

}
