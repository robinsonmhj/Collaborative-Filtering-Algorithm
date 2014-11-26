package com.esu.edu.recommendation.cf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Data {
    static String[] films = { "十面埋伏", "一路向北", "那些年我们一起追过的女孩", "CCAV", "非诚勿扰" };
    static String[] users = { "aaa", "bbb", "ccc", "ddd", "葛二蛋" };
    static Map score = new HashMap();
    static Set userSet = new HashSet();
    static Set filmSet = new HashSet();
    static {
        for (String str : Data.users) {
            userSet.add(str);
        }
        for (String str : Data.films) {
            filmSet.add(str);
        }
        score = getScore();
    }

    public static void outNearbyUserList(String user) {
        Map scores = new HashMap();
        for (String tempUser : users) {
            if (tempUser.equalsIgnoreCase(user)) {
                continue;
            }
            double score = getOSScore(user, tempUser);
            scores.put(tempUser, score);
        }
        System.out.println(scores.toString());
    }

    private static Double getOSScore(String user1, String user2) {
        HashMap user1Score = (HashMap) score.get(user1);
        HashMap user2Score = (HashMap) score.get(user2);
        double totalscore = 0.0;
        Iterator it = user1Score.keySet().iterator();
        while (it.hasNext()) {
            String film = (String) it.next();
            int a1 = (Integer) user1Score.get(film);
            int a2 = (Integer) user1Score.get(film);
            int b1 = (Integer) user2Score.get(film);
            int b2 = (Integer) user2Score.get(film);
            int a = a1 * a2 - b1 * b2;
            //System.out.println(Math.abs(a));
            totalscore += Math.sqrt(Math.abs(a));
        }
        return totalscore;
    }

    private static Map getScore() {
        Map score = new HashMap();
        // aaa
        HashMap tempScore = new HashMap();
        tempScore.put(films[0], 9);
        tempScore.put(films[1], 1);
        tempScore.put(films[2], 9);
        tempScore.put(films[3], 7);
        tempScore.put(films[4], 1);
        score.put(Data.users[0], tempScore);
        // bbb
        tempScore = new HashMap();
        tempScore.put(films[0], 2);
        tempScore.put(films[1], 9);
        tempScore.put(films[2], 2);
        tempScore.put(films[3], 2);
        tempScore.put(films[4], 2);
        score.put(Data.users[1], tempScore);
        // ccc
        tempScore = new HashMap();
        tempScore.put(films[0], 9);
        tempScore.put(films[1], 9);
        tempScore.put(films[2], 9);
        tempScore.put(films[3], 3);
        tempScore.put(films[4], 3);
        score.put(Data.users[2], tempScore);
        // ddd
        tempScore = new HashMap();
        tempScore.put(films[0], 4);
        tempScore.put(films[1], 9);
        tempScore.put(films[2], 9);
        tempScore.put(films[3], 4);
        tempScore.put(films[4], 4);
        score.put(Data.users[3], tempScore);
        // 葛二蛋
        tempScore = new HashMap();
        tempScore.put(films[0], 5);
        tempScore.put(films[1], 5);
        tempScore.put(films[2], 5);
        tempScore.put(films[3], 5);
        tempScore.put(films[4], 5);
        score.put(Data.users[4], tempScore);
        return score;
    }

    public static void main(String[] args) {
        //
        System.out.println(Data.users[0] + " 与其他人的相似度（分值越低越相似）：");
        Data.outNearbyUserList(Data.users[0]);
        
        System.out.println(Math.sqrt(1.1588107544435668));
    }
}