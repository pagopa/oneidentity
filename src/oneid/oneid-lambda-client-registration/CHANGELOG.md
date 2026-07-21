# oneid-lambda-client-registration

## 2.6.1

### Patch Changes

- 2f9f8ee: add lambda client publisher implementation and infra, update common with dynamo event deserialization logic and depshas

## 2.6.0

### Minor Changes

- 3cde471: feat: add eidas support

## 2.5.3

### Patch Changes

- c05e2ee: cache updater implementation with sns notifications, add infra redis and sns variables, add redis client common implementation, update depsha caused by redis client in common package
- 7285c39: add enable configuration for redis client

## 2.5.2

### Patch Changes

- d59ced9: validation using quarkus annotation, increase input check be and fe admin control panel

## 2.5.1

### Patch Changes

- 650cc2d: fix: change validation logic for content map

## 2.5.0

### Minor Changes

- 09a8418: feat: add http-redirect binding

## 2.4.1

### Patch Changes

- c86ef5f: implement newest agid specs for spid minor

## 2.4.0

### Minor Changes

- 626c6ec: Refactor docker local develop environment, removed Quarkus quinoa

## 2.3.1

### Patch Changes

- 00551ce: update logging.log4j:log4j-core dep

## 2.3.0

### Minor Changes

- 6d0665a: implement support for spid minors

## 2.2.0

### Minor Changes

- 8e08c43: add admin contorl panel input checks

## 2.1.4

### Patch Changes

- f7c6c3e: fixed SNS notification logic for pairwise update

## 2.1.3

### Patch Changes

- 65c155c: add sns notification for pairwise change

## 2.1.2

### Patch Changes

- 04a5d8a: update bouncycastle

## 2.1.1

### Patch Changes

- a311aeb: update nimbus-jose-jwt lib in oneid pom.xml and update sha

## 2.1.0

### Minor Changes

- 6d37ff4: Change headers for pairwise feature

### Patch Changes

- 71f2274: Log level refactoring

## 2.0.0

### Major Changes

- 86064f9: PDV refactor: move PDV logic to oneid-common, add new APIs (/plan-list, /validate-api-key), update infra + OpenAPI
