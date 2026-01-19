# infra

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
