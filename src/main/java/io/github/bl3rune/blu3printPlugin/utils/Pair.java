package io.github.bl3rune.blu3printPlugin.utils;

public class Pair<A,B> {

    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

}
