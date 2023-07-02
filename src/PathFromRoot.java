public class PathFromRoot {
    /**
     * checks if there's a path from the root of the tree which builds the string str in order
     * @param root the root of the tree
     * @param str the string we want to find a path to
     * @return true if there is a path for this string from the root, false otherwise
     */
    public static boolean doesPathExist(BinNode<Character> root, String str) {
        //end of string
        if(str.isEmpty())
            return true;
        //reached end of branch before finishing path
        if(root == null)
            return false;
        //the current node's letter doesn't continue path
        if(root.getData() != str.charAt(0))
            return false;

        //checking if path for str minus the first word exists in the right or left sub trees
        boolean is_right_path = doesPathExist(root.getRight(), str.substring(1));
        boolean is_left_path = doesPathExist(root.getLeft(), str.substring(1));
        return (is_right_path || is_left_path);
    }

}
