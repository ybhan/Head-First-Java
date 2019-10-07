// Won't compile
// Because i is a null pointer, i.intValue() cannot be called.
// It is wrong to assume i.intValue() == 0 by default.
public class TestBox {
    Integer i;
    int j;

    public static void main(String[] args) {
        TestBox t = new TestBox();
        t.go();
    }

    public void go() {
        j = i;
        System.out.println(j);
        System.out.println(i);
    }
}