package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author HHZZ
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Repository.init();
                break;
            case "add":
                validateInit();
                validateNumArgs("add", args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateInit();
                validateNumArgs("commit", args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateInit();
                validateNumArgs("rm", args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateInit();
                validateNumArgs("log", args, 1);
                Repository.log();
                break;
            case "global-log":
                validateInit();
                validateNumArgs("global-log", args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateInit();
                validateNumArgs("find", args, 2);
                Repository.find(args[1]);
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
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                validateInit();
                validateNumArgs("rm-branch", args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                validateInit();
                validateNumArgs("reset", args, 2);
                Repository.reset(args[1]);
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
    public static void validateInit() {
        if (!Repository.GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void validateAndCallCheckout(String[] args) {
        boolean valid = false;
        if (args.length == 3 && args[1].equals("--")) {
            valid = true;
            String fileToCheckout = args[2];
            Repository.checkout(Commit.getCurrentCommit().getID(), fileToCheckout);
        } else if (args.length == 4 && args[2].equals("--")) {
            valid = true;
            String commitToCheckoutID = args[1];
            String fileToCheckout = args[3];
            Repository.checkout(commitToCheckoutID, fileToCheckout);
        } else if (args.length == 2) {
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
