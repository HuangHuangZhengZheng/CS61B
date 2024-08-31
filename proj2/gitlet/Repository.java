package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author HHZZ
 */
public class Repository {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory.
     * note that Staging Area is considered to be part of Repo instead of a java class
     * */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGING = join(GITLET_DIR, "staging");
    public static final File STAGING_FILES = join(STAGING, "staging-files");


    public static void init() {
        // check if init yet
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        } else {
            System.out.println("A Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        }
        // already init gitlet dir, begin the SA
        STAGING.mkdir();
        STAGING_FILES.mkdir();
        File stagingForAdd = join(STAGING, "add");
        File stagingForRemove = join(STAGING, "remove");
        TreeMap<String, String> addedFiles = new TreeMap<>();
        TreeMap<String, String> removedFiles = new TreeMap<>();
        try {
            stagingForAdd.createNewFile();
            stagingForRemove.createNewFile();
        } catch (IOException e) {
            // nothing here
        }
        writeObject(stagingForAdd, addedFiles);
        writeObject(stagingForRemove, removedFiles);


        Commit.COMMIT_FOLDER.mkdir();
        Commit.BRANCHES.mkdir();
        Commit.BRANCHES_FOLDER.mkdir();
        Blob.BLOB_FOLDER.mkdir();

        // begin commit & head & branch
        Commit commit = new Commit(); // create an "empty" commit
        commit.saveCommit();
        Commit.creatBranch("master", commit.getID()); // have master branch
        Commit.checkHead("master"); // setup HEAD as master
    }


    public static void add(String filename) {
        List<String> names = plainFilenamesIn(CWD);
        boolean found = false;
        TreeMap<String, String> addedFiles, removedFiles; // <Filename, ID>
        File c = null;
        Commit currentCommit = Commit.getCurrentCommit();
        // read add
        File stagingForAdd = join(STAGING, "add");
        File stagingForRemove = join(STAGING, "remove");

        addedFiles = readObject(stagingForAdd, TreeMap.class);
        removedFiles = readObject(stagingForRemove, TreeMap.class);

        // find file
        for (String name : names) {
            if (name.equals(filename)) {
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        /**
         * If the current working version of the file is identical to the version in the
         * current commit, do not stage it to be added, and remove it from the staging area
         * if it is already there (as can happen when a file is changed, added, and then
         * changed back to it’s original version).
         * */
        // first try to update Remove
        if (removedFiles.containsKey(filename)) {
            removedFiles.remove(filename);
            writeObject(stagingForRemove, removedFiles);
        }
        String foundFileID = sha1(readContents(join(CWD, filename)));
        if (currentCommit.getBlobsID().containsKey(filename)
                && currentCommit.getBlobsID().
                get(filename).equals(foundFileID)) {
            MyUtils.restrictedDelete(join(STAGING_FILES, foundFileID));
            addedFiles.remove(filename);
            writeObject(stagingForAdd, addedFiles);
            return;
        }
        if (addedFiles.containsKey(filename)) {
            MyUtils.restrictedDelete(join(STAGING_FILES, foundFileID));
            addedFiles.remove(filename);
        }
        // copy the file into staging area
        c = join(STAGING_FILES, foundFileID);
        try {
            c.createNewFile();
        } catch (IOException e) {
            // nothing here
        }
        writeContents(c, readContents(join(CWD, filename)));
        // update the TreeMap
        addedFiles.put(filename, foundFileID);
        // read-modify-write diagram!
        writeObject(stagingForAdd, addedFiles);
    }

    public static void commit(String msg) {
        if (msg == null || msg.isBlank()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        File stagingForAdd = join(STAGING, "add");
        File stagingForRemove = join(STAGING, "remove");
        TreeMap<String, String> addedFiles = readObject(stagingForAdd, TreeMap.class);
        TreeMap<String, String> removedFiles = readObject(stagingForRemove, TreeMap.class);
        if (addedFiles.isEmpty() && removedFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        /**
         * By default, each commit’s snapshot of files
         * will be exactly the same as its parent commit’s snapshot of files;
         * init new commit and begin setting blobsID......
         * do not forget to save new commit!
         * */
        Commit currentCommit = Commit.getCurrentCommit();
        String currentBranch = Commit.getCurrentBranch();
        Commit newCommit = new Commit(msg, currentCommit.getID(), currentCommit.getBlobsID());
        TreeMap<String, String> mergedFiles = new TreeMap<>();
        // avoid changing the parent's blobsID
        mergedFiles.putAll(currentCommit.getBlobsID());
        mergedFiles.putAll(addedFiles);
        for (String removal : removedFiles.keySet()) {
            mergedFiles.remove(removal);
        }
        newCommit.setBlobsID(mergedFiles);
        newCommit.saveCommit();
        // update "head"
        Commit.setBranchHeadID(currentBranch, newCommit.getID());

        // copy file into Blobs folder......
        List<String> stagingFileIDs = plainFilenamesIn(STAGING_FILES);
        for (String id : stagingFileIDs) {
            File intoCommits = join(Blob.BLOB_FOLDER, id);
            try {
                intoCommits.createNewFile();
            } catch (IOException e) {
                // nothing here
            }
            writeContents(intoCommits, readContents(join(STAGING_FILES, id)));
        }
        // clean staging area...
        for (String id : stagingFileIDs) {
            MyUtils.restrictedDelete(join(STAGING_FILES, id));
        }
        // clean 'add' and 'remove', wrong? DO NOT DELETE the TreeMaps!
        addedFiles.clear();
        writeObject(stagingForAdd, addedFiles);
        removedFiles.clear();
        writeObject(stagingForRemove, removedFiles);
    }

    public static void rm(String filename) {
        Commit currentCommit = Commit.getCurrentCommit();
        // assume that add is existed
        TreeMap<String, String> addedFiles = readObject(join(STAGING, "add"), TreeMap.class);
        if ((!currentCommit.getBlobsID().containsKey(filename))
                && (!addedFiles.containsKey(filename))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        // read remove
        File stagingForRemove = join(STAGING, "remove");
        TreeMap<String, String> removedFiles;
        if (stagingForRemove.exists()) {
            removedFiles = readObject(stagingForRemove, TreeMap.class);
        } else {
            removedFiles = new TreeMap<>();
        }
        /**
         *  If the file is tracked in the current commit,
         *  stage it for removal and remove the file
         *  from the working directory if the user has not already done so
         *  (do not remove it unless it is tracked in the current commit).
         * */
        if (currentCommit.getBlobsID().containsKey(filename)) {
            removedFiles.put(filename, currentCommit.getBlobsID().get(filename));
            // even though I think that it is useless to store removal's ID......
            MyUtils.restrictedDelete(join(CWD, filename));
        }

        if (addedFiles.containsKey(filename)) {
            MyUtils.restrictedDelete(join(STAGING_FILES, addedFiles.get(filename)));
        }
        // read-modify-write ---> update the TreeMaps!
        addedFiles.remove(filename);
        writeObject(stagingForRemove, removedFiles);
        writeObject(join(STAGING, "add"), addedFiles);
    }

    public static void log() {
        Commit currentCommit = Commit.getCurrentCommit();
        while (currentCommit.getParentID() != null) {
            System.out.println(currentCommit.toString());
            currentCommit = Commit.fromFile(currentCommit.getParentID());
        }
        System.out.println(currentCommit.toString());
    }

    public static void globalLog() {
        List<String> commitsID = plainFilenamesIn(Commit.COMMIT_FOLDER);
        for (String id : commitsID) {
            System.out.println(Commit.fromFile(id).toString());
        }
    }

    public static void find(String msg) {
        List<String> commitsID = plainFilenamesIn(Commit.COMMIT_FOLDER);
        boolean found = false;
        for (String id : commitsID) {
            Commit commit = Commit.fromFile(id);
            if (commit.getMessage().equals(msg)) {
                found = true;
                System.out.println(commit.getID());
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        // === Branches ===
        System.out.println("=== Branches ===");
        String currentBranch = Commit.getCurrentBranch();
        System.out.println("*" + currentBranch);
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        TreeSet<String> otherBranchNames = new TreeSet<>(branchNames);
        otherBranchNames.remove(currentBranch); // remove the current!
        for (String branchName : otherBranchNames) {
            System.out.println(branchName);
        }
        System.out.println("");
        // === Staged Files ===
        System.out.println("=== Staged Files ===");
        if (join(STAGING, "add").exists()) {
            TreeMap<String, String> addedFiles = readObject(join(STAGING, "add"), TreeMap.class);
            for (String filename : addedFiles.keySet()) {
                System.out.println(filename);
            }
        }
        System.out.println("");
        // === Removed Files ===
        System.out.println("=== Removed Files ===");
        if (join(STAGING, "remove").exists()) {
            TreeMap<String, String> removedFiles = readObject(join(STAGING, "remove"),
                    TreeMap.class);
            for (String filename : removedFiles.keySet()) {
                System.out.println(filename);
            }
        }
        System.out.println("");
        // === Modifications Not Staged For Commit === extra credit!
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println("");
        // === Untracked Files === extra credit!
        System.out.println("=== Untracked Files ===");
        System.out.println("");
    }


    public static void checkout(String commitID, String filename) {
        List<String> commitsID = plainFilenamesIn(Commit.COMMIT_FOLDER);
        // handle shortened id
        for (String id : commitsID) {
            if (id.substring(0, commitID.length()).
                    equals(commitID)) {
                commitID = id;
            }
        }

        if (!commitsID.contains(commitID)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Commit.fromFile(commitID);
        if (!commit.getBlobsID().containsKey(filename)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        File checkedoutFile = join(CWD, filename);
        writeContents(checkedoutFile,
                readContents(join(Blob.BLOB_FOLDER,
                        commit.getBlobsID().get(filename))));

        // The new version of the file is not staged. If add exists, then update add
        File stagingForAdd = join(STAGING, "add");
        if (stagingForAdd.exists()) {
            TreeMap<String, String> addedFiles = readObject(stagingForAdd, TreeMap.class);
            if (addedFiles.containsKey(filename)) {
                MyUtils.restrictedDelete(join(STAGING_FILES, addedFiles.get(filename)));
                addedFiles.remove(filename);
            }
            writeObject(join(STAGING, "add"), addedFiles);
        }
    }

    // 有问题！
    public static void checkout(String branchname) {
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        if (!branchNames.contains(branchname)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (Commit.getCurrentBranch().equals(branchname)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        // store the original one
        String originalBranch = Commit.getCurrentBranch();
        Commit originalCommit = Commit.getCurrentCommit();
        // then switch the branch
        Commit.checkHead(branchname);
        Commit changedCommit = Commit.getCurrentCommit();
        List<String> currentWorkingDirFileNames = plainFilenamesIn(CWD);
        for (String filename
                : changedCommit.getBlobsID().keySet()) {
            if (!originalCommit.getBlobsID().containsKey(filename)
                    && currentWorkingDirFileNames.contains(filename)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                Commit.checkHead(originalBranch); // back to the original branch
                System.exit(0);
            }
        }
        // first delete those in original branch
        for (String filename
                : originalCommit.getBlobsID().keySet()) {
            MyUtils.restrictedDelete(join(CWD, filename));
        }
        // then add back
        for (String filename : changedCommit.getBlobsID().keySet()) {
            writeContents(join(CWD, filename),
                    Blob.getBlobContent(changedCommit.getBlobsID().get(filename)));
        }
        // clean staging area, add and reomove
        MyUtils.restrictedDeleteAll(STAGING_FILES);
        clearStagingForAddAndRemove();
    }

    public static void branch(String branch) {
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        if (branchNames.contains(branch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit currentCommit = Commit.getCurrentCommit();
        Commit.creatBranch(branch, currentCommit.getID());
    }

    public static void removeBranch(String branchname) {
        Commit.validateBranch(branchname);
        if (readContentsAsString(join(Commit.BRANCHES, "head"))
                .equals(branchname)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        MyUtils.restrictedDelete(join(Commit.BRANCHES_FOLDER, branchname));
    }

    public static void reset(String commitID) {
        Commit target = Commit.fromFile(commitID);
        String targetBranch = target.getBranch();
        Commit.setBranchHeadID(targetBranch, target.getID());
        if (!targetBranch.equals(Commit.getCurrentBranch())) {
            checkout(targetBranch);
        }
        Commit tBHead = Commit.getCurrentCommit();
        for (String filename : tBHead.getBlobsID().keySet()) {
            MyUtils.restrictedDelete(join(CWD, filename));
        }
        for (String filename : target.getBlobsID().keySet()) {
            writeContents(join(CWD, filename),
                    Blob.getBlobContent(target.getBlobsID().get(filename)));
        }
        clearStagingForAddAndRemove();
    }
    // finally
    public static void merge(String branchname) {
        if (!stagingForAddAndRemoveIsCleared()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        //  If a branch with the given name does not exist
        Commit.validateBranch(branchname);
        // if merge itself...
        if (Commit.getCurrentBranch().equals(branchname)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        /**
         * 我认为应该预先备好 新的Commit的BlobID然后再来判断
         * If merge would generate an error because the commit that
         * it does has no changes in it, just let the normal commit
         * error message for this go through.
         *
         * If an untracked file
         * in the current commit would be overwritten or deleted by
         * the merge, print There is an untracked file in the way;
         * delete it, or add and commit it first. and exit;
         * */

        // find SP
        Commit currentBranchCommit = Commit.getCurrentCommit();
        Commit givenBranchCommit = Commit.fromFile(Commit.getBranchHeadID(
                branchname
        ));
        Commit splitPoint = Commit.findSplitPoint(Commit.getCurrentCommit().getID(),
                Commit.getBranchHeadID(branchname));
        if (splitPoint.getID()
                .equals(Commit.getBranchHeadID(branchname))) {
            System.out.println("Given branch is an ancestor"
                    + " of the current branch.");
            System.exit(0);
        }
        /**
         * If the split point is the current branch, then
         * the effect is to check out the given branch
         * */
        // special merges below...
        if (splitPoint.getID()
                .equals(Commit.getCurrentCommit().getID())) {
            checkout(branchname);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }

        // 以上情况全部通过，现在开始制定写入规则 create the TreeMap for new commit
        TreeMap<String, String> collectedBlobsID = new TreeMap<>();
        TreeMap<String, String> blobsFI = new TreeMap<>();
        // collect all the filenames
        collectedBlobsID.putAll(currentBranchCommit.getBlobsID());
        collectedBlobsID.putAll(givenBranchCommit.getBlobsID());
        collectedBlobsID.putAll(splitPoint.getBlobsID());
        // find the correct file ID
        for (String filename : collectedBlobsID.keySet()) {
            String correctID = Commit.getCorrectID(filename,
                    splitPoint, currentBranchCommit, givenBranchCommit);

            blobsFI.put(filename, correctID);
        }
        // check carefully before do sth
        List<String> originalFileNames = plainFilenamesIn(CWD);
        for (String filename : originalFileNames) {
            String id = sha1(readContents(join(CWD, filename)));
            if ((!currentBranchCommit.getBlobsID().containsKey(filename))
                    && (blobsFI.containsKey(filename))
                    && (!Objects.equals(blobsFI.get(filename), id))) {
                System.out.println("There is an untracked file"
                        + " in the way; delete it, or add and "
                        + "commit it first.");
                System.exit(0);
            }
        }
        // remove null ID and reset the collectedBlobs
        collectedBlobsID.clear();
        originalFileNames.clear(); // for keep to delete
        for (String filename : blobsFI.keySet()) {
            if (blobsFI.get(filename) == null) {
                // check carefully before do sth
                originalFileNames.add(filename);
                // keep to delete
                continue;
            }
            collectedBlobsID.put(filename, blobsFI.get(filename));
        }
        // If no changes......
        if (collectedBlobsID.equals(currentBranchCommit.getBlobsID())) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        // remove null in CWD
        for (String filename : originalFileNames) {
            MyUtils.restrictedDelete(join(CWD, filename));
        }
        // put into CWD
        for (String filename : collectedBlobsID.keySet()) {
            writeContents(join(CWD, filename),
                    Blob.getBlobContent(collectedBlobsID.get(filename)));
        }

        // done with blobsFI
        /**
         * mergedCommit branch is the current branch...
         * */
        String mergeInfo = "Merged " + branchname
                + " into " + Commit.getCurrentBranch() + ".";

        Commit mergedCommit = new Commit(mergeInfo,
                currentBranchCommit.getID(), currentBranchCommit.getBlobsID());

        mergedCommit.setSecondParentID(givenBranchCommit.getID());
        mergedCommit.setBlobsID(collectedBlobsID); // set!
        mergedCommit.saveCommit();
        // update the two branch heads
        Commit.setBranchHeadID(Commit.getCurrentBranch(), mergedCommit.getID());
        Commit.setBranchHeadID(branchname, mergedCommit.getID());
    }



    // helper functions

    private static void clearStagingForAddAndRemove() {
        File stagingForAdd = join(STAGING, "add");
        File stagingForRemove = join(STAGING, "remove");
        TreeMap<String, String> addedFiles = readObject(stagingForAdd, TreeMap.class);
        TreeMap<String, String> removedFiles = readObject(stagingForRemove, TreeMap.class);
        addedFiles.clear();
        removedFiles.clear();
        writeObject(stagingForAdd, addedFiles);
        writeObject(stagingForRemove, removedFiles);
    }

    private static boolean stagingForAddAndRemoveIsCleared() {
        File stagingForAdd = join(STAGING, "add");
        File stagingForRemove = join(STAGING, "remove");
        TreeMap<String, String> addedFiles = readObject(stagingForAdd, TreeMap.class);
        TreeMap<String, String> removedFiles = readObject(stagingForRemove, TreeMap.class);
        if (addedFiles.isEmpty() && removedFiles.isEmpty()) {
            return true;
        }
        return false;
    }

}
