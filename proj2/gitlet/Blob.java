package gitlet;

import java.io.File;
import java.io.IOException;

public class Blob {
    static final File BLOB_FOLDER = Utils.join(Repository.GITLET_DIR, "blobs");


    public static byte[] getBlobContent(String sha1ID){
        return Utils.readContents(Utils.join(BLOB_FOLDER, sha1ID));
    }
}