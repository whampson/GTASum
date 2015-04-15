
package thehambone.gtatools.gtasum.checksum;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author thehambone
 */
public class Checksum {
    public static boolean calculateChecksum(File f) throws IOException {
        boolean sumUpdated = false;
        byte[] existingSumBuffer = new byte[4];
        byte[] fileBuffer = new byte[(int)f.length() - 4];
        RandomAccessFile randomAccessFile = new RandomAccessFile(f, "rw");
        randomAccessFile.read(fileBuffer);
        randomAccessFile.read(existingSumBuffer);
        int sum = 0;
        for (int i = 0; i < fileBuffer.length; i++) {
            sum += (int)(fileBuffer[i] & 0xFF);
        }
        int existingSum = ((existingSumBuffer[3] & 0xFF) << 24) | ((existingSumBuffer[2] & 0xFF) << 16) | ((existingSumBuffer[1] & 0xFF) << 8) | (existingSumBuffer[0] & 0xFF);
        if (existingSum != sum) {
            randomAccessFile.seek(f.length() - 4);
            for (int i = 0; i < 4; i++) {
                randomAccessFile.writeByte((byte)(sum >>> (i * 8)));
            }
            sumUpdated = true;
            System.out.printf("[INFO]: %s - Checksum updated! 0x%02X\n", f.getName(), sum);
        } else {
            System.out.printf("[INFO]: %s - Checksum is already correct.\n", f.getName());
        }
        randomAccessFile.close();
        return sumUpdated;
    }
}