import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Withdraw from account - success"
    name "withdraw_success"
    request {
        method PUT()
        urlPath("/api/accounts/ivanov/withdraw")
        headers {
            contentType(applicationJson())
        }
        body([
                amount: 50
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
                id      : 1,
                username: "ivanov",
                fullName: "Иванов Иван",
                birthDate: "2001-01-01",
                balance : 50.00
        ])
    }
}
