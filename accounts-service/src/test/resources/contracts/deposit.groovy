import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Deposit to account"
    name "deposit_success"
    request {
        method PUT()
        urlPath("/api/accounts/ivanov/deposit")
        headers {
            contentType(applicationJson())
        }
        body([
                amount: 100
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
                balance : 200.00
        ])
    }
}
