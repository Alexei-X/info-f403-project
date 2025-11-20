package Parser;

/**
 * Implementation of Pair for 2 different types
 *
 * @author Code taken from https://www.javaspring.net/blog/pairs-in-java/
 */
class Pair<T1, T2> {
    /** First element of the pair*/
    private T1 first;
    /** Second element of the pair*/
    private T2 second;
 
    /**
     * Pair constructor storing first and second in corresponding attributes
     *
     * @param first the first element of the pair
     * @param second the second element of the pair
     */
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
 
    /**
     * Return first element of pair
     *
     * @return first element of pair
     */
    public T1 getFirst() {
        return first;
    }
 
    /**
     * Return second element of pair
     *
     * @return second element of pair
     */
    public T2 getSecond() {
        return second;
    }
 
    /**
     * Converts the pair to a String
     *
     * @return String containing first and second element
     */
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
