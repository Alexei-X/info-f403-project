package Parser;

/**
 * Implementation of Pair for 2 different types
 *
 * @author Code taken from https://www.javaspring.net/blog/pairs-in-java/
 */
class Pair<T1, T2> {
    private T1 first;
    private T2 second;
 
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
