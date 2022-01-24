package com.company;

public class Cell implements Comparable<Cell>
{
    public int row;
    public int col;
    public long heuristicCost = 0;
    public int type = 0;
    public long cost;
    public long hcost;
    public Cell prev;
    public int direction;
    public int blocks;
    private long w;
    private long h;




    @Override
    public int compareTo(Cell o)
    {
        if(this.hcost<o.hcost) return -1;
        if(this.hcost == o.hcost) return 0;
        return 1;
    }

    public Cell(int w,int h, int type)
    {

        this.w = w;
        this.h = h;
        this.type = 0;
    }
}
