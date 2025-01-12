package files;

import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomAccess {
    /**
     * Treat the file as an array of (unsigned) 8-bit values and sort them
     * in-place using a bubble-sort algorithm.
     * You may not read the whole file into memory!
     *
     * @param file
     */
    public static void sortBytes(RandomAccessFile file) throws IOException {
        boolean swapped = true;
        long n = file.length(); //returns file length in bytes
        if (n <= 1) return; //No need to sort

        for (int i = 0; i < n - 1; i++) {
            swapped = false;
            for (int j = 0; j < n - i - 1; j++) {
                file.seek(j); //Like using an array.
                int byte1 = file.read();
                int byte2 = file.read();
                if (byte1 > byte2) //If true, swap.
                {
                    file.seek(j);
                    file.write(byte2);
                    file.write(byte1);
                    swapped = true;
                }
            }
            if (!swapped) //If we didnt swap anything the file is sorted and we can finish.
                break;
        }
    }

    /**
     * Treat the file as an array of unsigned 24-bit values (stored MSB first) and sort
     * them in-place using a bubble-sort algorithm.
     * You may not read the whole file into memory!
     *
     * @param file
     * @throws IOException
     */
    public static void sortTriBytes(RandomAccessFile file) throws IOException {
        boolean swapped = true;
        long n = file.length();          //Returns file length in bytes
        if (n < 6 || n % 3 != 0) return;  //No need to sort

        for (int i = 0; i < (n / 3) - 1; i++) {
            swapped = false;

            for (int j = 0; j < (n / 3) - i - 1; j++) {
                file.seek(j * 3);
                int byte1 = file.read() << 16 | file.read() << 8 | file.read();
                int byte2 = file.read() << 16 | file.read() << 8 | file.read();
                if (byte1 > byte2) //If true, swap.
                {
                    file.seek(j * 3);
                    file.write(byte2 >> 16);
                    file.write(byte2 >> 8);
                    file.write(byte2);

                    file.write(byte1 >> 16);
                    file.write(byte1 >> 8);
                    file.write(byte1);
                    swapped = true;
                }
            }
            if (!swapped) //If we didnt swap anything the file is sorted and we can finish.
                break;
        }
    }
}
//result = (result << 8) | (currentByte & 0xFF);