package Parser;

/**
 *
 * Code taken from https://www.javaspring.net/blog/pairs-in-java/
 *
 */
class Pair<T1, T2> {
    private T1 first;
    private T2 second;
 
    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }
 
    public T1 getFirst() {
        return first;
    }
 
    public T2 getSecond() {
        return second;
    }
 
    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}
