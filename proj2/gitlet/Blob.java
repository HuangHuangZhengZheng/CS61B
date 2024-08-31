package gitlet;

import java.io.File;

public class Blob {
    static final File BLOB_FOLDER = Utils.join(Repository.GITLET_DIR, "blobs");
    public static byte[] getBlobContent(String sha1ID) {
        return Utils.readContents(Utils.join(BLOB_FOLDER, sha1ID));
    }
    public static String getBlobContentAsString(String sha1ID) {
        return Utils.readContentsAsString(Utils.join(BLOB_FOLDER, sha1ID));
    }
}
