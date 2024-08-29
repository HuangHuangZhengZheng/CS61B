package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            // 注意，接下来都要init之后再说
            // Not in an initialized Gitlet directory.
            case "add":
                // TODO: handle the `add [filename]` command
                validateInit();
                validateNumArgs("add", args, 2);
                String fileToAdd = args[1];
                Repository.add(fileToAdd);
                break;
            case "commit":
                validateInit();
                validateNumArgs("commit", args, 2);
                String msg = args[1];
                Repository.commit(msg);
                break;
            case "rm":
                validateInit();
                validateNumArgs("rm", args, 2);
                String fileToRemove = args[1];
                Repository.rm(fileToRemove);
                break;
            case "log":
                validateInit();
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "global-log":
                validateInit();
                validateNumArgs("global-log", args, 1);
                Repository.global_log();
                break;
            case "find":
                validateInit();
                validateNumArgs("find", args, 2);
                String msgToFind = args[1];
                Repository.find(msgToFind);
                break;
            case "status":
                validateInit();
                validateNumArgs("status", args, 1);
                Repository.status();
                break;
            case "checkout":
                validateInit();
                validateAndCallCheckout(args);
                break;
            case "branch":
                validateInit();
                validateNumArgs("branch", args, 2);
                String branch = args[1];
                Repository.branch(branch);
                break;
            case "rm-branch":
                validateInit();
                validateNumArgs("rm-branch", args, 2);
                String branchToRemove = args[1];
                Repository.removeBranch(branchToRemove);
                break;
            case "reset":
                validateInit();
                validateNumArgs("reset", args, 2);
                String commitToReset = args[1];
                Repository.reset(commitToReset);
                break;
            case "merge":
                validateInit();
                validateNumArgs("merge", args, 2);
                String branchToMerge = args[1];
                Repository.merge(branchToMerge);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
    /** If a user inputs a command with the wrong number
     * or format
     * of operands*/
    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    // check if init yet for other commands
    public static void validateInit(){
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void validateAndCallCheckout(String[] args){
        boolean valid = false;
        if (args.length == 3 && args[1].equals("--")) {
            valid = true;
            String fileToCheckout = args[2];
            Repository.checkout(Commit.getCurrentCommit().getID(), fileToCheckout);
        }else if (args.length == 4 && args[2].equals("--")) {
            valid = true;
            String commitToCheckoutID = args[1];
            String fileToCheckout = args[3];
            Repository.checkout(commitToCheckoutID, fileToCheckout);
        }else if (args.length == 2) {
            valid = true;
            String branchToCheckout = args[1];
            Repository.checkout(branchToCheckout);
        }
        if (!valid) {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }
}
