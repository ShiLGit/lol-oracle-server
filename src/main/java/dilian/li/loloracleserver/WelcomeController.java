package dilian.li.loloracleserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class WelcomeController {
    private final DataScraperService dataScraperService;



    @Autowired
    public WelcomeController(DataScraperService dataScraperService){
        this.dataScraperService = dataScraperService;
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Hello World";
    }

    @PostMapping("/make-prediction")
    public ResponseEntity<String> handleMakePrediction(@RequestBody Map<String, Object> requestBody){
        System.out.println(requestBody.get("summonerName"));
        String summoner = (String)requestBody.get("summonerName");

        String apiKey = (String)requestBody.get("apiKey");
        apiKey = apiKey.replace("=", "");

        String x = dataScraperService.makePrediction(apiKey, summoner);
        System.out.println("Caught? [" + x + "]");
        return ResponseEntity.status(HttpStatus.OK).body("fak u");
    }
    @PostMapping("/make-prediction-manual")
    public ResponseEntity<String> handleManualMakePrediction(@RequestBody Map<String, Object> requestBody){
        System.out.println(requestBody.get("teamOne"));
        ArrayList<String> teamOne = (ArrayList<String>)requestBody.get("teamTwo");
        ArrayList <String> teamTwo = (ArrayList<String>)requestBody.get("teamOne");
        String apiKey = (String)requestBody.get("apiKey");
        apiKey = apiKey.replace("=", "");

        dataScraperService.makePrediction(apiKey, teamOne, teamTwo);
        return ResponseEntity.status(HttpStatus.OK).body("fak u");
    }
    @PostMapping("/validate-key" )
    public ResponseEntity<String> validateKey(@RequestBody String apiKey){
        apiKey = apiKey.replace("=", "");
        System.out.println("\nvalidate-key: apikey=[" + apiKey + "]\n");
        return dataScraperService.validateKey(apiKey);
        //mono is more or less an asynchronous object >> not wat u want, but will give u wat u want in future
//        WebClient.ResponseSpec res = client.get().uri("https://quoters.apps.pcfone.io/api/random").retrieve();
//        String responseBody = res.bodyToMono(String.class).block();
//        return responseBody;
        //return "fk u";
    }
    @ExceptionHandler(WebClientResponseException.class)
    private ResponseEntity<String> handleError(WebClientResponseException w){
        System.out.println("wats up idoit \n" + w.getMessage());
        if (w.getStatusCode() == HttpStatus.FORBIDDEN){
            return new ResponseEntity<String>("ERROR: Invalid API key.", w.getStatusCode());
        }
        return new ResponseEntity<>(w.getMessage(), w.getStatusCode());
    }
}
