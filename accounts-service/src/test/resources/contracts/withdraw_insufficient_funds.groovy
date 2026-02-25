import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Withdraw from account - insufficient funds"
    name "withdraw_insufficient_funds"
    request {
        method PUT()
        urlPath("/api/accounts/ivanov/withdraw")
        headers {
            contentType(applicationJson())
        }
        body([
                amount: 500
        ])
    }
    response {
        status 400
        headers {
            contentType(applicationJson())
        }
        body([
                error: "Недостаточно средств на счету"
        ])
    }
}
