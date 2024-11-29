const { SCOPE, CLIENT_ID, RESPONSE_TYPE, STATE, NONCE, REDIRECT_URI } = process.env;

export const TEST_QUERY_PARAMS = `client_id=${CLIENT_ID}&response_type=${RESPONSE_TYPE}&scope=${SCOPE}&state=${STATE}&nonce=${NONCE}&redirect_uri=${REDIRECT_URI}`;

export const TEST_LOGIN_URL = `/login?${TEST_QUERY_PARAMS}`;

export const TEST_IDP_ID = 'https://koz3yhpkscymaqgp4m7ceguu6m0tffuz.lambda-url.eu-south-1.on.aws';
