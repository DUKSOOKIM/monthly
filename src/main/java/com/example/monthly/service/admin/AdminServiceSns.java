package com.example.monthly.service.admin;

import com.example.monthly.dto.SellerDto;
import com.example.monthly.mapper.AdminMapper;
import com.example.monthly.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
public class AdminServiceSns {
    //네이버 클라우드 가입 후 진행
    //가입 절차, serviceId발급, accessKey발급, secretKey발급 아래 사이트 참고
    //(https://www.autooffice.io/knowhow/send-sms-via-naver-sens-api )

    private String serviceId = "ncp:sms:kr:311302469407:monthly"; //발급받은 serviceId

    @Value("${accessKey}")
    private String accessKey; //발급받은 accessKey

    @Value("${secretKey}")
    private String secretKey; // 발급받은 secretKey

    private String method ="POST";
    private String timeStamp = Long.toString(System.currentTimeMillis());

    private String requestUrl = "/sms/v2/services/" + serviceId + "/messages";
    private String apiUrl = "https://sens.apigw.ntruss.com" + requestUrl;

    public Mono<Map> sendMessage(String phoneNumber){
        Map<String, String> message = new HashMap<>();
        message.put("to", phoneNumber);

        List<Map> messages = new ArrayList<>();
        messages.add(message);

        Map<String, Object> body = new HashMap<>();
        body.put("content", "[Monthly.] \n" +
                "안녕하세요.\n\n " +
                "회원님의 상품 구독 재결제일까지 \n" +
                "3일 남았습니다.\n" +
                "\n 알고있으삼^^;");     //"인증 번호(6자리) : " + makeAuthNumber()
        body.put("type", "SMS");
        body.put("from", "01083298026");
        body.put("messages", messages);

        WebClient webClient = null;
        try {
//            헤더와 바디에 필요한 데이터는 네이버 클라우드 sms api 문서 참고
//            https://api.ncloud-docs.com/docs/ai-application-service-sens-smsv2#%EB%A9%94%EC%8B%9C%EC%A7%80%EB%B0%9C%EC%86%A1
            webClient = WebClient.builder()
                    .baseUrl(apiUrl)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) //"application/json"
                    .defaultHeader("x-ncp-apigw-timestamp", timeStamp)
                    .defaultHeader("x-ncp-iam-access-key", accessKey)
                    .defaultHeader("x-ncp-apigw-signature-v2", makeSignature())
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
//바디 사용하는곳          ->위에 맵으로 바디 설정해놓은것 사용
        Mono<Map> resultBody = webClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(Map.class);

        return resultBody;
    }

    //    헤더에 필요한 x-ncp-apigw-signature-v2를 생성하는 메소드
//    정해진 값을 SHA256과 Base64로 암호화 해야한다. signature 문서를 참고한다. 양방향 암호화    / sha256 단방향 암호화  x 암호화 강함 시크릿키 사용
//    https://api.ncloud-docs.com/docs/common-ncpapi
    private String makeSignature() throws NoSuchAlgorithmException, InvalidKeyException {
        String message = new StringBuilder()
                .append(method)
                .append(" ")
                .append(requestUrl)
                .append("\n")
                .append(timeStamp)
                .append("\n")
                .append(accessKey)
                .toString();
        System.out.println("========================================================");
        System.out.println(message);
        System.out.println("========================================================");
        SecretKeySpec secretKeySpec = null;
        String encBase64 = null;
        Mac mac = null;

        try {
            // SecretKeySpec은 인코딩(암호화) 디코딩(복호화)를 위한 비밀키 생성해준다.
            secretKeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");

            // Mac(메세지 인증 코드)은 위에서 생성한 비밀키를 전송할 때 보안을 더해준다. (해시 함수에 기반한 MAC을 HMAC이라고 불린다.)
            mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            encBase64 = Base64.getEncoder().encodeToString(rawHmac);
        } catch (UnsupportedEncodingException e) {
            encBase64 = e.toString();
        }

        System.out.println("----------------------------------------------------");
        System.out.println(encBase64);
        System.out.println("----------------------------------------------------");

        return encBase64;
    }

    //    클라이언트에게 인증 메세지를 보내기 위해
//    6자리 난수를 생성하는 메소드
//    private String makeAuthNumber(){
//        Random random = new Random();
//        String authNumber = "";
//
//        for(int i=0; i<6; i++){
//            authNumber += random.nextInt(10);
//        }
//
//        return authNumber;
//    }
}
