# infra

## 2.10.0

### Minor Changes

- 363f4ad: enable cache solution in all environments, remove local cache in ecs and use distributed cache, add mterics and alarms for ecs and lambda cache updeter

## 2.9.0

### Minor Changes

- 087cacf: feat: add latest_eidas tag

## 2.8.0

### Minor Changes

- 3cde471: feat: add eidas support

## 2.7.0

### Minor Changes

- 6a8192e: switch idps endpoint to s3 and api gw

## 2.6.0

### Minor Changes

- a3132a2: Move /idps endpoint to serve it statically

## 2.5.0

### Minor Changes

- e5e331b: feat: update step scaling policy

### Patch Changes

- c05e2ee: cache updater implementation with sns notifications, add infra redis and sns variables, add redis client common implementation, update depsha caused by redis client in common package

## 2.4.0

### Minor Changes

- 362daea: implement /userinfo endpoint

## 2.3.4

### Patch Changes

- 6cad980: elasticache valkey setup with event pipe dlq and cache updater lambda template

## 2.3.3

### Patch Changes

- 144a0d8: add notifiche digitali in csp

## 2.3.2

### Patch Changes

- 09a8418: feat: add http-redirect binding

## 2.3.1

### Patch Changes

- 09255c8: update lib metrics archiver lambda

## 2.3.0

### Minor Changes

- 8f83ccb: add update to status of /idps route

## 2.2.0

### Minor Changes

- 33abe8b: Custom metrics archiver properties fix

## 2.1.0

### Minor Changes

- 9f0d442: CloudWatch Custom Metrics extend expiration

## 2.0.0

### Major Changes

- 8d7893e: enable cors for /idps route

## 1.9.0

### Minor Changes

- 3c92ac5: Removed validator from dev env

## 1.8.0

### Minor Changes

- 0b35dfc: Created event_mode for ecs desired count

## 1.7.0

### Minor Changes

- bc866e8: Add XSW assertions handling with dedicated storage and alarms

## 1.6.0

### Minor Changes

- a55e3d7: Add keyId to jwt header
- 0099b5c: Edit no traffic alarms logic

## 1.5.1

### Patch Changes

- fcb0258: Edit period for no traffic error alarms to 1 hour

## 1.5.0

### Minor Changes

- b362d39: Set trigger install dependency on requirements changes and remove sns from alarms in dev

## 1.4.1

### Patch Changes

- 65c155c: Remove SNS notifications in dev for client registration and idp and client errors

## 1.4.0

### Minor Changes

- bd80132: add alarm for clients with no traffic

## 1.3.0

### Minor Changes

- dea511a: Add Cloudwatch Alarm for IDPs with no traffic

## 1.2.0

### Minor Changes

- 5803f74: feat: Alarm ECS task running
- ca70fb6: Increased autoscaling capacity of ecs-core tasks for bonus elettrodomestici

### Patch Changes

- 71f2274: Log level refactoring

## 1.1.0

### Minor Changes

- 86064f9: PDV refactor: move PDV logic to oneid-common, add new APIs (/plan-list, /validate-api-key), update infra + OpenAPI
