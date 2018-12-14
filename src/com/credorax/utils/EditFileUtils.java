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

    static public void findRightOffsetAndInsert(String filename, long targetMillisecond, byte[] content) throws IOException {
        insert(filename, findOffset(filename, targetMillisecond), content);
    }


    static public long findOffset(String filename, long targetMillisecond) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(filename), "r");
        long fileSize = r.length();
        r.seek(0);
        String finishTime;
        long currentLineStartPointer;

        do {
            currentLineStartPointer = r.getFilePointer();
            if((finishTime = r.readLine()) == null || Long.valueOf(finishTime) > targetMillisecond) return currentLineStartPointer;
        } while(r.getFilePointer() < fileSize);

        return r.getFilePointer();
    }

    static public void insert(String filename, long offset, byte[] content) throws IOException {
        RandomAccessFile r = new RandomAccessFile(new File(filename), "rw");
        RandomAccessFile rtemp = new RandomAccessFile(new File(filename + "~"), "rw");
        long fileSize = r.length();
        FileChannel sourceChannel = r.getChannel();
        FileChannel targetChannel = rtemp.getChannel();
        sourceChannel.transferTo(offset, (fileSize - offset), targetChannel);
        sourceChannel.truncate(offset);
        r.seek(offset);
        r.write(content);
        long newOffset = r.getFilePointer();
        targetChannel.position(0L);
        sourceChannel.transferFrom(targetChannel, newOffset, (fileSize - offset));
        sourceChannel.close();
        targetChannel.close();
    }

}
