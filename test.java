import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrptApi {
   private final RateLimiter rateLimiter;
   private final HttpClient httpClient;
   public CrptApi(TimeUnit timeUnit, int requestLimit) {
       rateLimiter=new RateLimiter(timeUnit, requestLimit);
       httpClient= HttpClient.newBuilder().build();
   }
   public Map<String, String> getAuthTokens() throws InterruptedException, IOException {
       rateLimiter.acquire();
       String authCertUrl="/api/v3/auth/cert";
       String authKeyUrl="/api/v3/auth/cert/key";
       String authCertRequestBody="{\"cert\":\"<CERT>\"}";
       HttpRequest authCertRequest=HttpRequest.newBuilder()
               .uri(URI.create(authCertUrl))
               .header("Content-Type", "application/json")
               .POST(HttpRequest.BodyPublishers.ofString(authCertRequestBody))
               .build();
       HttpRequest authKeyRequest=HttpRequest.newBuilder()
               .uri(URI.create(authKeyUrl))
               .GET()
               .build();
       HttpResponse<String> authCertResponse = httpClient.send(
               authCertRequest,
               HttpResponse.BodyHandlers.ofString());
       if (authCertResponse.statusCode()==200) {
           String authToken=
                   getAuthTokenFromResponse(authCertResponse.body());
           HttpResponse<String> authKeyResponse = httpClient.send(
                   authKeyRequest,
                   HttpResponse.BodyHandlers.ofString());
           if (authKeyResponse.statusCode()==200) {
               String authKey=
                       getAuthKeyFromResponse(authKeyResponse.body());
               Map<String, String> authTokens=new HashMap<>();
               authTokens.put("token", authToken);
               authTokens.put("key", authKey);
               return authTokens;
           } else {
               throw new IOException("Не получен код авторизации");
           }
       } else {
           throw new IOException("Не получен токен авторизации");
       }

   }

   private String getAuthTokenFromResponse(String responseBody) {
       Pattern pattern=Pattern.compile("\"token\":\"(.*?)\"");
       Matcher matcher=pattern.matcher(responseBody);
       if (matcher.find()) {
           return matcher.group(1);
       }
       return null;
   }

   private String getAuthKeyFromResponse(String responseBody) {
       Pattern pattern=Pattern.compile("\"data\":\"(.*?)\"");
       Matcher matcher=pattern.matcher(responseBody);
       if (matcher.find()) {
           return matcher.group(1);
       }
       return null;
   }

   private static class RateLimiter {
       private final TimeUnit timeUnit;
       private final int requestLimit;
       private final Queue<Long> requestTimes;
       public RateLimiter(TimeUnit timeUnit, int requestLimit) {
           this.timeUnit=timeUnit;
           this.requestLimit=requestLimit;
           this.requestTimes=new LinkedList<>();
       }
       public synchronized void acquire() throws InterruptedException {
           long now=System.currentTimeMillis();
           long limitInMillis=timeUnit.toMillis(1);
           while (!requestTimes.isEmpty() && requestTimes.peek()<=now-limitInMillis) {
               requestTimes.poll();
           }
           while (requestTimes.size()>=requestLimit) {
               long nextAvailableTime=requestTimes.peek()+limitInMillis;
               long sleepTime=nextAvailableTime-now;
               wait(sleepTime);
               now=System.currentTimeMillis();
           }
           requestTimes.offer(now);
       }
   }
}
