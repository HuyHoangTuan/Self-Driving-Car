package com.company;

public class Cell implements Comparable<Cell>
{
    public long row;
    public long col;
    public long heuristicCost = 0;
    public long cost;
    public long hcost;
    public Cell prev;
    public int direction;
    public int blocks;
    private long w;
    private long h;

    public long calHeuristicCost(long x, long y)
    {
        return Math.abs(row-x)*Math.abs(row-x)+Math.abs(col-y)*Math.abs(col-y);
    }


    @Override
    public int compareTo(Cell o)
    {
        if(this.hcost<o.hcost) return -1;
        if(this.hcost == o.hcost) return 0;
        return 1;
    }

    public Cell(int w,int h)
    {

        this.w = w;
        this.h = h;
    }
}
