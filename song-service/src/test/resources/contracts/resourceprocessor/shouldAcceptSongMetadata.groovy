import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should accept valid song metadata and return created id"
    request {
        method POST()
        url "/songs"
        headers {
            contentType(applicationJson())
        }
        body([
            id      : 1,
            name    : "Test Song",
            artist  : "Test Artist",
            album   : "Test Album",
            duration: "06:22",
            year    : "2020"
        ])
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            id: 1
        ])
    }
}
