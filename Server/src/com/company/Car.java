package com.company;

import java.util.*;

public class Car
{

    public static int START = -2, NOTHING = 0,OBSTACLE = 5, FINISH = 10;
    public static int TURN_SPD = 90;

    public static int UP =1, RIGHT =2, DOWN =3, LEFT = 4;
    public static double currentAngle = -1;
    public static int R = 5;

    public static long w = 1;
    public static long h = 1;

    public static long X = 0;
    public static long Y = 0;
    public static long finishX = 0;
    public static long finishY = 0;
    public static boolean turning = false;
    public static Cell carCell = null;
    private static PriorityQueue<Cell> openCells;
    private static Map<String, Boolean> closedCells;
    private static Map<String, Cell> realCell;
    public static Map<String, Integer> realMap = new HashMap<>();
    public static ArrayList<Cell> listPath = new ArrayList<>();

    static
    {
        X = 0;
        Y = 0;
        finishX = 0;
        finishY = 0;
        carCell = null;
    }

    public static void PointToMap(long x, long y, int type)
    {
        if(type == START)
        {
            Main.logger.info("Car :["+x+", "+y+"]");
            X = x;
            Y = y;
        }
    }
    public static boolean isObstacle(double distance, double angle)
    {
        double deviation = angle;
        long x = Math.round(Math.sin(Math.toRadians(deviation))*distance);
        long y = Math.round(Math.cos(Math.toRadians(deviation))*distance);
        int col = (int) (x/w + ((x%w != 0) ? 1: 0));
        int row = (int) (y/h + ((y%h != 0) ? 1: 0));

        x = X+row;
        y = Y+col;
        String point = Long.toString(x)+","+Long.toString(y);
        if(realMap.get(point) == null) return true;
        int eva = Math.min(realMap.get(point), OBSTACLE);
        if(eva == OBSTACLE) return true;
        return false;
    }
    public static void PointToMap(double distance, double angle, int type)
    {

        double deviation = angle;
        long x = Math.round(Math.sin(Math.toRadians(deviation))*distance);
        long y = Math.round(Math.cos(Math.toRadians(deviation))*distance);
        int col = (int) (x/w + ((x%w != 0) ? 1: 0));
        int row = (int) (y/h + ((y%h != 0) ? 1: 0));

        x = X+row;
        y = Y+col;
        if (type == OBSTACLE)
        {
            ///System.out.println("["+X+", "+Y+", "+row+", "+col+"]");
            for(long i= x-R;i<=x+R;i++)
            {
                for(long j = y-R;j<=y+R;j++)
                {
                    String point = Long.toString(i)+","+Long.toString(j);
                    ///if (i==-15 && j ==0) System.out.println("["+x+", "+y+"]");
                    if(realMap.get(point) == null) realMap.put(point, 0 );
                    int eva = Math.min(realMap.get(point)+1, OBSTACLE);
                    if(eva == OBSTACLE) Main.logger.info("OBSTACLE: ["+point+"]");
                    realMap.put(point, eva);
                }
            }
        }
        else if (type == START)
        {
            X = x;
            Y = y;
        }
        else if (type == FINISH)
        {
            if(finishX !=0 || finishY !=0)
            {
                for(long i = finishX - R; i<=finishX+R; i++)
                {
                    for(long j = finishY - R; j<=finishY+R; j++)
                    {
                        String point = Long.toString(i)+","+Long.toString(j);
                        realMap.put(point, NOTHING);
                    }
                }
            }
            for(long i= x-R;i<=x+R;i++)
            {
                for(long j = y-R;j<=y+R;j++)
                {
                    String point = Long.toString(i)+","+Long.toString(j);
                    System.out.println(point);
                    if(realMap.get(point) == null) {realMap.put(point, FINISH);continue;}
                    if(realMap.get(point)!= null && realMap.get(point)!= OBSTACLE) realMap.put(point, FINISH);
                }
            }
            finishX = x;
            finishY = y;
            ///System.out.println(finishX+", "+finishY);
        }
    }





    private static void update(Cell current, long x,long y,int direction,int blocks)
    {
        if(Math.abs(x-X)*Math.abs(x-X)+Math.abs(y-Y)*Math.abs(y-Y) > 320000) return;
        ///System.out.println(x+", "+y+", "+X+", "+Y);
        String point = Long.toString(x)+","+Long.toString(y);
        if(realCell.get(point) == null)
        {
            Cell newCell = new Cell(1,1);
            newCell.row = x;
            newCell.col = y;
            newCell.cost = Long.MAX_VALUE;
            newCell.hcost = Long.MAX_VALUE;
            newCell.direction = 0;
            newCell.blocks = 0;
            newCell.prev = null;
            newCell.heuristicCost = newCell.calHeuristicCost(finishX, finishY);
            realCell.put(point, newCell);
        }
        Cell target = realCell.get(point);
        if(realMap.get(point) == null) realMap.put(point, NOTHING);
        /*if(realMap.get(point) == OBSTACLE)
        {
            System.out.println(x+", "+y);
        }*/
        if( realMap.get(point) == OBSTACLE || (closedCells.get(point)!= null && closedCells.get(point) == true) ) return;

        if(current.direction != 0 && current.direction !=direction && blocks <=TURN_SPD) return;

        long cost = current.cost +1;
        boolean in = openCells.contains(target);
        if(current.direction!=0 && current.direction!=direction) blocks = -1*TURN_SPD;
        if(!in || cost < target.cost)
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

        ///long minDis = Long.MAX_VALUE;
        ///Cell alternativeCell = null;
        for(Cell cell: listPath)
        {
            String point = Long.toString(cell.row)+","+Long.toString(cell.col);
            if(realMap.get(point) == null) continue;
            if(realMap.get(point)!= null && realMap.get(point)== OBSTACLE) return false;
            /*if( minDis >cell.calHeuristicCost(X,Y))
            {
                minDis = cell.calHeuristicCost(X,Y);
                alternativeCell = cell;
            }*/
        }
        ///System.out.println("["+X+", "+Y+"]"+"; "+"["+alternativeCell.row+", "+alternativeCell.col+"]");
        ///System.out.println("["+X+", "+Y+"]"+ ", "+minDis+ ", ["+alternativeCell.row+", "+alternativeCell.col+"]");
        /*if(minDis>TURN_SPD*TURN_SPD+TURN_SPD*TURN_SPD || alternativeCell == null) return false;
        if(alternativeCell == listPath.get(listPath.size()-1)) return true;
        carCell = alternativeCell;
        if(carCell.direction == 4 || carCell.direction == 6)
        {
            if(turning == false)
            {
                turning = true;
                queueModule.clear();
                queueModule.add( Main.JSONConverter( new String [] {
                        "type","data-server",
                        "entity","1",
                        "path",Integer.toString(carCell.direction),
                        "times","0"
                }));
            }
            else
            {
                queueModule.add(Main.JSONConverter( new String [] {
                        "type","data-server",
                        "entity","2",
                        "path","8",
                        "times","50"
                }));
            }
        }
        else if (carCell.direction == 8)
        {
            turning = false;
            queueModule.add(Main.JSONConverter( new String [] {
                    "type","data-server",
                    "entity","2",
                    "path","8",
                    "times","50"
            }));
        }*/
        return true;
    }
    public static Queue<String> queue = new LinkedList<>();
    public static Queue<String> queueModule = new LinkedList<>();

    public static void find_path( )
    {
        /// Start find_path process
        Main.inProcess = 1;

        /// Clean queue
        closedCells = new HashMap<>();
        openCells = new PriorityQueue<Cell>();
        realCell = new HashMap<>();

        /// Clean output data
        queue.clear();
        listPath.clear();
        queueModule.clear();

        //// Make start point
        Cell current = new Cell(1,1);
        current.row = X;
        current.col = Y;

        current.blocks = 0;
        current.direction = 0;
        current.prev = null;

        current.heuristicCost = current.calHeuristicCost(finishX, finishY);
        current.hcost = current.heuristicCost;
        current.cost = 0;
        realCell.put(Long.toString(X)+","+Long.toString(Y), current);
        System.out.println(X+", "+Y+"->"+finishX+", "+finishY);
        /// Add start to pri_queue
        openCells.add(current);
        carCell = current;

        /// Make null
        current = null;

        /// Starting find path
        while(!openCells.isEmpty())
        {
            /// Get lowest h-cost in queue
            current = openCells.poll();

            /// Can't find finish point
            if(current == null) break;


            /// Get point
            long x = current.row;
            long y = current.col;
            String point = Long.toString(x)+","+Long.toString(y);
            ///System.out.println("("+current.row+", "+current.col+", "+realMap.get(point)+")");
            closedCells.put(point, true);
            ///System.out.println(realMap.get(point));
            /// Check finish
            if(realMap.get(point)!=null && realMap.get(point)==FINISH) break;

            /// get past blocks
            int blocks = current.blocks;

            /// Go Up
            update(current, x-1, y, UP, blocks+1);

            /// other directions
            if(current.direction!=0)
            {
                /// Go Down
                update(current, x+1, y, DOWN, blocks+1);

                /// Go Left
                update(current, x, y-1, LEFT, blocks+1);

                /// Go Right
                update(current, x, y+1, RIGHT, blocks+1);
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

        /// Make current null
        current = null;

        /// First direction (always Up - Straight)
        int cur_direction = UP;

        /// Find finish point that has been arrived
        for(long i= finishX-R;i<=finishX+R;i++)
        {
            for(long j = finishY-R;j<=finishY+R;j++)
            {
                String point = Long.toString(i)+","+Long.toString(j);
                ///System.out.println(point+" "+realMap.get(point));
                if(realCell.get(point) == null) continue;

                if(realMap.get(point) == FINISH)
                {
                    Cell finishCell = realCell.get(point);
                    if(finishCell!=null && finishCell.prev != null)
                    {
                        current = finishCell;
                        break;
                    }
                }
            }
            if(current != null) break;
        }

        /// Can't find finish point -> find alternative point that is close as much as possible
        if(current == null)
        {
            ///System.out.println("Something wrong!");
            for(long temp = 1;temp<=100;temp++)
            {
                for(long j= finishY-R-temp;j<=finishY+R+temp;j++)
                {
                    String point = Long.toString(finishX-R-temp)+","+Long.toString(j);
                    Cell finishCell =  realCell.get(point);
                    if(finishCell!=null && finishCell.prev != null)
                    {
                        current = finishCell;
                        break;
                    }
                    point = Long.toString(finishX+R+temp)+","+Long.toString(j);
                    finishCell = realCell.get(point);
                    if(finishCell != null && finishCell.prev!=null)
                    {
                        current=finishCell;
                        break;
                    }
                }
                for(long i = finishX-R-temp;i<=finishX+R+temp;i++)
                {
                    String point = Long.toString(i)+","+Long.toString(finishY-R-temp);
                    Cell finishCell =  realCell.get(point);
                    if(finishCell!=null && finishCell.prev != null)
                    {
                        current = finishCell;
                        break;
                    }
                    point = Long.toString(i)+","+Long.toString(finishY+R+temp);
                    finishCell = realCell.get(point);
                    if(finishCell != null && finishCell.prev!=null)
                    {
                        current=finishCell;
                        break;
                    }
                }
                if(current!= null) break;
            }
        }

        if(current == null)
        {
            System.out.println("Can't find path!!!");
            Main.inProcess = 0;
            return;
        }
        Main.logger.info("["+X+", "+Y+"->"+finishX+", "+finishY+"]");
        Main.logger.info("["+X+", "+Y+"->"+current.row+", "+current.col+"]");
        ArrayList<Integer> path = new ArrayList<>();
        ArrayList<Integer> times = new ArrayList<>();
        ArrayList<Cell> result = new ArrayList<>();

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
            if(current.row == X && current.col == Y) break;
            current = current.prev;
        }
        Collections.reverse(result);
        current = null;
        for(Cell ans: result)
        {
            ///System.out.println("("+ans.row+", "+ans.col+")");
            String point = Long.toString(ans.row)+","+Long.toString(ans.col);
            listPath.add(ans);

             /*Main.JSONConverter(
                            new String[]    {
                                    "type","data-server",
                                    "entity","path",
                                    "x", Double.toString(ans.col*w*601/801),
                                    "y", Double.toString((ans.row*h) * 601/801)
                            }
                    )
            );*/
            if(current == null)
            {
                current = ans;
                continue;
            }

            long step_row = ans.row-current.row;
            long step_col = ans.col-current.col;

            if(step_row == 0 && step_col == 1)
            {
                if(cur_direction == UP)
                {
                    path.add(2);
                    times.add(20);
                    path.add(6);
                    times.add(1);
                }
                else if(cur_direction == DOWN)
                {
                    path.add(2);
                    times.add(20);
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
                    path.add(2);
                    times.add(20);
                    path.add(4);
                    times.add(1);
                }
                else if(cur_direction == DOWN)
                {
                    path.add(2);
                    times.add(20);
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
                    path.add(2);
                    times.add(20);
                    path.add(4);
                    times.add(1);
                }
                else if(cur_direction == RIGHT)
                {
                    path.add(2);
                    times.add(20);
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
                    path.add(2);
                    times.add(20);
                    path.add(6);
                    times.add(1);
                }
                else if(cur_direction == RIGHT)
                {
                    path.add(2);
                    times.add(20);
                    path.add(4);
                    times.add(1);
                }
                cur_direction = UP;
            }
            current = ans;
        }
        System.out.println(path.toString());
        System.out.println(times.toString());
        for(int i=0;i<path.size();i++)
        {
            if(path.get(i) ==4 || path.get(i) == 6)
            {
                times.set(i,150);
                if(i-2>=0)
                {
                    times.set(i-2, Math.max(90,times.get(i-2)-TURN_SPD));
                }
                if(i+1<path.size())
                {
                    times.set(i+1, times.get(i+1));
                }
                else
                {
                    path.add(8);
                    times.add(90);
                    break;
                }
            }
            else if(path.get(i) == 8)
            {
                times.set(i, Math.max(90, times.get(i)));
            }
        }

        Main.logger.info(path.toString()+times.toString());
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

            if(msgPayload == 2 || i == path.size()-1)
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


}
