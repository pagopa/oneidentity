"""
Unit tests for index.py
"""
from os import path
import unittest
from unittest.mock import patch, Mock
from index import check_client_id_exists, get_cognito_client, get_dynamodb_client, \
    create_or_update_optional_attributes, update_user_attributes_with_client_id, get_optional_attributes


class TestCheckClientIdExists(unittest.TestCase):
    """Tests for the check_client_id_exists function."""
    
    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_item_found(self, mock_getenv, mock_dynamodb_client):
        mock_dynamodb_client.get_item.return_value = {"Item": {"clientId": {"S": "test_client_id"}}}
        result = check_client_id_exists("test_client_id")
        mock_dynamodb_client.get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertTrue(result)

    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_item_not_found(self, mock_getenv, mock_dynamodb_client):
        mock_dynamodb_client.get_item.return_value = {}
        result = check_client_id_exists("test_client_id")
        mock_dynamodb_client.get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )
        self.assertFalse(result)

    @patch("index.dynamodb_client")
    @patch("os.getenv", return_value="test_table")
    def test_dynamodb_exception(self, mock_getenv, mock_dynamodb_client):
        mock_dynamodb_client.get_item.side_effect = Exception("DynamoDB error")
        result = check_client_id_exists("test_client_id")
        mock_dynamodb_client.get_item.assert_called_once_with(
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
    @patch("index.cognito_client")
    @patch("os.getenv")
    def test_successful_update(
        self, mock_getenv, mock_cognito_client, mock_check_client_id_exists, mock_app
    ):

        json_body = {"client_id": "test_client_id", "user_id": "test_user_id"}
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = True
        mock_cognito_client.admin_update_user_attributes.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200}
        }
        mock_getenv.side_effect = lambda key: "test_user_pool_id" if key == "USER_POOL_ID" else None

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 204)
        self.assertEqual(response["message"], "Optional attributes updated successfully")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_cognito_client.admin_update_user_attributes.assert_called_once_with(
            UserPoolId="test_user_pool_id",
            Username="test_user_id",
            UserAttributes=[{"Name": "custom:client_id", "Value": "test_client_id"}],
        )

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client")
    @patch("os.getenv")
    def test_missing_client_id_or_user_id(
        self, mock_getenv, mock_cognito_client, mock_check_client_id_exists, mock_app
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
        mock_cognito_client.admin_update_user_attributes.assert_not_called()

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client")
    @patch("os.getenv")
    def test_client_id_not_found(
        self, mock_getenv, mock_cognito_client, mock_check_client_id_exists, mock_app
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
        mock_cognito_client.admin_update_user_attributes.assert_not_called()

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client")
    @patch("os.getenv")
    def test_cognito_update_failure(
        self, mock_getenv, mock_cognito_client, mock_check_client_id_exists, mock_app
    ):
        # Test case where Cognito update fails
        json_body = {"client_id": "test_client_id", "user_id": "test_user_id"}
        current_event = Mock()
        current_event.json_body = json_body
        mock_app.current_event = current_event

        mock_check_client_id_exists.return_value = True
        mock_cognito_client.admin_update_user_attributes.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 500}
        }
        mock_getenv.side_effect = lambda key: "test_user_pool_id" if key == "USER_POOL_ID" else None

        response, status_code = update_user_attributes_with_client_id()

        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Failed to update user attributes")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_cognito_client.admin_update_user_attributes.assert_called_once_with(
            UserPoolId="test_user_pool_id",
            Username="test_user_id",
            UserAttributes=[{"Name": "custom:client_id", "Value": "test_client_id"}],
        )

    @patch("index.app")
    @patch("index.check_client_id_exists")
    @patch("index.cognito_client")
    @patch("os.getenv")
    def test_internal_server_error(
        self, mock_getenv, mock_cognito_client, mock_check_client_id_exists, mock_app
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
        mock_cognito_client.admin_update_user_attributes.assert_not_called()


class TestCreateOrUpdateOptionalAttributes(unittest.TestCase):
    """Tests for the create_or_update_optional_attributes function."""

    @patch("index.app")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.check_client_id_exists")
    @patch("index.LocalizedContentMap")
    @patch("index.dynamodb_client")
    @patch("index.os.getenv", return_value="test_table")
    def test_successful_update(
        self, mock_getenv, mock_dynamodb_client, mock_LocalizedContentMap, mock_check_client_id_exists,
        mock_extract_client_id, mock_app
    ):
        # Setup mocks
        mock_extract_client_id.return_value = "test_client_id"
        mock_check_client_id_exists.return_value = True
        mock_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {"homepage": {}}}
        }
        current_event = Mock()
        current_event.json_body = mock_body
        mock_app.current_event = current_event

        mock_lcm_instance = Mock()
        mock_LocalizedContentMap.from_json.return_value = mock_lcm_instance
        mock_lcm_instance.to_dynamodb.return_value = {"en": {"M": {}}}

        mock_dynamodb_client.update_item.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200}
        }

        response, status_code = create_or_update_optional_attributes("test_user_id")

        self.assertEqual(status_code, 204)
        self.assertEqual(response["message"], "Optional attributes updated successfully")
        mock_check_client_id_exists.assert_called_once_with("test_client_id")
        mock_dynamodb_client.update_item.assert_called_once_with(
            TableName="test_table",
            Key={"clientId": {"S": "test_client_id"}},
            UpdateExpression="SET a11yUri = :a11yUri, backButtonEnabled = :backButtonEnabled, localizedContentMap = :localizedContentMap",
            ExpressionAttributeValues={
                ":a11yUri": {"S": "https://example.com"},
                ":backButtonEnabled": {"BOOL": True},
                ":localizedContentMap": {"en": {"M": {}}},
            },
        )

    @patch("index.app")
    def test_missing_body(self, mock_app):
        current_event = Mock()
        current_event.json_body = None
        mock_app.current_event = current_event

        response, status_code = create_or_update_optional_attributes("test_user_id")
        self.assertEqual(status_code, 400)
        self.assertEqual(response["message"], "Request body is required")

    @patch("index.app")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.check_client_id_exists")
    def test_client_id_not_found(
        self, mock_check_client_id_exists, mock_extract_client_id, mock_app
    ):
        mock_extract_client_id.return_value = "test_client_id"
        mock_check_client_id_exists.return_value = False
        mock_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {"homepage": {}}}
        }
        current_event = Mock()
        current_event.json_body = mock_body
        mock_app.current_event = current_event

        response, status_code = create_or_update_optional_attributes("test_user_id")
        self.assertEqual(status_code, 404)
        self.assertEqual(response["message"], "client_id not found")

    @patch("index.app")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.check_client_id_exists")
    @patch("index.LocalizedContentMap")
    @patch("index.dynamodb_client")
    @patch("index.os.getenv", return_value="test_table")
    def test_dynamodb_update_failure(
        self, mock_getenv, mock_dynamodb_client, mock_LocalizedContentMap, mock_check_client_id_exists,
        mock_extract_client_id, mock_app
    ):
        mock_extract_client_id.return_value = "test_client_id"
        mock_check_client_id_exists.return_value = True
        mock_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {"homepage": {}}}
        }
        current_event = Mock()
        current_event.json_body = mock_body
        mock_app.current_event = current_event

        mock_lcm_instance = Mock()
        mock_LocalizedContentMap.from_json.return_value = mock_lcm_instance
        mock_lcm_instance.to_dynamodb.return_value = {"en": {"M": {}}}

        mock_dynamodb_client.update_item.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 500}
        }

        response, status_code = create_or_update_optional_attributes("test_user_id")
        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Failed to update optional attributes")

    @patch("index.app")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.check_client_id_exists")
    @patch("index.LocalizedContentMap")
    @patch("index.dynamodb_client")
    @patch("index.os.getenv", return_value="test_table")
    def test_exception_handling(
        self, mock_getenv, mock_dynamodb_client, mock_LocalizedContentMap, mock_check_client_id_exists,
        mock_extract_client_id, mock_app
    ):
        mock_extract_client_id.return_value = "test_client_id"
        mock_check_client_id_exists.return_value = True
        mock_body = {
            "a11y_uri": "https://example.com",
            "back_button_enabled": True,
            "localizedContentMap": {"en": {"homepage": {}}}
        }
        current_event = Mock()
        current_event.json_body = mock_body
        mock_app.current_event = current_event

        mock_lcm_instance = Mock()
        mock_LocalizedContentMap.from_json.return_value = mock_lcm_instance
        mock_lcm_instance.to_dynamodb.return_value = {"en": {"M": {}}}

        mock_dynamodb_client.update_item.side_effect = Exception("Unexpected error")

        response, status_code = create_or_update_optional_attributes("test_user_id")
        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Internal server error")

class TestGetOptionalAttributes(unittest.TestCase):
    """Tests for the get_optional_attributes function."""

    @patch("index.dynamodb_client")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.LocalizedContentMap")
    @patch("index.os.getenv", return_value="test_table")
    def test_successful_retrieval(
        self, mock_getenv, mock_LocalizedContentMap, mock_extract_client_id, mock_dynamodb_client
    ):
        mock_extract_client_id.return_value = "test_client_id"
        item = {
            "a11yUri": {"S": "https://example.com"},
            "backButtonEnabled": {"BOOL": True},
            "localizedContentMap": {"M": {"en": {"M": {}}}},
        }
        mock_dynamodb_client.get_item.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200},
            "Item": item,
        }
        mock_LocalizedContentMap.from_dynamodb.return_value = {"en": {"homepage": {}}}

        response, status_code = get_optional_attributes("test_user_id")
        self.assertEqual(status_code, 200)
        self.assertEqual(response["a11y_uri"], "https://example.com")
        self.assertTrue(response["back_button_enabled"])
        self.assertEqual(response["localizedContentMap"], {"en": {"homepage": {}}})
        mock_dynamodb_client.get_item.assert_called_once_with(
            TableName="test_table", Key={"clientId": {"S": "test_client_id"}}
        )

    @patch("index.dynamodb_client")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.os.getenv", return_value="test_table")
    def test_dynamodb_failure(
        self, mock_getenv, mock_extract_client_id, mock_dynamodb_client
    ):
        mock_extract_client_id.return_value = "test_client_id"
        mock_dynamodb_client.get_item.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 500}
        }
        response, status_code = get_optional_attributes("test_user_id")
        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Failed to retrieve optional attributes")

    @patch("index.dynamodb_client")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.os.getenv", return_value="test_table")
    def test_item_not_found(
        self, mock_getenv, mock_extract_client_id, mock_dynamodb_client
    ):
        mock_extract_client_id.return_value = "test_client_id"
        mock_dynamodb_client.get_item.return_value = {
            "ResponseMetadata": {"HTTPStatusCode": 200},
            "Item": None,
        }
        response, status_code = get_optional_attributes("test_user_id")
        self.assertEqual(status_code, 404)
        self.assertEqual(response["message"], "client_id not found")

    @patch("index.dynamodb_client")
    @patch("index.extract_client_id_from_connected_user")
    @patch("index.os.getenv", return_value="test_table")
    def test_exception_handling(
        self, mock_getenv, mock_extract_client_id, mock_dynamodb_client
    ):
        mock_extract_client_id.return_value = "test_client_id"
        mock_dynamodb_client.get_item.side_effect = Exception("Unexpected error")
        response, status_code = get_optional_attributes("test_user_id")
        self.assertEqual(status_code, 500)
        self.assertEqual(response["message"], "Internal server error")




if __name__ == "__main__":
    unittest.main()