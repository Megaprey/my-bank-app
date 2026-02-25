package com.bank.transfer.contract;

import com.bank.api.dto.AccountDto;
import com.bank.api.dto.BalanceOperationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@AutoConfigureStubRunner(
        ids = "com.bank:accounts-service:+:stubs:8081",
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
class AccountClientContractTest {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("http://localhost:8081")
            .build();

    @Test
    void deposit_contract_parsesResponseCorrectly() {
        AccountDto result = webClient.put()
                .uri("/api/accounts/ivanov/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new BalanceOperationDto(new BigDecimal("100")))
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("ivanov");
        assertThat(result.balance()).isEqualByComparingTo("200");
    }

    @Test
    void withdraw_contract_parsesResponseCorrectly() {
        AccountDto result = webClient.put()
                .uri("/api/accounts/ivanov/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new BalanceOperationDto(new BigDecimal("50")))
                .retrieve()
                .bodyToMono(AccountDto.class)
                .block();

        assertThat(result).isNotNull();
        assertThat(result.username()).isEqualTo("ivanov");
        assertThat(result.balance()).isEqualByComparingTo("50");
    }
}
