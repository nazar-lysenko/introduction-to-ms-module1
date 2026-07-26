import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "should return MP3 binary data for an existing resource"
    request {
        method GET()
        url "/resources/1"
    }
    response {
        status OK()
        headers {
            header("Content-Type", "audio/mpeg")
        }
        body(fileAsBytes("sample.mp3"))
    }
}
