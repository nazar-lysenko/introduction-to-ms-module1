import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return 404 for a non-existing resource"
    request {
        method GET()
        url "/resources/999"
    }
    response {
        status NOT_FOUND()
    }
}
