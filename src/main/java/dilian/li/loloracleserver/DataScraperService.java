package dilian.li.loloracleserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DataScraperService {
    @Autowired
    private WebClient.Builder builder;
    private static HashMap<String, Float> rankMap = new HashMap(){{
        put("IRON", 0f);
        put("BRONZE", 5f);
        put("SILVER", 10f);
        put("GOLD", 15f);
        put("PLATINUM", 20f);
        put("DIAMOND", 25f);
        put("IV", 1f);
        put("III", 2f);
        put("II", 3f);
        put("I", 4f);
    }};
    public String makePrediction(String apiKey, ArrayList<String> teamOne, ArrayList<String> teamTwo)
    {
        TeamData teamOneData = new TeamData();
        TeamData teamTwoData = new TeamData();
        populateSummonerIdsFromActiveGame(apiKey, teamOne.get(0), teamOneData, teamTwoData);
//        setTeamIds(teamOneData, apiKey, teamOne);
//        setTeamIds(teamTwoData, apiKey, teamTwo);
        populateDataFromBySummonerReq(teamOneData, apiKey);
        populateDataFromBySummonerReq(teamTwoData, apiKey);
        return "";
    }
    private float mapRank(Map<String, String> reqData){
        float total = 0;
        if(reqData.get("tier") != null){
            total += rankMap.get(reqData.get("tier"));
            total += rankMap.get(reqData.get("rank"));
        }
        return total;
    }

    private <T> Object makeRiotAPIRequest(String apiKey, String uri, Class<T> respType) throws WebClientResponseException{
        Object res;
        res = builder.build()
                .get()
                .uri(uri)
                .header("X-Riot-Token", apiKey)
                .retrieve()
                .bodyToMono(respType)
                .block();
        System.out.println("makeRiotAPIRequest: \n\tresstr= " + res + "\n\t uri="+uri );

        return res;

    }
    private void populateDataFromBySummonerReq(TeamData team, String apiKey){
        float avgWR = 0;
        float hotstreak = 0;
        float denom = 0;
        float denomRankData = 0;
        float rankScore = 0;
        for (String tp: team.getSummonerIds()){
            ArrayList<LinkedHashMap> resArray = (ArrayList<LinkedHashMap>) makeRiotAPIRequest(apiKey, "https://na1.api.riotgames.com/lol/league/v4/entries/by-summoner/" + tp, ArrayList.class);
            //Special case: no data is returned because summoner has been completely inactive in ranked
            if(resArray.size() == 0)
                continue;
            LinkedHashMap res = resArray.get(0);
            int wins = (int)res.get("wins");
            int losses = (int)res.get("losses");
            avgWR += (float)wins / (float)(wins + losses);
            hotstreak += ((boolean)res.get("hotStreak")) ? 1 : 0;
            float rankIncrement = mapRank(res);
            if(rankIncrement > 0){
                denomRankData++;
            }
            rankScore += rankIncrement;
            denom += 1;
            //System.out.println("Updated data for " + tp + ":\n" + team.toString());
        }
        team.setAvgWR(avgWR/denom);
        team.setAvgHotstreak(hotstreak/denom);
        team.setRankAvg(rankScore/denomRankData);

        System.out.println("\nFinal TeamData:  " + team);
    }    /*
    param: String apiKey
    param: ArrayList summonerNames
: name of all LoL summoners on one team
    return: ArrayList of corresponding puuids of a team. Used to make other API requests (MH, etc.). Should be used for scraping IDs with manual entry for all summoner names
    * */
    private void setTeamIds(TeamData team, String apiKey, ArrayList<String> summonerNames) throws WebClientResponseException{
        ArrayList<String> puuids = new ArrayList<String>();
        ArrayList<String> summIds = new ArrayList<String>();
        for (String s : summonerNames){
                LinkedHashMap res = (LinkedHashMap) makeRiotAPIRequest(apiKey, "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + s, LinkedHashMap.class);
                String puuid = res.get("puuid").toString();
                String summId = res.get("id").toString();

                if (puuid == null || summId == null) {
                    throw new WebClientResponseException(400, "From getting puuids: summoner " + s + " DNE animal >:(", null, null, null);
                }
                summIds.add(summId);
                puuids.add(puuid);
        }
        team.setPuuids(puuids);
        team.setSummonerIds(summIds);
    }
    public void populateSummonerIdsFromActiveGame(String apiKey, String summonerName, TeamData teamOneData, TeamData teamTwoData){
        ArrayList<String> teamOne = new ArrayList<>();
        ArrayList<String> teamTwo = new ArrayList<>();
        LinkedHashMap resGameInfo;

        System.out.println("Getting active game for " + summonerName);

        LinkedHashMap res = (LinkedHashMap) makeRiotAPIRequest(apiKey, "https://na1.api.riotgames.com/lol/summoner/v4/summoners/by-name/" + summonerName, LinkedHashMap.class);
        String summId = res.get("id").toString();

        try {
            resGameInfo = (LinkedHashMap) makeRiotAPIRequest(apiKey, "https://na1.api.riotgames.com/lol/spectator/v4/active-games/by-summoner/" + summId, LinkedHashMap.class);
        }catch(WebClientResponseException e){
            System.out.println("NOT IN ACTIVE GAME" + e);

            return;
        }
        ArrayList<LinkedHashMap> resArray = (ArrayList<LinkedHashMap>)resGameInfo.get("participants");

        for (LinkedHashMap data: resArray){
            System.out.println(data.get("teamId") + " " + data.get("teamId").getClass());
            if(data.get("teamId").equals(100)){
               teamOne.add(data.get("summonerId").toString());
            }else{
                teamTwo.add(data.get("summonerId").toString());
            }
        }
        System.out.println("final :" + teamOne + "\n\t" + teamTwo);
        teamOneData.setSummonerIds(teamOne);
        teamTwoData.setSummonerIds(teamTwo);
    }
    /*
    param: String apiKey
    return: ResponseEntity that specifies whether API key is valid or not - just make a basic request to check if they go through
    * */
    public ResponseEntity validateKey(String apiKey){
        apiKey = apiKey.replace("=", "");
        System.out.println("\nvalidate-key: apikey=[" + apiKey + "]\n");

        try{
            makeRiotAPIRequest(apiKey, "https://americas.api.riotgames.com/riot/account/v1/accounts/by-puuid/b1tEOKTgKzWKM6uRTlhnmz-2aN4jvZFoOkkzDAWZXWj4fQH9O0EF9ghrTDyuMLemLWKYUdPM0ec1IA,", Object.class);
        }catch(WebClientResponseException e){
            if (e.getStatusCode() == HttpStatus.FORBIDDEN)
                return ResponseEntity.status(HttpStatus.OK).body("{\"error\": \"true\", \"message\": \"Error: Invalid API key.\"}");
            else
                return ResponseEntity.status(HttpStatus.OK).body("{\"error\": \"true\", \"message\": \"Error: Unknown error has occurred.\"}");
        }
        return ResponseEntity.status(HttpStatus.OK).body("{\"error\": \"false\", \"message\": \"API key validated.\"}");
    }

}
