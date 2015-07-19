package algtestjclient;

public class Pair<L, R> {
    private final L m_l;
    private final R m_r;

    public Pair(L l, R r) {
        this.m_l = l;
        this.m_r = r;
    }

    public L getL() { return this.m_l; }

    public R getR() { return this.m_r; }
}