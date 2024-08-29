package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author HHZZ
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File STAGING = join(GITLET_DIR, "staging");
    public static final File STAGING_FILES = join(STAGING, "staging-files");


    public static void init(){
        // check if init yet
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdir();
        }else{
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        // already init gitlet dir
        STAGING.mkdir();
        STAGING_FILES.mkdir();
        Commit.COMMIT_FOLDER.mkdir();
        Commit.BRANCHES.mkdir();
        Commit.BRANCHES_FOLDER.mkdir();
        Blob.BLOB_FOLDER.mkdir();

        // begin commit & head & branch
        Commit commit = new Commit(); // create an "empty" commit
        commit.saveCommit();
        Commit.creatBranch("master", commit.getID());// have master branch
        Commit.checkHead("master"); // setup HEAD as master
    }


    public static void add(String filename){
        List<String> names = plainFilenamesIn(CWD);
        boolean found = false;
        TreeMap<String, String> addedFiles; // tracking files staging for addiction, --->  <Filename, ID>
        File c = null;
        Commit currentCommit = Commit.getCurrentCommit();
        // read add
        File stagingForAdd = join(STAGING, "add");
        if (stagingForAdd.exists()) {
            addedFiles = readObject(stagingForAdd, TreeMap.class); // see more in Things to avoid in spec!
        }else{
            try{
                stagingForAdd.createNewFile();
            } catch (IOException e) {
                // nothing
            }
            addedFiles = new TreeMap<>();
        }
        // find file
        for (String name : names){
            if (name.equals(filename)){
                found = true;
                break;
            }
        }
        if (!found){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        /**
         * If the current working version of the file is identical to the version in the
         * current commit, do not stage it to be added, and remove it from the staging area
         * if it is already there (as can happen when a file is changed, added, and then
         * changed back to it’s original version).
         * */
        String foundFileID = sha1(readContents(join(CWD, filename)));
        if (currentCommit.getBlobsID().containsValue(foundFileID) || addedFiles.containsKey(filename)) {
            restrictedDelete(join(STAGING_FILES, foundFileID));
            addedFiles.remove(filename);
        }
        // copy the file into staging area
        c = join(STAGING_FILES, foundFileID);
        try{
            c.createNewFile();
        } catch (IOException e){
            // nothing here
        }
        writeContents(c, readContents(join(CWD, filename)));
        // update the TreeMap
        addedFiles.put(filename, foundFileID);
        // read-modify-write diagram!
        writeObject(stagingForAdd, addedFiles); // trying to cast, maybe wrong?
    }

    public static void commit(String msg){
        if (msg == null || msg.isBlank()){
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }
        // assume that add is existed
        Map<String, String> addedFiles = readObject(join(STAGING, "add"), TreeMap.class);
        if (addedFiles == null || addedFiles.isEmpty()){
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
        TreeMap<String, String> mergedFiles = new TreeMap<>(); // avoid changing the parent's blobsID
        mergedFiles.putAll(currentCommit.getBlobsID());
        mergedFiles.putAll(addedFiles);
        newCommit.setBlobsID(mergedFiles);
        newCommit.saveCommit();

        // update "head"
        writeContents(join(Commit.BRANCHES_FOLDER, currentBranch), newCommit.getID());

        // copy file into Blobs folder......
        List<String> stagingFileIDs = plainFilenamesIn(STAGING_FILES);
        for (String id : stagingFileIDs){
            File intoCommits = join(Blob.BLOB_FOLDER, id);
            try{
                intoCommits.createNewFile();
            } catch (IOException e){
                // nothing here
            }
            writeContents(intoCommits, readContents(join(STAGING_FILES, id)));
        }
        // clean staging area...
        for (String id : stagingFileIDs){
            restrictedDelete(join(STAGING_FILES, id));
        }
        // clean 'add' and 'remove', wrong?
        restrictedDelete(join(STAGING, "remove"));
        restrictedDelete(join(STAGING, "add"));
    }

    public static void rm(String filename){
        Commit currentCommit = Commit.getCurrentCommit();
        // assume that add is existed
        TreeMap<String, String> addedFiles = readObject(join(STAGING, "add"), TreeMap.class);
        if ((!currentCommit.getBlobsID().containsKey(filename)) || (!addedFiles.containsKey(filename))) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        // read remove
        File stagingForRemove = join(STAGING, "remove");
        TreeMap<String, String> removedFiles;
        if (stagingForRemove.exists()) {
            removedFiles = readObject(stagingForRemove, TreeMap.class); // see more in Things to avoid in spec!
        }else{
            removedFiles = new TreeMap<>();
        }
        /**
         *  If the file is tracked in the current commit, stage it for removal and remove the file
         *  from the working directory if the user has not already done so
         *  (do not remove it unless it is tracked in the current commit).
         * */
        if (currentCommit.getBlobsID().containsKey(filename)) {
            removedFiles.put(filename, currentCommit.getBlobsID().get(filename)); // even though I think that it is useless to store removal's ID......
            restrictedDelete(join(CWD, filename));
        }

        // remove from staging area...
        restrictedDelete(join(STAGING_FILES, addedFiles.get(filename)));
        // read-modify-write ---> update the TreeMaps!
        addedFiles.remove(filename);
        writeObject(stagingForRemove, removedFiles);
        writeObject(join(STAGING, "add"), addedFiles);
    }

    public static void log(){
        Commit currentCommit = Commit.getCurrentCommit();
        while(currentCommit.getParentID() != null){
            System.out.println(currentCommit.toString());
            currentCommit = Commit.fromFile(currentCommit.getParentID());
        }
        System.out.println(currentCommit.toString());
    }
    public static void global_log(){
        List<String> commitsID = plainFilenamesIn(Commit.COMMIT_FOLDER);
        for (String id : commitsID){
            System.out.println(Commit.fromFile(id).toString());
        }
    }

    public static void find(String msg){
        List<String> commitsID = plainFilenamesIn(Commit.COMMIT_FOLDER);
        boolean found = false;
        for (String id : commitsID){
            Commit commit = Commit.fromFile(id);
            if (commit.getMessage().equals(msg)){
                found = true;
                System.out.println(commit.getID());
            }
        }
        if (!found){
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status(){
        // === Branches ===
        System.out.println("=== Branches ===");
        String currentBranch = Commit.getCurrentBranch();
        System.out.println("*" + currentBranch);
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        TreeSet<String> otherBranchNames = new TreeSet<>(branchNames);
        otherBranchNames.remove(currentBranch); // remove the current!
        for (String branchName : otherBranchNames){
            System.out.println(branchName);
        }
        System.out.println("");
        // === Staged Files ===
        System.out.println("=== Staged Files ===");
        if (join(STAGING, "add").exists()){
            TreeMap<String, String> addedFiles = readObject(join(STAGING, "add"), TreeMap.class);
            for (String filename : addedFiles.keySet()){
                System.out.println(filename);
            }
        }
        System.out.println("");
        // === Removed Files ===
        System.out.println("=== Removed Files ===");
        if (join(STAGING, "remove").exists()){
            TreeMap<String, String> removedFiles = readObject(join(STAGING, "remove"), TreeMap.class);
            for (String filename : removedFiles.keySet()){
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


    public static void checkout(String commitID, String filename){
        List<String> commitsID = plainFilenamesIn(Commit.COMMIT_FOLDER);
        // handle shortened id
        if (commitID.length() == 6) {
            for (String id : commitsID){
                if (id.substring(0,7).equals(commitID)){
                    commitID = id;
                }
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
        writeContents(checkedoutFile, readContents(join(Blob.BLOB_FOLDER, commit.getBlobsID().get(filename))));

        // The new version of the file is not staged. If add exists, then update add
        File ADD = join(STAGING, "add");
        if (ADD.exists()) {
            TreeMap<String, String> addedFiles = readObject(ADD, TreeMap.class);
            if (addedFiles.containsKey(filename)) {
                restrictedDelete(join(STAGING_FILES, addedFiles.get(filename)));
                addedFiles.remove(filename);
            }
            writeObject(join(STAGING, "add"), addedFiles);
        }
    }


    public static void checkout(String branchname){
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        if (!branchNames.contains(branchname)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (Commit.getCurrentBranch().equals(branchname)){
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        // store the original one
        String originalBranch = Commit.getCurrentBranch();
        Commit originalCommit = Commit.getCurrentCommit();
        // then switch the branch
        Commit.checkHead(branchname);
        Commit changedCommit = Commit.getCurrentCommit();
        for (String filename : changedCommit.getBlobsID().keySet()) {
            if (!originalCommit.getBlobsID().containsKey(filename)) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                Commit.checkHead(originalBranch); // back to the original branch
                System.exit(0);
            }
        }
        // first delete those in original branch
        for (String filename : originalCommit.getBlobsID().keySet()) {
            restrictedDelete(join(CWD, filename));
        }
        // then add back
        for (String filename : changedCommit.getBlobsID().keySet()) {
            writeContents(join(CWD, filename), Blob.getBlobContent(changedCommit.getBlobsID().get(filename)));
        }
        // clean staging area
        restrictedDeleteAll(STAGING_FILES);
        restrictedDelete(join(STAGING, "add"));
        restrictedDelete(join(STAGING, "remove"));
    }

    public static void branch(String branch){
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        if (branchNames.contains(branch)){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        Commit currentCommit = Commit.getCurrentCommit();
        File newBranch = join(Commit.BRANCHES_FOLDER, branch);
        writeContents(newBranch, currentCommit.getID());
    }

    public static void removeBranch(String branchname){
        List<String> branchNames = plainFilenamesIn(Commit.BRANCHES_FOLDER);
        if (!branchNames.contains(branchname)){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if(readContentsAsString(join(Commit.BRANCHES, "head")).equals(branchname)){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        restrictedDelete(join(Commit.BRANCHES_FOLDER, branchname));
    }

    public static void reset(String commitID){
        Commit target = Commit.fromFile(commitID);
        String targetBranch = target.getBranch();
        checkout(targetBranch);
        Commit tBHead = Commit.getCurrentCommit();
        for (String filename : tBHead.getBlobsID().keySet()) {
            restrictedDelete(join(CWD, filename));
        }
        for (String filename : target.getBlobsID().keySet()) {
            writeContents(join(CWD, filename), Blob.getBlobContent(target.getBlobsID().get(filename)));
        }
        writeContents(join(Commit.BRANCHES_FOLDER, targetBranch), target.getID());
    }

    public static void merge(String branchname){

    }



    // some private helper functions here.
    private static void restrictedDeleteAll(File Dir){
        // assume that file already exists
        List<String> fileNames = plainFilenamesIn(Dir);
        for (String filename : fileNames){
            restrictedDelete(join(Dir, filename));
        }
    }
}
