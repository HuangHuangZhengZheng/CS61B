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
        List<String> ids = plainFilenamesIn(join(COMMIT_FOLDER));
        if (!ids.contains(id)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
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

    public static void validateBranch(String branch) {
        List<String> branches = plainFilenamesIn(BRANCHES_FOLDER);
        if (!branches.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
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

    public static String getBranchHeadID(String branch) {
        return readContentsAsString(join(Commit.BRANCHES_FOLDER, branch));
    }

    public static Commit findSplitPoint(String commitID1, String commitID2) {
        Set<String> marked = new TreeSet<>();
        bfsMark(commitID1, marked);
        String foundID = bfsFind(commitID2, marked);
        return fromFile(foundID);
    }
    private static void bfsMark(String commitID, Set<String> marked) {
        // only mark
        Queue<String> q = new LinkedList<>();
        q.add(commitID);
        marked.add(commitID); // mark current commit
        while (!q.isEmpty()) {
            String s = q.poll();
            Commit c = fromFile(s);
            List<String> parentIDs = new ArrayList<>();
            // can append null into the list, do not want null
            if (c.parentID != null) {
                parentIDs.add(c.parentID);
            }
            if (c.secondParentID != null) {
                parentIDs.add(c.secondParentID);
            }

            for (String parentID : parentIDs) {
                if (!marked.contains(parentID)) {
                    marked.add(parentID);
                    q.add(parentID);
                }
            }
        }
    }

    private static String bfsFind(String commitID, Set<String> marked) {
        Queue<String> q = new LinkedList<>();
        q.add(commitID);
        if (marked.contains(commitID)) {
            return commitID;
        }
        while (!q.isEmpty()) {
            String s = q.poll();
            Commit c = fromFile(s);
            List<String> parentIDs = new ArrayList<>();
            parentIDs.add(c.parentID);
            parentIDs.add(c.secondParentID);
            for (String parentID : parentIDs) {
                if (marked.contains(parentID)) {
                    return parentID;
                }
            }
        }
        return "L$P"; // avoid null
    }

    public static String getCorrectID(String filename, Commit s, Commit c, Commit o) {
        if (s.getBlobsID().containsKey(filename)) {
            // assume that sID is not NULL!
            String sID = s.getBlobsID().get(filename);
            String cID = c.getBlobsID().get(filename);
            String oID = o.getBlobsID().get(filename);
            // 6, 7
            if ((cID != null && cID.equals(sID)) && oID == null) {
                return null; // special cases
            }
            if ((oID != null && oID.equals(sID)) && cID == null) {
                return null;
            }
            // 1, 2
            if (!Objects.equals(sID, oID) && Objects.equals(sID, cID)) {
                return oID;
            }
            if (!Objects.equals(sID, cID) && Objects.equals(sID, oID)) {
                return cID;
            }

            // 3.1 in the same way
            if (Objects.equals(cID, oID)) {
                return cID;
            }
            // 3.2 Conflict!
            if ((!Objects.equals(sID, oID))
                    && (!Objects.equals(sID, cID))
                    && (!Objects.equals(cID, oID))) {
                System.out.println("Encountered a merge conflict.");
                // create the conflict file in BlobFolder
                // then return the new ID
                String contentsOfCurrent = "";
                String contentsOfGiven = "";
                if (cID != null) {
                    contentsOfCurrent = Blob.getBlobContentAsString(cID);
                }
                if (oID != null) {
                    contentsOfGiven = Blob.getBlobContentAsString(oID);
                }
                String conflictContents = String.format(
                        "<<<<<<< HEAD\n%s=======\n%s>>>>>>>\n",
                        contentsOfCurrent, contentsOfGiven
                );
                // directly save into Blobs
                String conflictContentsID = Utils.sha1(conflictContents); // may wrong
                Utils.writeContents(join(Blob.BLOB_FOLDER, conflictContentsID), conflictContents);
                return conflictContentsID;
            }

        } else {
            // 4, 5
            if (!o.blobsID.containsKey(filename) && c.blobsID.containsKey(filename)) {
                return c.blobsID.get(filename);
            }

            if (!c.blobsID.containsKey(filename) && o.blobsID.containsKey(filename)) {
                return o.blobsID.get(filename);
            }
        }
        return "L$P"; // avoid null
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
            ID, parentID.substring(0, 7), secondParentID.substring(0, 7),
                    timestamp, message); // 7?
        }
    }

    private static byte[] serializeTreeMap(TreeMap<String, String> treeMap) {
        try (ByteArrayOutputStream bArrOutS = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(bArrOutS)) {
            objectOutputStream.writeObject(treeMap);
            objectOutputStream.flush();
            return bArrOutS.toByteArray();
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
