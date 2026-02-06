package com.bfhl.webhook;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class StartupRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {

        RestTemplate restTemplate = new RestTemplate();

        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        WebhookRequest req = new WebhookRequest();
        req.setName("Mousumi Das");          
        req.setRegNo("250850120105");         
        req.setEmail("mousumi@gmail.com"); 

        ResponseEntity<WebhookResponse> response =
                restTemplate.postForEntity(url, req, WebhookResponse.class);

        String webhookUrl = response.getBody().getWebhook();
        String token = response.getBody().getAccessToken();

        System.out.println("Webhook URL: " + webhookUrl);
        System.out.println("Token: " + token);


        String regNo = "250850120105";
        int lastDigit = Integer.parseInt(regNo.substring(regNo.length() - 1));

        String finalSQL;

        if (lastDigit % 2 == 1) {
        	finalSQL = "SELECT d.DEPARTMENT_NAME, t.total_salary AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE FROM ( SELECT emp_id, SUM(amount) AS total_salary, ROW_NUMBER() OVER (PARTITION BY department ORDER BY SUM(amount) DESC) AS rn FROM employee e JOIN payments p ON e.emp_id = p.emp_id WHERE DAY(p.payment_time) <> 1 GROUP BY emp_id, department ) t JOIN employee e ON t.emp_id = e.emp_id JOIN department d ON e.department = d.department_id WHERE t.rn = 1;";

        } else {
            finalSQL = "NO";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        SolutionRequest sol = new SolutionRequest(finalSQL);
        HttpEntity<SolutionRequest> entity = new HttpEntity<>(sol, headers);

        ResponseEntity<String> submitResponse =
                restTemplate.postForEntity(webhookUrl, entity, String.class);

        System.out.println("Submission Response: " + submitResponse.getBody());
    }
}
