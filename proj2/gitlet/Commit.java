package gitlet;

import java.text.SimpleDateFormat;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *
 *
 *  @author HHZZ
 */
public class Commit implements Serializable {
    /**
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    static final File COMMIT_FOLDER = Utils.join(Repository.CWD, ".gitlet", "commits");
    static final File BRANCHES = Utils.join(Repository.CWD, ".gitlet", "commits", "branches");
    static final File BRANCHES_FOLDER = Utils.join(BRANCHES, "staging-branches");

    private final String ID;

    private final String message;
    private final String timestamp;
    private final String parentID;
    private String secondParentID;
    private TreeMap<String, String> blobsID; // ---> <filename, ID>
    private final String branch;

    /* init "empty" one */
    public Commit() {
        message = "initial commit";
        parentID = null;
        secondParentID = null;
        Date z = new Date(0);
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        timestamp = df.format(z);
        blobsID = new TreeMap<>(); // empty
        branch = "master";
        if (serializeTreeMap(blobsID) == null) {
            ID = Utils.sha1(message, timestamp);
        } else {
            ID = Utils.sha1(message, timestamp, serializeTreeMap(blobsID));
        }
    }

    public Commit(String message, String parentID, TreeMap<String, String> blobsID) {
        this.message = message;
        this.parentID = parentID;
        this.blobsID = blobsID;
        Date now = new Date();
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy Z", Locale.ENGLISH);
        timestamp = df.format(now);
        secondParentID = null; // wrong maybe when come into merge

        branch = getCurrentBranch();
        if (serializeTreeMap(blobsID) == null) {
            ID = Utils.sha1(message, timestamp, parentID);
        } else {
            ID = Utils.sha1(message, timestamp, parentID, serializeTreeMap(blobsID));
        }
    }

    public static Commit fromFile(String id) {
        return Utils.readObject(Utils.join(COMMIT_FOLDER, id), Commit.class);
    }

    public void saveCommit() {
        File c = new File(COMMIT_FOLDER, ID);
        try {
            c.createNewFile();
        } catch (IOException e) {
            // do nothing;
        }
        Utils.writeObject(c, this);
    }

    public static void creatBranch(String branchName, String commitID) {
        File b = new File(BRANCHES_FOLDER, branchName);
        try {
            b.createNewFile();
        } catch (IOException e) {
            // nothing
        }
        Utils.writeContents(b, commitID);
    }
    // update or create head, storing String current branch_name
    public static void checkHead(String branchName) {
        File h = Utils.join(BRANCHES, "head");
        try {
            h.createNewFile();
        } catch (IOException e) {
            //nothing
        }
        Utils.writeContents(h, branchName);
    }

    public static Commit getCurrentCommit() {
        String currentBranch = readContentsAsString(join(Commit.BRANCHES, "head"));
        String headCommitID = readContentsAsString(join(Commit.BRANCHES_FOLDER, currentBranch));
        Commit c = fromFile(headCommitID);
        return c;
    }
    public static String getCurrentBranch() {
        return readContentsAsString(join(Commit.BRANCHES, "head"));
    }


    // helper functions below
    @Override
    public String toString() {
        if (secondParentID == null) {
            return String.format(
              "===\ncommit %s\nDate: %s\n%s\n",
            ID, timestamp, message); // 结尾不需要 \n ?
        } else {
            return String.format(
              "===\ncommit %s\nMerge: %s %s\nDate: %s\n%s\n",
            ID, parentID.substring(0, 8), secondParentID.substring(0, 8), timestamp, message); // 结尾不需要 \n ?
        }
    }

    private static byte[] serializeTreeMap(TreeMap<String, String> treeMap) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(treeMap);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void setBranchHeadID(String branch, String commitID) {
        writeContents(join(Commit.BRANCHES_FOLDER, branch), commitID);
    }


    public String getID() {
        return ID;
    }
    public String getMessage() {
        return message;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public String getParentID() {
        return parentID;
    }
    public String getSecondParentID() {
        return secondParentID;
    }
    public void setSecondParentID(String secondParentID) {
        this.secondParentID = secondParentID;
    }
    public TreeMap<String, String> getBlobsID() {
        return blobsID;
    }
    public void setBlobsID(TreeMap<String, String> blobsID) {
        this.blobsID = blobsID;
    }
    public String getBranch() {
        return branch;
    }
}
