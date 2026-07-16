# oneid-ecs-core

## 1.10.0

### Minor Changes

- 3cde471: feat: add eidas support

## 1.9.0

### Minor Changes

- a3132a2: Move /idps endpoint to serve it statically

## 1.8.1

### Patch Changes

- c05e2ee: cache updater implementation with sns notifications, add infra redis and sns variables, add redis client common implementation, update depsha caused by redis client in common package
- 7285c39: add enable configuration for redis client

## 1.8.0

### Minor Changes

- 362daea: implement /userinfo endpoint

## 1.7.0

### Minor Changes

- 09a8418: feat: add http-redirect binding

## 1.6.1

### Patch Changes

- c86ef5f: implement newest agid specs for spid minor

## 1.6.0

### Minor Changes

- 626c6ec: Refactor docker local develop environment, removed Quarkus quinoa

## 1.5.0

### Minor Changes

- 8d7893e: enable cors for /idps route

### Patch Changes

- 00551ce: update logging.log4j:log4j-core dep
- a6b0a05: update org.bouncycastle:bcpkix ecs core

## 1.4.0

### Minor Changes

- 6d0665a: implement support for spid minors

## 1.3.0

### Minor Changes

- bc866e8: Add XSW assertions handling with dedicated storage and alarms

## 1.2.0

### Minor Changes

- a55e3d7: Add keyId to jwt header

## 1.1.1

### Patch Changes

- bd9dad3: update log4j to 2.25.3

## 1.1.0

### Minor Changes

- f6a6bd1: Enable Virtual threads usage for ecs-core

## 1.0.4

### Patch Changes

- 04a5d8a: update bouncycastle

## 1.0.3

### Patch Changes

- a311aeb: update nimbus-jose-jwt lib in oneid pom.xml and update sha

## 1.0.2

### Patch Changes

- 71f2274: Log level refactoring

## 1.0.1

### Patch Changes

- 86064f9: PDV refactor: move PDV logic to oneid-common, add new APIs (/plan-list, /validate-api-key), update infra + OpenAPI
