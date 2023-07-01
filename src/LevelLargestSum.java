import java.util.ArrayDeque;

/**
 * class that contains the function which finds out the level of a tree, that its elements has the largest sum
 */
public class LevelLargestSum {
    /**
     * calculates the level of the tree which the sum of its elements is the largest
     * @param root the root of the tree
     * @return the level of the tree which the sum of its elements is the largest
     */
    public static int getLevelWithLargestSum(BinNode<Integer> root) {
        // if root is null return -1
        if (root == null) {
            return -1;
        }

        //if root exists, initiate the elements queue with the root
        ArrayDeque<BinNode<Integer>> currLevelQueue = new ArrayDeque<>(); // a queue that contains elements from 1
        // level, then remove them one by one while adding elements from the next level.
        currLevelQueue.add(root);

        //initiate parameters by level 0 (the root)
        int maxSum = root.getData();
        int levelMaxSum = 0;
        int numCurrLevelElements = 1;
        int numNextLevelElements = 0;
        int currSum = 0;
        int currLevel = 0;
        BinNode<Integer> currElement;

        // while there is elements that wasn't taken in the calculations
        while(!currLevelQueue.isEmpty()) {
            // run over the elements of 1 level
            for(int i = 0; i < numCurrLevelElements; i++) {
                // remove parent and add him to the calculations
                currElement = currLevelQueue.remove();
                currSum += currElement.getData();
                // add left son if exists
                if (currElement.getLeft() != null) {
                    currLevelQueue.add(currElement.getLeft());
                    numNextLevelElements += 1;
                }
                // add right son if exists
                if (currElement.getRight() != null) {
                    currLevelQueue.add(currElement.getRight());
                    numNextLevelElements += 1;
                }
            }
            // change LevelMaxSum if needed
            if (currSum > maxSum) {
                maxSum = currSum;
                levelMaxSum = currLevel;
            }
            // initiate parameters for the next level iteration
            numCurrLevelElements = numNextLevelElements;
            numNextLevelElements = 0;
            currSum = 0;
            currLevel++;
        }
        return levelMaxSum;
    }
}
