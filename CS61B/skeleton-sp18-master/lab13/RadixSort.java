/**
 * Class for doing Radix sort
 *
 * @author Akhil Batra, Alexander Hwang
 *
 */
public class RadixSort {
    /**
     * Does LSD radix sort on the passed in array with the following restrictions:
     * The array can only have ASCII Strings (sequence of 1 byte characters)
     * The sorting is stable and non-destructive
     * The Strings can be variable length (all Strings are not constrained to 1 length)
     *
     * @param asciis String[] that needs to be sorted
     *
     * @return String[] the sorted array
     */
    public static String[] sort(String[] asciis) {
        // TODO: Implement LSD Sort
        if (asciis == null || asciis.length == 1 || asciis.length == 0) {
            return asciis;
        }

        int maxLen = 0;
        for (String ascii : asciis) {
            maxLen = Math.max(ascii.length(), maxLen);
        }

        String[] sorted = new String[asciis.length];
        System.arraycopy(asciis, 0, sorted, 0, asciis.length);
        // begin from right, which is LSD
        for (int index = maxLen - 1; index >=0; index--) {
            sortHelperLSD(sorted, index);
        }

        return sorted;
    }

    /**
     * LSD helper method that performs a destructive counting sort the array of
     * Strings based off characters at a specific index.
     * @param asciis Input array of Strings
     * @param index The position to sort the Strings on.
     */
    private static void sortHelperLSD(String[] asciis, int index) {
        // Optional LSD helper method for required LSD radix sort
        String[] output = new String[asciis.length];
        int[] count = new int[256];
        for (String ascii : asciis) {
            char c = (index < ascii.length())? ascii.charAt(index) : 0;
            count[c]++;
        }
        // Change count[i] so that it contains the actual position of this character in output[]
        for (int i = 1; i < 256; i++) {
            count[i] += count[i - 1];
        }
        // Build the output array. Insert from behind instead of from head.
        for (int i = asciis.length - 1; i >= 0; i--) {
            char charAtIndex = index < asciis[i].length() ? asciis[i].charAt(index) : 0; // Use 0 for out-of-bounds
            output[count[charAtIndex] - 1] = asciis[i];
            count[charAtIndex]--;
        }

        System.arraycopy(output, 0, asciis, 0, asciis.length);
        return;
    }

    /**
     * MSD radix sort helper function that recursively calls itself to achieve the sorted array.
     * Destructive method that changes the passed in array, asciis.
     *
     * @param asciis String[] to be sorted
     * @param start int for where to start sorting in this method (includes String at start)
     * @param end int for where to end sorting in this method (does not include String at end)
     * @param index the index of the character the method is currently sorting on
     *
     **/
    private static void sortHelperMSD(String[] asciis, int start, int end, int index) {
        // Optional MSD helper method for optional MSD radix sort
        return;
    }

    public static void main(String[] args) {
        String[] asciis = {"banana", "apple", "grape", "cherry", "date"};

        String[] sorted = sort(asciis);

        for (String str : sorted) {
            System.out.println(str);
        }
    }
}
