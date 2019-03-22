package org.squirrelnest.fairies.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.RandomAccessFile;

import static org.squirrelnest.fairies.service.ConfigReadService.PIECE_SIZE;

/**
 * Created by Inoria on 2019/3/22.
 */
@Service
public class RandomAccessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAccessService.class);

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
}
