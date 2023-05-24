package dilian.li.loloracleserver;

import java.util.ArrayList;

public class TeamData {
    private ArrayList<String> puuids;
    private ArrayList<String> summonerIds;
    private float avgWR;
    private float avgHotstreak;
    private float rankAvg;
    public TeamData(){
        puuids = new ArrayList<String>();
        summonerIds = new ArrayList<String>();
        avgWR = 0f;
        avgHotstreak = 0f;
        rankAvg = 0f;
    }
    public float getAvgWR() {
        return avgWR;
    }

    public void setAvgWR(float avgWR) {
        this.avgWR = avgWR;
    }

    public float getAvgHotstreak() {
        return avgHotstreak;
    }

    public void setAvgHotstreak(float avgHotstreak) {
        this.avgHotstreak = avgHotstreak;
    }

    public float getRankAvg() {
        return rankAvg;
    }

    public void setRankAvg(float rankAvg) {
        this.rankAvg = rankAvg;
    }

    public void setPuuids(ArrayList<String> puuids){
        this.puuids = puuids;
    }
    public void setSummonerIds(ArrayList<String> summIds){
        this.summonerIds = summIds;
    }
    public ArrayList<String> getSummonerIds(){
        return this.summonerIds;
    }
    public ArrayList<String> getPuuids(){
        return this.puuids;
    }

    @Override
    public String toString(){
        String toReturn = "~~~~~~~~~~~~~~TEAM DATA~~~~~~~~~~~~~~\n";

        toReturn += "Len puuids: " + puuids.size() + "\nLen suids: " + summonerIds.size();
        toReturn += "\nRankAvg: " + rankAvg + "\navgWR: " + avgWR + "\navgHotstreak: " + avgHotstreak;
        return toReturn;
    }
}
