package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.*;

import javax.annotation.Nonnull;
import java.util.*;


/*
    知道mrX的位置
    每个detective的位置
    mrX手里的票
    detectives手里的票
    轮数
    而生成一个分数，mrX的任务是使这个分数尽可能的高，而detectives的任务是使这个分数尽可能的低
 */

public class Score {
    private int source;
    private int mrXLoc;
    private List<Integer> detectivesLoc;
    private Boolean useDouble; //用没用double卡（singlemove or doublemove）
    private Boolean useSecret;
    private Map<ScotlandYard.Ticket, Integer> mrXTickets;
    private Map<ScotlandYard.Ticket, Integer> detectivesTickets;
    private ScotlandYard.Ticket usedTicket;

    //一个普通的构造函数
    //所有算分数需要用的东西都在这个地方传进来


    public Score(int source, int mrXLoc, List<Integer> detectivesLoc, Boolean useDouble, Boolean useSecret,
                 Map<ScotlandYard.Ticket, Integer> mrXTickets, Map<ScotlandYard.Ticket, Integer> detectivesTickets,
                 ScotlandYard.Ticket usedTicket) {
        this.source = source;
        this.mrXLoc = mrXLoc;
        this.detectivesLoc = detectivesLoc;
        this.useDouble = useDouble;
        this.useSecret = useSecret;
        this.mrXTickets = mrXTickets;
        this.detectivesTickets = detectivesTickets;
        this.usedTicket = usedTicket;
    }

    public Score() { }

    /*
                    当reveal前一轮和后一轮时候改变权重
                    reveal前一轮 能到的点评分权重增加
                    reveal后一轮是交通方式 或者用secret卡
                    判断离reveal的轮数
                    考虑用不用设置位置分数和卡分数
                    对secret和double卡的设置 当reveal前一轮secret卡评分降低 后一轮增加
                    double卡当detectives离mrX距离很近时使用得分增加
                    对于单次的move 其实是同一套评分系统 detectives想办法让mrX分更低
                 */
    //得到分数就用这个函数
    public int giveScore(@Nonnull Board board, int mrXLoc, List<Integer> detectivesLoc, int source, ScotlandYard.Ticket usedTicket){
        Xbot xbot = new Xbot();
        int score = 0;
        if(distanceScore(board, mrXLoc,detectivesLoc)<=0) return -10;
        score = distanceScore(board, mrXLoc,detectivesLoc)*10 + transportationScore(board, mrXLoc)
                + guessPossibilityScore(board, source, usedTicket); //还没设置参数
        System.out.println("score: "+score);
        return score;
    }

    //score about the distance
    //就是这个函数 source和location很混乱 写完xbot再回来看这个
    //距离和剩下的票分开算
    private int distanceScore(@Nonnull Board board, int mrXLoc, List<Integer> detectivesLoc) {
        Dijkstra dk = new Dijkstra(board);
        //用一下Dijkstra
        List<Integer> distance = dk.getDetectivesDistance();
        List<Integer> edgeList = new ArrayList<Integer>(Arrays.asList(1,4,7,92,175,56));
        Dijkstra dk1 = new Dijkstra(mrXLoc,edgeList,board);
        //加权算一下
        int distanceScore = 0;
        for(Integer d: distance){
            distanceScore += d;
            distanceScore *= 4; //这个数不合适的话后期可以改
        }
        if(distanceScore <= 0) return -10;
        System.out.println("dddddddistance score: "+distanceScore);
        for(Integer d1 : dk1.getDetectivesDistance()){
            distanceScore += d1;
            if (d1<2) distanceScore -= Math.pow(2,(distance.size()-1));
        }
        distanceScore /= (Math.pow(4,distance.size()-1));
        //测试用
        System.out.println("distanceScore: "+distanceScore);
        return distanceScore;
    }

    //这个点与其相邻点的交通方式数量（意思就是这是个大站还是小站）和相邻点个数的评分
    //传进去一个点位，然后就返回这些信息（大站or小站）
    private int transportationScore (@Nonnull Board board, int loc){
        int adjacentNodesNum = board.getSetup().graph.adjacentNodes(loc).size();
        int transportationTypeNum = 0;
        for (Integer i: board.getSetup().graph.adjacentNodes(loc)){
            transportationTypeNum += board.getSetup().graph.edgeValueOrDefault(loc,i, ImmutableSet.of()).size();
        }
        //测试用
        System.out.println("transportationScore: "+ (2 * adjacentNodesNum + transportationTypeNum));
        return 2 * adjacentNodesNum + transportationTypeNum; //先这么设置，不合适再改
    }

    //从起始点用这种交通方式能到几个点
    //如果用的secret那就是与它相连的所有点
    private int guessPossibilityScore(@Nonnull Board board, int source, ScotlandYard.Ticket ticket){
        int possibleNode = 0;
        Set<Integer> allAdjacentNodes = board.getSetup().graph.adjacentNodes(source);
        if(ticket == ScotlandYard.Ticket.SECRET) possibleNode = allAdjacentNodes.size();
        else {
            for(Integer node: allAdjacentNodes){
                for(ScotlandYard.Transport t: board.getSetup().graph.edgeValueOrDefault(source,node,ImmutableSet.of())){
                    if (t.requiredTicket() == ticket) possibleNode++;
                }
            }
        }
        //测试用
        System.out.println("guessPossibilityScore: "+ possibleNode);
        return possibleNode;
    }
}
