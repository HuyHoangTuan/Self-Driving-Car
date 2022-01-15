package com.company;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class Car
{

    public static int START = -2, OBSTACLE = 1, FINISH = 2, COLSTART = 401, ROWSTART = 401;
    public static int COLFINISH = 401, ROWFINISH = 401;
    public static int NUM_ROW = 800, NUM_COl =800;
    public static int UP =1, RIGHT =2, DOWN =3, LEFT = 4;
    public static double currentAngle = -1;
    public static int R = 5;

    public static long w = 1;
    public static long h = 1;

    public static Cell[][] map = new Cell[800][800];
    static{
        for(int i=0;i<NUM_ROW;i++)
        {
            for(int j=0;j<NUM_COl;j++)
            {
                map[i][j]= new Cell(1,1,0);
                map[i][j].row = i;
                map[i][j].col = j;
                map[i][j].type = 0;
                map[i][j].direction = 0;
            }
        }
        map[ROWSTART][COLSTART].cost = 0;
        map[ROWSTART][COLSTART].type = START;
        map[ROWSTART][COLSTART].row = ROWSTART;
        map[ROWSTART][COLSTART].col = COLSTART;

    }
    public static void reset()
    {
        for(int i=0;i<NUM_ROW;i++)
        {
            for(int j=0;j<NUM_COl;j++)
            {
                map[i][j]= new Cell(1,1,0);
                map[i][j].row = i;
                map[i][j].col = j;
                map[i][j].type = 0;
                map[i][j].direction = 0;
            }
        }
        map[ROWSTART][COLSTART].cost = 0;
        map[ROWSTART][COLSTART].type = START;
        map[ROWSTART][COLSTART].row = ROWSTART;
        map[ROWSTART][COLSTART].col = COLSTART;
    }
    public static void PointToMap(double distance, double angle, int type)
    {

        double deviation = angle;
        long x = Math.round(Math.sin(Math.toRadians(deviation))*distance);
        long y = Math.round(Math.cos(Math.toRadians(deviation))*distance);
        int col = (int) (x/w + ((x%w != 0) ? 1: 0));
        int row = (int) (y/h + ((y%h != 0) ? 1: 0));

        if(ROWSTART+row>=NUM_ROW || ROWSTART+row<0 || COLSTART+col>=NUM_COl || COLSTART+col<0) return ;
        if(map[ROWSTART+row][COLSTART+col].type == OBSTACLE) return ;
        map[ROWSTART+row][COLSTART+col].type = type;
        map[ROWSTART+row][COLSTART+col].row = ROWSTART+row;
        map[ROWSTART+row][COLSTART+col].col = COLSTART+col;
        System.out.println(map[ROWSTART+row][COLSTART+col].type +", "+map[ROWSTART+row][COLSTART+col].row+", "+map[ROWSTART+row][COLSTART+col].col);

        if(type == FINISH)
        {
            ROWFINISH = ROWSTART+row;
            COLFINISH = COLSTART+col;
            /// System.out.println(ROWFINISH+" "+COLFINISH);
        }
        else if(type == OBSTACLE)
        {
            for(int i= Math.max(0, ROWSTART+row-R);i<=Math.min(ROWSTART+row+R, NUM_ROW);i++)
            {
                for(int j= Math.max(0,COLSTART+col-R);j<=Math.min(COLSTART+col+R,NUM_COl);j++)
                {
                    map[i][j].row = i;
                    map[i][j].col = j;
                    map[i][j].type = OBSTACLE;
                }
            }
        }
    }

    public static ArrayList<Cell> listPath = new ArrayList<Cell>();

    private static PriorityQueue<Cell> openCells;
    private static boolean closedCells[][] = new boolean[800][800];

    private static void update(Cell current, Cell target,int direction)
    {
        if(target.type == OBSTACLE || closedCells[target.row][target.col]) return;
        long cost = current.cost+target.heuristicCost;
        boolean in = openCells.contains(target);
        if(!in || cost <target.cost)
        {
            target.cost = cost;
            target.direction = direction;
            if(!in) openCells.add(target);
        }
    }
    public static boolean checkPath()
    {
        if(listPath.size() == 0 || Main.inProcess == 1) return true;
        for(Cell cell: listPath)
        {
            if(cell.type == OBSTACLE) return false;
        }
        return true;
    }
    public static Queue<String> queue = new LinkedList<String>();
    public static Queue<String> queueModule = new LinkedList<String>();
    public static void find_path( )
    {
        Main.inProcess = 1;
        queue.clear();
        listPath.clear();
        for(int i=0;i< NUM_ROW;i++)
            for(int j=0;j<NUM_COl;j++)
            {
                closedCells[i][j]=false;
                map[i][j].cost = 0;
                map[i][j].heuristicCost = Math.abs(i-ROWSTART)*Math.abs(i-ROWSTART)+Math.abs(j-COLSTART)*Math.abs(j-COLSTART);
                map[i][j].direction = 0;
            }

        openCells = new PriorityQueue<Cell>();
        openCells.add(map[ROWFINISH][COLFINISH]);

        map[ROWFINISH][COLFINISH].cost = 0;

        Cell current;
        while(!openCells.isEmpty())
        {
            current = openCells.poll();
            /// System.out.println(current.row+" , "+current.col+" : "+current.cost);
            if(current == null) break;

            closedCells[current.row][current.col] = true;

            if(current.equals(map[ROWSTART][COLSTART])) break;

            if(current.row - 1 >=0)
            {
                update(current, map[current.row-1][current.col], UP);
            }
            if(current.row +1 < NUM_ROW)
            {
                update(current, map[current.row+1][current.col],DOWN);
            }
            if(current.col -1 >=0)
            {
                update(current, map[current.row][current.col-1],RIGHT);
            }
            if(current.col + 1 <NUM_COl)
            {
                update(current, map[current.row][current.col+1],LEFT);
            }
        }

        if(map[ROWSTART][COLSTART].direction == 0)
        {
            System.out.println("Can't find path !!!");
            return;
        }
        int currentDirrection = UP;

        current = map[ROWSTART][COLSTART];

        /*
            direction :
                8 --> FORWARD
                2 --> BACKWARD
                4 --> LEFT
                6 --> RIGHT

            type : data-server
            entity : instruction
            path : [ direction ]
            times : [???]
         */

        ArrayList<Integer> path = new ArrayList<Integer>();
        ArrayList<Integer> times = new ArrayList<Integer>();
        path.add(8);
        times.add(0);
        while(true)
        {
            int direction = current.direction;
            listPath.add(current);
            queue.add(
                    Main.JSONConverter(
                            new String[]    {
                                    "type","data-server",
                                    "entity","path",
                                    "x", Double.toString((current.col-COLSTART)*w*601/801),
                                    "y", Double.toString(((current.row-ROWSTART)*h*-1) * 601/801)
                            }
                    )
            );
            ///System.out.print("("+current.row+", "+current.col+"), ");
            if(current.equals(map[ROWFINISH][COLFINISH])) break;

            if(currentDirrection == direction )
            {
                if(path.get(path.size()-1) !=8)
                {
                    path.add(8);
                    times.add(1);
                }
                else times.set(times.size()-1, times.get(times.size()-1)+1);
            }
            else
            {
                if(currentDirrection == UP)
                {
                    if(direction == DOWN)
                    {
                        path.add(2);
                        times.add(1);
                    }
                    else if(direction == RIGHT)
                    {
                        path.add(6);
                        times.add(1);
                    }
                    else if(direction == LEFT)
                    {
                        path.add(4);
                        times.add(1);
                    }
                }
                else if(currentDirrection == DOWN)
                {
                    if(direction == UP)
                    {
                        path.add(2);
                        times.add(1);
                    }
                    else if(direction == RIGHT)
                    {
                        path.add(6);
                        times.add(1);
                    }
                    else if(direction == LEFT)
                    {
                        path.add(4);
                        times.add(1);
                    }
                }
                else if(currentDirrection == RIGHT)
                {
                    if(direction == UP)
                    {
                        path.add(4);
                        times.add(1);
                    }
                    else if(direction == LEFT)
                    {
                        path.add(2);
                        times.add(1);
                        ///System.out.print("Backward");
                    }
                    else if(direction == DOWN)
                    {
                        path.add(6);
                        times.add(1);
                        ///System.out.print("Right");
                    }
                }
                else if( currentDirrection == LEFT)
                {
                    if(direction == UP)
                    {
                        path.add(6);
                        times.add(1);
                    }
                    else if(direction == RIGHT)
                    {
                        path.add(2);
                        times.add(1);
                    }
                    else if(direction == DOWN)
                    {
                        path.add(4);
                        times.add(1);
                    }
                }
            }
            if(direction == UP)
            {
                current = map[current.row+1][current.col];
            }
            else if(direction == DOWN)
            {
                current = map[current.row-1][current.col];
            }
            else if(direction == LEFT)
            {
                current = map[current.row][current.col-1];
            }
            else if(direction == RIGHT)
            {
                current = map[current.row][current.col+1];
            }
            currentDirrection = direction;
        }
        int msgPayload = 0;
        int cur = 0;
        String msgPath = "";
        String msgTimes = "";
        for(int i=0;i<path.size();i++)
        {
            msgPayload ++ ;

            if(msgPath.length()!=0)
            {
                msgPath+=",";
                msgTimes+=",";
            }
            msgPath+= path.get(i).toString();
            msgTimes+= times.get(i).toString();

            if(msgPayload == 11 || i == path.size()-1)
            {
                cur++;
                msgPayload=1;
                queueModule.add(
                                "{"+
                                            "\"type\":"+ "\"data-server\","+
                                            "\"entity\":"+ "\"" + Integer.toString(cur) +"\"," +
                                            "\"path\":"+ "\""+msgPath+"\","+
                                            "\"times\":"+ "\""+msgTimes+"\""+
                                        "}"
                                );
                msgPath= "";
                msgTimes = "";
            }
        }
        ///System.out.println("\n");
        ///System.out.println(path.toString());
        ///System.out.println(times.toString());
        Main.inProcess = 0;
    }





}
