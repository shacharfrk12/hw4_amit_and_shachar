public class PathFromRoot {
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
