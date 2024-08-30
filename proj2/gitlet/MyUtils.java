package gitlet;

import java.io.File;
import java.util.List;

import static gitlet.Utils.*;

public class MyUtils {
    // close same as the one in Utils.java, but do not care about the parent directory
    static boolean restrictedDelete(File file) {
        if (!file.isDirectory()) {
            return file.delete();
        } else {
            return false;
        }
    }
    // same as above...
    static void restrictedDeleteAll(File dir) {
        List<String> fileNames = plainFilenamesIn(dir);
        for (String filename : fileNames) {
            restrictedDelete(join(dir, filename));
        }
    }
}
