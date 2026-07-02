package it.pagopa.oneid.service;

public interface UserInfoService {

  String getSignedUserInfo(String bearerToken);
}
