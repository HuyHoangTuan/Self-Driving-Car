package com.company;

import java.util.*;

public class Car
{

    public static int START = -2, OBSTACLE = 1, FINISH = 2, COLSTART = 401, ROWSTART = 401;
    public static int COLFINISH = 401, ROWFINISH = 401;
    public static int TURN_SPD = 20;
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
                map[i][j].blocks = 0;
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
                map[i][j].blocks = 0;
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
            if(ROWFINISH != ROWSTART && COLFINISH!=COLSTART)
            {
                for (int i = Math.max(0, ROWFINISH - R); i <= Math.min(ROWFINISH + R, NUM_ROW); i++) {
                    for (int j = Math.max(0, COLFINISH - R); j <= Math.min(COLFINISH + R, NUM_COl); j++) {
                        if (map[i][j].type == OBSTACLE) continue;
                        map[i][j].type = 0;
                    }
                }
            }

            ROWFINISH = ROWSTART+row;
            COLFINISH = COLSTART+col;

            for(int i= Math.max(0, ROWSTART+row-R);i<=Math.min(ROWSTART+row+R, NUM_ROW);i++)
            {
                for(int j= Math.max(0,COLSTART+col-R);j<=Math.min(COLSTART+col+R,NUM_COl);j++)
                {
                    if(map[i][j].type == OBSTACLE) continue;
                    map[i][j].row = i;
                    map[i][j].col = j;
                    map[i][j].type = FINISH;
                }
            }
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

    private static void update(Cell current, Cell target,int direction,int blocks)
    {
        if(target.type == OBSTACLE || closedCells[target.row][target.col]) return;
        if(current.direction != 0 && current.direction !=direction && blocks <=TURN_SPD) return;
        long cost = current.cost+1;
        boolean in = openCells.contains(target);
        if(current.direction!=0 && current.direction!=direction) blocks = -1*TURN_SPD;
        if(!in || cost <target.cost)
        {
            target.direction = direction;
            target.blocks = blocks;
            target.prev=current;
            target.cost = cost;
            target.hcost = cost+target.heuristicCost;
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
                map[i][j].cost = Long.MAX_VALUE;
                map[i][j].hcost = Long.MAX_VALUE;
                map[i][j].heuristicCost = Math.abs(i-ROWFINISH)*Math.abs(i-ROWFINISH)+Math.abs(j-COLFINISH)*Math.abs(j-COLFINISH);
                map[i][j].direction = 0;
                map[i][j].blocks = 0;
                map[i][j].prev=null;
            }
        openCells = new PriorityQueue<Cell>();
        openCells.add(map[ROWSTART][COLSTART]);

        map[ROWSTART][COLSTART].cost = 0;
        map[ROWSTART][COLSTART].hcost = map[ROWSTART][COLSTART].heuristicCost;

        Cell current;
        while(!openCells.isEmpty())
        {
            current = openCells.poll();
            if(current == null) break;

            closedCells[current.row][current.col] = true;
            if(current.type == FINISH) break;
            int blocks = current.blocks;
            if(current.row - 1 >=0  )
            {
                update(current, map[current.row-1][current.col], UP, blocks+1);
            }
            if(current.row +1 < NUM_ROW && current.direction != 0)
            {
                update(current, map[current.row+1][current.col],DOWN, blocks+1);
            }
            if(current.col -1 >=0 && current.direction != 0)
            {
                update(current, map[current.row][current.col-1],LEFT, blocks+1 );
            }
            if(current.col + 1 <NUM_COl && current.direction != 0)
            {
                update(current, map[current.row][current.col+1],RIGHT, blocks+1 );
            }
        }

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

        current = null;
        int cur_direction = UP;
        ArrayList<Integer> path = new ArrayList<Integer>();
        ArrayList<Integer> times = new ArrayList<Integer>();
        ArrayList<Cell> result = new ArrayList<>();

        for(int i=0;i< NUM_ROW;i++)
        {
            for(int j=0;j<NUM_COl;j++)
            {
                if(map[i][j].type == FINISH && map[i][j].prev != null)
                {
                    current = map[i][j];
                    break;
                }
            }
            if(current !=null) break;
        }
        if(current == null)
        {
            long alter_hcost_destination =Long.MAX_VALUE;
            for(int i=0;i<NUM_ROW;i++)
            {
                for(int j=0;j<NUM_COl;j++)
                {
                    if(map[i][j].type!=OBSTACLE && map[i][j].type!=START && map[i][j].prev != null)
                    {
                        if(alter_hcost_destination > Math.abs(i-ROWFINISH)*Math.abs(i-ROWFINISH)+Math.abs(j-COLFINISH)*Math.abs(j-COLFINISH))
                        {
                            alter_hcost_destination = Math.abs(i-ROWFINISH)*Math.abs(i-ROWFINISH)+Math.abs(j-COLFINISH)*Math.abs(j-COLFINISH);
                            current = map[i][j];
                        }
                    }
                }
            }
        }
        if(current == null)
        {
            System.out.println("Can't find path!!!");
            return;
        }
        path.add(8);
        times.add(0);
        while(true)
        {
            if(current==null)
            {
                System.out.println("FUCKED UP!!!");
                break;
            }
            result.add(current);
            if(current.type == START) break;
            current = current.prev;
        }
        Collections.reverse(result);
        current = null;
        for(Cell ans: result)
        {
            System.out.println("("+ans.row+", "+ans.col+", "+ ans.type+")");
            listPath.add(ans);
            queue.add(
                    Main.JSONConverter(
                            new String[]    {
                                    "type","data-server",
                                    "entity","path",
                                    "x", Double.toString((ans.col-COLSTART)*w*601/801),
                                    "y", Double.toString(((ans.row-ROWSTART)*h) * 601/801)
                            }
                    )
            );
            if(current == null)
            {
                current = ans;
                continue;
            }

            int step_row = ans.row-current.row;
            int step_col = ans.col-current.col;

            if(step_row == 0 && step_col == 1)
            {
                if(cur_direction == UP)
                {
                    path.add(6);
                    times.add(1);
                }
                else if(cur_direction == DOWN)
                {
                    path.add(4);
                    times.add(1);
                }
                else if(cur_direction == LEFT)
                {
                    path.add(2);
                    times.add(1);
                }
                else if(cur_direction == RIGHT)
                {
                    if(path.get(path.size()-1) !=8)
                    {
                        path.add(8);
                        times.add(1);
                    }
                    else times.set(times.size()-1, times.get(times.size()-1)+1);
                }
                cur_direction = RIGHT;
            }
            else if(step_row == 0 && step_col == -1)
            {

                if(cur_direction == UP)
                {
                    path.add(4);
                    times.add(1);
                }
                else if(cur_direction == DOWN)
                {
                    path.add(6);
                    times.add(1);
                }
                else if(cur_direction == LEFT)
                {
                    if(path.get(path.size()-1) !=8)
                    {
                        path.add(8);
                        times.add(1);
                    }
                    else times.set(times.size()-1, times.get(times.size()-1)+1);
                }
                else if(cur_direction == RIGHT)
                {
                    path.add(2);
                    times.add(1);
                }
                cur_direction = LEFT;
            }
            else if(step_row == 1 && step_col == 0)
            {
                if(cur_direction == UP)
                {
                    path.add(2);
                    times.add(1);
                }
                else if(cur_direction == DOWN)
                {
                    if(path.get(path.size()-1) !=8)
                    {
                        path.add(8);
                        times.add(1);
                    }
                    else times.set(times.size()-1, times.get(times.size()-1)+1);
                }
                else if(cur_direction == LEFT)
                {
                    path.add(4);
                    times.add(1);
                }
                else if(cur_direction == RIGHT)
                {
                    path.add(6);
                    times.add(1);
                }
                cur_direction = DOWN;
            }
            else if(step_row == -1 && step_col == 0)
            {
                if(cur_direction == UP)
                {
                    if(path.get(path.size()-1) !=8)
                    {
                        path.add(8);
                        times.add(1);
                    }
                    else times.set(times.size()-1, times.get(times.size()-1)+1);
                }
                else if(cur_direction == DOWN)
                {
                    path.add(2);
                    times.add(1);
                }
                else if(cur_direction == LEFT)
                {
                    path.add(6);
                    times.add(1);
                }
                else if(cur_direction == RIGHT)
                {
                    path.add(4);
                    times.add(1);
                }
                cur_direction = UP;
            }
            current = ans;
        }
        for(int i=0;i<path.size();i++)
        {
            if(path.get(i) ==4 || path.get(i) == 6)
            {
                times.set(i,99);
                if(i-1 >=0)
                {
                    times.set(i-1,times.get(i-1)-TURN_SPD);
                }
                if(i+1<path.size())
                {
                    times.set(i+1, Math.max(0,times.get(i+1)-TURN_SPD));
                }
            }
        }
        System.out.println(path.toString());
        System.out.println(times.toString());
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
        Main.inProcess = 0;
    }
    public static void change_map(int x,int y)
    {
        x*=-1;

        if(x<0)
        {
            if(y<0)
            {
                for(int i=NUM_ROW-1;i>=0;i--)
                {
                    for(int j=NUM_COl-1;j>=0;j--)
                    {
                        if(i== ROWSTART && j == COLSTART) continue;
                        if(i+x<0 || j+y<0)
                        {
                            map[i][j].type = 0;
                        }
                        else map[i][j].type = map[i+x][j+y].type;
                    }
                }
            }
            else
            {
                for(int i=NUM_ROW-1;i>=0;i--)
                {
                    for(int j=0;j<NUM_COl;j++)
                    {
                        if(i== ROWSTART && j == COLSTART) continue;
                        if(i+x<0 || j+y<0)
                        {
                            map[i][j].type = 0;
                        }
                        else map[i][j].type = map[i+x][j+y].type;
                    }
                }
            }
        }
        else
        {
            if(y<0)
            {
                for(int i=NUM_ROW-1;i>=0;i--)
                {
                    for(int j=NUM_COl-1;j>=0;j--)
                    {
                        if(i== ROWSTART && j == COLSTART) continue;
                        if(i+x<0 || j+y<0)
                        {
                            map[i][j].type = 0;
                        }
                        else map[i][j].type = map[i+x][j+y].type;
                    }
                }
            }
            else
            {
                for(int i=NUM_ROW-1;i>=0;i--)
                {
                    for(int j=0;j<NUM_COl;j++)
                    {
                        if(i== ROWSTART && j == COLSTART) continue;
                        if(i+x<0 || j+y<0)
                        {
                            map[i][j].type = 0;
                        }
                        else map[i][j].type = map[i+x][j+y].type;
                    }
                }
            }
        }
    }


}
