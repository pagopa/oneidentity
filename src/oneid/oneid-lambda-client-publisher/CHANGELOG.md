# oneid-lambda-client-publisher

## 0.7.1

### Patch Changes

- 41571a5: fix depsha

## 0.7.0

### Minor Changes

- fc36acb: feat: implement alert mechanism for client reactivation

### Patch Changes

- 1f68381: Exclude client 99-100 from cache and clients route, add acsindex to publisher pipe

## 0.6.0

### Minor Changes

- 4e0d06c: Deploy pipelines in eu-central

## 0.5.0

### Minor Changes

- 68c48f1: Deploy pipelines ecs and lambda name fix
- 5663a24: Pipeline deploy oneid-io

## 0.4.0

### Minor Changes

- e7231fb: feat: add ability to skip OI Saml Error page

## 0.3.0

### Minor Changes

- fe4d802: add handling of active flag and remove clientsMap producer

## 0.2.1

### Patch Changes

- eb93b86: Move /clients /clients/{clientId} routes to static s3 routes, remove ecs clients routes code, remove client-publisher lambda bootstrap event code

## 0.2.0

### Minor Changes

- 2f9f8ee: add lambda client publisher implementation and infra, update common with dynamo event deserialization logic and depshas
