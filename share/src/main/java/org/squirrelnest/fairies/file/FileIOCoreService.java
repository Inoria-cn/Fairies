package org.squirrelnest.fairies.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.RandomAccessFile;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * Created by Inoria on 2019/3/24.
 */
@Service
public class FileIOCoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileIOCoreService.class);

    public byte[] getPieceData(String absolutePath, Integer sliceIndex, Integer pieceIndex, Integer sliceSize) {
        byte[] buffer = new byte[PIECE_SIZE];

        try {
            RandomAccessFile file = new RandomAccessFile(absolutePath, "r");
            int pointerPosition = sliceIndex * sliceSize + pieceIndex * PIECE_SIZE;
            file.seek(pointerPosition);
            file.read(buffer);
            file.close();
            return buffer;
        } catch (Exception e) {
            LOGGER.error("Read piece data raised an error in file " + absolutePath, e);
            return null;
        }
    }

    public byte[] getSliceData(String absolutePath, Integer sliceIndex, Integer sliceSize) {
        byte[] buffer = new byte[sliceIndex];

        try {
            RandomAccessFile file = new RandomAccessFile(absolutePath, "r");
            int pointerPosition = sliceIndex * sliceSize;
            file.seek(pointerPosition);
            file.read(buffer);
            file.close();
            return buffer;
        } catch (Exception e) {
            LOGGER.error("Read slice data raised an error in file " + absolutePath, e);
            return null;
        }
    }

    public boolean writePieceData(String absolutePath, Integer sliceIndex, Integer pieceIndex, Integer sliceSize, byte[] data) {
        if (data == null) {
            return false;
        }

        try {
            RandomAccessFile file = new RandomAccessFile(absolutePath, "rw");
            int pointerPosition = sliceIndex * sliceSize + pieceIndex * PIECE_SIZE;
            file.seek(pointerPosition);
            file.write(data);
            file.close();
            return true;
        } catch (Exception e) {
            LOGGER.error("Write piece data raised an error in file " + absolutePath, e);
            return false;
        }
    }

    public boolean writeSliceData(String absolutePath, Integer sliceIndex, Integer sliceSize, byte[] data) {
        if (data == null) {
            return false;
        }

        try {
            RandomAccessFile file = new RandomAccessFile(absolutePath, "rw");
            int pointerPosition = sliceIndex * sliceSize;
            file.seek(pointerPosition);
            file.write(data);
            file.close();
            return true;
        } catch (Exception e) {
            LOGGER.error("Write slice data raised an error in file " + absolutePath, e);
            return false;
        }
    }
}
