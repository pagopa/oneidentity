# oneid-lambda-cache-updater

## 0.5.1

### Patch Changes

- 1f68381: Exclude client 99-100 from cache and clients route, add acsindex to publisher pipe

## 0.5.0

### Minor Changes

- 4e0d06c: Deploy pipelines in eu-central

## 0.4.0

### Minor Changes

- 68c48f1: Deploy pipelines ecs and lambda name fix
- 5663a24: Pipeline deploy oneid-io

## 0.3.1

### Patch Changes

- 2f9f8ee: add lambda client publisher implementation and infra, update common with dynamo event deserialization logic and depshas

## 0.3.0

### Minor Changes

- 363f4ad: enable cache solution in all environments, remove local cache in ecs and use distributed cache, add mterics and alarms for ecs and lambda cache updeter

## 0.2.0

### Minor Changes

- c05e2ee: cache updater implementation with sns notifications, add infra redis and sns variables, add redis client common implementation, update depsha caused by redis client in common package

### Patch Changes

- 7285c39: add enable configuration for redis client
