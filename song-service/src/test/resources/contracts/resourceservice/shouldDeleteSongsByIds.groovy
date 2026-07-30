import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should delete songs by comma-separated ids"
    request {
        method DELETE()
        urlPath("/songs") {
            queryParameters {
                parameter "id": "1,2"
            }
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body([
            ids: [1, 2]
        ])
    }
}
