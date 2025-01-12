package files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class Streams {
    /**
     * Read from an InputStream until a quote character (") is found, then read
     * until another quote character is found and return the bytes in between the two quotes.
     * If no quote character was found return null, if only one, return the bytes from the quote to the end of the stream.
     *
     * @param in
     * @return A list containing the bytes between the first occurrence of a quote character and the second.
     */
    public static List<Byte> getQuoted(InputStream in) throws IOException {
        List<Byte> result = new ArrayList<>();
        boolean FoundFirst = false;
        int Currentbyte;
        while ((Currentbyte = in.read()) != -1) //Doesnt End
        {
            if (Currentbyte == '"') {
                if (FoundFirst) {
                    return result;  //Second "
                } else {
                    FoundFirst = true;  //First "
                }
            } else if (FoundFirst) {
                result.add((byte) Currentbyte);  //If after first " add byte
            }
        }
        if (FoundFirst) return result; //If we had one " return list
        else return null;              //No " at all.
    }



    /**
     * Read from the input until a specific string is read, return the string read up to (not including) the endMark.
     *
     * @param in      the Reader to read from
     * @param endMark the string indicating to stop reading.
     * @return The string read up to (not including) the endMark (if the endMark is not found, return up to the end of the stream).
     */
    public static String readUntil(Reader in, String endMark) throws IOException {
        if (endMark == null || endMark.isEmpty()) {            //Return all of the stream.
            StringBuilder entireInput = new StringBuilder();
            int curr;
            while ((curr = in.read()) != -1) {
                entireInput.append((char) curr);
            }
            return entireInput.toString();
        }

        StringBuilder result = new StringBuilder();
        StringBuilder flag = new StringBuilder();
        int current;

        while ((current = in.read()) != -1) { //Doesnt End
            char ch = (char) current;
            result.append(ch);
            flag.append(ch);

            if (flag.length() > endMark.length()) {   //If flag is longer than endMark delete first letter in flag.
                flag.deleteCharAt(0);
            }
            if (flag.toString().equals(endMark)) {     //Flag==endMark, we found the endMark string.
                result.setLength(result.length() - endMark.length());  //Delete the endMark from our result.
                return result.toString();    //We need to return string and not StringBuilder.
            }
        }
        return result.toString();  //return everything if the endMark isn't found.
    }

    /**
     * Copy bytes from input to output, ignoring all occurrences of badByte.
     *
     * @param in
     * @param out
     * @param badByte
     */
    public static void filterOut(InputStream in, OutputStream out, byte badByte) throws IOException {
        int badByteInt = badByte & 0xFF;   //bad type can be negative to, so me sure to convert to range 0 to 255.
        int currentByte;
        while ((currentByte = in.read()) != -1) {
            if (currentByte != badByteInt) {
                out.write(currentByte);
            }
        }
    }

    /**
     * Read a 40-bit (unsigned) integer from the stream and return it. The number is represented as five bytes,
     * with the most-significant byte first.
     * If the stream ends before 5 bytes are read, return -1.
     *
     * @param in
     * @return the number read from the stream
     */
    public static long readNumber(InputStream in) throws IOException {
        long result = 0; //long can handle 64 bit values
        for (int i = 0; i < 5; i++) {
            int currentByte = in.read();
            if (currentByte == -1) {
                return -1;
            }
            result = (result << 8) | (currentByte); //We make result "go" 8 digits left and add curr by using OR.
        }
        return result;
    }
}
