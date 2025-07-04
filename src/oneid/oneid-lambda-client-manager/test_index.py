"""
Unit tests for index.py
"""
import unittest
from unittest.mock import patch, Mock
from index import check_client_id_exists, get_cognito_client, get_dynamodb_client, \
    create_or_update_optional_attributes, update_user_attributes_with_client_id, get_optional_attributes


class TestCheckClientIdExists(unittest.TestCase):
    """Tests for the check_client_id_exists function."""
    
    @patch("index.dynamodb_client.get_item")
    @patch("os.getenv", return_value="test_table")
    def test_item_found(self, mock_getenv, mock_get_item):
        mock_get_item.return_value = {"Item": {"clientId": {"S": "test_client_id"}}}
        result = check_client_id_exists("test_client_id")
        mock_get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertTrue(result)

    @patch("index.dynamodb_client.get_item")
    @patch("os.getenv", return_value="test_table")
    def test_item_not_found(self, mock_getenv, mock_get_item):
        mock_get_item.return_value = {}
        result = check_client_id_exists("test_client_id")
        mock_get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertFalse(result)

    @patch("index.dynamodb_client.get_item")
    @patch("os.getenv", return_value="test_table")
    def test_dynamodb_exception(self, mock_getenv, mock_get_item):
        mock_get_item.side_effect = Exception("DynamoDB error")
        result = check_client_id_exists("test_client_id")
        mock_get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertFalse(result)


class TestGetCognitoClient(unittest.TestCase):
    """Tests for the get_cognito_client function."""
    
    @patch("index.boto3.client")
    def test_client_creation_success(self, mock_boto3_client):
        mock_boto3_client.return_value = Mock()
        result = get_cognito_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("cognito-idp", region_name="eu-south-1")
        self.assertIsNotNone(result)

    @patch("index.boto3.client")
    def test_client_creation_failure(self, mock_boto3_client):
        mock_boto3_client.side_effect = Exception("Cognito client error")
        result = get_cognito_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("cognito-idp", region_name="eu-south-1")
        self.assertIsNone(result)


class TestGetDynamoDBClient(unittest.TestCase):
    """Tests for the get_dynamodb_client function."""
    
    @patch("index.boto3.client")
    def test_client_creation_success(self, mock_boto3_client):
        mock_boto3_client.return_value = Mock()
        result = get_dynamodb_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("dynamodb", region_name="eu-south-1")
        self.assertIsNotNone(result)

    @patch("index.boto3.client")
    def test_client_creation_failure(self, mock_boto3_client):
        mock_boto3_client.side_effect = Exception("DynamoDB client error")
        result = get_dynamodb_client("eu-south-1")
        mock_boto3_client.assert_called_once_with("dynamodb", region_name="eu-south-1")
        self.assertIsNone(result)


class TestUpdateUserAttributesWithClientId(unittest.TestCase):
    """Tests for the update_user_attributes_with_client_id function."""

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client.admin_update_user_attributes")
    @patch("os.getenv")
    def test_successful_update(
        self, mock_getenv, mock_admin_update_user_attributes, mock_check_client_id_exists, mock_app
    ):

        json_body = {"client_id": "test_client_id", "user_id": "test_user_id"}
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = True
        mock_admin_update_user_attributes.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200}
        }
        mock_getenv.side_effect = lambda key: "test_user_pool_id" if key == "USER_POOL_ID" else None

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 200)
        self.assertEqual(response["message"], "Optional attributes updated successfully")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_admin_update_user_attributes.assert_called_once_with(
            UserPoolId="test_user_pool_id",
            Username="test_user_id",
            UserAttributes=[{"Name": "custom:client_id", "Value": "test_client_id"}],
        )

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client.admin_update_user_attributes")
    @patch("os.getenv")
    def test_missing_client_id_or_user_id(
        self, mock_getenv, mock_admin_update_user_attributes, mock_check_client_id_exists, mock_app
    ):
        # Test case where client_id or user_id is missing in the request body
        json_body = {"client_id": "test_client_id"}  # Missing user_id
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 400)
        self.assertEqual(response["message"], "client_id and user_id are required")
        mock_check_client_id_exists.assert_not_called()
        mock_admin_update_user_attributes.assert_not_called()

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client.admin_update_user_attributes")
    @patch("os.getenv")
    def test_client_id_not_found(
        self, mock_getenv, mock_admin_update_user_attributes, mock_check_client_id_exists, mock_app
    ):
        # Test case where client_id does not exist in the database
        json_body = {"client_id": "nonexistent_client_id", "user_id": "test_user_id"}
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = False

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 404)
        self.assertEqual(response["message"], "client_id not found")
        mock_check_client_id_exists.assert_called_once_with("nonexistent_client_id")
        mock_admin_update_user_attributes.assert_not_called()

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client.admin_update_user_attributes")
    @patch("os.getenv")
    def test_cognito_update_failure(
        self, mock_getenv, mock_admin_update_user_attributes, mock_check_client_id_exists, mock_app
    ):
        # Test case where Cognito update fails
        json_body = {"client_id": "test_client_id", "user_id": "test_user_id"}
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = True
        mock_admin_update_user_attributes.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 500}
        }
        mock_getenv.side_effect = lambda key: "test_user_pool_id" if key == "USER_POOL_ID" else None

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Failed to update user attributes")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_admin_update_user_attributes.assert_called_once_with(
            UserPoolId="test_user_pool_id",
            Username="test_user_id",
            UserAttributes=[{"Name": "custom:client_id", "Value": "test_client_id"}],
        )

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client.admin_update_user_attributes")
    @patch("os.getenv")
    def test_internal_server_error(
        self, mock_getenv, mock_admin_update_user_attributes, mock_check_client_id_exists, mock_app
    ):
        # Test case where an unexpected exception occurs
        json_body = {"client_id": "test_client_id", "user_id": "test_user_id"}
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.side_effect = Exception("Unexpected error")

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Internal server error")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_admin_update_user_attributes.assert_not_called()


class TestUpdateOptionalAttributes(unittest.TestCase):
    """Tests for the update_optional_attributes function."""

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.dynamodb_client.update_item")
    @patch("index.LocalizedContentMap.from_json")
    @patch("os.getenv")
    def test_successful_update(
        self, mock_getenv, mock_from_json, mock_update_item, mock_check_client_id_exists, mock_app
    ):
        json_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {
                "homepage":{
                    "title": "Homepage Title",
                    "desc": "Homepage Description",
                    "doc_uri": "https://example.com/doc",
                    "support_address": "https://example.com/doc",
                    "cookie_uri": "https://example.com/cookie"
                }
            }},
        }
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = True
        mock_from_json.return_value.to_dynamodb.return_value = {
            "M": {
            "en": {
                "M": {
                "homepage": {
                    "M": {
                    "title": {"S": "Homepage Title"},
                    "desc": {"S": "Homepage Description"},
                    "doc_uri": {"S": "https://example.com/doc"},
                    "support_address": {"S": "https://example.com/doc"},
                    "cookie_uri": {"S": "https://example.com/cookie"},
                    }
                }
                }
            }
            }
        }
        mock_update_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 200}}
        mock_getenv.return_value = "test_table"

        response, status_code = create_or_update_optional_attributes("test_client_id")

        self.assertEqual(status_code, 200)
        self.assertEqual(response["message"], "Optional attributes updated successfully")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_update_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}, 
            UpdateExpression="SET a11yUri = :a11yUri, backButtonEnabled = :backButtonEnabled, localizedContentMap = :localizedContentMap", 
            ExpressionAttributeValues={
            ":a11yUri": {"S": "https://example.com"}, 
            ":backButtonEnabled": {"BOOL": True}, 
            ":localizedContentMap": {
                "M": {
                "en": {
                    "M": {
                    "homepage": {
                        "M": {
                        "title": {"S": "Homepage Title"}, 
                        "desc": {"S": "Homepage Description"}, 
                        "doc_uri": {"S": "https://example.com/doc"}, 
                        "support_address": {"S": "https://example.com/doc"}, 
                        "cookie_uri": {"S": "https://example.com/cookie"}
                        }
                    }
                    }
                }
                }
            }
            }
        )

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.dynamodb_client.update_item")
    @patch("index.LocalizedContentMap.from_json")
    @patch("os.getenv")
    def test_client_id_not_found(
        self, mock_getenv, mock_from_json, mock_update_item, mock_check_client_id_exists, mock_app
    ):
        json_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {
                "homepage":{
                    "title": "Homepage Title",
                    "desc": "Homepage Description",
                    "doc_uri": "https://example.com/doc",
                    "support_address": "https://example.com/doc",
                    "cookie_uri": "https://example.com/cookie"
                }
            }},
        }
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = False
        mock_getenv.return_value = "test_table"

        response, status_code = create_or_update_optional_attributes("nonexistent_client_id")

        self.assertEqual(status_code, 404)
        self.assertEqual(response["message"], "client_id not found")
        mock_check_client_id_exists.assert_called_once_with("nonexistent_client_id")
        mock_update_item.assert_not_called()

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.dynamodb_client.update_item")
    @patch("index.LocalizedContentMap.from_json")
    @patch("os.getenv")
    def test_dynamodb_update_failure(
        self, mock_getenv, mock_from_json, mock_update_item, mock_check_client_id_exists, mock_app
    ):
        json_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {
                "homepage":{
                    "title": "Homepage Title",
                    "desc": "Homepage Description",
                    "doc_uri": "https://example.com/doc",
                    "support_address": "https://example.com/doc",
                    "cookie_uri": "https://example.com/cookie"
                }
            }},
        }
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = True
        mock_from_json.return_value.to_dynamodb.return_value = {
            "M": {
            "en": {
                "M": {
                "homepage": {
                    "M": {
                    "title": {"S": "Homepage Title"},
                    "desc": {"S": "Homepage Description"},
                    "doc_uri": {"S": "https://example.com/doc"},
                    "support_address": {"S": "https://example.com/doc"},
                    "cookie_uri": {"S": "https://example.com/cookie"},
                    }
                }
                }
            }
            }
        }
        mock_update_item.return_value = {"ResponseMetadata": {"HTTPStatusCode": 500}}
        mock_getenv.return_value = "test_table"

        response, status_code = create_or_update_optional_attributes("test_client_id")

        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Failed to update optional attributes")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_update_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}, 
            UpdateExpression="SET a11yUri = :a11yUri, backButtonEnabled = :backButtonEnabled, localizedContentMap = :localizedContentMap", 
            ExpressionAttributeValues={
            ":a11yUri": {"S": "https://example.com"}, 
            ":backButtonEnabled": {"BOOL": True}, 
            ":localizedContentMap": {
                "M": {
                "en": {
                    "M": {
                    "homepage": {
                        "M": {
                        "title": {"S": "Homepage Title"}, 
                        "desc": {"S": "Homepage Description"}, 
                        "doc_uri": {"S": "https://example.com/doc"}, 
                        "support_address": {"S": "https://example.com/doc"}, 
                        "cookie_uri": {"S": "https://example.com/cookie"}
                        }
                    }
                    }
                }
                }
            }
            }
        )

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.dynamodb_client.update_item")
    @patch("index.LocalizedContentMap.from_json")
    @patch("os.getenv")
    def test_internal_server_error(
        self, mock_getenv, mock_from_json, mock_update_item, mock_check_client_id_exists, mock_app
    ):
        json_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {
                "homepage":{
                    "title": "Homepage Title",
                    "desc": "Homepage Description",
                    "doc_uri": "https://example.com/doc",
                    "support_address": "https://example.com/doc",
                    "cookie_uri": "https://example.com/cookie"
                }
            }},
        }
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.side_effect = Exception("Unexpected error")
        mock_getenv.return_value = "test_table"

        response, status_code = create_or_update_optional_attributes("test_client_id")

        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Internal server error")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_update_item.assert_not_called()


class TestGetOptionalAttributes(unittest.TestCase):
            """Tests for the get_optional_attributes function."""

            @patch("index.dynamodb_client.get_item")
            @patch("index.os.getenv", return_value="test_table")
            @patch("index.LocalizedContentMap.from_dynamodb")
            def test_successful_retrieval(
                self, mock_from_dynamodb, mock_getenv, mock_get_item
            ):
                # Mock DynamoDB response
                mock_get_item.return_value = {
                    "ResponseMetadata": {"HTTPStatusCode": 200},
                    "Item": {
                        "a11yUri": {"S": "https://example.com"},
                        "backButtonEnabled": {"BOOL": True},
                        "localizedContentMap": {"M": {"en": {"M": {}}}},
                    },
                }
                mock_from_dynamodb.return_value = {"en": {"homepage": {}}}


                response, status_code = get_optional_attributes("test_client_id")
                self.assertEqual(status_code, 200)
                self.assertEqual(response["a11y_uri"], "https://example.com")
                self.assertEqual(response["back_button_enabled"], True)
                self.assertEqual(response["localizedContentMap"], {"en": {"homepage": {}}})
                mock_get_item.assert_called_once_with(
                    TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
                )
                mock_from_dynamodb.assert_called_once_with({"en": {"M": {}}})

            @patch("index.dynamodb_client.get_item")
            @patch("index.os.getenv", return_value="test_table")
            def test_dynamodb_failure(self, mock_getenv, mock_get_item):
                # Simulate DynamoDB returning a non-200 status code
                mock_get_item.return_value = {
                    "ResponseMetadata": {"HTTPStatusCode": 500}
                }


                response, status_code = get_optional_attributes("test_client_id")
                self.assertEqual(status_code, 500)
                self.assertEqual(response["message"], "Failed to retrieve optional attributes")
                mock_get_item.assert_called_once_with(
                    TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
                )

            @patch("index.dynamodb_client.get_item")
            @patch("index.os.getenv", return_value="test_table")
            def test_item_not_found(self, mock_getenv, mock_get_item):
                # Simulate DynamoDB returning no item
                mock_get_item.return_value = {
                    "ResponseMetadata": {"HTTPStatusCode": 200},
                }


                response, status_code = get_optional_attributes("test_client_id")
                self.assertEqual(status_code, 404)
                self.assertEqual(response["message"], "client_id not found")
                mock_get_item.assert_called_once_with(
                    TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
                )

            @patch("index.dynamodb_client.get_item")
            @patch("index.os.getenv", return_value="test_table")
            def test_exception_handling(self, mock_getenv, mock_get_item):
                # Simulate an exception being raised
                mock_get_item.side_effect = Exception("Unexpected error")


                response, status_code = get_optional_attributes("test_client_id")
                self.assertEqual(status_code, 500)
                self.assertEqual(response["message"], "Internal server error")
                mock_get_item.assert_called_once_with(
                    TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
                )

if __name__ == "__main__":
    unittest.main()