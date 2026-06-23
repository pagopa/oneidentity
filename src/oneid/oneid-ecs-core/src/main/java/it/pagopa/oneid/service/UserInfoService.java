package it.pagopa.oneid.service;

import it.pagopa.oneid.web.dto.UserInfoResponseDTO;

public interface UserInfoService {

  UserInfoResponseDTO getUserInfo(String accessToken);
}
